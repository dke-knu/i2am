package knu.cs.dke.topology_manager;

import java.util.HashMap;
import java.util.Map;

import knu.cs.dke.topology_manager.destinations.Destination;
import knu.cs.dke.topology_manager.sources.Source;

public class DestinationList {
	
	// singleton - 플랜리스트 > 플랜 > 토폴로지
	private volatile static DestinationList instance;
	
	public static DestinationList getInstance() {
		if(instance == null) {
			synchronized(DestinationList.class) {
				if(instance == null) {
					instance = new DestinationList();
				}
			}
		}
		return instance;
	}

	private Map<String, Destination> mDestinations;

	private DestinationList() {
		mDestinations = new HashMap<String, Destination>();
	}
	
	public synchronized Destination get(String destinationID) {
		return mDestinations.get(destinationID);
	}
	
	public synchronized boolean add(Destination destination) {
		if (mDestinations.containsKey(destination.getDestinationName())) return false;
		mDestinations.put(destination.getDestinationName(), destination);
		return true;
	}
	
	public synchronized boolean remove(Destination destination) {
		if (!mDestinations.containsKey(destination.getDestinationName())) return false;
		mDestinations.remove(destination.getDestinationName());
		return true;
	}
	
	public synchronized int size() {
		return mDestinations.size();
	}

	public synchronized boolean set(Destination destination) {		
		// 값이 있으면 Update, 없으면 Add 됨..
		if (!mDestinations.containsKey(destination.getDestinationName())) return false;
		mDestinations.put(destination.getDestinationName(), destination);		
		return true;
	}
}
