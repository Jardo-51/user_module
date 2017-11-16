package com.jardoapps.usermodule.jpa.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name="um_social_account", uniqueConstraints = @UniqueConstraint(columnNames = {"account_type", "original_account_id"}))
public class SocialAccountEntity {

	public static final int ACCOUNT_TYPE_MAX_LENGTH = 10;
	public static final int ORIGINAL_ACCOUNT_ID_MAX_LENGTH = 50;

	@Id
	@GeneratedValue
	private long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserEntity user;

	@Column(name = "account_type", nullable = false, length = ACCOUNT_TYPE_MAX_LENGTH)
	private String accountType;

	@Column(name = "original_account_id", nullable = false, length = ORIGINAL_ACCOUNT_ID_MAX_LENGTH)
	private String originalAccountId;

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

	public String getAccountType() {
		return accountType;
	}

	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}

	public String getOriginalAccountId() {
		return originalAccountId;
	}

	public void setOriginalAccountId(String originalAccountId) {
		this.originalAccountId = originalAccountId;
	}

}
