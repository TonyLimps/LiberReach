package org.tonylimps.liberreach.windows.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Core;
import org.tonylimps.liberreach.core.Profile;
import org.tonylimps.liberreach.core.managers.ExceptionManager;
import org.tonylimps.liberreach.core.managers.ResourceBundleManager;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.stream.Stream;

public class WindowsResourceBundleManager implements ResourceBundleManager {

	private final Logger logger = LogManager.getLogger(getClass());
	private ResourceBundle bundle;
	private ExceptionManager exceptionManager;
	private Profile profile;

	public WindowsResourceBundleManager(ExceptionManager exceptionManager, Profile profile) {
		this.exceptionManager = exceptionManager;
		this.profile = profile;
	}

	public ResourceBundle getBundle() {
		if (bundle == null) {
			Locale locale = profile.getLocale();
			HashMap<Locale, ResourceBundle> bundles = getSupportedResourceBundles();

			if (bundles.containsKey(locale)) {
				// 如果支持配置文件中指定的语言包，就使用配置文件中的语言包
				logger.info("Loading resource bundle.");
				bundle = bundles.get(locale);
			}
			else if (bundles.containsKey(Locale.getDefault())) {
				// 否则使用用户设备的语言包
				logger.warn("Load failed, using the system language.");
				bundle = bundles.get(Locale.getDefault());
			}
			else {
				// 不支持当前使用的语言包就用默认
				logger.warn("Load failed, using the default language.");
				bundle = bundles.get(Locale.forLanguageTag(Core.getConfig("defaultLocale")));
			}
			logger.info("Loaded resource bundle.");
		}
		return bundle;
	}

	public HashMap<Locale, ResourceBundle> getSupportedResourceBundles() {
		HashMap<Locale, ResourceBundle> result = new HashMap<>();
		try (Stream<Path> paths = Files.walk(Paths.get(Core.getConfig("languageBundlesPath")))) {
			paths.map(path -> path.toString())
				.filter(path -> path.endsWith(".properties"))
				.forEach(path -> {
					try {
						ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(path));
						Locale locale = Locale.forLanguageTag(bundle.getString("locale"));
						result.put(locale, bundle);
					}
					catch (IOException e) {
						logger.error(e);
						exceptionManager.throwException(e);
					}
				});
		}
		catch (IOException e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
		return result;
	}
}
