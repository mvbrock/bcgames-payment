package org.mvbrock.bcgames.payment.model;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class Game {
	private String id;
	private GameType type;
	private WagerTier tier;
	private GameStatus status;
	private Map<String, Player> players = new HashMap<String, Player>();
	private Map<String, Player> losers = new HashMap<String, Player>();
	private Player winner;
	
	public Game() { }
	
	public Game(GameType type, WagerTier wager) {
		this.id = UUID.randomUUID().toString() + (new Date()).getTime();
		this.type = type;
		this.tier = wager;
		this.status = GameStatus.Created;
	}
	
	public Game(String id, GameType type, WagerTier wager) {
		this.id = id;
		this.type = type;
		this.tier = wager;
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

	public WagerTier getTier() {
		return tier;
	}

	public GameStatus getStatus() {
		return status;
	}

	public void setStatus(GameStatus status) {
		this.status = status;
	}
	
	public Map<String, Player> getPlayers() {
		return players;
	}

	public void setPlayers(Map<String, Player> players) {
		this.players = players;
	}
	
	@JsonIgnore
	public Collection<Player> getPlayerCollection() {
		return players.values();
	}
	
	@JsonIgnore
	public Player [] getPlayerCollectionAsArray() {
		return players.values().toArray(new Player[players.values().size()]);
	}

	@JsonIgnore
	public Player getPlayer(String id) {
		return players.get(id);
	}
	
	public void addPlayer(Player player) {
		players.put(player.getId(), player);
	}
	
	public void removePlayer(String id) {
		players.remove(id);
	}
	
	public Map<String, Player> getLosers() {
		return losers;
	}

	public void setLosers(Map<String, Player> losers) {
		this.losers = losers;
	}

	public void playerLost(String id) {
		if(players.containsKey(id)) {
			Player player = players.remove(id);
			losers.put(id, player);
		}
	}
	
	@JsonIgnore
	public Collection<Player> getLoserCollection() {
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
