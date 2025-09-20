package org.tonylimps.liberreach.windows.managers;

import com.alibaba.fastjson2.JSON;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Core;
import org.tonylimps.liberreach.core.Profile;
import org.tonylimps.liberreach.core.managers.ProfileManager;

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

public class WindowsProfileManager implements ProfileManager {

	private final Logger logger = LogManager.getLogger(getClass());
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
	public void initProfile() {
		File profileFile = new File("profile.dat");
		if (!profileFile.exists()) {
			// 如果配置文件不存在就新建配置文件
			createNewProfile();
			logger.info("Created new profile.");
		}
		try (FileInputStream fileInputStream = new FileInputStream(profileFile)) {
			// 尝试读取配置文件
			String encryptedProfileString = new String(fileInputStream.readAllBytes());
			logger.info("Read profile.");
			//使用本机的机器码解密
			String decryptedProfileString = Core.decrypt(encryptedProfileString, UUID);
			logger.info("Decrypt profile.");
			// 解析配置
			try {
				// profile = JSON.parseObject(decryptedProfileString, Profile.class);
				profile = Profile.fromJSON(decryptedProfileString);
			}
			catch (Exception e) {
				logger.fatal("Parse profile failed.", e);
				throw new RuntimeException(e);
			}

			logger.info("Parsed profile.");
		}
		catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException |
			   BadPaddingException e) {
			// 有解密异常说明更换了设备，需要重新创建配置
			createNewProfile();
		}
		catch (Exception e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}

	@Override
	public void createNewProfile() {
		try {
			Path path = Paths.get("profile.dat");
			// 创建一个新的配置并用本机机器码加密
			Profile emptyProfile = Profile.getEmptyProfile(getDeviceName());
			String encryptedString = Core.encrypt(emptyProfile.toJSONString(), UUID);
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
	public void saveProfile() {
		try {
			Path path = Paths.get("profile.dat");
			String profileString = JSON.toJSONString(profile);
			String encryptedString = Core.encrypt(profileString, UUID);
			byte[] encryptedBytes = encryptedString.getBytes();
			Files.write(path, encryptedBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
		}
		catch (Exception e) {
			logger.error(e);
			exceptionManager.throwException(e);
		}
	}

	@Override
	public Profile getProfile() {
		return profile;
	}
}
