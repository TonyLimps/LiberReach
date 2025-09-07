package org.tonylimps.liberreach.core;

public class CustomPath {

	public final String split = "\\";
	public final String regexSplit = "\\\\";
	public final String deviceSplit = "::";

	private String fullPath;

	public CustomPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public CustomPath(CustomPath customPath) {
		this.fullPath = customPath.fullPath;
	}

	public void enter(String folder){
		if(folder.endsWith(split)){
			fullPath = fullPath + folder;
		}
		else{
			fullPath = fullPath + folder + split;
		}
	}
	public void back(){
		try{
			String[] folders = fullPath.split(regexSplit);
			if(folders.length <= 1){
				fullPath = getDeviceName() + deviceSplit;
				return;
			}
			StringBuilder result = new StringBuilder();
			for(int i = 0; i<folders.length-1; i++){
				result.append(folders[i]).append(split);
			}
			fullPath = result.toString();
		}
		catch(Exception e){
			return;
		}
	}

	public String getDeviceName() throws RuntimeException {
		if (isOnlyDeviceName()) {
			return fullPath.split(deviceSplit)[0];
		}
		String[] paths = fullPath.split(deviceSplit);
		return paths.length <= 2 ? paths[0] : "";
	}

	public String getPath() throws RuntimeException {
		String[] paths = fullPath.split(deviceSplit);
		return paths.length == 2 ? paths[1] : "";
	}

	public void setFullPath(String path) {
		if(!path.endsWith(split) && !path.endsWith(deviceSplit)){
			path += split;
		}
		this.fullPath = path;
	}

	public void setFullPath(CustomPath path) {
		this.fullPath = path.fullPath;
	}

	public boolean isOnlyDeviceName(){
		return fullPath.endsWith(deviceSplit);
	}

	@Override
	public String toString() {
		return fullPath;
	}

}
