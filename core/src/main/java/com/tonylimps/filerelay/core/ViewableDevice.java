package com.tonylimps.filerelay.core;

import com.alibaba.fastjson2.annotation.JSONField;
import com.tonylimps.filerelay.core.threads.ViewableCommandThread;

import java.net.InetSocketAddress;

public class ViewableDevice {

	private InetSocketAddress address;
	private boolean isAuthorized;
	private String remarkName;
	private String deviceName;

	@JSONField(serialize = false)
	private ViewableCommandThread viewableCommandThread;
	@JSONField(serialize = false)
	private boolean online;

	public ViewableDevice(String host, int port, String deviceName) {
		address = new InetSocketAddress(host, port);
		this.remarkName = deviceName;
		this.deviceName = deviceName;
	}

	public boolean isAuthorized() {
		return isAuthorized;
	}

	public void setAuthorized(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
	}

	public InetSocketAddress getAddress() {
		return address;
	}

	public void setAddress(InetSocketAddress address) {
		this.address = address;
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
