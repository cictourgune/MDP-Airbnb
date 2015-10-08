package org.tourgune.mdp.airbnb.plugin;

import static org.tourgune.mdp.airbnb.database.Conditions.is;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.tourgune.mdp.airbnb.config.Config;
import org.tourgune.mdp.airbnb.config.HttpRequestInfo;
import org.tourgune.mdp.airbnb.core.Context;
import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.core.RecordSet;
import org.tourgune.mdp.airbnb.database.Database;
import org.tourgune.mdp.airbnb.database.Functions;
import org.tourgune.mdp.airbnb.database.InsertStatement;
import org.tourgune.mdp.airbnb.database.SelectStatement;
import org.tourgune.mdp.airbnb.exception.DatabaseException;

public class AirbnbRatingsReviewsPlugin extends AbstractPlugin {
	
	private Map<String, String> config;
	
	private boolean getRatings;
	private boolean getReviews;
	
	private boolean followNextPages;
	
	private List<String> nextPageSelectors;
	
	private List<String> panelSelectors;
	private List<String> ratingsContainerSelectors;
	
	// ratings selectors
	private List<String> totalRatingsPanelSelectors;
	private List<String> ratingsPanelSelectors;
	private List<String> ratingPropertyNameSelectors;
	private List<String> ratingStarsCountSelectors;
	private List<String> ratingStarsPanelSelectors;
	private List<String> ratingStarsTags;
	
	// reviews selectors
	private List<String> reviewsContainerSelectors;
	private List<String> reviewPanelSelectors;
	private List<String> reviewTextSelectors;

	public AirbnbRatingsReviewsPlugin() {
		super();
		
		super.registerHost(".airbnb.es");
		super.registerLang("*");
		super.registerMime("text/html");
		super.registerPath("/rooms/");
		
		config = Config.getInstance().getParams("AirbnbRatingsReviews");
		
		nextPageSelectors = new ArrayList<String>();
		panelSelectors = new ArrayList<String>();
		ratingsContainerSelectors = new ArrayList<String>();
		totalRatingsPanelSelectors = new ArrayList<String>();
		ratingsPanelSelectors = new ArrayList<String>();
		ratingPropertyNameSelectors = new ArrayList<String>();
		ratingStarsCountSelectors = new ArrayList<String>();
		ratingStarsPanelSelectors = new ArrayList<String>();
		ratingStarsTags = new ArrayList<String>();
		reviewsContainerSelectors = new ArrayList<String>();
		reviewPanelSelectors = new ArrayList<String>();
		reviewTextSelectors = new ArrayList<String>();
		
		getRatings = config.containsKey("get_ratings") ? Boolean.parseBoolean(config.get("get_ratings")) : true;
		getReviews = config.containsKey("get_reviews") ? Boolean.parseBoolean(config.get("get_reviews")) : true;
		
		followNextPages = config.containsKey("follow_next_pages") ? Boolean.parseBoolean(config.get("follow_next_pages")) : true;
		
		if (config.containsKey("next_page_selectors"))
			for (String selector : config.get("next_page_selectors").split(","))
				nextPageSelectors.add(selector);
		
		if (config.containsKey("panel_selector"))
			for (String selector : config.get("panel_selector").split(","))
				panelSelectors.add(selector);
		
		if (config.containsKey("ratings_container_selector"))
			for (String selector : config.get("ratings_container_selector").split(","))
				ratingsContainerSelectors.add(selector);
		
		if (config.containsKey("total_ratings_panel_selector"))
			for (String selector : config.get("total_ratings_panel_selector").split(","))
				totalRatingsPanelSelectors.add(selector);
		
		if (config.containsKey("ratings_panel_selector"))
			for (String selector : config.get("ratings_panel_selector").split(","))
				ratingsPanelSelectors.add(selector);
		
		if (config.containsKey("rating_property_name_selector"))
			for (String selector : config.get("rating_property_name_selector").split(","))
				ratingPropertyNameSelectors.add(selector);
		
		if (config.containsKey("rating_stars_count_selector"))
			for (String selector : config.get("rating_stars_count_selector").split(","))
				ratingStarsCountSelectors.add(selector);
		
		if (config.containsKey("rating_stars_panel_selector"))
			for (String selector : config.get("rating_stars_panel_selector").split(","))
				ratingStarsPanelSelectors.add(selector);
		
		if (config.containsKey("rating_stars_tag"))
			for (String selector : config.get("rating_stars_tag").split(","))
				ratingStarsTags.add(selector);
		
		if (config.containsKey("reviews_container_selector"))
			for (String selector : config.get("reviews_container_selector").split(","))
				reviewsContainerSelectors.add(selector);
		
		if (config.containsKey("review_panel_selector"))
			for (String selector : config.get("review_panel_selector").split(","))
				reviewPanelSelectors.add(selector);
		
		if (config.containsKey("review_text_selector"))
			for (String selector : config.get("review_text_selector").split(","))
				reviewTextSelectors.add(selector);
	}

	@Override
	public void onPluginInit(List<RecordSet> recordSets) {
		RecordSet rs;
		Database database;
		
		int lengthOfStay = 4;
		
		try {
			database = Database.create();
			database.connect();
			
			// TODO eliminar el WHERE
			SelectStatement select = database.newSelect("d_product");
			select.field("id_hosting").where("id_hosting", is(592870));
			
			ResultSet resultSet = database.query(select);
			
			while (resultSet.next()) {
					rs = new RecordSet();
					
					rs.putInfoKey("hostname", "www.airbnb.es");
					rs.putInfoKey("port", "443");
					rs.putInfoKey("scheme", "https");
					rs.putInfoKey("geoid", "0");	// TODO revisar
					
					rs.putInfoKey("los", Integer.toString(lengthOfStay));
					rs.putInfoKey("path", "/rooms/this_hosting_reviews/" + resultSet.getInt(1) + "?reviews_page=1");
					
					recordSets.add(rs);
			}
			
			database.close();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onPluginEnd() {
		// TODO Auto-generated method stub
		super.onPluginEnd();
	}

	@Override
	public Record[] onFetch(HttpRequestInfo hri, String content, String geographyId) {
		List<Record> records = new ArrayList<Record>(32);
		Record record = null;
		
		// DEBUG
		System.out.println("Hosting: " + hri.getPath());
		// END DEBUG
		
		Document doc = Jsoup.parse(content);
		
		if (getRatings) {
			Element ratingsPanel = getRatingsPanel(doc);
			
			float[] ttlRatings = null;
			Map<String, Float> ratings = null;
			
			if (ratingsPanel != null) {
				record = new Record();
				record.addString("rating");
				record.addString(hri.getPath());
				/*
				 * Layout:
				 * 		ttlRatings[0] = total no. of ratings
				 * 		ttlRatings[1] = average rating (avg. no. of stars)
				 */
				ttlRatings = getTotalRatings(ratingsPanel);
				if (ttlRatings[0] > 0f) {		// algunos alojamientos no tienen ninguna evaluación
					// DEBUG
	//				System.out.println("Media: " + ttlRatings[1] + " (" + ttlRatings[0] + " evaluaciones)");
					// END DEBUG
					ratings = getRatingsPerParam(ratingsPanel);
				}
				record.addInt((int) ttlRatings[0]);
				record.addFloat((int) ttlRatings[1]);
				
				if (ratings != null) {
					for (Iterator<String> keys = ratings.keySet().iterator(); keys.hasNext();) {
						String key = keys.next();
						// DEBUG
		//				System.out.println("\t" + key + " : " + ratings.get(key));
						// END DEBUG
						record.addString(key);
						record.addFloat(ratings.get(key).floatValue());
					}
				}
				
				records.add(record);
			}
		}
		
		if (getReviews) {
			Elements reviewPanels = getReviewPanels(doc);
			if (reviewPanels != null) {
				record = new Record();
				record.addString("review");
				record.addString(hri.getPath());
				for (Element reviewPanel : reviewPanels) {
					String reviewText = getReviewText(reviewPanel);
					record.addString(reviewText);
				}
				records.add(record);
			}
		}
		
		if (getReviews && followNextPages) {	// las páginas siguientes sólo tienen sentido si estamos recogiendo reviews
			Elements pagination = getPagination(doc);
			if (pagination != null) {
				String nextPage = pagination.first().attr("href");
				String oldPath = hri.getWholePath();
				hri.setWholePath(nextPage);
				nextPage = hri.getQuery().get("reviews_page");
				hri.setWholePath(oldPath);
				System.out.println("Next page: " + nextPage);
			} else
				Context.getContext().unregisterGeography(0);
		}else
			Context.getContext().unregisterGeography(0);
		
		return records.toArray(new Record[0]);
	}
	
	/*
	 * Record layout:
	 * +----------+-------------+------------+---------------+-----------------+-----+
	 * | "rating" | hosting id | ttl ratings | avg rating | property name | property rating | ... |
	 * +----------+-------------+------------+---------------+-----------------+-----+
	 * 
	 * +
	 * | "review" | hosting_id | review_text | review_text | ... |
	 * +
	 */
	@Override
	public void onParse(RecordSet rs) {
		// TODO Auto-generated method stub
		try {
			Database database = Database.create();
			database.connect();
			
			for (Record record : rs.getRecords()) {
				String recordType = record.getString(0);
				String hostingId = record.getString(1).split("\\/")[2];
				
				// DEBUG
				System.out.println("Hosting: " + hostingId);
				// END DEBUG
				
				if (recordType.equals("rating")) {
					InsertStatement ttlRatingsInsert = database.newInsert("ft_product_ratings"),
							avgRatingsInsert = database.newInsert("ft_product_ratings");
					
					// TODO start transaction
					
					ttlRatingsInsert.field("id_hosting", Integer.parseInt(hostingId))
						.field("date", Functions.CUR_DATE)
						.field("property", "ttl_ratings")
						.field("value", record.getInt(2));
					
					avgRatingsInsert.field("id_hosting", Integer.parseInt(hostingId))
						.field("date", Functions.CUR_DATE)
						.field("property", "avg_rating")
						.field("value", record.getFloat(3));
					
					database.insert(ttlRatingsInsert);
					database.insert(avgRatingsInsert);
					
					int fieldIndex = 3;
					try {
						while (true) {
							InsertStatement propertyInsert = database.newInsert("ft_product_ratings");
							propertyInsert.field("id_hosting", Integer.parseInt(hostingId))
								.field("date", Functions.CUR_DATE)
								.field("property", record.getString(fieldIndex++))
								.field("value", record.getFloat(fieldIndex++));
							database.insert(propertyInsert);
						}
					} catch (IndexOutOfBoundsException e) {
						continue;
					}
					
					// TODO commit + end transaction
				} else if (recordType.equals("review")) {
					int fieldIndex = 2;
					try {
						while (true) {
							System.out.println("REVIEW: " + record.getString(fieldIndex++));
						}
					} catch (IndexOutOfBoundsException e){}
				}
			}
			
			database.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}

	private Elements getPagination(Document doc) {
		for (String nextPageSelector : nextPageSelectors) {
			Elements nextPages = doc.select(nextPageSelector);
			if (!nextPages.isEmpty()) {
				return nextPages;
			}
		}
		return null;
	}
	
	private Element getRatingsPanel(Document doc) {
		for (String selector : panelSelectors) {
			Elements elements = doc.select(selector);
			if (!elements.isEmpty())
				return elements.first();
		}
		return null;
	}
	
	private float[] getTotalRatings(Element ratingsPanel) {
		float evals = 0f;	// núm. de evaluaciones
		float stars = 0f;	// núm. de estrellas
		
		for (String containerSelector : ratingsContainerSelectors) {
			Elements container = ratingsPanel.select(containerSelector);
			if (!container.isEmpty()) {
				for (String panelSelector : totalRatingsPanelSelectors) {
					Elements panel = container.select(panelSelector);
					
					if (!panel.isEmpty()) {
						String strEvals = panel.text().trim();
						Matcher matcher = Pattern.compile("\\d+").matcher(strEvals);
						if (matcher.find())
							evals = Float.parseFloat(matcher.group());
						
						stars = countStars(panel.first());
						
						break;
					}
				}
				break;
			}
		}
		
		return new float[]{evals, stars};
	}
	
	private Map<String, Float> getRatingsPerParam(Element ratingsPanel) {
		List<String> propertyNames = new ArrayList<String>();
		List<Float> propertyRatings = new ArrayList<Float>();
		
		for (String panelSelector : ratingsPanelSelectors) {
			Elements panel = ratingsPanel.select(panelSelector);
			if (!panel.isEmpty()) {
				// obtén los nombres de las propiedades
				for (String propertyNameSelector : ratingPropertyNameSelectors) {
					Elements properties = panel.select(propertyNameSelector);
					if (!properties.isEmpty()) {
						for (Element property : properties)
							propertyNames.add(property.text());
						break;
					}
				}
				
				// obtén las calificaciones (núm. de estrellas) de las propiedades
				for (String starsPanelSelector : ratingStarsPanelSelectors) {
					Elements starsPanels = panel.select(starsPanelSelector);
					if (!starsPanels.isEmpty()) {
						for (Element starsPanel : starsPanels) {
							float stars = countStars(starsPanel);
							propertyRatings.add(stars);
						}
						break;
					}
				}
				
				break;
			}
		}
		
		Map<String, Float> ratings = new HashMap<String, Float>();
		for (int propertyIndex = 0; propertyIndex < propertyNames.size(); propertyIndex++)
			ratings.put(propertyNames.get(propertyIndex), propertyRatings.get(propertyIndex));
		
		return ratings;
	}
	
	private Elements getReviewPanels(Document doc) {
		Elements mainReviewContainer = null;
		Elements reviewPanels = null;
		
		for (String mainReviewContainerSelector : reviewsContainerSelectors) {
			mainReviewContainer = doc.select(mainReviewContainerSelector);
			if (!mainReviewContainer.isEmpty()) {
				for (String reviewPanelSelector : reviewPanelSelectors) {
					reviewPanels = mainReviewContainer.select(reviewPanelSelector);
					if (!reviewPanels.isEmpty())
						break;
				}
				break;
			}
		}
		
		return reviewPanels;
	}
	
	private String getReviewText(Element reviewPanel) {
		Elements review = null;
		String reviewText = null;
		
		for (String reviewTextSelector : reviewTextSelectors) {
			review = reviewPanel.select(reviewTextSelector);
			if (!review.isEmpty()) {
				reviewText = review.text();	// should be review.first()???
				break;
			}
		}
		
		return reviewText;
	}
	
	private float countStars(Element ratingPanel) {
		float ttlStars = 0f;
		
		for (String starsPanelSelector : ratingStarsCountSelectors) {
			Elements starsPanel = ratingPanel.select(starsPanelSelector);
			if (!starsPanel.isEmpty()) {
				for (String starTagSelector : ratingStarsTags) {
					Elements stars = starsPanel.select(starTagSelector);
					if (!stars.isEmpty()) {
						for (Element star : stars) {
							if (star.hasClass("icon-star") || star.hasClass("icon-star-half")) {
								if (star.hasClass("icon-star"))
									ttlStars += 1;
								else if (star.hasClass("icon-star-half"))
									ttlStars += 0.5;
							}
						}
						break;
					}
				}
				break;
			}
		}
		
		return ttlStars;
	}
}
