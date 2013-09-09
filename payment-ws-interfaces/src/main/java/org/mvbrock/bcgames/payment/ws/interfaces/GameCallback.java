package org.mvbrock.bcgames.payment.ws.interfaces;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("/game-callback")
public interface GameCallback {
	@POST
	@Path("/init-complete/{gameId}")
	@Consumes("application/json")
	public void initializeComplete(@PathParam("gameId") String gameId, boolean success);
}
