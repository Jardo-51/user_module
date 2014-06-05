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
