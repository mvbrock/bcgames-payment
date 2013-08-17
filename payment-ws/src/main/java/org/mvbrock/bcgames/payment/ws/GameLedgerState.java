package org.mvbrock.bcgames.payment.ws;

public enum GameLedgerState {
	OutgoingWinnerWaiting,
	OutgoingWinnerSent,
	OutgoingRefundWaiting,
	OutgoingRefundSent,
	IncomingConfirmed,
	IncomingReceived,
	IncomingWaiting
}
