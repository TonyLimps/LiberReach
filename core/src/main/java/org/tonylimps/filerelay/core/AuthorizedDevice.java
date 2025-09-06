package org.tonylimps.filerelay.core;

import com.alibaba.fastjson2.annotation.JSONField;
import org.tonylimps.filerelay.core.threads.AuthorizedCommandThread;

import java.net.InetAddress;

public class AuthorizedDevice {

	private boolean isAuthorized;
	private String remarkName;
	private String deviceName;
	private InetAddress address;
	private String host;
	private AuthorizedCommandThread commandThread;

	public AuthorizedDevice() {
	}

	public AuthorizedDevice(InetAddress address, String deviceName) {
		this.address = address;
		host = address.getHostAddress();
		this.remarkName = deviceName;
		this.deviceName = deviceName;
	}

	public boolean isAuthorized() {
		return isAuthorized;
	}

	public void setAuthorized(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
	}

	@JSONField(serialize = false)
	public AuthorizedCommandThread getCommandThread() {
		return commandThread;
	}

	public void setCommandThread(AuthorizedCommandThread commandThread) {
		this.commandThread = commandThread;
	}

	@JSONField(serialize = false)
	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
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

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}
}
