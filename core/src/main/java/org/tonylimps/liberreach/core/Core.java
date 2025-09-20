package org.tonylimps.liberreach.core;

import com.alibaba.fastjson2.JSON;
import org.tonylimps.liberreach.core.threads.ViewableCommandThread;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.tonylimps.liberreach.core.enums.CommandType.DOWNLOAD;

/**<h1>Core</h1>
 * <p>此软件的核心功能</p>
 * @author Tony Limps
 * @since 2025
 */

public class Core {

	private static final String HASH_ALGORITHM = "SHA3-256";
	private static final String ENCRYPT_ALGORITHM = "AES";
	private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";

	/**
	 * <p>获取配置</p>
	 * @param key config.properties中的键
	 * @return 在config.properties中的值
	 */
	public static String getConfig(String key) {
		return ResourceBundle.getBundle("config").getString(key);
	}
	/** <p>加密字符串</p>
	 * <p>不安全，仅用作识别设备</p>
	 * @param data 需要加密的数据
	 * @param stringKey 密钥
	 * @return String 加密后的数据
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 */
	public static String encrypt(String data, String stringKey) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
		byte[] key = stringKey.getBytes();
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, ENCRYPT_ALGORITHM);
		Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
		cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
		return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
	}

	/** <p>解密字符串</p>
	 * <p>不安全，仅用作识别设备</p>
	 * @param data 需要加密的数据
	 * @param stringKey 密钥
	 * @return String 解密后的数据
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws InvalidKeyException
	 */
	public static String decrypt(String data, String stringKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] key = stringKey.getBytes();
		SecretKeySpec secretKeySpec = new SecretKeySpec(key, ENCRYPT_ALGORITHM);
		Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
		cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
		byte[] result = Base64.getDecoder().decode(data.replaceAll("\r\n", ""));
		return new String(cipher.doFinal(result));
	}

	/**
	 * 获取堆栈信息
	 * @param e 异常
	 * @return 堆栈信息
	 */
	public static String getExceptionStackTrace(Exception e) {
		ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
		PrintStream originalErrorStream = System.err;
		System.setErr(new PrintStream(errorStream));
		e.printStackTrace();
		System.setErr(originalErrorStream);
		return errorStream.toString();
	}

	/**
	 * <p>将参数构建为命令</p>
	 * <p>K, V, K, V -> {K:V, K:V}</p>
	 * @param args 命令的参数
	 * @return 构建完成的命令
	 */
	public static String createCommand(Object... args) {
		HashMap<String, Object> command = new HashMap<>();
		for (int i = 0; i < args.length; i += 2) {
			String key = args[i].toString();
			Object value = args[i + 1];
			command.put(key, value);
		}
		return JSON.toJSONString(command);
	}

	/**
	 * <p>创建256位随机令牌</p>
	 * @return 令牌
	 * @throws NoSuchAlgorithmException
	 */
	public static String createToken() throws NoSuchAlgorithmException {
		String random = hashEncrypt(String.valueOf(new SecureRandom().nextLong()));
		String time = hashEncrypt(String.valueOf(System.currentTimeMillis()));
		return random + time;
	}

	/**
	 * <p>散列函数</p>
	 * @param data 源数据
	 * @return 哈希值 String
	 * @throws NoSuchAlgorithmException
	 */
	public static String hashEncrypt(String data) throws NoSuchAlgorithmException {
		MessageDigest sha3Digest = MessageDigest.getInstance(HASH_ALGORITHM);
		byte[] hashBytes = sha3Digest.digest(data.getBytes());
		StringBuilder hexString = new StringBuilder();
		for (byte b : hashBytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1) {
				hexString.append('0');
			}
			hexString.append(hex);
		}
		return hexString.toString();
	}

	/**
	 * <p>自动重命名</p>
	 * @param name 需要重命名的名称
	 * @param names 已存在的名称
	 * @return 重命名后的名称
	 * <p>如果name已经在names中存在，就会重命名为name (1)</p>
	 * <p>仍然存在则重命名为name (2) 以此类推</p>
	 */
	public static String rename(String name, List<String> names) {
		if (!names.contains(name)) {
			return name;
		}
		int renameTimes = 1;
		while (names.contains(name + " (" + renameTimes + ")")) {
			renameTimes++;
		}
		return name + " (" + renameTimes + ")";
	}

	/**
	 * 获取本机地址
	 * @return 地址
	 * @throws UnknownHostException
	 */
	public static String getHostAddress() throws UnknownHostException {
		InetAddress address = InetAddress.getLocalHost();
		return address.getHostAddress();
	}

	/**
	 * <p>获取IPv6公网地址</p>
	 * <p>没有公网v6返回null</p>
	 * @return 地址
	 * @throws SocketException
	 */
	public static String getHostAddress6(){
		try{
			List<String> publicIPv6Addresses = new ArrayList<>();
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				if (iface.isLoopback() || !iface.isUp()) {
					continue;
				}

				Enumeration<InetAddress> addresses = iface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();

					if (addr instanceof Inet6Address) {
						Inet6Address ipv6Addr = (Inet6Address) addr;
						byte[] bytes = ipv6Addr.getAddress();
						boolean isUniqueLocal = (bytes[0] & 0xFF) == 0xFC || (bytes[0] & 0xFF) == 0xFD;
						boolean isIPv4Mapped = bytes[10] == (byte) 0xFF && bytes[11] == (byte) 0xFF;
						for (int i = 0; i < 10; i++) {
							if (bytes[i] != 0) isIPv4Mapped = false;
						}
						// 排除各种类型的非公网地址
						if (!ipv6Addr.isAnyLocalAddress() &&
							!ipv6Addr.isLinkLocalAddress() &&
							!ipv6Addr.isLoopbackAddress() &&
							!ipv6Addr.isMCGlobal() &&
							!ipv6Addr.isMCLinkLocal() &&
							!ipv6Addr.isMCNodeLocal() &&
							!ipv6Addr.isMCOrgLocal() &&
							!ipv6Addr.isMCSiteLocal() &&
							!isUniqueLocal &&
							!isIPv4Mapped) {

							String ip = ipv6Addr.getHostAddress();
							int percentIndex = ip.indexOf('%');
							if (percentIndex != -1) {
								ip = ip.substring(0, percentIndex);
							}
							publicIPv6Addresses.add(ip);
						}
					}
				}
			}

			return publicIPv6Addresses.isEmpty() ? null : publicIPv6Addresses.get(0);
		}
		catch (SocketException e){
			return null;
		}
	}

	/**
	 * 格式化传输速度
	 * @param bytesPerSecond 每秒字节数
	 * @return 格式化后的速度(B/s, KB/s, MB/s, GB/s)
	 */
	public static String formatSpeed(long bytesPerSecond) {
		if (bytesPerSecond < 1024) {
			return bytesPerSecond + " B/s";
		}
		else if (bytesPerSecond < 1024 * 1024) {
			return String.format("%.1f KB/s", bytesPerSecond / 1024.0);
		}
		else if (bytesPerSecond < 1024 * 1024 * 1024) {
			return String.format("%.1f MB/s", bytesPerSecond / (1024.0 * 1024));
		}
		else {
			return String.format("%.1f GB/s", bytesPerSecond / (1024.0 * 1024 * 1024));
		}
	}

	/**
	 * 下载文件
	 * @param device 从此设备下载
	 * @param path 需要下载的文件
	 * @param targetPath 保存至此路径
	 */
	public static void downloadFile(ViewableDevice device, CustomPath path, Path targetPath) {
		ViewableCommandThread commandThread = device.getCommandThread();
		if (commandThread == null) {
			return;
		}
		String fileName = path.getFileName();
		// 尝试将文件重命名(当有重名的文件会避开)，如果出错就不改名了
		try{
			List<String> existsFiles = Files.list(targetPath)
				.map(p -> p.getFileName().toString())
				.toList();
			fileName = Core.rename(fileName, existsFiles);
		}
		catch (IOException e){

		}
		Path targetFilePath = targetPath.resolve(fileName);
		File file = targetFilePath.toFile();
		FileReceiver fr = new FileReceiver(device.getAddress(), file, file.hashCode());
		fr.createFile();
		fr.createServerSocket();
		int port = fr.getPort();
		commandThread.send(Core.createCommand(
			"type", DOWNLOAD,
			"path", path.toString(),
			"port", port,
			"hashCode", file.hashCode()
		));
		fr.start();
	}
}
