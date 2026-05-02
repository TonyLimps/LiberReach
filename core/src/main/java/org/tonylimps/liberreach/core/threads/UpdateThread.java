package org.tonylimps.liberreach.core.threads;

import org.tonylimps.liberreach.core.AppContext;
import org.tonylimps.liberreach.core.CustomPath;

import java.util.List;

public abstract class UpdateThread extends Thread {
	protected AppContext context;
	protected int updateDelayMillis;
	public abstract void setPaths(List<CustomPath> paths);
}
