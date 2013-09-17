package org.mvbrock.bcgames.payment.ws;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.ApplicationScoped;

import org.mvbrock.bcgames.payment.model.Game;
import org.mvbrock.bcgames.payment.model.Player;

@ApplicationScoped
public class GameManager implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// Map game ID to game
	private Map<String, Game> games = new ConcurrentHashMap<String, Game>();
	// Maps wager address to players
	private Map<String, Player> players = new ConcurrentHashMap<String, Player>();
	// Maps game ID to a mapping of player IDs to game ledgers
	private Map<String, Map<String, GameLedger>> ledgerSets = new ConcurrentHashMap<String, Map<String, GameLedger>>();
	// Maps player wager address to a game ledger
	private Map<String, GameLedger> ledgers = new ConcurrentHashMap<String, GameLedger>();
	
	public GameManager() { }
	
	public void addGame(Game game) {
		games.put(game.getId(), game);
	}
	
	public Game getGame(String gameId) {
		return games.get(gameId);
	}
	
	public void addPlayer(String wagerAddress, Player player) {
		players.put(wagerAddress, player);
	}
	
	public void addLedger(String gameId, GameLedger ledger) {
		// Add ledger to the game-to-ledger set map
		Map<String, GameLedger> ledgerMap;
		if(ledgerSets.containsKey(gameId) == true) {
			ledgerMap = ledgerSets.get(gameId);
		} else {
			ledgerMap = new ConcurrentHashMap<String, GameLedger>();
		}
		ledgerMap.put(ledger.getPlayerId(), ledger);
		ledgerSets.put(gameId, ledgerMap);
		
		// Add ledger to the player-to-ledger map
		ledgers.put(ledger.getWagerAddress(), ledger);
	}
	
	public GameLedger getLedger(String wagerAddress) {
		return ledgers.get(wagerAddress);
	}
	
	public Collection<GameLedger> getLedgerCollection(String gameId) {
		if(ledgerSets.containsKey(gameId) == true) {
			return ledgerSets.get(gameId).values();
		} else {
			return null;
		}
	}
}
