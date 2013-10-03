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
import org.mvbrock.bcgames.payment.model.Player;
import org.mvbrock.bcgames.payment.model.WagerTier;

@Singleton
public class BitcoinProcessingThread implements Runnable, Serializable {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private transient Logger log;
	
	@Inject
	private GameStore gameStore;
	
	@Inject
	private PaymentConfigStore config;
	
	@Inject
	private CallbackServiceTracker callbackTracker;
	
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
				log.info("Bitcoin processing thread sleep interrupted", e);
			}
		}
		log.info("Stopping Bitcoin processing thread");
	}
	
	private Collection<Transaction> retrieveIncomingTxs() {
		log.debug("Retrieving incoming transactions.");
		
		List<Transaction> incomingTxs = new LinkedList<Transaction>();
		int startingIndex = 0;
		int blockCounter = 0;
		boolean processAnotherBlock = false;
		
		do {
			// Retrieve a block of transactions
			processAnotherBlock = false;
			LinkedList<Transaction> incomingTxBlock =
					new LinkedList<Transaction>(
							Arrays.asList(bitcoin.listtransactions("", incomingWindow, startingIndex)));
			startingIndex += incomingTxBlock.size();
			
			if(incomingTxBlock.isEmpty() == false) {
				log.debug("Retrieved " + incomingTxBlock.size() + " transactions in block cycle.");
				
				Set<Transaction> incomingTxBlockSet = new HashSet<Transaction>(incomingTxBlock);
				// Only process another TX block if the newest TX is not present
				if(incomingTxBlockSet.contains(newestTx) == false) {
					processAnotherBlock = true;
				} else {
					// Remove the already-processed transactions prior to the newest TX
					incomingTxBlock = removeOlderTransactions(incomingTxBlock, newestTx);
				}
				incomingTxs.addAll(incomingTxBlock);
				
				// Update the newest TX ID for the first block of incoming transactions
				if(blockCounter == 0 && incomingTxBlock.isEmpty() == false) {
					newestTx = incomingTxBlock.getLast();
				}
			}
			
			blockCounter++;
		} while(processAnotherBlock == true);
		
		log.debug("Retrieved " + incomingTxs.size() + " new transactions.");
		return incomingTxs;
	}
	
	private LinkedList<Transaction> removeOlderTransactions(LinkedList<Transaction> txBlock, Transaction lastTx) {
		log.debug("Removing all transactions older than transaction: " + lastTx.getTxid());
		Iterator<Transaction> txIter = txBlock.iterator();
		while(txIter.hasNext()) {
			Transaction tx = txIter.next();
			// Remove all transactions older than the last TX
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
			Ledger ledger = gameStore.getLedger(wagerAddress);
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
		
		// Store the new TX in the ledger
		ledger.setIncomingTx(tx);
		
		// Ensure the ledger is in a valid state
		if(ledger.getIncomingState() != LedgerIncomingState.IncomingWaiting) {
			log.info("Received payment for a non-waiting ledger, issuing refund to player: " + ledger.getPlayerId());
			issueTxRefund(ledger, tx.getAmount());
			return false;
		}
		
		// Retrieve information about the associated wager
		WagerTier wager = config.getConfig().getWagerTiers().get(ledger.getWagerTier());
		Double amountRequired = wager.getAmount();
		DecimalFormat amountFormatter = new DecimalFormat("#.########");
		log.info("Received payment from player: " + ledger.getPlayerId() + "\n" +
				"\tBitcoin Address: " + ledger.getWagerAddress() + "\n" +
				"\tGame ID: " + ledger.getGameId() + "\n" +
				"\tWager Tier: " + ledger.getWagerTier() + "\n" +
				"\tAmount Required: " + amountFormatter.format(amountRequired) + "\n" +
				"\tAmount Received: " + amountFormatter.format(tx.getAmount()));
		
		// Ensure the correct amount was received
		if(amountRequired.compareTo(tx.getAmount()) != 0) {
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
			log.info("Received correct amount from player " + playerId + " for game: " + gameId);
			
			// Update the ledger
			ledger.setIncomingState(LedgerIncomingState.IncomingReceived);
			
			// Provide update to the WS client
			PaymentCallback callbackSvc = callbackTracker.get(gameId);
			if(callbackSvc != null) {
				callbackSvc.playerPaid(gameId, playerId, ledger.getIncomingTx().getAmount());
				
				// Determine if all players have paid
				if(allPlayersHavePaid(gameId) == true) {
					log.info("All players have paid for game: " + gameId);
					callbackSvc.gameCanStart(gameId);
				}
			} else {
				log.error("No callback client in the callback tracker for game: " + gameId);
			}
		}
	}
	
	public boolean allPlayersHavePaid(String gameId) {
		Collection<Ledger> ledgers = gameStore.getLedgerCollection(gameId);
		if(ledgers == null) {
			log.error("No ledger set found for game: " + gameId);
			return false;
		}
		
		boolean allPaid = true;
		// Look for any players that have waiting payments
		for(Ledger ledger : ledgers) {
			if(ledger.getIncomingState() == LedgerIncomingState.IncomingWaiting) {
				log.info("Still waiting for payment from player " + ledger.getPlayerId() + " for game: " + gameId);
				allPaid = false;
			}
		}
		
		// Determine if the number of ledgers is greater or equal to the minimum required players
		Game game = gameStore.getGame(gameId);
		final Integer minPlayers = game.getType().getMinPlayers();
		if(ledgers.size() < minPlayers) {
			log.info("Game currently has " + ledgers.size() + " players, requires at least " + minPlayers);
			allPaid = false;
		}
		return allPaid;
	}
	
	private void processOngoingLedgers(Collection<Ledger> ongoingLedgers) {
		for(Ledger ledger : ongoingLedgers) {
			processOngoingLedger(ledger);
		}
	}
	
	private void processOngoingLedger(Ledger ledger) {
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
				Double rake = ledger.getOutgoingAmount() * config.getConfig().getRake();
				Double payout = ledger.getOutgoingAmount() - rake;
				ledger.setOutgoingAmount(payout);
				
				// Issue payment to the player and rake address
				bitcoin.sendtoaddress(ledger.getPayoutAddress(), payout, "type=payout, gameId=" +
						ledger.getGameId() + ", playerId=" + ledger.getPlayerId());
				bitcoin.sendtoaddress(config.getConfig().getRakeAddress(), rake, "type=rake, gameId=" +
						ledger.getGameId() + ", playerId=" + ledger.getPlayerId());
			}
		}
	}

	public void issueRefund(Game game, String playerId) {
		Player player = game.getPlayer(playerId);
		String wagerAddress = player.getWagerAddress();
		Ledger ledger = gameStore.getLedger(wagerAddress);
		Double amountReceived = ledger.getIncomingTx().getAmount();
		issueTxRefund(ledger, amountReceived);
	}
	
	private void issueTxRefund(Ledger ledger, Double amountReceived) {
		log.info("Sending refund of " + amountReceived + " to " + ledger.getPayoutAddress() +
				" for game " + ledger.getGameId());
		bitcoin.sendtoaddress(ledger.getPayoutAddress(), amountReceived, "type=refund, gameId=" +
				ledger.getGameId() + ", playerId=" + ledger.getPlayerId());
	}
	
	public String generateWagerAddress(Game game, String playerId) {
		final String wagerAddress = bitcoin.getnewaddress();
		final String payoutAddress = game.getPlayer(playerId).getPayoutAddress();
		gameStore.addLedger(game.getId(),
				new Ledger(game.getId(), playerId, game.getTier().getId(), payoutAddress, wagerAddress));
		return wagerAddress;
	}
	
	public void queueWinnerPayout(Game game) {
		Player winner = game.getWinner();
		String winnerWagerAddress = winner.getWagerAddress();
		Ledger winningLedger = gameStore.getLedger(winnerWagerAddress);
		
		// Initialize the payout with the original incoming amount from the player
		Double payoutAmount = winningLedger.getIncomingTx().getAmount();
		
		for(Player loser : game.getLoserCollection()) {
			// Retrieve the losing ledger
			String loserWagerAddress = loser.getWagerAddress();
			Ledger losingLedger = gameStore.getLedger(loserWagerAddress);
			
			// Add the incoming amount from the losing ledger to the payout
			payoutAmount += losingLedger.getIncomingTx().getAmount();
		}
		
		// Update the winning ledger with the final payout amount
		winningLedger.setOutgoingState(LedgerOutgoingState.OutgoingWinnerWaiting);
		winningLedger.setOutgoingAmount(payoutAmount);
	}
}
