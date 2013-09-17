package org.mvbrock.bcgames.payment.ws;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.mvbrock.bcgames.payment.ws.interfaces.PaymentWsCallback;

@ApplicationScoped
public class CallbackServiceTracker implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Inject
	private transient Logger log;
	
	// Maps a game ID to callback service
	private Map<String, PaymentWsCallback> serviceMap = new HashMap<String, PaymentWsCallback>();
	
	public void addClient(String gameId, String callbackUrl) {
		log.info("Creating proxy client for Payment Callback WS URL: " + callbackUrl);
		ResteasyClient restEasyClient = new ResteasyClientBuilder().build();
        ResteasyWebTarget target = restEasyClient.target(callbackUrl);
		PaymentWsCallback service = target.proxy(PaymentWsCallback.class);
		serviceMap.put(gameId, service);
	}
	
	public PaymentWsCallback get(String gameId) {
		return serviceMap.get(gameId);
	}
}
