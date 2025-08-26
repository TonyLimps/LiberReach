package com.tonylimps.filerelay.core;

import com.tonylimps.filerelay.core.threads.AuthorizedCommandThread;

import java.net.InetSocketAddress;

public class AuthorizedDevice {

	private boolean isAuthorized;
	private InetSocketAddress address;
	private AuthorizedCommandThread commandThread;
	private String remarkName;
	private String deviceName;

	public AuthorizedDevice(InetSocketAddress address, String deviceName) {
		this.address = address;
		this.remarkName = deviceName;
		this.deviceName = deviceName;
	}

	public boolean isAuthorized() {
		return isAuthorized;
	}

	public AuthorizedCommandThread getCommandThread() {
		return commandThread;
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

	public void setCommandThread(AuthorizedCommandThread commandThread) {
		this.commandThread = commandThread;
	}

	public void setAuthorized(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
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
}
