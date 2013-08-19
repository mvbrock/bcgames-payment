package org.mvbrock.bcgames.payment.ws;

import java.io.Serializable;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;
import com.googlecode.jsonrpc4j.ProxyUtil;

@SessionScoped
public class BitcoinClientFactoryJR4J implements BitcoinClientFactory, Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(BitcoinClientFactoryJR4J.class);
	
	private BitcoinRpcClient bcRpcClient = null;

	@Inject
	private PaymentWsConfigStore config;
	
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
						config.getProperty(PaymentWsConfigStore.BitcoinUsername),
						config.getProperty(PaymentWsConfigStore.BitcoinPassword).toCharArray());
			}
		});
		String bcUrlStr = config.getProperty(PaymentWsConfigStore.BitcoinUrl);
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
