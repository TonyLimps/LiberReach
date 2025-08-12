package com.tonylimps.filerelay.core;

import com.alibaba.fastjson2.JSON;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.random.RandomGenerator;

public class Core{

    private static final String HASH_ALGORITHM = "SHA3-256";
    private static final String ENCRYPT_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";

	public static String getConfig(String key) {
		return ResourceBundle.getBundle("config").getString(key);
	}
    // 根据key密钥加密data字符串
    public static String encrypt(String data, String stringKey) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] key = stringKey.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ENCRYPT_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }
    // 同上文 解密
    public static String decrypt(String data, String stringKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] key = stringKey.getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, ENCRYPT_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] result = Base64.getDecoder().decode(data.replaceAll("\r\n", ""));
        return new String(cipher.doFinal(result));
    }
    // 哈希加密
    public static String hashEncrypt(String data) throws Exception {
        MessageDigest sha3Digest = MessageDigest.getInstance(HASH_ALGORITHM);
        byte[] hashBytes = sha3Digest.digest(data.getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
    // 获取异常堆栈
    public static String getExceptionStackTrace(Exception e) {
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PrintStream originalErrorStream = System.err;
        System.setErr(new PrintStream(errorStream));
        e.printStackTrace();
        System.setErr(originalErrorStream);
        return errorStream.toString();
    }
    // 获取当前时间
    public static String getCurrentTime(){
        SimpleDateFormat formatter= new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
    // 获取socket的地址
    public static String getAddress(Socket socket){
        SocketAddress socketAddress = socket.getRemoteSocketAddress();
        InetSocketAddress inetSocketAddress = (InetSocketAddress) socketAddress;
        return inetSocketAddress.getAddress().getHostAddress();
    }
    // 将参数解析为哈希表命令
    public static String createCommand(String... args){
        HashMap<String,String> command = new HashMap<>();
        for(int k = 0,v = 0; v<args.length; v+=2,k+=2){
            command.put(args[k],args[v]);
        }
        return JSON.toJSONString(command);
    }
	// 将字符串命令解析
	public static HashMap<String, String> parseCommand(String command){
		return JSON.parseObject(command,HashMap.class);
	}
    // 生成token
    public static String createToken() throws Exception {
        String random = hashEncrypt(String.valueOf(RandomGenerator.of("L128X128MixRandom").nextLong()));
        return hashEncrypt(random);
    }
    // 命名重名设备
    public static String rename(String name, Set<String> names){
        int renameTimes = 1;
        while(names.contains(name+" ("+renameTimes+")")){
            renameTimes++;
        }
        return name+" ("+renameTimes+")";
    }
    // 获取IP地址
    public static String getHostAddress() throws UnknownHostException {
        InetAddress address = InetAddress.getLocalHost();
        return address.getHostAddress();
    }

}
