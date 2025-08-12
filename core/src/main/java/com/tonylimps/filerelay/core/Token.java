package com.tonylimps.filerelay.core;

public class Token {
	private String value;

	public void flush() throws Exception {
		value = Core.createToken();
	}

	public String getValue() {
		return value;
	}
}
