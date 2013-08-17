package org.mvbrock.bcgames.payment.interfaces;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.mvbrock.bcgames.payment.model.Game;
import org.mvbrock.bcgames.payment.model.GameType;
import org.mvbrock.bcgames.payment.model.Player;
import org.mvbrock.bcgames.payment.model.WagerTier;


@Path("/payment")
public interface PaymentMspService {
	@POST
	@Path("/creategame")
	@Consumes("application/json")
	public Game createGame(Game game, String callbackUrl);

	@POST
	@Path("/{gameId}/joined")
	@Consumes("application/json")
	public void playerJoined(@PathParam("gameId") String gameId, Player player);

	@POST
	@Path("/{gameId}/{playerId}/left")
	@Consumes("application/json")
	public void playerLeft(@PathParam("gameId") String gameId, @PathParam("playerId") String playerId);

	@POST
	@Path("/{gameId}/start")
	@Consumes("application/json")
	public void startGame(@PathParam("gameId") String gameId, String gameCallbackUrl);

	@POST
	@Path("/{gameId}/end")
	@Consumes("application/json")
	public void endGame(@PathParam("gameId") String gameId, String winnerId);

	@POST
	@Path("/{gameId}/paywinner")
	@Consumes("application/json")
	public void payWinner(@PathParam("gameId") String gameId);

	@GET
	@Path("/get/wagertiers")
	@Produces("application/json")
	public List<WagerTier> getWagerTiers();

	@GET
	@Path("/get/gametypes")
	@Produces("application/json")
	public List<GameType> getGameTypes();
}
