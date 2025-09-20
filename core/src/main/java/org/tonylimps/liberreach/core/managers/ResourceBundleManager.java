package org.tonylimps.liberreach.core.managers;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public interface ResourceBundleManager {

    public abstract HashMap<Locale, ResourceBundle> getSupportedResourceBundles();

}
