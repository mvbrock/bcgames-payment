package org.mvbrock.bcgames.payment.rs;

import java.io.Serializable;

import javax.enterprise.context.ApplicationScoped;

import org.mvbrock.bcgames.common.config.ConfigStore;


@ApplicationScoped
public class PaymentConfigStore extends ConfigStore<PaymentConfig> implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public static final String BitcoinUrl = "bitcoin.url";
	public static final String BitcoinUsername = "bitcoin.username";
	public static final String BitcoinPassword = "bitcoin.password";
	public static final String BitcoinPollInterval = "bitcoin.pollInterval";
	
	public PaymentConfigStore() { }

	@Override
	protected Class<PaymentConfig> getConfigType() {
		return PaymentConfig.class;
	}
}
