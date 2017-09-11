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

package com.jardoapps.usermodule.jpa.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.jardoapps.usermodule.containers.PasswordResetToken;

/**
 * An entity class for representing password reset tokens.
 * <p>
 * This class is a part of this library's public API.
 * 
 * @author Jaroslav Brtiš
 *
 */
@Entity
@Table(name = "um_password_reset_token")
public class PasswordResetTokenEntity {

	@Id
	@GeneratedValue
	long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	UserEntity user;

	@Column(name = "date_time")
	Date time;

	@Column(name = "token_key")
	String key;

	@Column(name = "valid")
	boolean valid;

	public PasswordResetTokenEntity() {
		super();
	}

	public PasswordResetTokenEntity(PasswordResetToken passwordResetToken) {
		this.user = new UserEntity();
		this.user.setId(passwordResetToken.getUserId());
		this.time = passwordResetToken.getCreationTime();
		this.key = passwordResetToken.getKey();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public UserEntity getUser() {
		return user;
	}

	public void setUser(UserEntity user) {
		this.user = user;
	}

	public Date getTime() {
		return time;
	}

	public void setTime(Date time) {
		this.time = time;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}
}
