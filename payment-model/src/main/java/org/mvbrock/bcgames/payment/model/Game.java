package org.mvbrock.bcgames.payment.model;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class Game {
	private String id;
	private GameType type;
	private WagerTier wager;
	private GameStatus status;
	private Map<String, Player> players = new HashMap<String, Player>();
	private Map<String, Player> losers = new HashMap<String, Player>();
	private Player winner;
	
	public Game() { }
	
	public Game(GameType type, WagerTier wager) {
		this.id = UUID.randomUUID().toString() + (new Date()).getTime();
		this.type = type;
		this.wager = wager;
		this.status = GameStatus.Created;
	}
	
	public Game(String id, GameType type, WagerTier wager) {
		this.id = id;
		this.type = type;
		this.wager = wager;
		this.status = GameStatus.Created;
	}

	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	public GameType getType() {
		return type;
	}

	public WagerTier getWager() {
		return wager;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}
	
	public Collection<Player> getPlayers() {
		return players.values();
	}
	
	public Player [] getPlayersAsArray() {
		return players.values().toArray(new Player[players.values().size()]);
	}

	public Player getPlayer(String id) {
		return players.get(id);
	}
	
	public void addPlayer(Player player) {
		players.put(player.getId(), player);
	}
	
	public void removePlayer(String id) {
		players.remove(id);
	}
	
	public void playerLost(String id) {
		if(players.containsKey(id)) {
			Player player = players.remove(id);
			losers.put(id, player);
		}
	}
	
	public Collection<Player> getLosers() {
		return losers.values();
	}
	
	public void playerWon(String id) {
		if(players.containsKey(id)) {
			Player player = players.remove(id);
			winner = player;
		}
	}
	
	public Player getWinner() {
		return winner;
	}
}
