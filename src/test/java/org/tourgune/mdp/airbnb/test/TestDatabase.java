package org.tourgune.mdp.airbnb.test;

import static org.tourgune.mdp.airbnb.database.Conditions.is;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.tourgune.mdp.airbnb.config.Config;
import org.tourgune.mdp.airbnb.database.Database;
import org.tourgune.mdp.airbnb.database.Functions;
import org.tourgune.mdp.airbnb.database.InsertStatement;
import org.tourgune.mdp.airbnb.database.SelectStatement;
import org.tourgune.mdp.airbnb.exception.ConfigException;
import org.tourgune.mdp.airbnb.exception.DatabaseException;

public class TestDatabase {

	public static void main(String[] args) {
		try {
			Config.load(new File("airbnb.conf"));
			Database db = Database.create();
			db.connect();
			SelectStatement s = db.newSelect("my_table");
			InsertStatement i = db.newInsert("d_product");
			s.field("title");
			s.alias("t");
			s.where("my_field", is("a")).where("id_booking_date", is(Functions.CUR_DATE));
			i.field("description", "Bonita casa!!").field("price", 80).field("booking_date", Functions.CUR_DATE);
			String sql = s.prepare();
			System.out.println(sql);
			PreparedStatement ps = s.compile(sql);
			System.out.println(ps.toString());
			System.out.println();
			sql = i.prepare();
			System.out.println(sql);
			ps = i.compile(sql);
			System.out.println(ps.toString());
			db.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ConfigException e) {
			e.printStackTrace();
		}
	}
}
