package org.mvbrock.bcgames.payment.ws;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;

import org.mvbrock.bcgames.common.config.ConfigStore;


@ApplicationScoped
public class PaymentWsConfigStore extends ConfigStore<PaymentWsConfig> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String BitcoinUrl = "bitcoin.url";
	public static final String BitcoinUsername = "bitcoin.username";
	public static final String BitcoinPassword = "bitcoin.password";
	public static final String BitcoinPollInterval = "bitcoin.pollInterval";
	
	public PaymentWsConfigStore() { }

	@Override
	protected Class<PaymentWsConfig> getConfigType() {
		return PaymentWsConfig.class;
	}
}
