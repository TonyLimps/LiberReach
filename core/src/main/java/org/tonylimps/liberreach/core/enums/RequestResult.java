package org.tonylimps.liberreach.core.enums;

public enum RequestResult {
	OFFLINE,
	WRONGTOKEN,
	TIMEOUT,
	SUCCESS,
	TOOMANYREQUESTS;

	public static RequestResult fromString(String type){
		for(RequestResult c : values()){
			if(type.contains(c.name())){
				return c;
			}
		}
		return null;
	}
}
