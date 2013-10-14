package org.mvbrock.bcgames.payment.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public enum GameStatus {
	Created,
	Initialized,
	Started,
	Ended,
	PaidOut,
	Removed
}
