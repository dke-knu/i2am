package knu.cs.dke.topology_manager_v1;

import redis.clients.jedis.Jedis;

public class DbAdapter {
	private static final Class<?> klass = (new Object() {
	}).getClass().getEnclosingClass();

	// singleton
	private volatile static DbAdapter instance;
	public static DbAdapter getInstance() {
		if(instance == null) {
			synchronized(DbAdapter.class) {
				if(instance == null) {
					instance = new DbAdapter();
				}
			}
		}
		return instance;
	}

	private final Jedis jedis;

	private DbAdapter() {
		jedis = new Jedis("", -1);
	}
	
	public void set(String key, String value) {
		jedis.set(key, value);
	}
}