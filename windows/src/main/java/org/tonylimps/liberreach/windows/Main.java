package org.tonylimps.liberreach.windows;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.threads.ConnectThread;
import org.tonylimps.liberreach.core.threads.HeartBeatThread;
import org.tonylimps.liberreach.core.threads.TokenThread;
import org.tonylimps.liberreach.windows.annotations.SingleFocusHandler;
import org.tonylimps.liberreach.windows.controllers.MainController;
import org.tonylimps.liberreach.windows.managers.WindowManager;
import org.tonylimps.liberreach.windows.managers.WindowsExceptionManager;
import org.tonylimps.liberreach.windows.managers.WindowsProfileManager;
import org.tonylimps.liberreach.windows.managers.WindowsResourceBundleManager;
import org.tonylimps.liberreach.windows.threads.WindowsUpdateThread;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <h1>LiberReach</h1>
 * <p>更自由的文件传输软件</p>
 *
 * @author Tony Limps
 * @version Windows 1.0.0
 * @since 2025
 */

public class Main extends Application {

	private static final Logger logger = LogManager.getLogger(Main.class);
	private static AtomicBoolean running;
	// managers
	private static WindowsProfileManager profileManager;
	private static WindowsExceptionManager exceptionManager;
	private static WindowsResourceBundleManager bundleManager;

	// threads
	private static TokenThread tokenThread;
	private static ConnectThread connectThread;
	private static HeartBeatThread heartBeatThread;

	public static void main(String[] args) {
		logger.info("Program started.");
		running = new AtomicBoolean(true);
		exceptionManager = new WindowsExceptionManager();
		profileManager = new WindowsProfileManager(exceptionManager);
		bundleManager = new WindowsResourceBundleManager(exceptionManager, profileManager.getProfile());

		WindowsUpdateThread updateThread = new WindowsUpdateThread(exceptionManager, running, profileManager.getProfile());
		updateThread.start();
		logger.info("UI update thread started.");
		tokenThread = new TokenThread(exceptionManager, running, updateThread);
		tokenThread.start();
		logger.info("Token thread started.");
		connectThread = new ConnectThread(exceptionManager, bundleManager.getBundle(), running, profileManager, tokenThread.getToken(), updateThread);
		connectThread.start();
		logger.info("Connect thread started.");
		heartBeatThread = new HeartBeatThread(exceptionManager, running, profileManager, tokenThread.getToken(), updateThread);
		heartBeatThread.start();
		logger.info("Heartbeat thread started.");

		launch(args);
	}

	public static void exit(int code) {
		running.set(false);
		tokenThread.close();
		connectThread.close();
		heartBeatThread.close();
		logger.info("Application exited with code {}.", code);
		System.exit(code);
	}

	public static WindowsProfileManager getProfileManager() {
		return profileManager;
	}

	public static WindowsExceptionManager getExceptionManager() {
		return exceptionManager;
	}

	public static WindowsResourceBundleManager getResourceBundleManager() {
		return bundleManager;
	}

	public static TokenThread getTokenThread() {
		return tokenThread;
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			ResourceBundle bundle = bundleManager.getBundle();
			WindowManager.initWindow("settings", "/fxmls/settings.fxml", bundle);
			WindowManager.initWindow("main", "/fxmls/main.fxml", bundle);
			WindowManager.initWindow("add", "/fxmls/add.fxml", bundle);
			WindowManager.show("main");
			logger.info("Application started.");
			SingleFocusHandler singleFocusHandler = new SingleFocusHandler();
			singleFocusHandler.handle(MainController.getInstance());
		}
		catch (Exception e) {
			logger.fatal("Load UI content failed.");
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}

}

