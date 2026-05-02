package org.tonylimps.liberreach.core.managers;

import com.google.common.reflect.ClassPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Config;
import org.tonylimps.liberreach.core.Util;

import java.io.IOException;
import java.util.*;

public class ResourceBundleManager {

	private final Logger logger = LogManager.getLogger(getClass());
	private ResourceBundle bundle;
	private ExceptionManager exceptionManager;
	private Config config;

	public ResourceBundleManager(ExceptionManager exceptionManager, Config config) {
		this.exceptionManager = exceptionManager;
		this.config = config;
	}

	public ResourceBundle getBundle() {
		if (bundle == null) {
			Locale locale = config.getLocale();
			HashMap<Locale, ResourceBundle> bundles = getSupportedResourceBundles();

			if (bundles.containsKey(locale)) {
				// 如果支持配置文件中指定的语言包，就使用配置文件中的语言包
				logger.info("Loading language bundle.");
				bundle = bundles.get(locale);
			} else if (bundles.containsKey(Locale.getDefault())) {
				// 否则使用用户设备的语言包
				logger.warn("Load failed, using the system language.");
				bundle = bundles.get(Locale.getDefault());
			} else {
				// 不支持当前使用的语言包就用默认
				logger.warn("Load failed, using the default language.");
				bundle = bundles.get(Locale.forLanguageTag(Util.getAppConfig("defaultLocale")));
				if (bundle == null) {
					logger.fatal("Cannot load language bundle.");
					return null;
				}
			}
			logger.info("Loaded language bundle.");
		}
		return bundle;
	}

	public HashMap<Locale, ResourceBundle> getSupportedResourceBundles() {
		HashMap<Locale, ResourceBundle> result = new HashMap<>();
		try {
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			ClassPath.from(loader).getResources().stream()
				.filter(resource -> resource.getResourceName().startsWith(Util.getAppConfig("languageBundlesPath")))
				.filter(resource -> resource.getResourceName().endsWith(".properties"))
				.map(resource -> {
					try {
						return resource.asByteSource().openStream();
					} catch (IOException e) {
						return null;
					}
				}).filter(Objects::nonNull).forEach(stream -> {
					try {
						ResourceBundle bundle = new PropertyResourceBundle(stream);
						Locale locale = Locale.forLanguageTag(bundle.getString("locale"));
						result.put(locale, bundle);
					} catch (IOException e) {
						logger.error(e);
						exceptionManager.throwException(e);
					}
				});
		} catch (IOException e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
		return result;
	}
}
