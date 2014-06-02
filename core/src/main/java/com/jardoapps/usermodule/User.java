package com.jardoapps.usermodule;

import com.jardoapps.usermodule.containers.UserPassword;

public class User {

	private final int id;

	private final String name;

	private final String email;

	private final String registrationControlCode;

	private final boolean registrationConfirmed;

	private final UserPassword password;

	private int rank;

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

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public User(int id, String name, String email, String registrationControlCode, boolean registrationConfirmed, UserPassword password, int rank) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.registrationControlCode = registrationControlCode;
		this.registrationConfirmed = registrationConfirmed;
		this.password = password;
		this.rank = rank;
	}

}
