package org.mvbrock.bcgames.payment.callback.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class Player {
	private String id;
	private String playerAddress;
	private String gameAddress;

	public Player() { }
	
	public Player(String id) {
		this.id = id;
	}

	public Player(String id, String playerAddress, String gameAddress) {
		this.id = id;
		this.playerAddress = playerAddress;
		this.gameAddress = gameAddress;
	}

	public String getId() {
		return id;
	}

	public String getPlayerAddress() {
		return playerAddress;
	}

	public void setPlayerAddress(String playerAddress) {
		this.playerAddress = playerAddress;
	}

	public String getGameAddress() {
		return gameAddress;
	}

	public void setGameAddress(String gameAddress) {
		this.gameAddress = gameAddress;
	}
}
