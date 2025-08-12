package com.tonylimps.filerelay.windows;

import com.tonylimps.filerelay.core.ExceptionManager;
import com.tonylimps.filerelay.core.Profile;
import com.tonylimps.filerelay.core.ProfileManager;
import com.tonylimps.filerelay.core.ResourceBundleManager;

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

public class WindowsResourceBundleManager extends ResourceBundleManager {

    private ResourceBundle bundle;
    private ExceptionManager exceptionManager;

    public WindowsResourceBundleManager(ExceptionManager exceptionManager, ProfileManager profileManager) {
        this.exceptionManager = exceptionManager;
    }

    public HashMap<Locale, ResourceBundle> getSupportedResourceBundles(){
        HashMap<Locale, ResourceBundle> result = new HashMap<>();
        try(Stream<Path> paths = Files.walk(Paths.get("properties"))){
            paths.map(path->path.toString())
                    .filter(path-> path.endsWith(".properties"))
                    .forEach(path->{
                        try {
                            ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(path));
                            Locale locale = Locale.forLanguageTag(bundle.getString("locale"));
                            result.put(locale, bundle);
                        } catch (IOException e) {
                            exceptionManager.throwException(e);
                        }
                    });
        }
        catch(IOException e){
            exceptionManager.throwException(e);
        }
        return result;
    }

    public void initBundle(Profile profile){
    }

    public ResourceBundle getBundle(Locale locale){
        if(bundle == null){
            Locale locale = profile.getLocale();
            HashMap<Locale, ResourceBundle> bundles = getSupportedResourceBundles();

            if(bundles.containsKey(locale)){
                // 如果支持配置文件中指定的语言包，就使用配置文件中的语言包
                bundle = bundles.get(locale);
            }
            else if(bundles.containsKey(Locale.getDefault())){
                // 否则使用用户设备的语言包
                bundle = bundles.get(Locale.getDefault());
            }
            else{
                // 不支持当前使用的语言包就用英语
                bundle = bundles.get(Locale.ENGLISH);
            }
        }
        return bundle;
    }
}
