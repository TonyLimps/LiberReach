package com.tonylimps.filerelay.core;

import com.alibaba.fastjson2.annotation.JSONField;
import com.tonylimps.filerelay.core.threads.AuthorizedCommandThread;

import java.net.InetSocketAddress;

public class AuthorizedDevice {

	private InetSocketAddress address;
	private String remarkName;
	private String deviceName;

	@JSONField(serialize = false)
	private AuthorizedCommandThread authorizedCommandThread;
	@JSONField(serialize = false)
	private boolean online;

	public AuthorizedDevice(InetSocketAddress address, String remarkName, String deviceName) {
		this.address = address;
		this.remarkName = remarkName;
		this.deviceName = deviceName;
	}

	public AuthorizedDevice(InetSocketAddress address, String deviceName) {
		this.address = address;
		this.remarkName = deviceName;
		this.deviceName = deviceName;
	}

	public String getHost() {
		return address.getAddress().getHostName();
	}

	public int getPort() {
		return address.getPort();
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

	public AuthorizedCommandThread getCommandThread() {
		return authorizedCommandThread;
	}

	public void setCommandThread(AuthorizedCommandThread authorizedCommandThread) {
		this.authorizedCommandThread = authorizedCommandThread;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}
}
