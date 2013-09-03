package org.mvbrock.bcgames.payment.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class WagerTier {
	private String id;
	private String name;
	private Double amount;
	
	public WagerTier() { }
	
	public WagerTier(String id, String name, Double amount) {
		this.id = id;
		this.name = name;
		this.amount = amount;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Double getAmount() {
		return amount;
	}
}
