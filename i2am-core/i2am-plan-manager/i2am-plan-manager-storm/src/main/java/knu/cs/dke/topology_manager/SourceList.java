package knu.cs.dke.topology_manager;

import java.util.HashMap;
import java.util.Map;

import knu.cs.dke.topology_manager.sources.Source;

public class SourceList {
	
	// singleton - 플랜리스트 > 플랜 > 토폴로지
	private volatile static SourceList instance;
	
	public static SourceList getInstance() {
		if(instance == null) {
			synchronized(SourceList.class) {
				if(instance == null) {
					instance = new SourceList();
				}
			}
		}
		return instance;
	}

	private Map<String, Source> mSources;
	private Map<String, Thread> runningThreads;

	private SourceList() {
		mSources = new HashMap<String, Source>();
		runningThreads = new HashMap<String, Thread>();
	}
	
	public synchronized Source get(String owner, String sourceName) {
		
		String sourceId = owner + sourceName;
		
		return mSources.get(sourceId);
	}
	
	public synchronized boolean add(Source source) {
		
		String srcId = source.getOwner() + source.getSourceName();
		
		if (mSources.containsKey(srcId)) return false;
		mSources.put(srcId, source);
		return true;
	}
	
	public synchronized boolean remove(Source source) {
		
		String srcId = source.getOwner() + source.getSourceName();
		
		if (!mSources.containsKey(srcId)) return false;
		mSources.remove(srcId);
		return true;
	}
	
	public synchronized boolean set(Source source) {	
		// 값이 있으면 Update, 없으면 Add 됨..
		String srcId = source.getOwner() + source.getSourceName();
		
		if (!mSources.containsKey(srcId)) return false;
		mSources.put(srcId, source);		
		return true;
	}
	
	public synchronized int size() {
		return mSources.size();
	}
	
	public synchronized boolean addThread(Thread thread) {
						
		runningThreads.put(thread.getName(), thread);		
		return true;
	}
	
	public synchronized Thread getThread(String sourceName) {
							
		return runningThreads.get(sourceName);
	}
	
	public synchronized void printSummary() {
		
		System.out.println("\n[Source List Summary]\n");
		System.out.println("Map Size: " + mSources.size() + "\n");
				
		int i = 0;
		for(String key: mSources.keySet()) {			
			System.out.println("[Source " + i + "]");
			System.out.println("Source Name: " + mSources.get(key).getSourceName());
			System.out.println("Source Type: " + mSources.get(key).getSrcType());
			System.out.println("Thread Name: " + mSources.get(key).getName());
			System.out.println("Thread Status: " + mSources.get(key).isAlive());
			System.out.println("Status: " + mSources.get(key).getStatus());		
			System.out.println("\n");
			i++;
		}
	}
}
