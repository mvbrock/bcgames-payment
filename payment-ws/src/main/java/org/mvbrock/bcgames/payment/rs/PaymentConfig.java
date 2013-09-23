package org.mvbrock.bcgames.payment.rs;

import java.util.Map;

import org.mvbrock.bcgames.payment.model.GameType;
import org.mvbrock.bcgames.payment.model.WagerTier;

import org.mvbrock.bcgames.common.config.Config;

public class PaymentConfig extends Config {
	
	protected Map<String, WagerTier> wagerTiers;
	protected Map<String, GameType> gameTypes;
	protected Integer requiredConfirmations;
	protected Double rake;
	protected String rakeAddress;
	
	public PaymentConfig() { }
	
	public PaymentConfig(
			Map<String, WagerTier> wagerTiers,
			Map<String, GameType> gameTypes,
			String token,
			Integer requiredConfirmations,
			Double rake,
			String rakeAddress) {
		super(token);
		this.wagerTiers = wagerTiers;
		this.gameTypes = gameTypes;
		this.requiredConfirmations = requiredConfirmations;
		this.rake = rake;
		this.rakeAddress = rakeAddress;
	}

	public Map<String, WagerTier> getWagerTiers() {
		return wagerTiers;
	}

	public Map<String, GameType> getGameTypes() {
		return gameTypes;
	}
	
	public Integer getRequiredConfirmations() {
		return requiredConfirmations;
	}
	
	public Double getRake() {
		return rake;
	}
	
	public String getRakeAddress() {
		return rakeAddress;
	}
}
