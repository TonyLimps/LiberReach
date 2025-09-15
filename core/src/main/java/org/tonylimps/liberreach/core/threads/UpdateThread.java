package org.tonylimps.liberreach.core.threads;

import org.tonylimps.liberreach.core.CustomPath;
import org.tonylimps.liberreach.core.Profile;
import org.tonylimps.liberreach.core.managers.ExceptionManager;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class UpdateThread extends Thread{
	protected Profile profile;
	protected ExceptionManager exceptionManager;
	protected int updateDelayMillis;
	protected AtomicBoolean running;
	public abstract void setPaths(List<CustomPath> paths);
}
