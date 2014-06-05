/*
 * This file is part of the User Module library.
 * Copyright (C) 2014 Jaroslav Brtiš
 *
 * User Module library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * User Module library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with User Module library. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jardoapps.usermodule;

import com.jardoapps.usermodule.containers.UserPassword;

/**
 * Class representing a registered user.
 * 
 * @author Jaroslav Brtiš
 * 
 */
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
