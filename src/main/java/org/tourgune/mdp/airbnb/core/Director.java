package org.tourgune.mdp.airbnb.core;

import static org.tourgune.mdp.airbnb.database.Conditions.is;
import static org.tourgune.mdp.airbnb.database.Conditions.isNotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.tourgune.mdp.airbnb.core.Context.CriticalSections;
import org.tourgune.mdp.airbnb.database.Database;
import org.tourgune.mdp.airbnb.database.SelectStatement;
import org.tourgune.mdp.airbnb.exception.DatabaseException;

public class Director {

	public void init() throws SQLException {
		RecordSet curRs = null;
		try {
			List<RecordSet> urlRecords = getUrlRecords();
			for (Iterator<RecordSet> it = urlRecords.iterator(); it.hasNext();) {
				curRs = it.next();
				Context.getContext().store(CriticalSections.PREFETCH, curRs);
				Context.getContext().registerGeography(Integer.parseInt(curRs.getInfo("geoid")));
			}
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	
	public List<RecordSet> getUrlRecords() throws SQLException, DatabaseException {
		List<RecordSet> rsList = new LinkedList<RecordSet>();
		RecordSet curRecordSet = null;
		StringBuilder sql = new StringBuilder();
		Map<String, String> baseInfo = new HashMap<String, String>(),
				info = null;
		
		Database database = Database.create();
		
		database.connect();
		
		SelectStatement select = database.newSelect("d_geography");
		select.field("id_geography")
			.storedProcedure("tr(locality, 'ÁáÉéÍíÓóÚú', 'AaEeIiOoUu')").alias("locality")
			.field("d_country", "name_en").alias("country")
			.where("NUTS2", is("País Vasco"))
			.where("NUTS3", isNotNull())
			.where("locality", isNotNull())
			.join("d_country", "id_country", "id_country");
		
		ResultSet rs = database.query(select);
		
		baseInfo.put("scheme", "https");
		baseInfo.put("hostname", "www.airbnb.es");
		baseInfo.put("port", "443");
		
		while (rs.next()) {
			info = new HashMap<String, String>(baseInfo);
			curRecordSet = new RecordSet(info);
			String suffix = urlEncode(rs.getString("locality") + " -- " + rs.getString("country"));
			curRecordSet.putInfoKey("path", "/s/" + suffix + "?search_by_map=false");
			curRecordSet.putInfoKey("geoid", Integer.toString(rs.getInt("id_geography")));
			rsList.add(curRecordSet);
			// DEBUG
			System.out.println("Added " + rs.getString("locality") + "(" + rs.getString("country") + ")");
			System.out.println("Registered geography ID " + rs.getInt("id_geography"));
			// END DEBUG
		}
		
		database.close();
		
		return rsList;
	}
	
	public static String urlEncode(String source) {
		return source.replace(" ", "%20");
	}
}
