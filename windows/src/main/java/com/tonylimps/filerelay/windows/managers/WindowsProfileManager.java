package com.tonylimps.filerelay.windows.managers;

import com.alibaba.fastjson2.JSON;
import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.Profile;

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
import java.util.Locale;
import java.util.Scanner;

public class WindowsProfileManager {

	private final WindowsExceptionManager exceptionManager;
	private final String UUID;
	private Profile profile;

	public WindowsProfileManager(WindowsExceptionManager exceptionManager) {
		try {
			this.UUID = getUUID();
			this.exceptionManager = exceptionManager;
		}
		catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}
		initProfile();
	}

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

	private String getDeviceName() {
		return System.getenv("COMPUTERNAME");
	}

	private void initProfile() {
		File profile = new File("profile.dat");
		if (!profile.exists()) {
			// 如果配置文件不存在就新建配置文件
			createNewProfile();
			Core.getLogger().info("Created new profile.");
		}
		try (FileInputStream fileInputStream = new FileInputStream(profile)) {
			// 尝试读取配置文件
			String encryptedProfileString = new String(fileInputStream.readAllBytes());
			Core.getLogger().info("Read profile.");
			//使用本机的机器码解密
			String decryptedProfileString = Core.decrypt(encryptedProfileString, UUID);
			Core.getLogger().info("Decrypt profile.");
			// 解析配置
			this.profile = JSON.parseObject(decryptedProfileString, Profile.class);
			Core.getLogger().info("Parsed profile.");
		}
		catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
			   BadPaddingException e) {
			// 有解密异常说明更换了设备，需要重新创建配置
			createNewProfile();
		}
		catch (Exception e) {
			exceptionManager.throwException(e);
		}
	}

	public void createNewProfile() {
		try {
			Path path = Paths.get("profile.dat");
			// 创建一个新的配置并用本机机器码加密
			Profile emptyProfile = new Profile(
				getDeviceName(),
				Locale.getDefault(),
				Integer.parseInt(Core.getConfig("defaultPort"))
			);
			String encryptedString = Core.encrypt(JSON.toJSONString(emptyProfile), UUID);
			byte[] encryptedBytes = encryptedString.getBytes();
			// 不存在则创建，存在则覆盖
			Files.write(path, encryptedBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch (Exception e) {
			exceptionManager.throwException(e);
		}
	}

	public void saveProfile() {
		try {
			Path path = Paths.get("profile.dat");
			String encryptedString = Core.encrypt(JSON.toJSONString(profile), UUID);
			byte[] encryptedBytes = encryptedString.getBytes();
			Files.write(path, encryptedBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch (Exception e) {
			exceptionManager.throwException(e);
		}
	}

	public Profile getProfile() {
		return profile;
	}
}
