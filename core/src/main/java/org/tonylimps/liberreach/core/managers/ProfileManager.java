package org.tonylimps.liberreach.core.managers;

import org.tonylimps.liberreach.core.Profile;

import java.io.IOException;

/**
 * ProfileManager 类
 * 用于管理用户配置文件，提供配置文件的创建、初始化、保存和获取功能
 *
 * <p>此类为抽象类，具体方法需要在不同平台中实现</p>
 *
 * <p><b>属性说明：</b></p>
 * <ul>
 *   <li>{@code Profile profile} - 用户配置数据对象</li>
 * </ul>
 *
 * <p><b>方法说明：</b></p>
 * <ul>
 *   <li>{@code String getUUID()} - 获取此设备的128位唯一标识符</li>
 *   <li>{@code String getDeviceName()} - 获取此设备的名称</li>
 *   <li>{@code void createNewProfile()} - 创建新的Profile对象，序列化后加密并写入配置文件</li>
 *   <li>{@code void initProfile()} - 将配置文件解密并反序列化，填充profile属性</li>
 *   <li>{@code void saveProfile()} - 保存当前profile到配置文件</li>
 *   <li>{@code Profile getProfile()} - 返回profile属性</li>
 * </ul>
 */
public abstract class ProfileManager {
	protected Profile profile;
    public abstract String getUUID() throws IOException, InterruptedException;
	protected abstract String getDeviceName();
	protected  abstract void createNewProfile();
	protected abstract void initProfile();
	public abstract void saveProfile();
	public abstract Profile getProfile();
}
