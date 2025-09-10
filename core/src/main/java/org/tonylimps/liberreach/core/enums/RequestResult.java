package org.tonylimps.liberreach.core.enums;

public enum RequestResult {
	OFFLINE("0"),
	WRONGTOKEN("1"),
	TIMEOUT("2"),
	SUCCESS("3"),
	TOOMANYREQUESTS("4");

	private final String code;

	RequestResult(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static RequestResult fromCode(String code) {
		for (RequestResult type : values()) {
			if (type.code.equals(code)) {
				return type;
			}
		}
		throw new IllegalArgumentException("Unknown command code: " + code);
	}
}
