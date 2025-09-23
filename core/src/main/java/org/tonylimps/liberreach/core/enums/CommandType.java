package org.tonylimps.liberreach.core.enums;

/**
 * <h1>CommandType</h1>
 * <p>命令类型枚举</p>
 * @author Tony Limps
 * @since 2025
 */
public enum CommandType {
	/**
	 * 回应对应的命令(由Authorized设备发出)
	 */
	ANSWER,
	/**
	 * <p>心跳命令</p>
	 * <p>是否在线(online: boolean)</p>
	 * <p>是否已授权(authorized: boolean)</p>
	 */
	HEARTBEAT,
	/**
	 * 添加设备
	 * <p>设备名称(name: string)</p>
	 * <p>地址(host: string)</p>
	 * <p>端口号(port: string/int)</p>
	 */
	ADD,
	GETPATH,
	DOWNLOAD,
	UPLOAD;

}
