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
	private Map<String, Thread> runningThreads;

	private DestinationList() {
		mDestinations = new HashMap<String, Destination>();
		runningThreads = new HashMap<String, Thread>();
	}
	
	public synchronized Destination get(String owner, String destinationName) {
		
		String destinationId = owner + destinationName;		
		
		return mDestinations.get(destinationId);
	}
	
	public synchronized boolean add(Destination destination) {
		
		String dstId = destination.getOwner() + destination.getDestinationName(); 
		
		if (mDestinations.containsKey(dstId)) return false;
		mDestinations.put(dstId, destination);
		return true;
	}
	
	public synchronized boolean remove(Destination destination) {
		
		String dstId = destination.getOwner() + destination.getDestinationName();
		
		if (!mDestinations.containsKey(dstId)) return false;
		mDestinations.remove(dstId);
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
	
	public synchronized boolean addThread(Thread thread) {
		
		runningThreads.put(thread.getName(), thread);		
		return true;
	}
	
	public synchronized Thread getThread(String destinationName) {
							
		return runningThreads.get(destinationName);
	}
	
	public synchronized void printSummary() {
		
		System.out.println("\n[Destination List Summary]\n");
		System.out.println("Map Size: " + mDestinations.size() + "\n");
				
		int i = 0;
		for(String key: mDestinations.keySet()) {			
			System.out.println("[Destination " + i + "]");
			System.out.println("Destination Name: " + mDestinations.get(key).getDestinationName());			
			System.out.println("Thread Name: " + mDestinations.get(key).getName());
			System.out.println("Thread Status: " + mDestinations.get(key).isAlive());
			System.out.println("Status: " + mDestinations.get(key).getStatus());	
			System.out.println("\n");
			i++;
		}
	}
}
