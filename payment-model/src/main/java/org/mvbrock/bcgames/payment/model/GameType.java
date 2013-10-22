package org.mvbrock.bcgames.payment.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class GameType extends GameDescriptor {
	private String url;
	private Integer minPlayers;
	private Integer maxPlayers;
	
	public GameType() { }
	
	public GameType(String id, String name, String url, Integer minPlayers, Integer maxPlayers) {
		super(id, name);
		this.url = url;
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
	}
	
	public String getUrl() {
		return url;
	}

	public Integer getMinPlayers() {
		return minPlayers;
	}

	public Integer getMaxPlayers() {
		return maxPlayers;
	}
}
