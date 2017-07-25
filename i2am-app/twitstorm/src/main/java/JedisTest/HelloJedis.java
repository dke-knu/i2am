package JedisTest;

import redis.clients.jedis.Jedis;

public class HelloJedis {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		Jedis jedis = new Jedis("192.168.56.100", 6379);
		
		String result = jedis.set("Redisbook", "Hello Redis!");
		
		System.out.println(result);
		System.out.println(jedis.get("Redisbook"));
		
	}

}
