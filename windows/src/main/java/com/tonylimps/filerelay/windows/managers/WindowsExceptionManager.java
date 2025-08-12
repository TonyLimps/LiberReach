package com.tonylimps.filerelay.windows.managers;

import com.tonylimps.filerelay.core.managers.ExceptionManager;
import com.tonylimps.filerelay.windows.Main;

import java.util.Scanner;

public class WindowsExceptionManager extends ExceptionManager {

	private int exceptions;

	public void throwException(Exception e) {
		if (Main.isDebug()) {
			e.printStackTrace();
			new Scanner(System.in).nextLine();
			System.exit(1);
		}
		try {
			new ExceptionDialog(e, exceptions).show();
			exceptions++;
		}
		catch (NullPointerException npe) {
			e.printStackTrace();
			new Scanner(System.in).nextLine();
			System.exit(1);
		}
	}
}
