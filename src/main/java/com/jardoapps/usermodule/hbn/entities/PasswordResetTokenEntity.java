package com.jardoapps.usermodule.hbn.entities;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.jardoapps.usermodule.containers.PasswordResetToken;

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
