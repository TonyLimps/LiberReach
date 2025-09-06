package org.tonylimps.filerelay.core.threads;

import org.tonylimps.filerelay.core.Profile;
import org.tonylimps.filerelay.core.managers.ExceptionManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class UpdateThread extends Thread{
	protected Profile profile;
	protected ExceptionManager exceptionManager;
	protected int updateDelayMillis;
	protected AtomicBoolean running;
	protected AtomicBoolean needToFlushToken;
	public void setNeedToFlushToken(boolean needToFlushToken) {
		this.needToFlushToken.set(needToFlushToken);
	}
	public abstract void setFilesList(List<String> filesList, List<String> foldersList, List<String> errs);
}
