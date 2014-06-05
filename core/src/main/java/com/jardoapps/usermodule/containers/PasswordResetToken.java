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

import java.util.Date;

import com.jardoapps.usermodule.UserManager;

/**
 * Container class representing password reset tokens. Password reset tokens are
 * used to set a new password if the user forgets his current password. They
 * contain a random generated security code and creation date, so their validity
 * can be limited to a specified time period. See
 * {@link UserManager#createPasswordResetToken(String)} and
 * {@link UserManager#resetPassword(String, String, String)} for more details.
 * 
 * @author Jaroslav Brtiš
 * 
 */
public class PasswordResetToken {

	private final int userId;
	private final Date creationTime;
	private final String key;

	public Date getCreationTime() {
		return creationTime;
	}

	public String getKey() {
		return key;
	}

	public int getUserId() {
		return userId;
	}

	public PasswordResetToken(int userId, String key, Date creationTime) {
		this.userId = userId;
		this.creationTime = creationTime;
		this.key = key;
	}
}
