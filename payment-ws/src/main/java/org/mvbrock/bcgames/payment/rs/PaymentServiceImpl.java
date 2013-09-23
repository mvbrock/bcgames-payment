package org.mvbrock.bcgames.payment.rs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.mvbrock.bcgames.payment.rs.interfaces.PaymentCallback;
import org.mvbrock.bcgames.payment.rs.interfaces.PaymentService;
import org.mvbrock.bcgames.payment.model.Game;
import org.mvbrock.bcgames.payment.model.GameStatus;
import org.mvbrock.bcgames.payment.model.GameType;
import org.mvbrock.bcgames.payment.model.Player;
import org.mvbrock.bcgames.payment.model.WagerTier;

@SessionScoped
public class PaymentServiceImpl implements PaymentService, Serializable {
	private static final long serialVersionUID = 1L;

	@Inject
	private transient Logger log;
	
	@Inject
	private PaymentConfigStore config;

	@Inject
	private BitcoinController bitcoinCtl;

	@Inject
	private CallbackServiceTracker callbackTracker;
	
	@Inject
	private GameStore gameMgr;

	public PaymentServiceImpl() { }

	@PostConstruct
	public void init() { }

	public Game createGame(Game game, String callbackUrl) {
		log.info("Creating new game \"" + game.getId() + "\" from: " + callbackUrl);
		
		// Create and store the RESTEasy client
		callbackTracker.addClient(game.getId(), callbackUrl);
		
		// Store the game and return it
		gameMgr.addGame(game);
		return game;
	}

	public void playerJoined(String gameId, Player player) {
		log.info("Player " + player.getId() + " joined game: " + gameId);
		
		// Wait for the incoming payment
		Game game = gameMgr.getGame(gameId);
		game.addPlayer(player);
		
		String wagerAddress = bitcoinCtl.generateWagerAddress(game, player.getId());
		player.setWagerAddress(wagerAddress);
		log.info("Waiting for incoming payment from player " + player.getId() + " on Bitcoin address: " +
				wagerAddress);
		
		// Return the generated address to the player
		PaymentCallback callback = callbackTracker.get(gameId);
		callback.updateWagerAddress(gameId, player.getId(), wagerAddress);
		log.info("Provided player " + player.getId() + " with game address: " + wagerAddress);
		
		// Store the player
		gameMgr.addPlayer(wagerAddress, player);
	}
	
	public void playerLeft(String gameId, String playerId) {
		Game game = gameMgr.getGame(gameId);
		
		switch(game.getStatus()) {
			case Created:
				// Issue a refund to the player if the game hasn't started yet
				bitcoinCtl.issueRefund(game, playerId);
				game.removePlayer(playerId);
				break;
			case Started:
				// Indicate that the player has lost
				game.playerLost(playerId);
				break;
			default:
				break;
		}
	}
	
	public void startGame(String gameId, String callbackUrl) {
		log.info("Setting game status to started.");
		Game game = gameMgr.getGame(gameId);
		game.setStatus(GameStatus.Started);
		
		log.info("Updating callback address for game " + gameId + ": " + callbackUrl);
		callbackTracker.addClient(gameId, callbackUrl);
	}
	
	public void endGame(String gameId, String winnerId) {
		Game game = gameMgr.getGame(gameId);
		game.playerWon(winnerId);
		for(Player player : game.getPlayerCollection()) {
			game.playerLost(player.getId());
		}
	}
	
	public void payWinner(String gameId) {
		Game game = gameMgr.getGame(gameId);
		bitcoinCtl.queueWinnerPayout(game);
	}

	public List<WagerTier> getWagerTiers() {
		List<WagerTier> wagerTiers = new ArrayList<WagerTier>(config.getConfig().getWagerTiers().values());
		log.debug("Returning " + wagerTiers.size() + " wager tiers.");
		return wagerTiers;
	}

	public List<GameType> getGameTypes() {
		List<GameType> gameTypes = new ArrayList<GameType>(config.getConfig().getGameTypes().values());
		log.debug("Returning " + gameTypes.size() + " game types.");
		return gameTypes;
	}
}
