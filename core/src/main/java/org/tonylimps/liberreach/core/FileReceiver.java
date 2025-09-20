package org.tonylimps.liberreach.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 文件传输协议: <br>
 * <p>
 * 1. 验证序列号(文件哈希值)是否相同<br>
 * 2. 发送文件大小 (long)<br>
 * 3. 发送总共切块数 (long)<br>
 * 4. 循环传输:<br>
 * - 发送第几块文件 (int)<br>
 * - 发送这一块的大小 (int)<br>
 * - 发送内容 (byte[])<br>
 * - 发送这一块的哈希值 (32byte String)<br>
 * - 接收端校验哈希值并返回结果 (boolean)<br>
 * - 完整: 继续下一次循环<br>
 * - 不完整: 重传当前块<br>
 * 5. 循环结束<br>
 * 6. 关闭连接<br>
 */
public class FileReceiver {

	private final Logger logger = LogManager.getLogger(getClass());
	private final InetAddress address;
	private final File file;
	private final int hashCode;
	private final int pieceSize;
	private int port;
	private DataOutputStream dos;
	private DataInputStream dis;
	private FileOutputStream fo;
	private Socket socket;
	private ServerSocket serverSocket;
	private long totalPieces;
	private long totalSize;
	private double progress;
	private long bytesPerSecond;

	public FileReceiver(InetAddress address, File file, int hashCode) {
		this.file = file;
		this.address = address;
		this.hashCode = hashCode;
		pieceSize = Integer.parseInt(Core.getConfig("filePieceSize"));
	}

	public boolean createServerSocket() {
		try {
			serverSocket = new ServerSocket(0);
			port = serverSocket.getLocalPort();
			return true;
		}
		catch (IOException e) {
			logger.error("Connect file sender failed.", e);
			return false;
		}
	}

	public boolean createFile() {
		try {
			File parentDir = file.getParentFile();
			if (parentDir != null && !parentDir.exists()) {
				parentDir.mkdirs();
			}

			fo = new FileOutputStream(file, false); // false表示覆盖模式
			fo = new FileOutputStream(file);
			return true;
		}
		catch (IOException e) {
			logger.error("Create file failed.", e);
			return false;
		}
	}

	public void start() {
		new Thread(this::receiveFile).start();
	}

	private void receiveFile() {
		try {
			socket = serverSocket.accept();
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			int hashCode = dis.readInt();
			if (hashCode == this.hashCode) {
				dos.writeBoolean(true);
			}
			else {
				dos.writeBoolean(false);
			}
			totalSize = dis.readLong();
			totalPieces = dis.readLong();
			byte[] buffer = new byte[pieceSize];
			int i = 1;
			long downloadedSize = 0;
			while (i <= totalPieces) {
				long startTime = System.currentTimeMillis();
				int piece = dis.readInt();
				int size = dis.readInt();
				dis.readFully(buffer, 0, size);
				String hash = dis.readUTF();
				String localHash = Core.hashEncrypt(new String(buffer, 0, size));
				if (hash.equals(localHash)) {
					long usedTimeMillis = System.currentTimeMillis() - startTime;
					bytesPerSecond = usedTimeMillis == 0
						? 0
						: (long) (size * 1000.0 / usedTimeMillis);
					downloadedSize += size;
					progress = (double) downloadedSize / totalSize;
					dos.writeBoolean(true);
					fo.write(buffer, 0, size);
					logger.info("Downloading {} Progress: {}/{} Speed: {}", file.getName(), piece, totalPieces, Core.formatSpeed(bytesPerSecond));
					i += 1;
				}
				else {
					dos.writeBoolean(false);
				}
			}
			socket.close();
			fo.close();
			logger.info("Download {} success.", file.getName());
		}
		catch (Exception e) {
			logger.error("Error receiving file.", e);
		}
	}

	public int getPort() {
		return port;
	}
}
