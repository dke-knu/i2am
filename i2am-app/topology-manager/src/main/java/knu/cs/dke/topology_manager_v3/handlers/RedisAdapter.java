package knu.cs.dke.topology_manager_v3.handlers;

import redis.clients.jedis.Jedis;

public class RedisAdapter {
	
	private static final Class<?> klass = (new Object() {
	}).getClass().getEnclosingClass();

	// singleton
	private volatile static RedisAdapter instance;
	public static RedisAdapter getInstance() {
		if(instance == null) {
			synchronized(RedisAdapter.class) {
				if(instance == null) {
					instance = new RedisAdapter();
				}
			}
		}
		return instance;
	}

	private final Jedis jedis;

	private RedisAdapter() {
		jedis = new Jedis("", -1);
	}
	
	public void set(String key, String value) {
		jedis.set(key, value);
	}
}