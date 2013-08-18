package org.mvbrock.bcgames.payment.ws.interfaces;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.mvbrock.bcgames.payment.model.Game;


@Path("/game")
public interface GameService {
	@GET
	@Path("/gamestatus")
	@Produces("application/json")
	public String getGameStatus();

	@POST
	@Path("/initgame")
	@Consumes("application/json")
	public void initGame(Game game);
}
