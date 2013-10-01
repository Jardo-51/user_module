package com.jardo.usermodule.containers;

public class User {

	private final int id;

	private final String name;

	private final String email;

	private final String registrationControlCode;

	private final boolean registrationConfirmed;

	private final UserPassword password;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public String getRegistrationControlCode() {
		return registrationControlCode;
	}

	public boolean isRegistrationConfirmed() {
		return registrationConfirmed;
	}

	public UserPassword getPassword() {
		return password;
	}

	public User(int id, String name, String email, String registrationControlCode, boolean registrationConfirmed, UserPassword password) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.registrationControlCode = registrationControlCode;
		this.registrationConfirmed = registrationConfirmed;
		this.password = password;
	}

}
