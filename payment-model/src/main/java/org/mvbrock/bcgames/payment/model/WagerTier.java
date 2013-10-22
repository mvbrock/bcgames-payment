package org.mvbrock.bcgames.payment.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class WagerTier extends GameDescriptor {
	private Double amount;
	
	public WagerTier() { }
	
	public WagerTier(String id, String name, Double amount) {
		super(id, name);
		this.amount = amount;
	}

	public Double getAmount() {
		return amount;
	}
}
