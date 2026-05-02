package org.tonylimps.liberreach.windows.managers;

import com.alibaba.fastjson2.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Config;
import org.tonylimps.liberreach.core.Util;
import org.tonylimps.liberreach.core.managers.ConfigManager;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class WindowsConfigManager implements ConfigManager {

	private final Logger logger = LogManager.getLogger(getClass());
	private final WindowsExceptionManager exceptionManager;
	private final String UUID;
	private Config config;

	public WindowsConfigManager(WindowsExceptionManager exceptionManager) {
		try {
			this.UUID = getUUID();
			this.exceptionManager = exceptionManager;
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		initConfig();
	}

	@Override
	public String getUUID() throws IOException, InterruptedException {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("cmd", "/c", "wmic csproduct get uuid");
		processBuilder.redirectErrorStream(true);
		Process process = processBuilder.start();
		Scanner scanner = new Scanner(process.getInputStream());
		scanner.next();
		String uuid = scanner.next().trim().replaceAll("-", "");
		process.waitFor();
		process.destroy();
		return uuid;
	}

	@Override
	public void initConfig() {
		File configFile = new File("config.dat");
		if (!configFile.exists()) {
			// 如果配置文件不存在就新建配置文件
			createNewConfig();
			logger.info("Created new config.");
		}
		try (FileInputStream fileInputStream = new FileInputStream(configFile)) {
			// 尝试读取配置文件
			String encryptedConfigString = new String(fileInputStream.readAllBytes());
			logger.info("Read config.");
			//使用本机的机器码解密
			String decryptedConfigString = Util.decrypt(encryptedConfigString, UUID);
			logger.info("Decrypt config.");
			// 解析配置
			try {
				// config = JSON.parseObject(decryptedConfigString, Config.class);
				config = Config.fromJSON(decryptedConfigString);
			}
			catch (Exception e) {
				logger.fatal("Parse config failed.", e);
				throw new RuntimeException(e);
			}

			logger.info("Parsed config.");
		}
		catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
			   BadPaddingException e) {
			// 有解密异常说明更换了设备，需要重新创建配置
			createNewConfig();
		}
		catch (Exception e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}

	@Override
	public void createNewConfig() {
		try {
			Path path = Paths.get("config.dat");
			// 创建一个新的配置并用本机机器码加密
			Config emptyConfig = Config.createEmptyConfig(getDeviceName());
			String encryptedString = Util.encrypt(emptyConfig.toJSONString(), UUID);
			byte[] encryptedBytes = encryptedString.getBytes();
			// 不存在则创建，存在则覆盖
			Files.write(path, encryptedBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch (Exception e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}

	@Override
	public String getDeviceName() {
		return System.getenv("COMPUTERNAME");
	}

	@Override
	public void saveConfig() {
		try {
			Path path = Paths.get("config.dat");
			String configString = JSON.toJSONString(config);
			String encryptedString = Util.encrypt(configString, UUID);
			byte[] encryptedBytes = encryptedString.getBytes();
			Files.write(path, encryptedBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch (Exception e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}

	@Override
	public Config getConfig() {
		return config;
	}
}
