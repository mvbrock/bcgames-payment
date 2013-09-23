package org.mvbrock.bcgames.payment.rs;

public enum LedgerOutgoingState {
	OutgoingWaiting,
	OutgoingWinnerWaiting,
	OutgoingWinnerSent,
	OutgoingRefundWaiting,
	OutgoingRefundSent
}
