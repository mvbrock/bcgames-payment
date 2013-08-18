package org.mvbrock.bcgames.payment.ws;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.resteasy.client.ProxyFactory;
import org.mvbrock.bcgames.payment.ws.interfaces.PaymentMspCallbackService;


@ApplicationScoped
public class CallbackServiceTracker implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// Maps a game ID to callback service
	private Map<String, PaymentMspCallbackService> serviceMap = new HashMap<String, PaymentMspCallbackService>();
	
	public void addClient(String gameId, String callbackUrl) {
		PaymentMspCallbackService service = ProxyFactory.create(PaymentMspCallbackService.class, callbackUrl);
		serviceMap.put(gameId, service);
	}
	
	public PaymentMspCallbackService getService(String gameId) {
		return serviceMap.get(gameId);
	}
}
