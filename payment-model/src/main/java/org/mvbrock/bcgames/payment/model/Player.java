package org.mvbrock.bcgames.payment.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class Player {
	private String id;
	private String payoutAddress;
	private String wagerAddress;

	public Player() { }
	
	public Player(String id) {
		this.id = id;
	}

	public Player(String id, String playerAddress, String gameAddress) {
		this.id = id;
		this.payoutAddress = playerAddress;
		this.wagerAddress = gameAddress;
	}

	public String getId() {
		return id;
	}

	public String getPayoutAddress() {
		return payoutAddress;
	}

	public void setPayoutAddress(String payoutAddress) {
		this.payoutAddress = payoutAddress;
	}

	public String getWagerAddress() {
		return wagerAddress;
	}

	public void setWagerAddress(String wagerAddress) {
		this.wagerAddress = wagerAddress;
	}
}
