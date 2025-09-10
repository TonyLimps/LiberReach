package org.tonylimps.liberreach.core.enums;

public enum CommandType {
	ANSWER("0"),
	HEARTBEAT("1"),
	ADD("2"),
	GETPATH("3"),
	DOWNLOAD("4"),
	UPLOAD("5");

	private final String code;

	CommandType(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static CommandType fromCode(String code) {
		for (CommandType type : values()) {
			if (type.code.equals(code)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown command code: " + code);
	}

}
