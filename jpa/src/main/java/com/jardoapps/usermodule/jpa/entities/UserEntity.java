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
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.jardoapps.usermodule.User;
import com.jardoapps.usermodule.containers.UserPassword;

/**
 * An entity class for representing registered users.
 * <p>
 * This class is a part of this library's public API.
 * 
 * @author Jaroslav Brtiš
 *
 */
@Entity
@Table(name = "um_user")
public class UserEntity {

	@Id
	@Column(name = "id")
	@SequenceGenerator(name = "um_user_id_seq", sequenceName = "um_user_id_seq")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "um_user_id_seq")
	private int id;

	@Column(name = "name")
	private String name;

	@Column(name = "email")
	private String email;

	@Column(name = "reg_date")
	private Date registrationDate;

	@Column(name = "reg_control_code")
	private String registrationControlCode;

	@Column(name = "confirmed")
	private boolean registrationConfirmed;

	@Column(name = "deleted")
	private boolean deleted;

	@Column(name = "rank")
	private int rank;

	@Column(name = "password")
	private String passwordHash;

	@Column(name = "salt")
	private String passwordSalt;

	public UserEntity() {
		super();
	}

	public void copyUser(User user) {
		id = user.getId();
		name = user.getName();
		email = user.getEmail();
		registrationControlCode = user.getRegistrationControlCode();
		registrationConfirmed = user.isRegistrationConfirmed();
		rank = user.getRank();

		UserPassword password = user.getPassword();
		if (password != null) {
			passwordHash = password.getHash();
			passwordSalt = password.getSalt();
		} else {
			passwordHash = null;
			passwordSalt = null;
		}
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public String getRegistrationControlCode() {
		return registrationControlCode;
	}

	public boolean isRegistrationConfirmed() {
		return registrationConfirmed;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public int getRank() {
		return rank;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public String getPasswordSalt() {
		return passwordSalt;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public void setRegistrationControlCode(String registrationControlCode) {
		this.registrationControlCode = registrationControlCode;
	}

	public void setRegistrationConfirmed(boolean registrationConfirmed) {
		this.registrationConfirmed = registrationConfirmed;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void setPasswordSalt(String passwordSalt) {
		this.passwordSalt = passwordSalt;
	}

	public User toUser() {
		UserPassword password = new UserPassword(passwordHash, passwordSalt);
		User result = new User(id, name, email, registrationControlCode, registrationConfirmed, password, rank);
		return result;
	}

}
