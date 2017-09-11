package com.jardoapps.usermodule.hbn.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.jardoapps.usermodule.UserDatabaseModel;
import com.jardoapps.usermodule.hbn.UserDatabaseModelHbn;
import com.jardoapps.usermodule.hbn.dao.LogInRecordEntityDao;
import com.jardoapps.usermodule.hbn.dao.PasswordResetTokenEntityDao;
import com.jardoapps.usermodule.hbn.dao.UserEntityDao;

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
		return new UserDatabaseModelHbn();
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
	public UserEntityDao getUserEntityDao() {
		return new UserEntityDao();
	}

}
