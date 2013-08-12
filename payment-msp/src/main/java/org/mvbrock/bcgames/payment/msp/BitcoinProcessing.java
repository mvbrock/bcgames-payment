package org.mvbrock.bcgames.payment.msp;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.mvbrock.bcgames.payment.callback.interfaces.PaymentMspCallbackService;
import org.mvbrock.bcgames.payment.callback.model.WagerTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SessionScoped
public class BitcoinProcessing implements Runnable, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(BitcoinProcessing.class);
	
	private final int incomingWindow = 100;
	private int incomingIndex = 0;
	
	// Ledgers mapped by incoming Bitcoin address
	private Map<String, GameLedger> ledgers = new ConcurrentHashMap<String, GameLedger>();
	// Transactions mapped by incoming Bitcoin address
	private Map<String, Transaction> transactions = new ConcurrentHashMap<String, Transaction>();
	
	private AtomicBoolean isRunning;
	private Thread thread;
	
	@Inject
	private PaymentMspConfigStore config;
	
	@Inject
	private CallbackServiceTracker finderServices;
	
	@Inject
	private BitcoinRpcClient bitcoin;
	
	public BitcoinProcessing() { }
	
	@PostConstruct
	public void init() {
		isRunning = new AtomicBoolean(true);
		thread = new Thread(this);
		thread.start();
	}
	
	@PreDestroy
	public void uninit() {
		isRunning.set(false);
		thread.interrupt();
	}
	
	public void createLedger(String gameAddress, GameLedger ledger) {
		ledgers.put(gameAddress, ledger);
	}
	
	public GameLedger getLedger(String gameAddress) {
		return ledgers.get(gameAddress);
	}
	
	public void run() {
		final Integer pollInterval = Integer.decode(config.getProperty(PaymentMspConfigStore.BitcoinPollInterval));
		log.info("Bitcoin client polling interval: " +
				config.getProperty(PaymentMspConfigStore.BitcoinPollInterval));
		
		log.info("Starting Bitcoin processing thread");
		while(isRunning.get() == true) {
			// Process the incoming transactions
			int transactionCount = 0;
			do {
				Transaction [] incoming = bitcoin.listtransactions("", incomingWindow, incomingIndex);
				transactionCount = incoming.length;
				incomingIndex += transactionCount;
				processIncoming(incoming);
			} while(transactionCount == incomingWindow);
			
			// Check existing transaction confirmations for payout
			for(Transaction transaction : transactions.values()) {
				processExisting(transaction);
			}

			try {
				Thread.sleep(pollInterval);
			} catch (InterruptedException e) {
				log.info("Bitcoin processing thread interrupted", e);
			}
		}
		log.info("Stopping Bitcoin processing thread");
	}
	
	private void processIncoming(Transaction [] incoming) {
		for(Transaction transaction : incoming) {
			String gameAddress = transaction.getAddress();
			GameLedger ledger = ledgers.get(gameAddress);
			if(ledger != null) {
				// Lock on the ledger while updating its state based on the most recent transaction
				synchronized(ledger) {
					switch(ledger.getState()) {
						case IncomingWaiting:
							// Only process incoming transactions if the ledger is waiting
							processIncoming(ledger, transaction);
							break;
						default:
							// Issue automatic refund for any incoming transaction on a non-waiting ledger
							issueTransactionRefund(ledger, transaction);
							break;
					}
				}
			} else {
				log.info("Received payment for non-existent address: " + transaction);
			}
		}
	}
	
	private void processExisting(Transaction transaction) {
		String gameAddress = transaction.getAddress();
		Transaction updatedTransaction = bitcoin.gettransaction(transaction.getTxid());
		
		// Update the confirmation count if it has increased
		if(updatedTransaction.getConfirmations() > transaction.getConfirmations()) {
			transaction = updatedTransaction;
			transactions.put(gameAddress, updatedTransaction);
		}
		
		// Check if the required number of confirmations have been reached
		if(transaction.getConfirmations() >= config.getConfig().getRequiredConfirmations()) {
			// Look for the corresponding game and determine if winner should be paid
			GameLedger ledger = ledgers.get(gameAddress);
			// If the ledger indicates the winner is waiting for payment, send the payment
			if(ledger.getState() == GameLedgerState.OutgoingWinnerWaiting) {
				// Separate the rake from the outgoing amount
				Double payout = ledger.getOutgoingAmount();
				Double rake = payout * config.getConfig().getRake();
				ledger.setOutgoingAmount(payout - rake);
				
				// Retrieve the addresses/info to send to
				String gameId = ledger.getGameId();
				String playerId = ledger.getPlayerId();
				String playerAddress = ledger.getPlayerAddress();
				String rakeAddress = config.getConfig().getRakeAddress();
				
				// Issue payment to the player and rake address
				bitcoin.sendtoaddress(playerAddress, payout, "type=payout, gameId=" + gameId + ", playerId=" + playerId);
				bitcoin.sendtoaddress(rakeAddress, rake, "type=rake, gameId=" + gameId + ", playerId=" + playerId);
			}
		}
	}
	
	private void processIncoming(GameLedger ledger, Transaction transaction) {
		Double amountReceived = transaction.getAmount();
		// Determine if the transaction is sufficient
		if(amountReceived != 0.0) {
			// Retrieve the information about the transaction
			String gameAddress = ledger.getGameAddress();
			String gameId = ledger.getGameId();
			String playerId = ledger.getPlayerId();
			String wagerTier = ledger.getWagerTier();
			
			WagerTier wager = config.getConfig().getWagerTiers().get(wagerTier);
			Double amountRequired = wager.getAmount();
			log.info("Received payment from player: " + playerId + "\n" +
					"\tBitcoin Address: " + gameAddress + "\n" +
					"\tGame ID: " + gameId + "\n" +
					"\tWager Tier: " + wagerTier + "\n" +
					"\tAmount Required: " + amountRequired + "\n" +
					"\tAmount Received: " + amountReceived);
			
			// Compare the amount received to the amount expected
			if(amountRequired == amountReceived) {
				log.info("Received correct amount from player.");
				
				// Provide update to the WS client
				PaymentMspCallbackService callbackSvc = finderServices.getService(ledger.getGameId());
				callbackSvc.playerPaid(gameId, playerId, amountReceived);
				
				// Store the transaction
				transactions.put(gameAddress, transaction);
				
				// Update the ledger
				ledger.setState(GameLedgerState.IncomingReceived);
				ledger.setIncomingAmount(amountReceived);
				ledger.setIncomingReceivedDate(new Date());
			} else {
				log.info("Did not receive correct amount, returning it back to the player.");
				issueTransactionRefund(ledger, transaction);
			}
		}
	}
	
	private void issueTransactionRefund(GameLedger ledger, Transaction transaction) {
		Double amountReceived = transaction.getAmount();
		String playerAddress = ledger.getPlayerAddress();
		String gameId = ledger.getGameId();
		String playerId = ledger.getPlayerId();
		bitcoin.sendtoaddress(playerAddress, amountReceived, "type=refund, gameId=" + gameId + ", playerId=" + playerId);
		log.info("Sent refund of " + amountReceived + " to " + playerAddress + " for game " + gameId);
	}
}
