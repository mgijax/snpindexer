package org.jax.mgi.snpindexer.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;

public class SQLExecutor {

	protected Connection con = null;

	private Date start;
	private Date end;

	private int	cursorSize = 0;
	private boolean autoCommit = true;
	private boolean debug = false;
	private Logger log = Logger.getLogger(SQLExecutor.class);
	
	public SQLExecutor(int cursorSize, boolean autoCommit, boolean debug) {
		this.cursorSize  = cursorSize;
		this.autoCommit = autoCommit;
		this.debug = debug;
		try {
			Class.forName(ConfigurationHelper.getDriver());
		}
		catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
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
			if(debug) log.info("Query: " + query);
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
			if(debug) log.info("Query: " + query);
			stmt.execute(query);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public ResultSet executeQuery(String query) {
		ResultSet set = null;
		initializeConnection();
		try {
			Statement stmt = con.createStatement();
			stmt.setFetchSize(cursorSize);
			start = new Date();
			if(debug) log.info("Query: " + query);
			set = stmt.executeQuery(query);
			end = new Date();
			return set;
		} catch (Exception e) {
			e.printStackTrace();
			log.error("DB Error: " + e.getMessage());
			System.exit(1);
		}
		return set;
	}

	private void initializeConnection() {
		if (con == null) {
			try {
				con = DriverManager.getConnection(ConfigurationHelper.getDatabaseUrl(), ConfigurationHelper.getUser(), ConfigurationHelper.getPassword());
				con.setAutoCommit(autoCommit);
				log.info("Database Connection Initialized to: " + ConfigurationHelper.getDatabaseUrl());
			} catch (SQLException e) {
				log.error("Database Connection ERROR: " + e.getMessage());
				log.error("DB Url: " + ConfigurationHelper.getDatabaseUrl());
				System.exit(1);
			}
		}
	}

	public long getTiming() {
		return end.getTime() - start.getTime();
	}

	@Override
	public String toString() {
		return "SQLExecutor[databaseUrl=" + ConfigurationHelper.getDatabaseUrl() + "]";
	}

}
