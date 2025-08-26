package com.tonylimps.filerelay.core;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public class Profile {
    private String deviceName;
    private Locale locale;
    private Integer port;
    private HashMap<InetSocketAddress, AuthorizedDevice> authorizedDevices;
    private HashMap<InetSocketAddress, ViewableDevice> viewableDevices;

    public Profile(String deviceName,Locale locale, Integer port){
        authorizedDevices = new HashMap<>();
        viewableDevices = new HashMap<>();
        this.deviceName = deviceName;
        this.locale = locale;
        this.port = port;
    }

    public String getDeviceName() {
        return deviceName;
    }
    public Locale getLocale() {
        return locale;
    }
    public HashMap<InetSocketAddress, AuthorizedDevice> getAuthorizedDevices() {
        return authorizedDevices;
    }
    public HashMap<InetSocketAddress, ViewableDevice> getViewableDevices() {
        return viewableDevices;
    }
    public Integer getPort() {
        return port;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
    public void setAuthorizedDevices(HashMap<InetSocketAddress, AuthorizedDevice> authorized) {
        this.authorizedDevices = authorized;
    }
    public void setViewableDevices(HashMap<InetSocketAddress, ViewableDevice> viewable) {
        this.viewableDevices = viewable;
    }
    public void setPort(Integer port) {
        this.port = port;
    }

	public void addAuthorizedDevice(AuthorizedDevice device) {
		String name = device.getDeviceName();
		Set<String> names = authorizedDevices.values().stream()
			.map(AuthorizedDevice::getRemarkName)
			.collect(Collectors.toSet());
		device.setRemarkName(Core.rename(name,names));
		String host = device.getHost();
		int port = device.getPort();
		authorizedDevices.put(new InetSocketAddress(host,port), device);
	}

	public void addViewableDevice(ViewableDevice device) {
		String name = device.getDeviceName();
		Set<String> names = viewableDevices.values().stream()
											 .map(ViewableDevice::getRemarkName)
											 .collect(Collectors.toSet());
		device.setRemarkName(Core.rename(name,names));
		viewableDevices.put(device.getAddress(), device);
	}
}
