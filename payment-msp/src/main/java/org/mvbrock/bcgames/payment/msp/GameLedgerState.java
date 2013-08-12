package org.mvbrock.bcgames.payment.msp;

public enum GameLedgerState {
	OutgoingWinnerWaiting,
	OutgoingWinnerSent,
	OutgoingRefundWaiting,
	OutgoingRefundSent,
	IncomingConfirmed,
	IncomingReceived,
	IncomingWaiting
}
