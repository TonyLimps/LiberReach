package com.tonylimps.filerelay.core.threads;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class UpdateThread extends Thread{
	protected AtomicBoolean needToFlushToken;

	public void setNeedToFlushToken(boolean needToFlushToken) {
		this.needToFlushToken.set(needToFlushToken);
	}
}
