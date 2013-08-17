package org.mvbrock.bcgames.payment.ws;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.mvbrock.bcgames.payment.model.Game;
import org.mvbrock.bcgames.payment.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@SessionScoped
public class BitcoinController implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(BitcoinController.class);
	private static final long serialVersionUID = 1L;

	@Inject
	private BitcoinRpcClient bitcoin;
	
	@Inject
	private BitcoinProcessing bcProcessing;
	
	@Inject
	private PaymentMspConfigStore config;
	
	public BitcoinController() { }

	public void queueWinnerPayout(Game game) {
		Player winner = game.getWinner();
		String winnerGameAddress = winner.getGameAddress();
		GameLedger winningLedger = bcProcessing.getLedger(winnerGameAddress);
		
		// Initialize the payout with the original incoming amount from the player
		Double payout = winningLedger.getIncomingAmount();
		
		for(Player loser : game.getLosers()) {
			// Retrieve the losing ledger
			String loserGameAddress = loser.getGameAddress();
			GameLedger losingLedger = bcProcessing.getLedger(loserGameAddress);
			
			// Add the incoming amount from the losing ledger to the payout
			payout += losingLedger.getIncomingAmount();
		}
		
		// Update the winning ledger with the final outgoing amount
		winningLedger.setState(GameLedgerState.OutgoingWinnerWaiting);
		winningLedger.setOutgoingAmount(payout);
	}
	
	public void issueRefund(Game game, String playerId) {
		// Retrieve the player info in order to issue the refund
		String gameId = game.getId();
		Player player = game.getPlayer(playerId);
		String playerAddress = player.getPlayerAddress();
		String gameAddress = player.getGameAddress();
		
		// Retrieve the ledger and the original amount received
		GameLedger ledger = bcProcessing.getLedger(gameAddress);
		Double amountReceived = ledger.getIncomingAmount();
		
		// Issue the refund
		bitcoin.sendtoaddress(playerAddress, amountReceived, "type=refund, gameId=" + gameId + ", playerId=" + playerId);
		log.info("Sent refund of " + amountReceived + " to " + playerAddress + " for game " + gameId);
	}
	
	public String waitForIncomingPayment(Game game, String playerId) {
		String gameAddress = bitcoin.getnewaddress();
		String playerAddress = game.getPlayer(playerId).getPlayerAddress();
		GameLedger ledger = new GameLedger(game.getId(), playerId, GameLedgerState.IncomingWaiting,
				game.getWager().getId(), playerAddress, gameAddress);
		bcProcessing.createLedger(gameAddress, ledger);
		return gameAddress;
	}
}
