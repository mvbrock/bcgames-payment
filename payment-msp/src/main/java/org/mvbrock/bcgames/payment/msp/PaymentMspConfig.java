package org.mvbrock.bcgames.payment.msp;

import java.io.IOException;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.mvbrock.bcgames.payment.callback.model.GameType;
import org.mvbrock.bcgames.payment.callback.model.WagerTier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.slopeware.bcgames.common.config.Config;

public class PaymentMspConfig extends Config {
	private static final Logger log = LoggerFactory.getLogger(PaymentMspConfig.class);
	private static final ObjectMapper mapper = new ObjectMapper();
	
	protected Map<String, WagerTier> wagerTiers;
	protected Map<String, GameType> gameTypes;
	protected Integer requiredConfirmations;
	protected Double rake;
	protected String rakeAddress;
	
	public PaymentMspConfig() { }
	
	public PaymentMspConfig(
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
	
	@Override
	public String toString() {
		try {
			return mapper.writeValueAsString(this);
		} catch (JsonGenerationException e) {
			log.error("Cannot generate JSON from object", e);
		} catch (JsonMappingException e) {
			log.error("Cannot map JSON from object", e);
		} catch (IOException e) {
			log.error("Cannot write JSON from object", e);
		}
		return null;
	}
}
