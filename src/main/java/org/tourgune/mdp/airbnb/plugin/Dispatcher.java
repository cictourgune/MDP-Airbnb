package org.tourgune.mdp.airbnb.plugin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.tourgune.mdp.airbnb.config.HttpRequestInfo;
import org.tourgune.mdp.airbnb.core.Context;
import org.tourgune.mdp.airbnb.core.Record;
import org.tourgune.mdp.airbnb.core.RecordSet;
import org.tourgune.mdp.airbnb.utils.Args;

public class Dispatcher {

	public static enum Event {
		INIT,
		END,
		FETCH,
		PARSE
	}
	
	private List<AbstractPlugin> plugins;
	
	public Dispatcher() {
		plugins = new ArrayList<AbstractPlugin>();
	}
	
	public void loadPlugin(String pluginName) {
		Args.checkNotNull("plugin name cannot be null", pluginName);
		
		try {
			Class<AbstractPlugin> plugin = (Class<AbstractPlugin>) Class.forName(this.getClass().getPackage().getName() + "." + pluginName);
			plugins.add(plugin.newInstance());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void dispatch(Event event, RecordSet input) {
		List<Record> newRecordList = new ArrayList<Record>();
		
		for (Iterator<AbstractPlugin> it = plugins.iterator(); it.hasNext();) {
			AbstractPlugin plugin = it.next();
			switch (event) {
			case INIT:
				plugin.init();
				break;
			case END:
				plugin.end();
				break;
			case FETCH:
				if (input != null) {
					Record r = input.getRecord(0);
					// TODO 'HttpRequestInfo' might be a subclass of 'Record' as well
					HttpRequestInfo hri = new HttpRequestInfo(
							input.getInfo("scheme"),					// scheme
							input.getInfo("hostname"),					// host
							Integer.parseInt(input.getInfo("port")),	// port
							input.getInfo("path")						// path
							);
					hri.setMime(r.getString(1));
					hri.setLang(r.getString(2));
					hri.setCheckinDate(input.getInfo("checkin"));
					hri.setCheckoutDate(input.getInfo("checkout"));
					hri.setLengthOfStay(input.getInfo("los"));
					hri.setGuests(input.getInfo("guests"));
//					input.clean();
					Record[] newRecords = plugin.parse(hri, r.getString(3), input.getInfo("geoid"));
					if (newRecords != null)
						for (Record record : newRecords)
							newRecordList.add(record);
				}
				break;
			case PARSE:
				if (input != null)
					plugin.store(input);
				break;
			default:
				break;
			}
		}
		
		if (input != null) {
			input.clean();
			for (Record record : newRecordList)
				input.addRecord(record);
		}
	}
	
	public void work() {
		RecordSet rs = null;
		
		rs = Context.getContext().get(Context.CriticalSections.POSTFETCH);
		
		if (rs != null) {
//			notify(rs);
			// DEBUG
			//System.out.println(rs);
			// END DEBUG
			Context.getContext().store(Context.CriticalSections.POSTPARSE, rs);
		}
	}
}
