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
	private Path path;
	private String stringPath;

	// MetaData
	private boolean directory;
	private long size;
	private long lastModified;
	private String type;

	private CustomPath() {}

	// 从完整路径实例化
	public CustomPath(String fullPath, boolean fillMetaData) {
		String[] paths = fullPath.split(deviceSplit);

		if (paths.length == 0) {
			// ::
			deviceName = "";
			path = root;
		}
		else if (paths.length == 1 && fullPath.endsWith(deviceSplit)) {
			// name::
			deviceName = paths[0];
			path = root;
		}
		else if (paths.length == 2) {
			// name::path
			deviceName = paths[0];
			path = Paths.get(paths[1]);
		}
		else {
			deviceName = "";
			path = new EmptyPath();
		}

		stringPath = path.toString();
		if(fillMetaData) {
			directory = path.equals(root) || Files.isDirectory(path);
			if(directory) {
				size = Long.MIN_VALUE;
				type = "";
				lastModified = path.equals(root)
					? Long.MIN_VALUE
					: path.toFile().lastModified();
			}
			else{
				File file = path.toFile();
				lastModified = file.lastModified();
				size = file.length();
				if(file.getName().contains(".")) {
					type = file.getName().substring(file.getName().lastIndexOf("."));
				}
				else{
					type = "";
				}
			}
		}
	}

	// 复制一个对象，复制元数据
	public CustomPath(CustomPath customPath) {
		this.path = customPath.getPath();
		this.stringPath = customPath.stringPath;
		this.deviceName = customPath.getDeviceName();
		this.directory = customPath.directory;
		this.size = customPath.size;
		this.lastModified = customPath.lastModified;
		this.type = customPath.type;
	}

	// 根据设备名称和路径实例化一个对象
	public CustomPath(String deviceName, Path path, boolean fillMetaData) {
		this.deviceName = deviceName;
		this.path = path;
		stringPath = path.toString();
		if(fillMetaData){
			directory = path.equals(root) || Files.isDirectory(path);
			if(directory) {
				size = Long.MIN_VALUE;
				type = "";
				lastModified = path.equals(root)
					? Long.MIN_VALUE
					: path.toFile().lastModified();
			}
			else{
				File file = path.toFile();
				lastModified = file.lastModified();
				size = file.length();
				if(file.getName().contains(".")) {
					type = file.getName().substring(file.getName().lastIndexOf("."));
				}
				else{
					type = "";
				}
			}
		}
	}

	// 从JSON字符串实例化，包含元数据
	public static CustomPath fromJSONString(String jsonString) {
		HashMap<String, Object> map = JSON.parseObject(jsonString, HashMap.class);
		String deviceName = (String) map.get("deviceName");
		String stringPath = (String) map.get("stringPath");
		Path path = stringPath == null
			? new EmptyPath()
			: Paths.get(stringPath);
		boolean directory = (boolean) map.get("directory");
		long size = parseLong(map.get("size"));
		long lastModified = parseLong(map.get("lastModified"));
		String type = (String) map.get("type");
		CustomPath result = new CustomPath();
		result.deviceName = deviceName;
		result.path = path;
		result.stringPath = stringPath;
		result.directory = directory;
		result.size = size;
		result.lastModified = lastModified;
		result.type = type;
		return result;
	}

	private static long parseLong(Object obj){
		if(obj instanceof Long){
			return (long) obj;
		}
		else if(obj instanceof Integer){
			return ((Integer)obj).longValue();
		}
		else {
			try{
				return (long)obj;
			}
			catch (Exception e) {
				return Long.MIN_VALUE;
			}
		}
	}

	public CustomPath enter(String name) {
		CustomPath customPath = new CustomPath(this);
		if (customPath.path instanceof EmptyPath) {
			customPath.path = Paths.get(name);
		}
		else {
			customPath.path = customPath.path.resolve(name);
		}
		return customPath;
	}

	public CustomPath back() {
		CustomPath customPath = new CustomPath(this);
		if (customPath.path instanceof EmptyPath || isRootPath()) {
			customPath.path = new EmptyPath();
		}
		else {
			customPath.path = customPath.path.getParent();
		}
		return customPath;
	}

	public File toFile() {
		return path.toFile();
	}

	@JSONField(serialize = false)
	public Path getPath() {
		return path;
	}

	@JSONField(serialize = false)
	// 是否是系统的根目录(比如windows的C:\ linux和mac的/)
	public boolean isRootPath() {
		return path.toString().endsWith(":" + WIN32SPLIT) || path.toString().equals(SPLIT);
	}

	@JSONField(serialize = false)
	public String getFileName() {
		return path.getFileName() == null
			? path.toString()
			: path.getFileName().toString();
	}

	@JSONField(deserialize = false)
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

	public String getDeviceName() {
		return deviceName;
	}

	public boolean isDirectory() {
		return directory;
	}

	public void setDirectory(boolean directory) {
		this.directory = directory;
	}

	public String getStringPath() {
		return stringPath;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
