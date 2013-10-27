package com.jardo.usermodule.hbn;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.dbunit.DBTestCase;
import org.dbunit.PropertiesBasedJdbcDatabaseTester;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.ReplacementDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;

public abstract class UMDatabaseTestCase extends DBTestCase {

	private IDataSet initialDataSet;

	private final FlatXmlDataSetBuilder dataSetBuilder;

	protected abstract IDataSet createInitialDataSet() throws Exception;

	@Override
	protected IDataSet getDataSet() throws Exception {
		if (initialDataSet == null) {
			initialDataSet = createInitialDataSet();
		}
		return initialDataSet;
	}

	protected ReplacementDataSet loadFlatXmlDataSet(String fileName) throws FileNotFoundException, DataSetException {
		FileInputStream inputStream = new FileInputStream(fileName);
		IDataSet dataSet = dataSetBuilder.build(inputStream);

		ReplacementDataSet result = new ReplacementDataSet(dataSet);
		return result;
	}

	public UMDatabaseTestCase() {
		super();
		this.initialDataSet = null;
		this.dataSetBuilder = new FlatXmlDataSetBuilder();

		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_DRIVER_CLASS, "com.mysql.jdbc.Driver");
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_CONNECTION_URL, "jdbc:mysql://127.0.0.1/user_module_test");
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_USERNAME, "user_module");
		System.setProperty(PropertiesBasedJdbcDatabaseTester.DBUNIT_PASSWORD, "password");
	}
}
