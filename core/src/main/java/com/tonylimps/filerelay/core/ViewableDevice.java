package com.tonylimps.filerelay.core;

import com.alibaba.fastjson2.annotation.JSONField;
import com.tonylimps.filerelay.core.threads.ViewableCommandThread;

public class ViewableDevice {

	private String host;
	private int port;
	private String remarkName;
	private String deviceName;

	@JSONField(serialize = false)
	private ViewableCommandThread viewableCommandThread;
	@JSONField(serialize = false)
	private boolean online;

	public ViewableDevice(String host, int port, String remarkName, String deviceName) {
		this.host = host;
		this.port = port;
		this.remarkName = remarkName;
		this.deviceName = deviceName;
	}

	public ViewableDevice(String host, int port, String deviceName) {
		this.host = host;
		this.port = port;
		this.remarkName = deviceName;
		this.deviceName = deviceName;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getRemarkName() {
		return remarkName;
	}

	public void setRemarkName(String remarkName) {
		this.remarkName = remarkName;
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public ViewableCommandThread getCommandThread() {
		return viewableCommandThread;
	}

	public void setCommandThread(ViewableCommandThread viewableCommandThread) {
		this.viewableCommandThread = viewableCommandThread;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
}
