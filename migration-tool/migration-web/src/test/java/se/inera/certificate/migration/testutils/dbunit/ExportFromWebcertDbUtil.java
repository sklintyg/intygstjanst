package se.inera.certificate.migration.testutils.dbunit;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.database.QueryDataSet;
import org.dbunit.dataset.xml.FlatDtdDataSet;
import org.dbunit.dataset.xml.FlatXmlWriter;

public class ExportFromWebcertDbUtil {

	/**
	 * Util for performing a db dump from a MySQL db to dbunit file.
	 * 
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws DatabaseUnitException 
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, DatabaseUnitException, UnsupportedEncodingException, IOException {
		
        System.out.println("Exporting dataset...");

        Class.forName("com.mysql.jdbc.Driver");

        Connection jdbcConnection = DriverManager.getConnection("jdbc:mysql://localhost/intyg114", "inera", "inera");
        IDatabaseConnection connection = new DatabaseConnection(jdbcConnection);

        QueryDataSet partialDataSet = new QueryDataSet(connection);

        // Mention all the tables here for which you want data to be extracted
        // take note of the order to prevent FK constraint violation when re-inserting
        partialDataSet.addTable("certificate", "select * from certificate");
        partialDataSet.addTable("certificate_state", "select * from certificate_state");
        partialDataSet.addTable("original_certificate", "select * from original_certificate");

        // XML file into which data needs to be extracted
        FlatXmlWriter xmlWriter = new FlatXmlWriter(new FileOutputStream("src/test/resources/data/certificate_dataset_25.xml"), "UTF-8");
        xmlWriter.setIncludeEmptyTable(true);
        xmlWriter.write(partialDataSet);

        System.out.println("Dataset written!");

        FlatDtdDataSet.write(connection.createDataSet(), new FileOutputStream("src/test/resources/data/certificate_dataset.dtd"));

        System.out.println("DTD written!");
        System.out.println("Done!");
		
	}

}
