package org.jax.mgi.snpindexer;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.jax.mgi.snpindexer.util.SQLExecutor;

public class Main {

	public static void main(String[] args) throws SQLException {


		SQLExecutor sql = new SQLExecutor();

		ResultSet set = sql.executeQuery("select * from snp_strain");

		while (set.next()) {
			System.out.println(set.getString(1));
			System.out.println(set.getString(2));
			System.out.println(set.getString(3));
			System.out.println(set.getString(4));
		}

		set.close();

	}

}
