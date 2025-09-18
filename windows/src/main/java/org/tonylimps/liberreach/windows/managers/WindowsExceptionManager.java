package org.tonylimps.liberreach.windows.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.windows.controllers.ExceptionDialog;

public class WindowsExceptionManager implements ExceptionManager {

	private final Logger logger = LogManager.getLogger(getClass());
	private int exceptions;

	@Override
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
