package org.tonylimps.liberreach.windows;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.AppContext;
import org.tonylimps.liberreach.core.Token;
import org.tonylimps.liberreach.core.managers.ResourceBundleManager;
import org.tonylimps.liberreach.core.threads.ConnectThread;
import org.tonylimps.liberreach.core.threads.HeartBeatThread;
import org.tonylimps.liberreach.core.threads.TokenThread;
import org.tonylimps.liberreach.windows.managers.WindowManager;
import org.tonylimps.liberreach.windows.managers.WindowsExceptionManager;
import org.tonylimps.liberreach.windows.managers.WindowsConfigManager;
import org.tonylimps.liberreach.windows.threads.WindowsUpdateThread;

import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <h1>LiberReach</h1>
 * <p>自由高效的文件传输软件</p>
 *
 * @author Tony Limps
 * @version Windows 1.0.0
 * @since 2025
 */

public class Main extends Application {
	private static AppContext context;
	private static final Logger logger = LogManager.getLogger(Main.class);
	// threads
	private static TokenThread tokenThread;
	private static ConnectThread connectThread;
	private static HeartBeatThread heartBeatThread;

	public static void main(String[] args) {
		logger.info("Program started.");
		AtomicBoolean running = new AtomicBoolean(false);
		running.set(true);
		WindowsExceptionManager exceptionManager = new WindowsExceptionManager();
		WindowsConfigManager configManager = new WindowsConfigManager(exceptionManager);
		ResourceBundleManager bundleManager = new ResourceBundleManager(exceptionManager, configManager.getConfig());

		AppContext.Builder builder = new AppContext.Builder();
		builder.setBundleManager(bundleManager);
		builder.setConfigManager(configManager);
		builder.setExceptionManager(exceptionManager);
		builder.setRunning(running);
		builder.setToken(new Token());
		context = builder.build();

		WindowsUpdateThread updateThread = new WindowsUpdateThread(context);
		updateThread.start();
		logger.info("UI update thread started.");
		tokenThread = new TokenThread(context);
		tokenThread.start();
		logger.info("Token thread started.");
		connectThread = new ConnectThread(context, updateThread);
		connectThread.start();
		logger.info("Connect thread started.");
		heartBeatThread = new HeartBeatThread(context, updateThread);
		heartBeatThread.start();
		logger.info("Heartbeat thread started.");

		launch(args);
	}

	public static void exit(int code) {
		context.running.set(false);
		tokenThread.close();
		connectThread.close();
		heartBeatThread.close();
		logger.info("Application exited with code {}.", code);
		System.exit(code);
	}

	public static AppContext getContext() {
		return context;
	}

	public static TokenThread getTokenThread() {
		return tokenThread;
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			ResourceBundle bundle = getContext().bundleManager.getBundle();
			WindowManager.initWindow("settings", "/fxmls/settings.fxml", bundle);
			WindowManager.initWindow("main", "/fxmls/main.fxml", bundle);
			WindowManager.initWindow("add", "/fxmls/add.fxml", bundle);
			WindowManager.show("main");
			logger.info("Application started.");
		}
		catch (Exception e) {
			logger.fatal("Load UI content failed.");
			logger.error(e);
			getContext().exceptionManager.throwException(e);
		}
	}

}

