package com.jardoapps.usermodule.hbn.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DriverManagerDataSourceFactoryBean implements FactoryBean<DataSource> {

	@Value("${jdbc.driverClassName}")
	private String driverClassName;

	@Value("${jdbc.url}")
	private String url;

	@Value("${jdbc.username}")
	private String username;

	@Value("${jdbc.password}")
	private String password;

	public DataSource getObject() throws Exception {

		DriverManagerDataSource ds = new DriverManagerDataSource();

		ds.setDriverClassName(driverClassName);
		ds.setUrl(url);
		ds.setUsername(username);
		ds.setPassword(password);

		return ds;
	}

	public Class<?> getObjectType() {
		return DataSource.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
