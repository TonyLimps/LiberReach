package com.tonylimps.filerelay.core;

import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;

public abstract class ResourceBundleManager {

    public abstract HashMap<Locale, ResourceBundle> getSupportedResourceBundles();

}
