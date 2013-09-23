package org.mvbrock.bcgames.payment.rs;

import java.util.Date;
import java.util.UUID;


public class Ledger {
	private String id;
	private Date date = new Date();
	
	private String gameId;
	private String playerId;
		
	private String wagerTier;
	private String payoutAddress;
	private String wagerAddress;

	private LedgerIncomingState incomingState = LedgerIncomingState.IncomingWaiting;
	private Transaction incomingTx = null;
	private LedgerOutgoingState outgoingState = LedgerOutgoingState.OutgoingWaiting;
	private Double outgoingAmount = 0.0;
	private Transaction outgoingTx = null;
	
	public Ledger() { }

	public Ledger(String gameId, String playerId, String wagerTier, String payoutAddress,
			String wagerAddress) {
		this.id = UUID.randomUUID().toString() + (new Date()).getTime();
		this.gameId = gameId;
		this.playerId = playerId;
		this.wagerTier = wagerTier;
		this.payoutAddress = payoutAddress;
		this.wagerAddress = wagerAddress;
	}
	
	public String getId() {
		return id;
	}

	public Date getDate() {
		return date;
	}

	public LedgerIncomingState getIncomingState() {
		return incomingState;
	}

	public void setIncomingState(LedgerIncomingState incomingState) {
		this.incomingState = incomingState;
	}

	public Transaction getIncomingTx() {
		return incomingTx;
	}

	public void setIncomingTx(Transaction incomingTx) {
		this.incomingTx = incomingTx;
	}

	public LedgerOutgoingState getOutgoingState() {
		return outgoingState;
	}

	public void setOutgoingState(LedgerOutgoingState outgoingState) {
		this.outgoingState = outgoingState;
	}

	public Double getOutgoingAmount() {
		return outgoingAmount;
	}

	public void setOutgoingAmount(Double outgoingAmount) {
		this.outgoingAmount = outgoingAmount;
	}

	public Transaction getOutgoingTx() {
		return outgoingTx;
	}

	public void setOutgoingTx(Transaction outgoingTx) {
		this.outgoingTx = outgoingTx;
	}

	public String getGameId() {
		return gameId;
	}

	public String getPlayerId() {
		return playerId;
	}

	public String getWagerTier() {
		return wagerTier;
	}

	public String getPayoutAddress() {
		return payoutAddress;
	}

	public String getWagerAddress() {
		return wagerAddress;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Ledger other = (Ledger) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
