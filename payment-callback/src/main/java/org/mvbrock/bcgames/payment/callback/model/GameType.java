package org.mvbrock.bcgames.payment.callback.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class GameType {
	private String id;
	private String name;
	private Integer minPlayers;
	private Integer maxPlayers;
	
	public GameType() { }
	
	public GameType(String id, String name, Integer minPlayers, Integer maxPlayers) {
		super();
		this.id = id;
		this.name = name;
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Integer getMinPlayers() {
		return minPlayers;
	}

	public Integer getMaxPlayers() {
		return maxPlayers;
	}
}
