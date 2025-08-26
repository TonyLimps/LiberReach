package com.tonylimps.filerelay.windows.managers;

import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.managers.ExceptionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WindowsExceptionManager extends ExceptionManager {

	private final Logger logger = LogManager.getLogger(this.getClass());
	private int exceptions;

	public void throwException(Exception e) {
		try {
			new ExceptionDialog(e, exceptions).show();
			exceptions++;
		}
		catch (NullPointerException npe) {
			logger.fatal(npe);
			logger.fatal(e);
		}
	}
}
