package org.mvbrock.bcgames.payment.ws;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;

import com.slopeware.bcgames.common.config.ConfigStore;

@ApplicationScoped
public class PaymentMspConfigStore extends ConfigStore<PaymentMspConfig> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String BitcoinUrl = "bitcoin.url";
	public static final String BitcoinUsername = "bitcoin.username";
	public static final String BitcoinPassword = "bitcoin.password";
	public static final String BitcoinPollInterval = "bitcoin.pollInterval";
	
	public PaymentMspConfigStore() { }

	@Override
	protected Class<PaymentMspConfig> getConfigType() {
		return PaymentMspConfig.class;
	}
}
