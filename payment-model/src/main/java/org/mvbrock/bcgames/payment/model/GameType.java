package org.mvbrock.bcgames.payment.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class GameType {
	private String id;
	private String name;
	private String url;
	private Integer minPlayers;
	private Integer maxPlayers;
	
	public GameType() { }
	
	public GameType(String id, String name, String url, Integer minPlayers, Integer maxPlayers) {
		this.id = id;
		this.name = name;
		this.url = url;
		this.minPlayers = minPlayers;
		this.maxPlayers = maxPlayers;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
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
