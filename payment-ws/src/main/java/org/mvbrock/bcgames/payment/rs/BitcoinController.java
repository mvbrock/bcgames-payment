package org.mvbrock.bcgames.payment.rs;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import org.mvbrock.bcgames.payment.model.Game;
import org.mvbrock.bcgames.payment.model.Player;

@SessionScoped
public class BitcoinController implements Serializable {
	private static final long serialVersionUID = 1L;

	@Inject
	private transient Logger log;
	
	@Inject
	private BitcoinRpcClient bitcoin;
	
	@Inject
	private BitcoinProcessingThread bcProcessing;
	
	@Inject
	private PaymentConfigStore config;
	
	@Inject
	private GameStore gameStore;
	
	public BitcoinController() { }

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
	
	public void issueRefund(Game game, String playerId) {
		// Retrieve the player info in order to issue the refund
		Player player = game.getPlayer(playerId);
		String wagerAddress = player.getWagerAddress();
		
		// Retrieve the ledger and the original amount received
		Ledger ledger = gameStore.getLedger(wagerAddress);
		Double amountReceived = ledger.getIncomingTx().getAmount();
		
		// Issue the refund
		bcProcessing.issueTxRefund(ledger, amountReceived);
	}
	
	public String generateWagerAddress(Game game, String playerId) {
		final String wagerAddress = bitcoin.getnewaddress();
		final String payoutAddress = game.getPlayer(playerId).getPayoutAddress();
		final String gameId = game.getId();
		Ledger ledger = new Ledger(gameId, playerId, game.getTier().getId(), payoutAddress, wagerAddress);
		gameStore.addLedger(gameId, ledger);
		return wagerAddress;
	}
}
