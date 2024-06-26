package org.jax.mgi.snpindexer.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.jax.mgi.snpindexer.config.ConfigurationHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SQLExecutor {

	protected Connection con = null;

	private Date start;
	private Date end;

	private int	cursorSize = 0;
	private boolean autoCommit = true;
	private boolean debug = false;

	public SQLExecutor(int cursorSize, boolean autoCommit) {
		this.cursorSize  = cursorSize;
		this.autoCommit = autoCommit;
		this.debug = ConfigurationHelper.isDebug();
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
			startQuery(query);
			stmt.executeUpdate(query);
			endQuery();
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
			startQuery(query);
			stmt.execute(query);
			endQuery();
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
			startQuery(query);
			set = stmt.executeQuery(query);
			endQuery();
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
				log.debug("Database Connection Initialized to: " + ConfigurationHelper.getDatabaseUrl());
			} catch (SQLException e) {
				log.error("Database Connection ERROR: " + e.getMessage());
				log.error("DB Url: " + ConfigurationHelper.getDatabaseUrl());
				System.exit(1);
			}
		}
	}

	public void startQuery(String query) {
		if(debug) {
			log.info("Running Query: " + query);
			start = new Date();
		}
	}
	
	public void endQuery() {
		if(debug) {
			end = new Date();
			log.info("Query took: " + (end.getTime() - start.getTime()) + "ms to run");
		}
	}

	@Override
	public String toString() {
		return "SQLExecutor[databaseUrl=" + ConfigurationHelper.getDatabaseUrl() + "]";
	}

}
