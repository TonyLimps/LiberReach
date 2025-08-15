package com.tonylimps.filerelay.windows.managers;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.managers.ExceptionManager;

public class WindowsExceptionManager extends ExceptionManager {

	private int exceptions;

	public void throwException(Exception e) {
		Core.getLogger().error(e);
		try {
			new ExceptionDialog(e, exceptions).show();
			exceptions++;
		}
		catch (NullPointerException npe) {
			Core.getLogger().fatal(npe);
		}
	}
}
