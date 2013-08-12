package org.mvbrock.bcgames.payment.callback.model;

import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public enum GameStatus {
	Created,
	Started,
	Ended,
	PayingOut,
	Removed
}
