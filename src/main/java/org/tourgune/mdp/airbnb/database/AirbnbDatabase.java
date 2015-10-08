package org.tourgune.mdp.airbnb.database;

import static org.tourgune.mdp.airbnb.database.Conditions.is;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.exception.DatabaseException;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class AirbnbDatabase extends AbstractDatabase {

	public AirbnbDatabase() {
		super();
		registerId(2);
	}
	
	@Override
	protected void insertRecord(Record record) {
		float price = 0f;
		String title = null;
		char roomType = 0;
		int productId = -1, channelId = -1;
		
		try {
			ResultSet rs = null;
			PreparedStatement ps = null;
//			Connection conn = DriverManager.getConnection("jdbc:mysql://10.10.0.116/airbnb?jdbcCompliantTruncation=false", "developer", "qwerty");
			Database database = Database.create();
			
			database.connect();
			
			if (record != null) {
				/*
				 * Record layout:
				 * +-------+-------+-----------+
				 * | title | price | room type |
				 * +-------+-------+-----------+
				 */
				title = record.getString(0);
				price = (float) record.getInt(1);	// TODO debería ser record.getFloat(0)
				roomType = record.getChar(2);
				
				InsertStatement i = null;
				
				// obtenemos el id del producto
				SelectStatement s = database.newSelect("d_product");
				s.field("id_product").where("description", is(title));
//				ps = conn.prepareStatement("SELECT id_product FROM d_product WHERE description = ?");
//				ps.setString(1, title);
				
				// TODO Cerrar el ResultSet!!
				rs = database.query(s);
//				rs = ps.executeQuery();
				if (rs.next()) {
					productId = rs.getInt(1);
				} else {
//					ps = conn.prepareStatement("INSERT INTO d_product (description) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
//					ps.setString(1, title);
					i = database.newInsert("d_product");
					i.field("description", title);
					rs = database.insertAndGet(i);
//					ps.executeUpdate();
//					rs = ps.getGeneratedKeys();
					if (rs.next())
						productId = rs.getInt(1);
//					rs.close();
//					ps.close();
				}
				
				// obtenemos el id del canal
//				ps = conn.prepareStatement("SELECT id_channel FROM d_channel WHERE description= 'airbnb'");
				s = database.newSelect("d_channel");
				s.field("id_channel").where("description", is("airbnb"));
				
				rs = database.query(s);
//				rs = ps.executeQuery();
				if (rs.next())
					channelId = rs.getInt(1);
//				rs.close();
//				ps.close();
				
				/*
				 * id_product
				 * id_channel
				 * booking_date = CURDATE()
				 * accommodation_type
				 * checkin_date = CURDATE() --> debería venirnos desde el RecordSet
				 * los = 6 --> debería venirnos desde el RecordSet
				 * price
				 */
				if (productId != -1 && channelId != -1) {
//					ps = conn.prepareStatement("INSERT INTO ft_product_price VALUES (?, ?, CURDATE(), ?, CURDATE(), 6, ?)");
					i = database.newInsert("ft_product_price");
					i.field("id_product", productId)
						.field("id_channel", channelId)
						.field("booking_date", Functions.CUR_DATE)
						.field("accommodation_type", String.valueOf(roomType))
						.field("checkin_date", Functions.CUR_DATE)
						.field("los", 6)	// tiempo de estancia fijo de 6 días
						.field("price", price);
					
//					ps.setInt(1, productId);
//					ps.setInt(2, channelId);
//					ps.setString(3, String.valueOf(roomType));
//					ps.setFloat(4, price);
					
					database.insert(i);
//					ps.executeUpdate();
//					ps.close();
				}
			}
			
//			conn.close();
			// TODO todos los ResultSets abiertos deberían cerrarse aquí
			database.close();
		} catch (MySQLIntegrityConstraintViolationException e) {
			// DEBUG
			System.err.println("MySQL integrity violation (probably a duplicated element). Description: \"" + e.getMessage() + "\"");
			// END DEBUG
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
