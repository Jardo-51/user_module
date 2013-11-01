package com.jardo.usermodule.hbn.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.jardo.usermodule.containers.User;

@Entity
@Table(name = "um_user")
public class UserEntity {

	@Id
	@GeneratedValue
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
		passwordHash = user.getPassword().getHash();
		passwordSalt = user.getPassword().getSalt();
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

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public void setPasswordSalt(String passwordSalt) {
		this.passwordSalt = passwordSalt;
	}

}
