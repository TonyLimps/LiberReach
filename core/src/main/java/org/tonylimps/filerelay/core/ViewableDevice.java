package org.tonylimps.filerelay.core;

import com.alibaba.fastjson2.annotation.JSONField;
import org.tonylimps.filerelay.core.threads.ViewableCommandThread;

import java.net.InetSocketAddress;

public class ViewableDevice {

	private boolean isAuthorized;
	private String remarkName;
	private String deviceName;
	private InetSocketAddress address;
	private String host;
	private int port;

	private ViewableCommandThread viewableCommandThread;
	private boolean online;

	public ViewableDevice() {
	}

	public ViewableDevice(String host, int port, String deviceName) {
		address = new InetSocketAddress(host, port);
		this.host = host;
		this.port = port;
		this.remarkName = deviceName;
		this.deviceName = deviceName;
	}

	@JSONField(serialize = false)
	public boolean isAuthorized() {
		return isAuthorized;
	}

	public void setAuthorized(boolean isAuthorized) {
		this.isAuthorized = isAuthorized;
	}

	@JSONField(serialize = false)
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

	@JSONField(serialize = false)
	public ViewableCommandThread getCommandThread() {
		return viewableCommandThread;
	}

	public void setCommandThread(ViewableCommandThread viewableCommandThread) {
		this.viewableCommandThread = viewableCommandThread;
	}

	@JSONField(serialize = false)
	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
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
}
