package org.mvbrock.bcgames.payment.ws;

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
	private PaymentWsConfigStore config;
	
	public BitcoinController() { }

	public void queueWinnerPayout(Game game) {
		Player winner = game.getWinner();
		String winnerPayoutAddress = winner.getWagerAddress();
		GameLedger winningLedger = bcProcessing.getLedger(winnerPayoutAddress);
		
		// Initialize the payout with the original incoming amount from the player
		Double payoutAmount = winningLedger.getIncomingAmount();
		
		for(Player loser : game.getLoserCollection()) {
			// Retrieve the losing ledger
			String loserWagerAddress = loser.getWagerAddress();
			GameLedger losingLedger = bcProcessing.getLedger(loserWagerAddress);
			
			// Add the incoming amount from the losing ledger to the payout
			payoutAmount += losingLedger.getIncomingAmount();
		}
		
		// Update the winning ledger with the final payout amount
		winningLedger.setState(GameLedgerState.OutgoingWinnerWaiting);
		winningLedger.setOutgoingAmount(payoutAmount);
	}
	
	public void issueRefund(Game game, String playerId) {
		// Retrieve the player info in order to issue the refund
		Player player = game.getPlayer(playerId);
		String gameAddress = player.getWagerAddress();
		
		// Retrieve the ledger and the original amount received
		GameLedger ledger = bcProcessing.getLedger(gameAddress);
		Double amountReceived = ledger.getIncomingAmount();
		
		// Issue the refund
		bcProcessing.issueTxRefund(ledger, amountReceived);
	}
	
	public String initializeNewGameAddress(Game game, String playerId) {
		String gameAddress = bitcoin.getnewaddress();
		String playerAddress = game.getPlayer(playerId).getPayoutAddress();
		GameLedger ledger = new GameLedger(game.getId(), playerId, GameLedgerState.IncomingWaiting,
				game.getTier().getId(), playerAddress, gameAddress);
		bcProcessing.createLedger(gameAddress, ledger);
		return gameAddress;
	}
}
