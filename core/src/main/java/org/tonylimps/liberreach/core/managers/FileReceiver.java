package org.tonylimps.liberreach.core.managers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tonylimps.liberreach.core.Core;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
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
public class FileReceiver {

	private final Logger logger = LogManager.getLogger(getClass());

	private int port;
	private final InetAddress address;
	private final File file;
	private final int hashCode;
	private DataOutputStream dos;
	private DataInputStream dis;
	private FileOutputStream fo;
	private Socket socket;
	private ServerSocket serverSocket;
	private long totalPieces;
	private long totalSize;
	private final int pieceSize;

	private double progress;
	private long bytesPerSecond;

	public FileReceiver(InetAddress address, File file) {
		this.file = file;
		this.address = address;
		this.hashCode = file.hashCode();
		pieceSize = Integer.parseInt(Core.getConfig("filePieceSize"));
	}

	public boolean createServerSocket() {
		try{
			serverSocket = new ServerSocket(0);
			port = serverSocket.getLocalPort();
			socket = serverSocket.accept();
			dos = new DataOutputStream(socket.getOutputStream());
			dis = new DataInputStream(socket.getInputStream());
			int hashCode = dis.readInt();
			if(hashCode == this.hashCode){
				dos.writeBoolean(true);
				return true;
			}
			else{
				dos.writeBoolean(false);
				return false;
			}
		}
		catch(IOException e){
			logger.error("Connect file sender failed.",e);
			return false;
		}
	}

	public boolean createFile(){
		try{
			fo = new FileOutputStream(file);
			return true;
		}
		catch(FileNotFoundException e){
			logger.error("File not found.",e);
			return false;
		}
	}

	public void receiveFile() {
		try{
			totalSize = dis.readLong();
			totalPieces = dis.readLong();
			byte[] buffer = new byte[pieceSize];
			int i = 1;
			while(i <= totalPieces){
				long startTime = System.currentTimeMillis();
				int piece = dis.readInt();
				int size = dis.readInt();
				dis.readFully(buffer, 0, size);
				String hash = dis.readUTF();
				String localHash = Core.hashEncrypt(new String(buffer, 0, size));
				if(hash.equals(localHash)){
					dos.writeBoolean(true);
					fo.write(buffer, 0, size);
					progress = i / (double)totalPieces;
					i += 1;
					long usedTimeSeconds = (System.currentTimeMillis() - startTime)/1000;
					bytesPerSecond = size / usedTimeSeconds;
					logger.info("Downloading {} Progress: {}/{} Speed: {}", file.getName(), piece, totalPieces , bytesPerSecond);
				}
				else{
					dos.writeBoolean(false);
				}
			}
			socket.close();
			fo.close();
		}
		catch(Exception e){
			logger.error("Error receiving file.", e);
		}
	}

	public int getPort() {
		return port;
	}
}
