package org.mvbrock.bcgames.payment.rs.interfaces;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.mvbrock.bcgames.payment.model.GameType;
import org.mvbrock.bcgames.payment.model.WagerTier;

@Path("/payment-callback")
public interface PaymentCallback {
	@POST
	@Path("/update/wagertiers")
	@Consumes("application/json")
	public void updateWagerTiers(List<WagerTier> tiers);

	@POST
	@Path("/update/gametypes")
	@Consumes("application/json")
	public void updateGameTypes(List<GameType> tiers);

	@POST
	@Path("/game/{gameId}/{playerId}/incorrectPayment")
	@Consumes("application/json")
	public void incorrectPayment(@PathParam("gameId") String gameId, @PathParam("playerId") String playerId,
			Double amount);

	@POST
	@Path("/game/{gameId}")
	@Consumes("application/json")
	public void gameCanStart(@PathParam("gameId") String gameId);
	
	@POST
	@Path("/game/{gameId}/{playerId}/address")
	@Consumes("application/json")
	public void updateWagerAddress(@PathParam("gameId") String gameId, @PathParam("playerId") String playerId,
			String gameAddress);

	@POST
	@Path("/game/{gameId}/{playerId}/paid")
	@Consumes("application/json")
	public void playerPaid(@PathParam("gameId") String gameId, @PathParam("playerId") String playerId, Double amount);
}
