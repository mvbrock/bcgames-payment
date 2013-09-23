package org.mvbrock.bcgames.payment.rs;

import java.io.Serializable;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

@SessionScoped
public class BitcoinClientFactoryJR4J implements BitcoinClientFactory, Serializable {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private transient Logger log;
	
	private BitcoinRpcClient bcRpcClient = null;

	@Inject
	private PaymentConfigStore config;
	
	public BitcoinClientFactoryJR4J() {
		super();
	}

	@Produces
	public BitcoinRpcClient create() {
		if(bcRpcClient != null) {
			return bcRpcClient;
		}
		
		// Initialize the Bitcoin client
		Authenticator.setDefault(new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(
						config.getProperty(PaymentConfigStore.BitcoinUsername),
						config.getProperty(PaymentConfigStore.BitcoinPassword).toCharArray());
			}
		});
		String bcUrlStr = config.getProperty(PaymentConfigStore.BitcoinUrl);
		log.info("Bitcoin client URL: " + bcUrlStr);
		URL bcUrl = null;
		JsonRpcHttpClient jsonRpcClient = null;
		try {
			bcUrl = new URL(bcUrlStr);
			jsonRpcClient = new JsonRpcHttpClient(bcUrl);
		} catch (MalformedURLException e) {
			log.error("Bad Bitcoin client URL: " + bcUrlStr, e);
		}
		BitcoinRpcClient bcRpcClient = ProxyUtil.createClientProxy(getClass().getClassLoader(),
				BitcoinRpcClient.class, jsonRpcClient);
		log.info("Created Bitcoin client.");
		return bcRpcClient;
	}
}
