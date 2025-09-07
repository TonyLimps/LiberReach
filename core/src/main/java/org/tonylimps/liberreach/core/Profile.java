package org.tonylimps.liberreach.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONException;
import org.tonylimps.liberreach.core.threads.AuthorizedCommandThread;
import org.tonylimps.liberreach.core.threads.ViewableCommandThread;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户配置文件（Profile）类，用于存储和管理用户相关配置信息
 * <p>
 * 包括设备名称、语言设置、端口号及设备授权信息等
 *
 * <p><b>属性说明：</b></p>
 * <ul>
 * <li>{@code String deviceName} - 用户的设备名称</li>
 * <li>{@code Locale locale} - 用户界面语言设置</li>
 * <li>{@code Integer port} - 用户设置的端口号，默认值取自Core资源文件夹下config.properties中的defaultPort</li>
 * <li>{@code HashMap<String, AuthorizedDevice> authorizedDevices} -
 * 已授权设备列表，包含用户可调整授权状态的设备信息</li>
 * <li>{@code HashMap<String, ViewableDevice> viewableDevices} -
 * 可查看设备列表，包含用户可能查看的设备信息，与authorizedDevices相对应</li>
 * </ul>
 * <p><b>构造方法：</b></p>
 * <ul>
 * <li>{@code Profile(String deviceName, Locale locale, Integer port)} -
 * 创建新的用户配置文档</li>
 * </ul>
 * <p><b>方法说明：</b></p>
 * <ul>
 * <li>{@code static Profile fromJSON(String json)} -
 * 将JSON字符串解析为配置对象
 * </li>
 * <li>{@code static Profile getEmptyProfile(String deviceName)} -
 * 创建一个默认配置对象
 * </li>
 * <li>{@code String toJSONString()} -
 * 将配置序列化为JSON
 * </li>
 * <li>{@code void addAuthorizedDevice(AuthorizedDevice device)} -
 * 添加设备到授权列表，如设备名称重复将通过{@code Core.rename}方法自动重命名</li>
 * <li>{@code void addViewableDevice(ViewableDevice device)} -
 * 添加设备到可查看列表，如设备名称重复将通过{@code Core.rename}方法自动重命名</li>
 * <li>{@code void removeAuthorizedDevice(String name)} -
 * 根据设备名称从授权列表中移除设备，若设备不存在则抛出{@code NullPointerException}</li>
 * <li>{@code void removeViewableDevice(String name)} -
 * 根据设备名称从可查看列表中移除设备，若设备不存在则抛出{@code NullPointerException}</li>
 * </ul>
 */

public class Profile {

	private String deviceName;
	private Locale locale;
	private Integer port;

	private HashMap<String, AuthorizedDevice> authorizedDevices;
	private HashMap<String, ViewableDevice> viewableDevices;

	public Profile(String deviceName, Locale locale, Integer port) {
		authorizedDevices = new HashMap<>();
		viewableDevices = new HashMap<>();
		this.deviceName = deviceName;
		this.locale = locale;
		this.port = port;
	}

	public static Profile fromJSON(String json) {
		Profile profile = JSON.parseObject(json, Profile.class);
		profile.getAuthorizedDevices().values().forEach(device -> {
			try {
				device.setAddress(InetAddress.getByName(device.getHost()));
			}
			catch (Exception e) {
				throw new JSONException(e.getMessage());
			}
		});
		profile.getViewableDevices().values().forEach(device -> {
			try {
				device.setAddress(new InetSocketAddress(device.getHost(), device.getPort()));
			}
			catch (Exception e) {
				throw new JSONException(e.getMessage());
			}
		});
		return profile;
	}

	public static Profile getEmptyProfile(String deviceName) {
		return new Profile(
			deviceName,
			Locale.getDefault(),
			Integer.parseInt(Core.getConfig("defaultPort"))
		);
	}

	public String toJSONString() {
		return JSON.toJSONString(this);
	}

	public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public HashMap<String, AuthorizedDevice> getAuthorizedDevices() {
		return authorizedDevices;
	}

	public void setAuthorizedDevices(HashMap<String, AuthorizedDevice> authorizedDevices) {
		this.authorizedDevices = authorizedDevices;
	}

	public HashMap<String, ViewableDevice> getViewableDevices() {
		return viewableDevices;
	}

	public void setViewableDevices(HashMap<String, ViewableDevice> viewableDevices) {
		this.viewableDevices = viewableDevices;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public void addAuthorizedDevice(AuthorizedDevice device) {
		String name = device.getDeviceName();
		Set<String> names = authorizedDevices.values().stream()
			.map(AuthorizedDevice::getRemarkName)
			.collect(Collectors.toSet());
		device.setRemarkName(Core.rename(name, names));
		authorizedDevices.put(device.getRemarkName(), device);
	}

	public void addViewableDevice(ViewableDevice device) {
		String name = device.getDeviceName();
		Set<String> names = viewableDevices.values().stream()
			.map(ViewableDevice::getRemarkName)
			.collect(Collectors.toSet());
		device.setRemarkName(Core.rename(name, names));
		viewableDevices.put(device.getRemarkName(), device);
	}

	public void removeAuthorizedDevice(String name) {
		AuthorizedDevice device = authorizedDevices.get(name);
		device.setAddress(null);
		AuthorizedCommandThread commandThread = device.getCommandThread();
		if(Objects.nonNull(commandThread)){
			commandThread.close();
			device.setCommandThread(null);
		}
		authorizedDevices.remove(name);
	}

	public void removeViewableDevice(String name) {
		ViewableDevice device = viewableDevices.get(name);
		ViewableCommandThread commandThread = device.getCommandThread();
		if(Objects.nonNull(commandThread)){
			commandThread.close();
			device.setCommandThread(null);
		}
		viewableDevices.remove(name);
	}

}
