package org.tonylimps.liberreach.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class CustomPath {
	@JSONField(serialize = false, deserialize = false)
	public static final String WIN32SPLIT = "\\";
	@JSONField(serialize = false, deserialize = false)
	public static final String SPLIT = "/";
	@JSONField(serialize = false, deserialize = false)
	public static final String deviceSplit = "::";
	@JSONField(serialize = false, deserialize = false)
	public static final EmptyPath root = new EmptyPath();

	private String deviceName;
	private boolean directory;
	private Path path;
	private String stringPath;

	private CustomPath(){}

	public CustomPath(String fullPath) {
		String[] paths = fullPath.split(deviceSplit);
		if(paths.length == 0){
			// ::
			deviceName = "";
			path = root;
		}
		else if(paths.length == 1 && fullPath.endsWith(deviceSplit)){
			// name::
			deviceName = paths[0];
			path = root;
		}
		else if(paths.length == 2){
			// name::path
			deviceName = paths[0];
			path = Paths.get(paths[1]);
		}
		else{
			deviceName = "";
			path = new EmptyPath();
		}
		stringPath = path.toString();
	}

	public CustomPath(CustomPath customPath) {
		this.path = customPath.getPath();
		this.stringPath = customPath.stringPath;
		this.directory = customPath.directory;
		this.deviceName = customPath.getDeviceName();
	}

	public CustomPath(String deviceName, Path path) {
		this.deviceName = deviceName;
		this.path = path;
		stringPath = path.toString();
		if(path.toString().endsWith(":"+WIN32SPLIT) || path.toString().equals(SPLIT)){
			directory = true;
		}
		else{
			directory = Files.isDirectory(path);
		}
	}

	public static CustomPath fromJSONString(String jsonString) {
		HashMap<String, Object> map = JSON.parseObject(jsonString, HashMap.class);
		String deviceName = (String)map.get("deviceName");
		boolean isDirectory = (boolean)map.get("directory");
		String stringPath = (String)map.get("stringPath");
		Path path = stringPath == null
			? new EmptyPath()
			: Paths.get(stringPath);
		CustomPath result = new CustomPath();
		result.deviceName = deviceName;
		result.directory = isDirectory;
		result.path = path;
		result.stringPath = stringPath;
		return result;
	}

	public CustomPath enter(String name){
		CustomPath customPath = new CustomPath(this);
		if(customPath.path instanceof EmptyPath){
			customPath.path = Paths.get(name);
		}
		else{
			customPath.path = customPath.path.resolve(name);
		}
		return customPath;
	}

	public CustomPath back(){
		CustomPath customPath = new CustomPath(this);
		if(customPath.path instanceof EmptyPath){
			customPath.path = new EmptyPath();
		}
		else{
			customPath.path = customPath.path.getParent();
		}
		return customPath;
	}

	public String getDeviceName(){
		return deviceName;
	}

	public String getFileName() {
		return path.getFileName() == null
			? path.toString()
			: path.getFileName().toString();
	}

	public File toFile() {
		return path.toFile();
	}

	@JSONField(serialize = false)
	public Path getPath(){
		return path;
	}

	public void setFullPath(CustomPath path) {
		this.path = path.getPath();
		this.deviceName = path.getDeviceName();
		this.stringPath = path.stringPath;
	}

	@JSONField(serialize = false)
	public boolean isOnlyDeviceName() {
		return path instanceof EmptyPath;
	}

	@Override
	public String toString() {
		return deviceName + deviceSplit + path.normalize();
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public String getStringPath(){
		return stringPath;
	}
}
