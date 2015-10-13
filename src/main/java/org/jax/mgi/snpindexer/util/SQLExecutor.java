package org.jax.mgi.snpindexer.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Properties;


public class SQLExecutor {

	public Properties props = new Properties();
	
	protected Connection con = null;
	private String databaseUrl;

	private Date start;
	private Date end;

	private int	cursorSize = 0;
	private boolean autoCommit = true;

	public SQLExecutor(int cursorSize, boolean autoCommit) {
		this.cursorSize  = cursorSize;
		this.autoCommit = autoCommit;
		try {

			InputStream in = SQLExecutor.class.getClassLoader().getResourceAsStream("config.properties");
			try {
				props.load(in);
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			Class.forName(props.getProperty("driver"));
			databaseUrl = props.getProperty("databaseUrl");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void cleanup() throws SQLException {
		if (con != null) {
			con.close();
		}
	}

	public void executeUpdate (String query) {

		try {
			initializeConnection();
			Statement stmt = con.createStatement();
			stmt.setFetchSize(cursorSize);
			start = new Date();
			stmt.executeUpdate(query);
			end = new Date();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void executeVoid(String query) {
		
		try {
			initializeConnection();
			Statement stmt = con.createStatement();
			stmt.setFetchSize(cursorSize);
			stmt.execute(query);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public ResultSet executeQuery(String query) {
		ResultSet set = null;
		try {
			initializeConnection();
			Statement stmt = con.createStatement();
			stmt.setFetchSize(cursorSize);
			start = new Date();
			set = stmt.executeQuery(query);
			end = new Date();
			return set;
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		return set;
	}

	private void initializeConnection() throws SQLException {
		if (con == null) {
			con = DriverManager.getConnection(databaseUrl);
			con.setAutoCommit(autoCommit);
		}
	}

	public long getTiming() {
		return end.getTime() - start.getTime();
	}

	@Override
	public String toString() {
		return "SQLExecutor[databaseUrl=" + databaseUrl + "]";
	}

}
