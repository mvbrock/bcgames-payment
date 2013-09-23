package org.mvbrock.bcgames.payment.rs;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.mvbrock.bcgames.payment.rs.interfaces.PaymentCallback;
import org.mvbrock.bcgames.payment.model.Game;
import org.mvbrock.bcgames.payment.model.WagerTier;

@Singleton
public class BitcoinProcessingThread implements Runnable, Serializable {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private transient Logger log;
	
	@Inject
	private GameStore gameMgr;
	
	@Inject
	private PaymentConfigStore config;
	
	@Inject
	private CallbackServiceTracker finderServices;
	
	@Inject
	private BitcoinRpcClient bitcoin;
	
	private final int incomingWindow = 100;
	
	// Transactions mapped by incoming Bitcoin address
	private Set<Ledger> ongoingLedgers = new HashSet<Ledger>();
	private Transaction newestTx = null;
	
	private Integer pollInterval;
	private AtomicBoolean isRunning;
	private Thread thread;
	
	public BitcoinProcessingThread() { }
	
	@PostConstruct
	public void init() {
		pollInterval = Integer.decode(config.getProperty(PaymentConfigStore.BitcoinPollInterval));
		isRunning = new AtomicBoolean(true);
		thread = new Thread(this);
		log.info("Initializing processing thread with polling interval: " + pollInterval);
		thread.start();
	}
	
	@PreDestroy
	public void uninit() {
		isRunning.set(false);
		thread.interrupt();
	}
	
	public void run() {
		log.info("Starting Bitcoin processing thread");
		while(isRunning.get() == true) {
			// Retrieve the incoming transactions
			Collection<Transaction> incomingTxs = retrieveIncomingTxs();
			
			// Process the valid incoming transactions
			Collection<Ledger> incomingLedgers = processIncomingTxs(incomingTxs);
				
			// Check existing transaction confirmations for payout
			processIncomingLedgers(incomingLedgers);
			
			// Process all of the ongoing ledgers
			ongoingLedgers.addAll(incomingLedgers);
			processOngoingLedgers(ongoingLedgers);
			
			try {
				Thread.sleep(pollInterval);
			} catch (InterruptedException e) {
				log.info("Bitcoin processing thread interrupted", e);
			}
		}
		log.info("Stopping Bitcoin processing thread");
	}
	
	private Collection<Transaction> retrieveIncomingTxs() {
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
	
	private Collection<Ledger> processIncomingTxs(Collection<Transaction> incomingTxs) {
		Set<Ledger> validIncomingLedgers =  new HashSet<Ledger>();
		for(Transaction tx : incomingTxs) {
			String wagerAddress = tx.getAddress();
			Ledger ledger = gameMgr.getLedger(wagerAddress);
			boolean isValid = validateIncomingTx(ledger, tx);
			if(isValid == true) {
				validIncomingLedgers.add(ledger);
			}
		}
		return validIncomingLedgers;
	}
	
	private boolean validateIncomingTx(Ledger ledger, Transaction tx) {
		// Ensure the ledger exists
		if(ledger == null) {
			return false;
		}
		
		// Retrieve the information about the transaction
		Double amountReceived = tx.getAmount();
		String gameAddress = ledger.getWagerAddress();
		String gameId = ledger.getGameId();
		String playerId = ledger.getPlayerId();
		String wagerTier = ledger.getWagerTier();
		
		// Ensure the ledger is in a valid state
		if(ledger.getIncomingState() != LedgerIncomingState.IncomingWaiting) {
			log.info("Received payment for a non-waiting ledger, issuing refund to player: " + playerId);
			issueTxRefund(ledger, tx.getAmount());
			return false;
		}
		
		// Retrieve information about the associated wager
		WagerTier wager = config.getConfig().getWagerTiers().get(wagerTier);
		Double amountRequired = wager.getAmount();
		
		DecimalFormat amountFormatter = new DecimalFormat("#.########");
		log.info("Received payment from player: " + playerId + "\n" +
				"\tBitcoin Address: " + gameAddress + "\n" +
				"\tGame ID: " + gameId + "\n" +
				"\tWager Tier: " + wagerTier + "\n" +
				"\tAmount Required: " + amountFormatter.format(amountRequired) + "\n" +
				"\tAmount Received: " + amountFormatter.format(amountReceived));
		
		// Ensure the correct amount was received
		if(amountRequired.compareTo(amountReceived) != 0) {
			log.info("Did not receive correct amount, returning it back to the player.");
			issueTxRefund(ledger, tx.getAmount());
			return false;
		}
		
		return true;
	}
	
	private void processIncomingLedgers(Collection<Ledger> incomingLedgers) {
		for(Ledger ledger : incomingLedgers) {
			String gameId = ledger.getGameId();
			String playerId = ledger.getPlayerId();
			log.info("Received correct amount from player: " + playerId);
			
			// Update the ledger
			ledger.setIncomingState(LedgerIncomingState.IncomingReceived);
			
			// Provide update to the WS client
			PaymentCallback callbackSvc = finderServices.get(gameId);
			callbackSvc.playerPaid(gameId, playerId, ledger.getIncomingTx().getAmount());
			
			// Determine if all players have paid
			if(allPlayersHavePaid(gameId) == true) {
				log.info("All players have paid for game: " + gameId);
				callbackSvc.gameCanStart(gameId);
			}
		}
	}
	
	private void processOngoingLedgers(Collection<Ledger> ongoingLedgers) {
		for(Ledger ledger : ongoingLedgers) {
			processOngoingTx(ledger);
		}
	}
	
	private void processOngoingTx(Ledger ledger) {
		Transaction incomingTx = ledger.getIncomingTx();
		Transaction updatedIncomingTx = bitcoin.gettransaction(incomingTx.getTxid());
		
		// Update the confirmation count if it has increased
		if(updatedIncomingTx.getConfirmations() > incomingTx.getConfirmations()) {
			incomingTx = updatedIncomingTx;
			ledger.setIncomingTx(updatedIncomingTx);
		}
		
		// Check if the required number of confirmations have been reached
		if(incomingTx.getConfirmations() >= config.getConfig().getRequiredConfirmations()) {
			// If the ledger indicates the winner is waiting for payment, send the payment
			if(ledger.getOutgoingState() == LedgerOutgoingState.OutgoingWinnerWaiting) {
				// Separate the rake from the outgoing amount
				Double payout = ledger.getOutgoingAmount();
				Double rake = payout * config.getConfig().getRake();
				ledger.setOutgoingAmount(payout - rake);
				
				// Retrieve the addresses/info to send to
				String gameId = ledger.getGameId();
				String playerId = ledger.getPlayerId();
				String playerAddress = ledger.getPayoutAddress();
				String rakeAddress = config.getConfig().getRakeAddress();
				
				// Issue payment to the player and rake address
				bitcoin.sendtoaddress(playerAddress, payout, "type=payout, gameId=" + gameId + ", playerId=" + playerId);
				bitcoin.sendtoaddress(rakeAddress, rake, "type=rake, gameId=" + gameId + ", playerId=" + playerId);
			}
		}
	}
	
	public boolean allPlayersHavePaid(String gameId) {
		Collection<Ledger> ledgers = gameMgr.getLedgerCollection(gameId);
		if(ledgers == null) {
			return false;
		}
		
		boolean allPaid = true;
		// Look for any players that have waiting payments
		for(Ledger ledger : ledgers) {
			if(ledger.getIncomingState() == LedgerIncomingState.IncomingWaiting) {
				allPaid = false;
				break;
			}
		}
		
		// Determine if the number of ledgers is greater or equal to the minimum required players
		Game game = gameMgr.getGame(gameId);
		if(ledgers.size() < game.getType().getMinPlayers()) {
			allPaid = false;
		}
		
		return allPaid;
	}
	
	public void issueTxRefund(Ledger ledger, Double amountReceived) {
		String playerAddress = ledger.getPayoutAddress();
		String gameId = ledger.getGameId();
		String playerId = ledger.getPlayerId();
		log.info("Sending refund of " + amountReceived + " to " + playerAddress + " for game " + gameId);
		bitcoin.sendtoaddress(playerAddress, amountReceived, "type=refund, gameId=" + gameId + ", playerId=" + playerId);
	}
}
