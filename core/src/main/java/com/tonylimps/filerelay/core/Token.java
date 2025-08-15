package com.tonylimps.filerelay.core;

import java.security.NoSuchAlgorithmException;

public class Token {
	private String value;

	public void flush() throws NoSuchAlgorithmException {
		value = Core.createToken();
		Core.getLogger().info("Token flushed.");
	}

	public String getValue() {
		return value;
	}
}
