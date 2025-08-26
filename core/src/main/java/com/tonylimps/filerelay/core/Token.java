package com.tonylimps.filerelay.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.NoSuchAlgorithmException;

public class Token {

	private final Logger logger = LogManager.getLogger(this.getClass());
	private String value;

	public void flush() throws NoSuchAlgorithmException {
		value = Core.createToken();
		logger.info("Token flushed.");
	}

	public String getValue() {
		return value;
	}
}
