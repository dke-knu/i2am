package knu.cs.dke.topology_manager_v3;

import java.util.HashMap;
import java.util.Map;

import knu.cs.dke.topology_manager_v3.sources.Source;

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

	private SourceList() {
		mSources = new HashMap<String, Source>();
	}
	
	public synchronized Source get(String sourceID) {
		return mSources.get(sourceID);
	}
	
	public synchronized boolean add(Source source) {
		if (mSources.containsKey(source.getSourceID())) return false;
		mSources.put(source.getSourceID(), source);
		return true;
	}
	
	public synchronized boolean remove(Source source) {
		if (!mSources.containsKey(source.getSourceID())) return false;
		mSources.remove(source.getSourceID());
		return true;
	}
	
	public synchronized int size() {
		return mSources.size();
	}
}
