package org.mvbrock.bcgames.payment.ws.interfaces;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/payment.interfaces")
public interface PaymentMspCallbackService {
	@POST
	@Path("/game/{gameId}/{playerId}/address")
	@Consumes("application/json")
	public void updateGameAddress(@PathParam("gameId") String gameId, @PathParam("playerId") String playerId,
			String gameAddress);

	@POST
	@Path("/game/{gameId}/{playerId}/paid")
	@Consumes("application/json")
	public void playerPaid(@PathParam("gameId") String gameId, @PathParam("playerId") String playerId, Double amount);
}
