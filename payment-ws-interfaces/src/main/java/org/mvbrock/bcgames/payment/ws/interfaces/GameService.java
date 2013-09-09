package org.mvbrock.bcgames.payment.ws.interfaces;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.mvbrock.bcgames.payment.model.Game;


@Path("/game")
public interface GameService {
	@POST
	@Path("/init")
	@Consumes("application/json")
	public void initialize(Game game);
}
