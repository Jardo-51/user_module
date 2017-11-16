package com.jardoapps.usermodule.jpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.jardoapps.usermodule.UserDatabaseModel;
import com.jardoapps.usermodule.jpa.UserDatabaseModelJpa;
import com.jardoapps.usermodule.jpa.dao.LogInRecordEntityDao;
import com.jardoapps.usermodule.jpa.dao.PasswordResetTokenEntityDao;
import com.jardoapps.usermodule.jpa.dao.SocialAccountDao;
import com.jardoapps.usermodule.jpa.dao.UserEntityDao;

@EnableTransactionManagement
@Import(DatasourceConfig.class)
@PropertySource("classpath:database_test.properties")
public class DatabaseTestConfig {

	@Bean
	public static PropertySourcesPlaceholderConfigurer getPropertyPlaceholderConfigurer() {
		PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
		return configurer;
	}

	@Bean
	public UserDatabaseModel getUserDatabaseModel() {
		return new UserDatabaseModelJpa();
	}

	@Bean
	public LogInRecordEntityDao getLogInRecordEntityDao() {
		return new LogInRecordEntityDao();
	}

	@Bean
	public PasswordResetTokenEntityDao getPasswordResetTokenEntityDao() {
		return new PasswordResetTokenEntityDao();
	}

	@Bean
	public SocialAccountDao getSocialAccountDao() {
		return new SocialAccountDao();
	}

	@Bean
	public UserEntityDao getUserEntityDao() {
		return new UserEntityDao();
	}

}
