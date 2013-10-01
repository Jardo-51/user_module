package com.jardo.usermodule.containers;

public class UserPassword {

	private final String hash;
	private final String salt;

	public String getHash() {
		return hash;
	}

	public String getSalt() {
		return salt;
	}

	public UserPassword(String hash, String salt) {
		this.hash = hash;
		this.salt = salt;
	}

}
