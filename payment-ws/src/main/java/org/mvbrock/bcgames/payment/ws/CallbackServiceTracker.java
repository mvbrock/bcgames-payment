package org.mvbrock.bcgames.payment.ws;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.resteasy.client.ProxyFactory;
import org.mvbrock.bcgames.payment.ws.interfaces.PaymentWsCallback;


@ApplicationScoped
public class CallbackServiceTracker implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// Maps a game ID to callback service
	private Map<String, PaymentWsCallback> serviceMap = new HashMap<String, PaymentWsCallback>();
	
	public void addClient(String gameId, String callbackUrl) {
		PaymentWsCallback service = ProxyFactory.create(PaymentWsCallback.class, callbackUrl);
		serviceMap.put(gameId, service);
	}
	
	public PaymentWsCallback getService(String gameId) {
		return serviceMap.get(gameId);
	}
}
