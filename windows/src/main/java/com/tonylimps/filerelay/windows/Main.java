package com.tonylimps.filerelay.windows;

import com.alibaba.fastjson2.JSON;
import com.tonylimps.filerelay.core.Core;
import com.tonylimps.filerelay.core.Device;
import com.tonylimps.filerelay.core.PendingRequirements;
import com.tonylimps.filerelay.core.Profile;
import com.tonylimps.filerelay.windows.controllers.ExceptionDialogController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;


public class Main extends Application implements PendingRequirements {

    protected static boolean DEBUG = true;
    protected static boolean running;

    protected static String UUID;
    protected static Profile userProfile;
    protected static HashMap<String, Device> viewableDevices;
    protected static HashMap<String, Device> authorizedDevices;

    private static int exceptions;

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
    public String getDeviceName(){
        return System.getenv("COMPUTERNAME");
    }
    @Override
    public String getUserName(){
        return System.getProperty("user.name");
    }
    @Override
    public void throwException(Exception e) {
        if(DEBUG){
            e.printStackTrace();
            System.exit(1);
        }
        new ExceptionDialog(e,exceptions).show();
    }
    @Override
    public void createNewProfile() {
        try{
            Path path = Paths.get("profile.dat");
            // 创建一个新的配置并用本机机器码加密
            Profile emptyProfile = new Profile(getDeviceName());
            String encryptedString = Core.encrypt(JSON.toJSONString(emptyProfile), UUID);
            byte[] encryptedBytes = encryptedString.getBytes();
            // 不存在则创建，存在则覆盖
            Files.write(path, encryptedBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch(Exception e){
            throwException(e);
        }
    }
    @Override
    public void initProfile(){
        File profile = new File("profile.dat");
        if (!profile.exists()) {
            // 如果配置文件不存在就新建配置文件
            createNewProfile();
        }
        try (FileInputStream fileInputStream = new FileInputStream(profile)) {
            // 尝试读取配置文件
            String encryptedProfileString = new String(fileInputStream.readAllBytes());
            //使用本机的机器码解密
            String decryptedProfileString = Core.decrypt(encryptedProfileString,UUID);
            // 解析配置
            userProfile = JSON.parseObject(decryptedProfileString,Profile.class);
        }
        catch(NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e){
            // 有解密异常说明更换了设备，需要重新创建配置
            createNewProfile();
        }
        catch(Exception e){
            throwException(e);
        }
    }

    public static void main(String[] args){
        if(args.length!=0){
            if(args[0].equals("--debug")){DEBUG = true;}
        }
        running = true;
        exceptions = 0;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage){
        try{
            UUID = getUUID();
            initProfile();
            ResourceBundle bundle;
            try{
                Locale language = userProfile.getLanguage();
                bundle = ResourceBundle.getBundle("com.tonylimps.filerelay.windows.filerelay", language);
            }
            catch(Exception e){
                bundle = ResourceBundle.getBundle("com.tonylimps.filerelay.windows.filerelay", Locale.ENGLISH);
            }
            WindowManager.initWindow("settings","views_settings.fxml", bundle);
            WindowManager.initWindow("main","views_main.fxml", bundle);
            WindowManager.initWindow("search","views_search.fxml", bundle);
            WindowManager.show("main");
            new ConnectThread(this).start();


            throwException(new NullPointerException("test"));


        }
        catch(Exception e){
            e.printStackTrace();
            new Scanner(System.in).nextLine();
        }
    }
}