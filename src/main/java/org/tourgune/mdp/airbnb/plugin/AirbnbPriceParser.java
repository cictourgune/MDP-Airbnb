package org.tourgune.mdp.airbnb.plugin;

import static org.tourgune.mdp.airbnb.database.Conditions.is;
import static org.tourgune.mdp.airbnb.database.Conditions.isNotNull;
import static org.tourgune.mdp.airbnb.database.Conditions.isNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tourgune.mdp.airbnb.config.Config;
import org.tourgune.mdp.airbnb.config.HttpRequestInfo;
import org.tourgune.mdp.airbnb.core.Context;
import org.tourgune.mdp.airbnb.core.Context.CriticalSections;
import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.core.RecordSet;
import org.tourgune.mdp.airbnb.database.Conditions;
import org.tourgune.mdp.airbnb.database.Database;
import org.tourgune.mdp.airbnb.database.Functions;
import org.tourgune.mdp.airbnb.database.InsertStatement;
import org.tourgune.mdp.airbnb.database.SelectStatement;
import org.tourgune.mdp.airbnb.exception.DatabaseException;
import org.tourgune.mdp.airbnb.utils.Args;
import org.tourgune.mdp.airbnb.utils.Constants;
import org.tourgune.mdp.airbnb.utils.Utils;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

/**
 * @todo Comprobar el path y el hostname
 * @todo ¿Charset?
 * @todo registerTypes() que acepte wildcards (ej. "text/*", "*./*", "*")
 * @todo Registrar etiquetas y propiedades que parsearemos (para poder limpiar el HTML con Jsoup.clean()).
 * @author Ander.Juaristi
 *
 */
public class AirbnbPriceParser extends AbstractPlugin {

	private Map<String, String> config;
	
	private String source;	// html o json
	
	private List<String> priceSelectors;
	private List<String> roomTypeSelectors;
	private List<String> titleSelectors, titleProperties, hostingIdProperties;
	private List<String> nextPageSelectors, nextPageProperties;
	private List<String> latProperties, lonProperties;
	
	private boolean followNextPages;
	private int numGuests;
	
	public AirbnbPriceParser() {
		super();
		
		super.registerHost(Constants.ABNB_PRICE_PARSER_REGISTERED_HOSTS);
		super.registerLang("*");
		
		config = Config.getInstance().getParams(Constants.ABNB_PRICE_PARSER_PARAMS_SECTION);
		source = config.containsKey(Constants.ABNB_PRICE_PARSER_PARAM_SOURCE)
				? config.get(Constants.ABNB_PRICE_PARSER_PARAM_SOURCE)
						: "html";	// html is the default source
		
		if (source.equalsIgnoreCase("html")) {
			super.registerMime(Constants.ABNB_PRICE_PARSER_MIME_HTML);
			super.registerPath(Constants.ABNB_PRICE_PARSER_SEARCH_PATH_HTML);
		} else {
			super.registerMime(Constants.ABNB_PRICE_PARSER_MIME_JSON);
			super.registerPath(Constants.ABNB_PRICE_PARSER_SEARCH_PATH_JSON);
		}
	}
	
	@Override
	public void onPluginInit(List<RecordSet> recordList) {
		priceSelectors = new ArrayList<String>();
		roomTypeSelectors = new ArrayList<String>();
		titleSelectors = new ArrayList<String>();
		titleProperties = new ArrayList<String>();
		hostingIdProperties = new ArrayList<String>();
		nextPageSelectors = new ArrayList<String>();
		nextPageProperties = new ArrayList<String>();
		latProperties = new ArrayList<String>();
		lonProperties = new ArrayList<String>();
		
		// TODO Usar Config.getParamValues()
		if (config.containsKey(Constants.ABNB_PRICE_PARSER_PARAM_PRICE_SELECTORS)) {
			for (String selector : config.get(Constants.ABNB_PRICE_PARSER_PARAM_PRICE_SELECTORS).split(","))
				priceSelectors.add(selector);
		}
		
		if (config.containsKey(Constants.ABNB_PRICE_PARSER_PARAM_ROOM_TYPE_SELECTORS)) {
			for (String selector : config.get(Constants.ABNB_PRICE_PARSER_PARAM_ROOM_TYPE_SELECTORS).split(","))
				roomTypeSelectors.add(selector);
		}
		
		if (config.containsKey(Constants.ABNB_PRICE_PARSER_PARAM_TITLE_SELECTORS)) {
			String allSelectors = config.get(Constants.ABNB_PRICE_PARSER_PARAM_TITLE_SELECTORS);
			String[] selectors = allSelectors.split(",");
			for (String selector : selectors)
				titleSelectors.add(selector.trim());
		}
		
		if (config.containsKey(Constants.ABNB_PRICE_PARSER_PARAM_TITLE_PROPERTIES)) {
			for (String property : config.get(Constants.ABNB_PRICE_PARSER_PARAM_TITLE_PROPERTIES).split(","))
				titleProperties.add(property.trim());
		}
		
		if (config.containsKey(Constants.ABNB_PRICE_PARSER_PARAM_TITLE_HOSTING_ID_PROPERTY)) {
			for (String property : config.get(Constants.ABNB_PRICE_PARSER_PARAM_TITLE_HOSTING_ID_PROPERTY).split(","))
				hostingIdProperties.add(property.trim());
		}
		
		if (config.containsKey(Constants.ABNB_PRICE_PARSER_PARAM_FOLLOW_NEXT_PAGES))
			followNextPages = config.get(Constants.ABNB_PRICE_PARSER_PARAM_FOLLOW_NEXT_PAGES).equals("true") ? true : false;
		else
			followNextPages = false;
		
		if (config.containsKey("lat_properties") && config.containsKey("lon_properties")) {
			for (String property : config.get("lat_properties").split(","))
				latProperties.add(property);
			for (String property : config.get("lon_properties").split(","))
				lonProperties.add(property);
		}
		
		if (followNextPages && config.containsKey(Constants.ABNB_PRICE_PARSER_PARAM_NEXT_PAGE_SELECTORS)) {
			for (String selector : config.get(Constants.ABNB_PRICE_PARSER_PARAM_NEXT_PAGE_SELECTORS).split(","))
				nextPageSelectors.add(selector);
			
			if (config.containsKey(Constants.ABNB_PRICE_PARSER_PARAM_NEXT_PAGE_PROPERTIES))
				for (String property : config.get(Constants.ABNB_PRICE_PARSER_PARAM_NEXT_PAGE_PROPERTIES).split(","))
					nextPageProperties.add(property);
		}
		
		if (config.containsKey(Constants.ABNB_PRICE_PARSER_PARAM_NUM_GUESTS))
			numGuests = Integer.parseInt(config.get(Constants.ABNB_PRICE_PARSER_PARAM_NUM_GUESTS));
		else
			numGuests = 1;
		
		try {
			// abrimos una conexión a la bd que se mantendrá hasta onPluginEnd().
			Database database = Database.create();
			database.connect();
			
			buildRecords(database, recordList);
			
			database.close();
		} catch (DatabaseException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private Elements getTitleElements(Document doc) {
		Elements elems = null;
		for (Iterator<String> it = titleSelectors.iterator(); it.hasNext();) {
			elems = doc.select(it.next());
			if (elems.size() > 0)
				break;
		}
		return elems;
	}
	
	private String getTitleValue(Element element) {
		String val = null, attr = null;
		for (Iterator<String> it = titleProperties.iterator(); it.hasNext() && val == null;) {
			attr = it.next();
			if (element.hasAttr(attr))
				val = element.attr(attr);
		}
		return val;
	}
	
	private String getHostingId(Element element) {
		String val = null, attr = null;
		for (Iterator<String> it = hostingIdProperties.iterator(); it.hasNext() && val == null;) {
			attr = it.next();
			if (element.hasAttr(attr))
				val = element.attr(attr);
		}
		return val;
	}
	
	private Elements getNextPage(Document doc) {
		Elements nextPageElems = null;
		for (Iterator<String> it = nextPageSelectors.iterator(); it.hasNext();) {
			String selector = it.next();
			nextPageElems = doc.select(selector);
			if (!nextPageElems.isEmpty())
				break;
		}
		return nextPageElems.isEmpty() ? null : nextPageElems;
	}
	
	private String getNextPageValue(Element element) {
		String val = null, attr = null;
		for (Iterator<String> it = nextPageProperties.iterator(); it.hasNext() && val == null;) {
			attr = it.next();
			if (element.hasAttr(attr))
				val = element.attr(attr);
		}
		return val;
	}
	
	private String getPriceValue(Element e) {
		String price = null;
		for (Iterator<String> it = priceSelectors.iterator(); it.hasNext();) {
			price = e.select(it.next()).text();
			if (!price.isEmpty())
				break;
		}
		return price.isEmpty() ? null : price;
	}
	
	private String getRoomTypeValue(Element e) {
		String roomType = null;
		for (Iterator<String> it = roomTypeSelectors.iterator(); it.hasNext();) {
			roomType = e.select(it.next()).text();
			if (!roomType.isEmpty())
				break;
		}
		return roomType.isEmpty() ? null : roomType;
	}
	
	private char getRoomType(String roomTypeText) {
		char roomType = 'n';
		
		int delim = roomTypeText.indexOf('·');	
		if (delim >= 0)
			roomTypeText = roomTypeText.substring(0, delim).trim();
		
		switch (roomTypeText.toLowerCase()) {
		case Constants.ABNB_PRICE_PARSER_ROOM_TYPE_W:
			roomType = 'w';
			break;
		case Constants.ABNB_PRICE_PARSER_ROOM_TYPE_P:
			roomType = 'p';
			break;
		case Constants.ABNB_PRICE_PARSER_ROOM_TYPE_S:
			roomType = 's';
			break;
		}
		return roomType;
	}
	
	private double[] getLatLon(Element e) {
		Double lat = null, lon = null;
		String attr = null, val = null;
		
		try {
			for (Iterator<String> it = latProperties.iterator(); it.hasNext() && val == null;) {
				attr = it.next();
				if (e.hasAttr(attr)) {
					val = e.attr(attr);
					lat = new Double(Double.parseDouble(val));
				}
			}
			
			val = null;
			for (Iterator<String> it = lonProperties.iterator(); it.hasNext() && val == null;) {
				attr = it.next();
				if (e.hasAttr(attr)) {
					val = e.attr(attr);
					lon = new Double(Double.parseDouble(val));
				}
			}
		} catch (NumberFormatException nfe) {
			// si hemos fallado al obtener lat o lon, terminamos
			// no tiene sentido devolver uno solo
		}
		
		return (lat == null || lon == null ? null : new double[]{lat.doubleValue(), lon.doubleValue()});
	}
	
	/*
	 * TODO esto se podría mejorar (siguiendo el estándar) y pasarlo a un package 'utils'
	 * TODO RFC3986
	 */
	public static String urlEncode(String source) {
		source = source.replace(" ", "+");
		source = source.replace("/", "%2F");
		source = source.replace(",", "%2C");
		return source;
	}
	
	private void computeDates(int lengthOfStay, List<String> checkinDates, List<String> checkoutDates) {
		int[] defaultCheckinOffsets = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,45,60,90};
		
		List<Integer> checkinOffsets = Utils.toIntList(
					Config.getInstance().getParamValues(Constants.ABNB_PRICE_PARSER_PARAMS_SECTION,
							"checkin_offsets",
							Utils.toStringArray(defaultCheckinOffsets))
				);
		
		Args.checkPositive(Constants.ABNB_PRICE_PARSER_MSG_LOS_POSITIVE, lengthOfStay);
		
		Locale locale = new Locale.Builder().setLanguageTag("es").build();
		GregorianCalendar checkinDate = null, checkoutDate = null;
		SimpleDateFormat formatter = new SimpleDateFormat(Constants.ABNB_PRICE_PARSER_DATE_FORMAT_ES, locale);
		
		for (int checkinOffset : checkinOffsets) {
			checkinDate = new GregorianCalendar(locale);
			checkoutDate = new GregorianCalendar(locale);
			
			checkinDate.add(Calendar.DAY_OF_MONTH, checkinOffset);
			
			checkoutDate.set(Calendar.MONTH, checkinDate.get(Calendar.MONTH));
			checkoutDate.set(Calendar.YEAR, checkinDate.get(Calendar.YEAR));
			checkoutDate.set(Calendar.DAY_OF_MONTH, checkinDate.get(Calendar.DAY_OF_MONTH) + lengthOfStay);
			
			checkinDates.add(formatter.format(checkinDate.getTime()));
			checkoutDates.add(formatter.format(checkoutDate.getTime()));
		}
	}
	
	/*
	 * Devuelve un array con los geoids
	 */
	private void buildRecords(Database database, List<RecordSet> rsList) throws SQLException {
		RecordSet curRecordSet = null;
		Map<String, String> baseInfo = new HashMap<String, String>(),
				info = null;
		
		// [country, NUTS2, NUTS3, locality]
		String[] regionInfo = {
				config.containsKey("region_country") ? config.get("region_country") : "all",
				config.containsKey("region_nuts2") ? config.get("region_nuts2") : "all",
				config.containsKey("region_nuts3") ? config.get("region_nuts3") : "all",
				config.containsKey("region_locality") ? config.get("region_locality") : "all"
		};
		String[] regionFieldNames = {
				"country",
				"NUTS2",
				"NUTS3",
				"locality"
		};
		
		List<String> regions = Config.getInstance().getParamValues("AirbnbPrice", "region_names", new String[]{"all"});
		
		int lengthOfStay = 0;
		
		
		try {
			lengthOfStay = config.containsKey("length_of_stay") ? Integer.parseInt(config.get("length_of_stay"))
					: Constants.ABNB_PRICE_PARSER_DEFAULT_LOS;
		} catch (NumberFormatException e) {
			lengthOfStay = Constants.ABNB_PRICE_PARSER_DEFAULT_LOS;
		}
		
		List<String> checkinDates = new ArrayList<String>(50);
		List<String> checkoutDates = new ArrayList<String>(50);
		
		computeDates(lengthOfStay, checkinDates, checkoutDates);
		// DEBUG
		System.out.println(checkinDates);
		System.out.println(checkoutDates);
		// END DEBUG
		
		SelectStatement select = database.newSelect("d_region");
		select.field("d_region", "id_region").field("d_region", "region_name").field("d_country", "name_en");
		
		if (!regions.contains("all")) {
			for (String region : regions) {
				select.where("region_name", is(region), Conditions.GlueType.OR);
			}
		}
		
		select.join("d_country", "id_country", "id_country");
		
		ResultSet rs = database.query(select);
		
		baseInfo.put(Constants.FIELD_SCHEME, "https");
		baseInfo.put(Constants.FIELD_HOSTNAME, "www.airbnb.es");
		baseInfo.put(Constants.FIELD_PORT, "443");
		
		while (rs.next()) {
			for (int dateIndex = 0; dateIndex < checkinDates.size(); dateIndex++) {
				info = new HashMap<String, String>(baseInfo);
				curRecordSet = new RecordSet(info);
				
				// TODO podríamos usar el HttpRequestInfo para formatear las URLs
				String location = urlEncode(rs.getString("region_name") +
						(source.equalsIgnoreCase("html") ? " -- " : ", ") +
						rs.getString("name_en"));
				
				curRecordSet.putInfoKey(Constants.FIELD_CHECKIN, checkinDates.get(dateIndex));
				curRecordSet.putInfoKey(Constants.FIELD_CHECKOUT, checkoutDates.get(dateIndex));
				curRecordSet.putInfoKey(Constants.FIELD_LENGTH_OF_STAY, Integer.toString(lengthOfStay));
				curRecordSet.putInfoKey(Constants.FIELD_NUM_GUESTS, Integer.toString(numGuests));
				curRecordSet.putInfoKey(Constants.FIELD_PATH,
						(source.equalsIgnoreCase("html") ? "/s/" : "/search/search_results?location=") + location +
						(source.equalsIgnoreCase("html") ? "?search_by_map=false" : "?page=1") +
								"&checkin=" + urlEncode(checkinDates.get(dateIndex)) +
								"&checkout=" + urlEncode(checkoutDates.get(dateIndex)) +
								(numGuests > 1 ? "&guests=" + numGuests : ""));
				// TODO add support for room type filter
				curRecordSet.putInfoKey(Constants.FIELD_GEOID, Integer.toString(rs.getInt("id_region")));
				rsList.add(curRecordSet);
				// DEBUG
				System.out.println("Added " + rs.getString("region_name") + " (" + rs.getString("name_en") + ")" +
						"[" + checkinDates.get(dateIndex) + " - " + checkoutDates.get(dateIndex) + "]");
				// END DEBUG
			}
		}
	}
	
	@Override
	public void onPluginEnd() {
	}
	
	
	@Override
	public void onParse(RecordSet rs) {
		try {
			Database database = Database.create();
			database.connect();
			
			for (Record record : rs.getRecords())
				insertRecord(record,
						rs.getInfo(Constants.FIELD_CHECKIN),
						Integer.parseInt(rs.getInfo(Constants.FIELD_LENGTH_OF_STAY)),
						rs.getInfo("geoid"),
						Integer.parseInt(rs.getInfo(Constants.FIELD_NUM_GUESTS)),
						database);
			
			database.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
	}

	private synchronized void insertRecord(Record record, String checkinDate, int lengthOfStay, String regionId, int guests, Database database) throws SQLException {
		float price = 0f;
		String title = null;
		int hostingId = 0;
		char roomType = 0;
		int productId = -1, channelId = -1;
		double lat = 0, lon = 0;
		
		try {
			SimpleDateFormat srcFormatter = new SimpleDateFormat(Constants.ABNB_PRICE_PARSER_DATE_FORMAT_ES),
					dstFormatter = new SimpleDateFormat(Constants.ABNB_PRICE_PARSER_DATE_FORMAT_US);
			ResultSet rs = null;
			if (record != null) {
				/*
				 * Record layout:
				 * +-------+-------+-----------+-----+-----+
				 * | title | price | room type | lat | lon |
				 * +-------+-------+-----------+-----+-----+
				 */
				title = record.getString(0);
				price = (float) record.getInt(1);	// TODO debería ser record.getFloat(0)
				roomType = record.getChar(2);
				lat = record.getDouble(3);
				lon = record.getDouble(4);
				hostingId = Integer.parseInt(record.getString(5));
				
				InsertStatement i = null;
				
				// obtenemos el id del producto
				SelectStatement s = database.newSelect(Constants.DB_TABLE_PRODUCTS);
				s.field(Constants.DB_FIELD_PROD_ID).where(Constants.DB_FIELD_PROD_ID, is(hostingId));
				
				rs = database.query(s);
				if (!rs.next()) {
					// Si este producto no existe, lo insertamos.
					i = database.newInsert(Constants.DB_TABLE_PRODUCTS);
					i.field(Constants.DB_FIELD_DESCR, title)
						.field(Constants.DB_FIELD_PROD_ID, hostingId)
						.field("region", Integer.parseInt(regionId))
						.field("lat", lat)
						.field("lon", lon);
					database.insert(i);
				}
				rs.close();
				
				/*
				 * id_product
				 * id_channel
				 * booking_date = CURDATE()
				 * accommodation_type
				 * checkin_date = viene del RecordSet (clave "checkin")
				 * los = viene del RecordSet (clave "los")
				 * price
				 */
				if (hostingId != -1) {
					i = database.newInsert(Constants.DB_TABLE_PRICES);
					i.field(Constants.DB_FIELD_PROD_ID, hostingId)
						.field(Constants.DB_FIELD_BOOKING_DATE, Functions.CUR_DATE)
						.field(Constants.DB_FIELD_ACCOMMODATION_TYPE, String.valueOf(roomType))
						.field(Constants.DB_FIELD_LENGTH_OF_STAY, lengthOfStay)
						.field(Constants.DB_FIELD_CHECKIN_DATE, dstFormatter.format(srcFormatter.parse(checkinDate)))
						.field(Constants.DB_FIELD_PRICE, price)
						.field(Constants.DB_FIELD_GUESTS, guests);
					
					database.insert(i);
				}
			}
		} catch (MySQLIntegrityConstraintViolationException e) {
			// TODO esto debería ir en la capa de abstracción de la BD
			// DEBUG
			System.err.println(Constants.MYSQL_MSG_INTEGRITY_VIOLATION + " Description: \"" + e.getMessage() + "\"");
			// END DEBUG
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// TODO 'HttpRequestInfo' might be a subclass of 'Record' as well
	@Override
	public Record[] onFetch(HttpRequestInfo hri, String content, String regionId) {
		List<Record> records = new ArrayList<Record>(36);	// airbnb suele sacar 18 resultados por página, le ponemos el doble de tamaño, para no tener que recargar el array
		Record record = null;
		String title = null;
		String hostingId = null;
		String nextPage = null;
		String price = null;
		String roomType = null;
		Elements titleElements = null, nextPageElements = null;
		Element curItem = null;
		double[] latlng = null;
		
		// es posible que la petición HTTP haya fallado
		if (content == null)
			return null;
		
		JSONObject jsonRoot = null;
		Document doc = null, footer = null;
		if (source.equalsIgnoreCase("html"))
			doc = Jsoup.parse(content);
		else {
			jsonRoot = new JSONObject(content);
			doc = Jsoup.parse(jsonRoot.getString("results"));
			footer = Jsoup.parse(jsonRoot.getString("pagination_footer"));
		}
		
		titleElements = getTitleElements(doc);
		if (titleElements != null) {
			for (Iterator<Element> it = titleElements.iterator(); it.hasNext();) {
				record = new Record();
				curItem = it.next();
				
				title = getTitleValue(curItem);
				if (title != null)
					record.addString(title);
				
				price = getPriceValue(curItem);
				if (price != null)
					record.addInt(Integer.parseInt(price));
				
				roomType = getRoomTypeValue(curItem);
				if (roomType != null) {
					char chrRoomType = getRoomType(roomType);
					record.addChar(chrRoomType);
				}
				
				latlng = getLatLon(curItem);
				if (latlng != null) {
					record.addDouble(latlng[0]);
					record.addDouble(latlng[1]);
				}
				
				hostingId = getHostingId(curItem);
				if (hostingId != null)
					record.addString(hostingId);
				
				records.add(record);
			}
		}
		
		if (followNextPages) {
			// obtener páginas siguientes...
			nextPageElements = getNextPage(source.equalsIgnoreCase("html") ? doc : footer);
			if (nextPageElements != null) {
				nextPage = getNextPageValue(nextPageElements.first());
				// DEBUG
//				System.out.println("Next page: " + (nextPage == null ? "<null>" : nextPage));
				// END DEBUG
				String nextPath = hri.appendQuery("page", nextPage).getWholePath();
				// DEBUG
				System.out.println("[" + Thread.currentThread().getName() + "] Next path: " + nextPath);
				// END DEBUG
				Context.getContext().store(
						CriticalSections.PREFETCH,
						new RecordSet()
							.putInfoKey("scheme", hri.getScheme())
							.putInfoKey("hostname", hri.getHostname())
							.putInfoKey("port", Integer.toString(hri.getPort()))
							.putInfoKey("path", nextPath)
							.putInfoKey("geoid", regionId)
							.putInfoKey("checkin", hri.getCheckinDate())
							.putInfoKey("checkout", hri.getCheckoutDate())
							.putInfoKey("los", hri.getLengthOfStay())
							.putInfoKey("guests", hri.getGuests()));
			} else {
				Context.getContext().unregisterGeography(Integer.parseInt(regionId));
			}
		} else {
			Context.getContext().unregisterGeography(Integer.parseInt(regionId));
		}
		
		return records.toArray(new Record[0]);
	}
}
