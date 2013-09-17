package org.mvbrock.bcgames.payment.ws;

import java.util.Date;
import java.util.UUID;


public class GameLedger {
	private String id;
	private Date date;
	
	private String gameId;
	private String playerId;
	private GameLedgerState state;
	
	private String wagerTier;
	private String payoutAddress;
	private String wagerAddress;

	private Date incomingReceivedDate;
	private Double incomingAmount;
	private Date outgoingSentDate;
	private Double outgoingAmount;
	
	public GameLedger() { }
	
	public GameLedger(String gameId, String playerId, GameLedgerState state, String wagerTier,
			String payoutAddress, String wagerAddress) {
		this.date = new Date();
		this.id = UUID.randomUUID().toString() + date.getTime();
		this.gameId = gameId;
		this.playerId = playerId;
		this.state = state;
		this.wagerTier = wagerTier;
		this.payoutAddress = payoutAddress;
		this.wagerAddress = wagerAddress;
		incomingAmount = 0.0;
		outgoingAmount = 0.0;
	}
	
	public String getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public String getGameId() {
		return gameId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public GameLedgerState getState() {
		return state;
	}

	public void setState(GameLedgerState type) {
		this.state = type;
	}

	public String getWagerTier() {
		return this.wagerTier;
	}

	public String getPayoutAddress() {
		return payoutAddress;
	}

	public String getWagerAddress() {
		return wagerAddress;
	}

	public Date getIncomingReceivedDate() {
		return incomingReceivedDate;
	}

	public void setIncomingReceivedDate(Date incomingReceivedDate) {
		this.incomingReceivedDate = incomingReceivedDate;
	}

	public Double getIncomingAmount() {
		return incomingAmount;
	}

	public void setIncomingAmount(Double incomingAmount) {
		this.incomingAmount = incomingAmount;
	}

	public Date getOutgoingSentDate() {
		return outgoingSentDate;
	}

	public void setOutgoingSentDate(Date outgoingSentDate) {
		this.outgoingSentDate = outgoingSentDate;
	}

	public Double getOutgoingAmount() {
		return outgoingAmount;
	}

	public void setOutgoingAmount(Double outgoingAmount) {
		this.outgoingAmount = outgoingAmount;
	}
}
