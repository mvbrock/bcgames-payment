package org.mvbrock.bcgames.payment.ws;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.mvbrock.bcgames.payment.ws.interfaces.PaymentWsCallback;
import org.mvbrock.bcgames.payment.ws.interfaces.PaymentWsService;
import org.mvbrock.bcgames.payment.model.Game;
import org.mvbrock.bcgames.payment.model.GameStatus;
import org.mvbrock.bcgames.payment.model.GameType;
import org.mvbrock.bcgames.payment.model.Player;
import org.mvbrock.bcgames.payment.model.WagerTier;

@SessionScoped
public class PaymentWsServiceImpl implements PaymentWsService, Serializable {
	private static final long serialVersionUID = 1L;

	@Inject
	private transient Logger log;
	
	// Maps indivual player game addresses to games
	private Map<String, Game> games = new HashMap<String, Game>();
	// Maps individual player game addresses to players
	private Map<String, Player> players = new HashMap<String, Player>();

	@Inject
	private PaymentWsConfigStore config;

	@Inject
	private BitcoinController bitcoinCtl;

	@Inject
	private CallbackServiceTracker finderServices;

	public PaymentWsServiceImpl() { }

	@PostConstruct
	public void init() { }

	public Game createGame(Game game, String callbackUrl) {
		log.info("Creating new game \"" + game.getId() + "\" from: " + callbackUrl);
		
		// Create and store the RESTEasy client
		finderServices.addClient(game.getId(), callbackUrl);
		
		// Store the game and return it
		games.put(game.getId(), game);
		return game;
	}

	public void playerJoined(String gameId, Player player) {
		log.info("Player " + player.getId() + " joined game: " + gameId);
		
		// Wait for the incoming payment
		Game game = games.get(gameId);
		game.addPlayer(player);
		
		String gameAddress = bitcoinCtl.initializeNewGameAddress(game, player.getId());
		player.setWagerAddress(gameAddress);
		log.info("Waiting for incoming payment from player " + player.getId() + " on Bitcoin address: " +
				gameAddress);
		
		// Return the generated address to the player
		PaymentWsCallback callbackSvc = finderServices.getService(gameId);
		callbackSvc.updateWagerAddress(gameId, player.getId(), gameAddress);
		log.info("Provided player " + player.getId() + " with game address: " + gameAddress);
		
		// Store the player
		players.put(gameAddress, player);
	}
	
	public void playerLeft(String gameId, String playerId) {
		Game game = games.get(gameId);
		
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
	
	public void startGame(String gameId, String gameCallbackUrl) {
		log.info("Setting game status to started.");
		Game game = games.get(gameId);
		game.setStatus(GameStatus.Started);
		
		log.info("Updating callback address for game " + gameId + ": " + gameCallbackUrl);
		finderServices.addClient(gameId, gameCallbackUrl);
	}
	
	public void endGame(String gameId, String winnerId) {
		Game game = games.get(gameId);
		game.playerWon(winnerId);
		for(Player player : game.getPlayerCollection()) {
			game.playerLost(player.getId());
		}
	}
	
	public void payWinner(String gameId) {
		Game game = games.get(gameId);
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
