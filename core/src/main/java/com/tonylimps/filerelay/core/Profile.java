package com.tonylimps.filerelay.windows;

import com.tonylimps.filerelay.core.Device;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Profile {
    protected String deviceName;
    protected Locale language;
    protected List<Device> authorized;
    protected List<Device> viewable;

    public Profile() {
        language = Locale.getDefault();
        authorized = new ArrayList<>();
        viewable = new ArrayList<>();
    }
    public Profile(Main main){
        language = Locale.getDefault();
        authorized = new ArrayList<>();
        viewable = new ArrayList<>();
        deviceName = main.getDeviceName();
    }
    public String getDeviceName() {
        return deviceName;
    }
    public Locale getLanguage() {
        return language;
    }
    public List<Device> getAuthorized() {
        return authorized;
    }
    public List<Device> getViewable() {
        return viewable;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
