package org.mvbrock.bcgames.payment.rs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.mvbrock.bcgames.payment.rs.interfaces.PaymentCallback;

@Singleton
public class CallbackServiceTracker implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private transient Logger log;
	
	// Maps a game ID to callback service
	private Map<String, PaymentCallback> serviceMap = new HashMap<String, PaymentCallback>();
	
	public void addClient(String gameId, String callbackUrl) {
		log.info("Creating proxy client for Payment Callback WS URL: " + callbackUrl);
		ResteasyClient restEasyClient = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = restEasyClient.target(callbackUrl);
		PaymentCallback callback = target.proxy(PaymentCallback.class);
		serviceMap.put(gameId, callback);
	}
	
	public PaymentCallback get(String gameId) {
		return serviceMap.get(gameId);
	}
}
