package org.tonylimps.liberreach.core;

import org.tonylimps.liberreach.core.managers.ConfigManager;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.managers.ResourceBundleManager;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 应用程序上下文，封装所有共享依赖
 */

public class AppContext {

	public final ExceptionManager exceptionManager;
	public final ConfigManager configManager;
	public final ResourceBundleManager bundleManager;
	public final AtomicBoolean running;
	public final Token token;

	public AppContext(ExceptionManager exceptionManager, ConfigManager configManager, ResourceBundleManager bundleManager, AtomicBoolean running, Token token) {
		this.exceptionManager = exceptionManager;
		this.configManager = configManager;
		this.bundleManager = bundleManager;
		this.running = running;
		this.token = token;
	}

	public static class Builder {
		private ExceptionManager exceptionManager;
		private ConfigManager configManager;
		private ResourceBundleManager bundleManager;
		private AtomicBoolean running;
		private Token token;

		public Builder setExceptionManager(ExceptionManager manager) {
			this.exceptionManager = manager;
			return this;
		}

		public Builder setConfigManager(ConfigManager manager) {
			this.configManager = manager;
			return this;
		}

		public Builder setBundleManager(ResourceBundleManager manager) {
			this.bundleManager = manager;
			return this;
		}

		public Builder setRunning(AtomicBoolean running) {
			this.running = running;
			return this;
		}

		public Builder setToken(Token token) {
			this.token = token;
			return this;
		}

		public AppContext build() {
			if (exceptionManager == null || configManager == null || bundleManager == null || running == null || token == null) {
				throw new IllegalStateException("All fields must be set");
			}
			return new AppContext(exceptionManager, configManager, bundleManager, running, token);
		}
	}
}
