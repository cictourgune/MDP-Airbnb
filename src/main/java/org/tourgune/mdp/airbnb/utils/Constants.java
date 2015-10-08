package org.tourgune.mdp.airbnb.utils;

public class Constants {

	/********************
	 * Valores internos *
	 ********************/
	public static final String FIELD_SCHEME = "scheme";
	public static final String FIELD_HOSTNAME = "hostname";
	public static final String FIELD_PORT = "port";
	public static final String FIELD_PATH = "path";
	public static final String FIELD_GEOID = "geoid";
	public static final String FIELD_CHECKIN = "checkin";
	public static final String FIELD_CHECKOUT = "checkout";
	public static final String FIELD_LENGTH_OF_STAY = "los";
	public static final String FIELD_NUM_GUESTS = "guests";
	public static final String CONFIG_DIRECTIVE_SEPARATOR = ".";
	
	/**********************************************
	 * Constantes del esquema de la base de datos *
	 **********************************************/
	public static final String DB_TABLE_GEOGRAPHY = "d_geography";
	public static final String DB_TABLE_PRODUCTS = "d_product";
	public static final String DB_TABLE_CHANNELS = "d_channel";
	public static final String DB_TABLE_COUNTRY = "d_country";
	public static final String DB_TABLE_PRICES = "ft_product_price";
	
	public static final String DB_FIELD_GEO_ID = "id_geography";
	public static final String DB_FIELD_PROD_ID = "id_product";
	public static final String DB_FIELD_HOST_ID = "id_hosting";
	public static final String DB_FIELD_CH_ID = "id_channel";
	public static final String DB_FIELD_DESCR = "description";
	public static final String DB_FIELD_BOOKING_DATE = "booking_date";
	public static final String DB_FIELD_ACCOMMODATION_TYPE = "accommodation_type";
	public static final String DB_FIELD_CHECKIN_DATE = "checkin_date";
	public static final String DB_FIELD_LENGTH_OF_STAY = FIELD_LENGTH_OF_STAY;
	public static final String DB_FIELD_PRICE = "price";
	public static final String DB_FIELD_LOCALITY = "locality";
	public static final String DB_FIELD_GUESTS = "guests";
	
	/*************************************
	 * Constantes para AirbnbPriceParser *
	 *************************************/
	public static final int ABNB_PRICE_PARSER_DEFAULT_LOS = 4;
	public static final String ABNB_PRICE_PARSER_REGISTERED_HOSTS = ".airbnb.es";
	public static final String ABNB_PRICE_PARSER_PARAMS_SECTION = "AirbnbPrice";
	public static final String ABNB_PRICE_PARSER_PARAM_SOURCE = "source";
	public static final String ABNB_PRICE_PARSER_PARAM_PRICE_SELECTORS = "price_selectors";
	public static final String ABNB_PRICE_PARSER_PARAM_ROOM_TYPE_SELECTORS = "room_type_selectors";
	public static final String ABNB_PRICE_PARSER_PARAM_TITLE_SELECTORS = "title_selectors";
	public static final String ABNB_PRICE_PARSER_PARAM_TITLE_PROPERTIES = "title_properties";
	public static final String ABNB_PRICE_PARSER_PARAM_TITLE_HOSTING_ID_PROPERTY = "title_hosting_id_property";
	public static final String ABNB_PRICE_PARSER_PARAM_FOLLOW_NEXT_PAGES = "follow_next_pages";
	public static final String ABNB_PRICE_PARSER_PARAM_NEXT_PAGE_SELECTORS = "next_page_selectors";
	public static final String ABNB_PRICE_PARSER_PARAM_NEXT_PAGE_PROPERTIES = "next_page_properties";
	public static final String ABNB_PRICE_PARSER_PARAM_NUM_GUESTS = "guests";
	
	public static final String ABNB_PRICE_PARSER_MIME_HTML = "text/html";
	public static final String ABNB_PRICE_PARSER_MIME_JSON = "application/json";
	public static final String ABNB_PRICE_PARSER_SEARCH_PATH_HTML = "/s/";
	public static final String ABNB_PRICE_PARSER_SEARCH_PATH_JSON = "/search/";
	
	public static final String ABNB_PRICE_PARSER_ROOM_TYPE_W = "casa/apto. entero";
	public static final String ABNB_PRICE_PARSER_ROOM_TYPE_P = "habitación privada";
	public static final String ABNB_PRICE_PARSER_ROOM_TYPE_S = "habitación compartida";
	
	public static final String ABNB_PRICE_PARSER_MSG_LOS_POSITIVE = "length of stay must be positive";
	public static final String ABNB_PRICE_PARSER_DATE_FORMAT_ES = "dd/MM/yyyy";
	public static final String ABNB_PRICE_PARSER_DATE_FORMAT_US = "yyyy-MM-dd";
	
	/*************************
	 * Tokens protocolo HTTP *
	 *************************/
	public static final String HTTP_REQUEST_VERB = "GET";
	public static final String HTTP_ACCEPT_HEADER = "Accept";
	public static final String HTTP_ACCEPT_LANG_HEADER = "Accept-Language";
	public static final String HTTP_ACCEPT_ENCODING_HEADER = "Accept-Encoding";
	public static final String HTTP_CONTENT_LANG_HEADER = "Content-Language";
	public static final String HTTP_CONTENT_ENCODING_HEADER = "Content-Encoding";
	
	public static final String HTTP_DEFAULT_CONTENT_TYPE = "application/octet-stream";		// Content-Type por defecto según RFC 2616
	public static final String HTTP_DEFAULT_CONTENT_LANG = "en-us";		// Content-Language por defecto
		// (no está especificado por el RFC, pero suele ser lo habitual)
	public static final String HTTP_DEFAULT_CONTENT_ENCODING = "identity";	// Content-Encoding por defecto según RFC 2616
	
	/****************************
	 * Tokens genéricos de DBAL *
	 ****************************/
	public static final String DB_MSG_NOT_IMPLEMENTED_INSERT = "This feature is not implemented in an INSERT statement";
	public static final String DB_CONN_IS_NULL = "Database connection is null. Did you call setConnection()?";
	public static final String DB_SQL_IS_NULL = "SQL statement cannot be null";
	public static final String DB_MSG_TABLE_NAME_UNKNOWN = "You must explicitly enter the table's name.";
	
	/****************
	 * Tokens MySQL *
	 ****************/
	public static final String MYSQL_JDBC_PREFIX = "jdbc:mysql://";
	public static final String MYSQL_JDBC_PARAMS = "jdbcCompliantTruncation=false";
	public static final String MYSQL_FUNC_CURDATE = "CURDATE()";
	public static final String MYSQL_ALIAS_SETTER = "AS";
	public static final String MYSQL_GLUE_AND = "AND";
	public static final String MYSQL_GLUE_OR = "OR";
	public static final String MYSQL_COND_EQUAL = "=";
	public static final String MYSQL_COND_NOTEQUAL = "!=";
	public static final String MYSQL_COND_NULL = "IS NULL";
	public static final String MYSQL_COND_NOTNULL = "IS NOT NULL";
	public static final String MYSQL_MSG_INTEGRITY_VIOLATION = "MySQL integrity violation (probably a duplicated element).";
}
