package org.tonylimps.liberreach.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * 文件传输协议: <br>
 *
 * 1. 验证序列号(文件哈希值)是否相同<br>
 * 2. 发送文件大小 (long)<br>
 * 3. 发送总共切块数 (long)<br>
 * 4. 循环传输:<br>
 *    - 发送第几块文件 (int)<br>
 *    - 发送这一块的大小 (int)<br>
 *    - 发送内容 (byte[])<br>
 *    - 发送这一块的哈希值 (32byte String)<br>
 *    - 接收端校验哈希值并返回结果 (boolean)<br>
 *      - 完整: 继续下一次循环<br>
 *      - 不完整: 重传当前块<br>
 * 5. 循环结束<br>
 * 6. 关闭连接<br>
 */
public class FileSender {

	private final Logger logger = LogManager.getLogger(getClass());

	private final File file;
	private final int port;
	private final InetAddress address;
	private DataOutputStream dos;
	private DataInputStream dis;
	private FileInputStream fi;
	private Socket socket;
	private long totalPieces;
	private long totalSize;
	private int pieceSize;

	private double progress;
	private long bytesPerSecond;

	public FileSender(File file, InetAddress address, int port) {
		this.address = address;
		this.file = file;
		this.port = port;
		totalSize = file.length();
		pieceSize = Integer.parseInt(Core.getConfig("filePieceSize"));
		totalPieces = totalSize % pieceSize == 0
			? totalSize / pieceSize
			: totalSize / pieceSize + 1;
	}

	public boolean createFile(){
		try{
			fi = new FileInputStream(file);
			return true;
		}
		catch(FileNotFoundException e){
			logger.error("File not found.",e);
			return false;
		}
	}

	public boolean connect(){
		try {
			socket = new Socket(address,port);
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			dos.writeInt(file.hashCode());
			dos.flush();
			return dis.readBoolean();
		}
		catch (IOException e) {
			logger.error("Connect file receiver failed.",e);
			return false;
		}
	}

	public void start(){
		new Thread(this::sendFile).start();
	}

	public void sendFile(){
		try {
			// 文件大小
			dos.writeLong(file.length());
			// 总共切块数
			dos.writeLong(totalPieces);
			dos.flush();
			byte[] buffer = new byte[pieceSize];
			int i = 1;
			while(i <= totalPieces){
				long startTime = System.currentTimeMillis();
				int size = fi.read(buffer, 0, pieceSize);
				String hash = Core.hashEncrypt(new String(buffer, 0, size));
				dos.writeInt(i);
				dos.writeInt(size);
				dos.write(buffer, 0, size);
				dos.writeUTF(hash);
				dos.flush();
				boolean check = dis.readBoolean();
				if(check){
					long usedTimeSeconds = (System.currentTimeMillis() - startTime)/1000;
					progress = (double)i / (double)totalPieces;
					bytesPerSecond = usedTimeSeconds == 0
						? 0
						: size / usedTimeSeconds;
					logger.info("Uploading {} Progress: {}/{} Speed: {}", file.getName(), i, totalPieces , bytesPerSecond);
					i += 1;
				}
			}
			socket.close();
			fi.close();
			logger.info("Upload {} success.", file.getName());
		}
		catch (Exception e) {
			logger.error("Error sending file.", e);
		}
	}
}
