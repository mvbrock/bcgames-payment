package org.mvbrock.bcgames.payment.ws;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.mvbrock.bcgames.payment.ws.interfaces.PaymentWsCallback;
import org.mvbrock.bcgames.payment.model.WagerTier;

@SessionScoped
public class BitcoinProcessingThread implements Runnable, Serializable {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private transient Logger log;
	
	private final int incomingWindow = 100;
	
	// Ledgers mapped by incoming address
	private Map<String, GameLedger> ledgers = new ConcurrentHashMap<String, GameLedger>();
	
	// Transactions mapped by incoming Bitcoin address
	private Set<Transaction> ongoingTxs = new HashSet<Transaction>();
	private Transaction newestTx = null;
	
	private Integer pollInterval;
	private AtomicBoolean isRunning;
	private Thread thread;
	
	@Inject
	private PaymentWsConfigStore config;
	
	@Inject
	private CallbackServiceTracker finderServices;
	
	@Inject
	private BitcoinRpcClient bitcoin;
	
	public BitcoinProcessingThread() { }
	
	@PostConstruct
	public void init() {
		pollInterval = Integer.decode(config.getProperty(PaymentWsConfigStore.BitcoinPollInterval));
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
		log.info("Bitcoin client polling interval: " + pollInterval);
		
		log.info("Starting Bitcoin processing thread");
		while(isRunning.get() == true) {
			// Retrieve the incoming transactions
			Collection<Transaction> incomingTxs = retrieveIncoming();
			
			// Process the incoming transactions
			processIncomingTxs(incomingTxs);
				
			// Check existing transaction confirmations for payout
			processOngoingTxs();
			
			try {
				Thread.sleep(pollInterval);
			} catch (InterruptedException e) {
				log.info("Bitcoin processing thread interrupted", e);
			}
		}
		log.info("Stopping Bitcoin processing thread");
	}
	
	private Collection<Transaction> retrieveIncoming() {
		log.debug("Retrieving incoming transactions.");
		
		List<Transaction> incomingTxs = new LinkedList<Transaction>();
		int startingIndex = 0;
		int blockCounter = 0;
		boolean continueProcessing = false;
		
		do {
			// Retrieve a block of transactions
			LinkedList<Transaction> incomingTxBlock =
					new LinkedList<Transaction>(Arrays.asList(
							bitcoin.listtransactions("", incomingWindow, startingIndex)));
			if(incomingTxBlock.isEmpty() == false) {
				log.debug("Retrieved " + incomingTxBlock.size() + " transactions in block cycle.");
				startingIndex += incomingTxBlock.size();
			}
			
			// Decide whether to process another transaction block
			continueProcessing = false;
			Set<Transaction> incomingTxBlockSet = new HashSet<Transaction>(incomingTxBlock);
			if(incomingTxBlock.isEmpty() == false) {
				if(incomingTxBlockSet.contains(newestTx) == true) {
					// Remove the old transactions
					incomingTxBlock = removeOlderTransactions(incomingTxBlock, newestTx);
				} else {
					continueProcessing = true;
				}
			}
			incomingTxs.addAll(incomingTxBlock);
			
			// Update the newest TX ID for the first block of incoming transactions
			if(blockCounter == 0 && incomingTxBlock.isEmpty() == false) {
				newestTx = incomingTxBlock.getLast();
			}
			blockCounter++;
		} while(continueProcessing == true);
		
		log.debug("Retrieved " + incomingTxs.size() + " new transactions.");
		return incomingTxs;
	}
	
	private LinkedList<Transaction> removeOlderTransactions(LinkedList<Transaction> txBlock, Transaction lastTx) {
		log.debug("Removing all transactions older than transaction: " + lastTx.getTxid());
		Iterator<Transaction> txIter = txBlock.iterator();
		while(txIter.hasNext()) {
			Transaction tx = txIter.next();
			// Remove all transactions 
			if(tx.getTime() <= lastTx.getTime()) {
				txIter.remove();
			}
		}
		return txBlock;
	}
	
	
	private Collection<Transaction> processIncomingTxs(Collection<Transaction> incomingTxs) {
		for(Transaction tx : incomingTxs) {
			String gameAddress = tx.getAddress();
			GameLedger ledger = ledgers.get(gameAddress);
			// Only process transactions for which a payment ledger exists
			if(ledger != null) {
				// Lock on the ledger while updating its state based on the most recent transaction
				synchronized(ledger) {
					switch(ledger.getState()) {
						case IncomingWaiting:
							// Only process incoming transactions if the ledger is waiting
							processValidIncoming(ledger, tx);
							break;
						default:
							// Issue automatic refund for any incoming transaction on a non-waiting ledger
							issueTxRefund(ledger, tx.getAmount());
							break;
					}
				}
			}
		}
		return ongoingTxs;
	}
	
	private void processOngoingTxs() {
		for(Transaction tx : ongoingTxs) {
			processOngoingTx(tx);
		}
	}
	
	private void processOngoingTx(Transaction tx) {
		String gameAddress = tx.getAddress();
		Transaction updatedTransaction = bitcoin.gettransaction(tx.getTxid());
		
		// Update the confirmation count if it has increased
		if(updatedTransaction.getConfirmations() > tx.getConfirmations()) {
			tx = updatedTransaction;
			ongoingTxs.add(updatedTransaction);
		}
		
		// Check if the required number of confirmations have been reached
		if(tx.getConfirmations() >= config.getConfig().getRequiredConfirmations()) {
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
	
	private void processValidIncoming(GameLedger ledger, Transaction transaction) {
		Double amountReceived = transaction.getAmount();
		// Determine if the transaction is sufficient
		if(amountReceived.compareTo(0.0) != 0) {
			// Retrieve the information about the transaction
			String gameAddress = ledger.getGameAddress();
			String gameId = ledger.getGameId();
			String playerId = ledger.getPlayerId();
			String wagerTier = ledger.getWagerTier();
			
			WagerTier wager = config.getConfig().getWagerTiers().get(wagerTier);
			Double amountRequired = wager.getAmount();
			
			DecimalFormat amountFormatter = new DecimalFormat("#.########");
			log.info("Received payment from player: " + playerId + "\n" +
					"\tBitcoin Address: " + gameAddress + "\n" +
					"\tGame ID: " + gameId + "\n" +
					"\tWager Tier: " + wagerTier + "\n" +
					"\tAmount Required: " + amountFormatter.format(amountRequired) + "\n" +
					"\tAmount Received: " + amountFormatter.format(amountReceived));
			
			// Compare the amount received to the amount expected
			if(amountRequired.compareTo(amountReceived) == 0) {
				log.info("Received correct amount from player: " + playerId);
				
				// Provide update to the WS client
				PaymentWsCallback callbackSvc = finderServices.getService(ledger.getGameId());
				callbackSvc.playerPaid(gameId, playerId, amountReceived);
				
				// Store the transaction
				ongoingTxs.add(transaction);
				
				// Update the ledger
				ledger.setState(GameLedgerState.IncomingReceived);
				ledger.setIncomingAmount(amountReceived);
				ledger.setIncomingReceivedDate(new Date());
			} else {
				log.info("Did not receive correct amount, returning it back to the player.");
				issueTxRefund(ledger, transaction.getAmount());
			}
		}
	}
	
	public void issueTxRefund(GameLedger ledger, Double amountReceived) {
		String playerAddress = ledger.getPlayerAddress();
		String gameId = ledger.getGameId();
		String playerId = ledger.getPlayerId();
		log.info("Sending refund of " + amountReceived + " to " + playerAddress + " for game " + gameId);
		bitcoin.sendtoaddress(playerAddress, amountReceived, "type=refund, gameId=" + gameId + ", playerId=" + playerId);
	}
}
