package com.zxtech.iot.verxtiot.common;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
	private static Logger log = LoggerFactory.getLogger(RedisUtil.class);
	private static JedisPool jedisPool = null;
	private static boolean isUseJedis = false;
	
	static {
		initCache("localhost");
	}

	public synchronized static void initCache(String host) {
		if(isUseJedis) {
			return;
		}
		JedisPoolConfig config = new JedisPoolConfig();
		config.setMaxTotal(50);
		config.setMaxIdle(10);
		config.setMaxWaitMillis(3000);

		// 初始化连接池
		jedisPool = new JedisPool(config, host, 6379);
		isUseJedis = true;
	}
	
	public static boolean set(String key, String value){
		if(!isUseJedis || StringUtils.isBlank(value)){
			return false;
		}
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.set(key, value);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
	}

	public static boolean set(String key, String value, int expireSecond){
		if(!isUseJedis || StringUtils.isBlank(value)){
			return false;
		}
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.setex(key, expireSecond, value);
			
			
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
	}
	
	public static boolean del(String key) {
		if(!isUseJedis){
			return false;
		}
		
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.del(key);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
	}

	public static boolean del(final String... keys) {
		if(!isUseJedis){
			return false;
		}
		
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			jedis.del(keys);
			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
	}
	
	public static String get(String key) {
		if(!isUseJedis){
			return null;
		}
		
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String value = jedis.get(key);
			return value;
		} catch (Exception e) {
			log.error(e.getMessage());
			return null;
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
	}
	
	public static Set<String> keys(final String pattern){
		if(!isUseJedis){
			return new HashSet<>();
		}
		
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			return jedis.keys(pattern);
		} catch (Exception e) {
			log.error(e.getMessage());
			return new HashSet<>();
		} finally {
			if (jedis!=null) {
				jedis.close();
			}
		}
	}
}
