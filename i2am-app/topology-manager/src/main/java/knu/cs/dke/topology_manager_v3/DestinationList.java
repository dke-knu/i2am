package knu.cs.dke.topology_manager_v3;

import java.util.HashMap;
import java.util.Map;

import knu.cs.dke.topology_manager_v3.destinations.Destination;
import knu.cs.dke.topology_manager_v3.sources.Source;

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
		if (mDestinations.containsKey(destination.getSourceID())) return false;
		mDestinations.put(destination.getSourceID(), destination);
		return true;
	}
	
	public synchronized boolean remove(Destination destination) {
		if (!mDestinations.containsKey(destination.getSourceID())) return false;
		mDestinations.remove(destination.getSourceID());
		return true;
	}
	
	public synchronized int size() {
		return mDestinations.size();
	}
}
