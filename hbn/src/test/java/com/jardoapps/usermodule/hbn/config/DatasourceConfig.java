package com.jardoapps.usermodule.hbn.config;

import java.util.Properties;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.StringUtils;

public class DatasourceConfig {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatasourceConfig.class);

	public static final String DATASOURCE_BEAN_NAME = "dataSource";

	public DatasourceConfig() {
		LOGGER.info("Creating JPA data source.");
	}

	@Bean(name = "entityManagerFactory")
	@DependsOn({ DATASOURCE_BEAN_NAME })
	public EntityManagerFactory getEntityManagerFactory(@Qualifier(DATASOURCE_BEAN_NAME) DataSource dataSource,
			@Value("#{'${jdbc.packagesToScan}'.split(',')}") String[] packagesToScan, @Value("${hibernate.dialect}") String dialect, @Value("${hibernate.hbm2ddl.auto}") String ddl,
			@Value("${hibernate.default_schema:}") String defaultSchema, @Value("${jdbc.extraMappings:}") String extraMappings) {
		HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
		vendorAdapter.setGenerateDdl(true);

		LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
		factory.setJpaVendorAdapter(vendorAdapter);
		factory.setDataSource(dataSource);
		factory.setPackagesToScan(packagesToScan);

		if (!StringUtils.isEmpty(extraMappings)) {
			String[] resources = extraMappings.split(",");
			factory.setMappingResources(resources);
		}

		Properties props = new Properties();
		props.put("hibernate.dialect", dialect);
		props.put("hibernate.hbm2ddl.auto", ddl);
		if (defaultSchema != null) {
			props.put("hibernate.default_schema", defaultSchema);
		}
		factory.setJpaProperties(props);

		LOGGER.debug("Packages to scan: {}", (Object[]) packagesToScan);
		LOGGER.debug("Entity manager factory properties: {}", props);

		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean(name = "platformTransactionalManager")
	@DependsOn({ "entityManagerFactory" })
	public PlatformTransactionManager platformTransactionalManager(@Qualifier("entityManagerFactory") EntityManagerFactory entityManagerFactory) {
		JpaTransactionManager txManager = new JpaTransactionManager();
		txManager.setEntityManagerFactory(entityManagerFactory);
		return txManager;
	}

	@Bean(name = DatasourceConfig.DATASOURCE_BEAN_NAME)
	public FactoryBean<DataSource> getDataSourceFactoryBean() {
		return new DriverManagerDataSourceFactoryBean();
	}

}
