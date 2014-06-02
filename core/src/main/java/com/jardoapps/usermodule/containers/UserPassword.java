package com.jardoapps.usermodule.containers;

/**
 * Container class representing a user password which consits out of the
 * password hash and a random generated salt.
 * 
 * @author Jaroslav Brtiš
 * 
 */
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
