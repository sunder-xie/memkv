package com.cmbc.util.memkv;

import java.io.Serializable;

import com.cmbc.util.memkv.serialize.DefaultSerializeUtil;
import com.cmbc.util.memkv.serialize.ISerialize;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisMemKV implements MemKV {

	private JedisPool jedisPool;
	private JedisPoolConfig jedisPoolConfig;
	private ISerialize serializeUtil;
	private String host;
	private int port = 6379;
	private int minConn = 100;
	private int maxConn = 100;
	private String name;
	private boolean inited = false;
	public RedisMemKV() {
		serializeUtil = new DefaultSerializeUtil();
	}
	public RedisMemKV(String name,String host,int port,int minConn,int maxConn) {
		serializeUtil = new DefaultSerializeUtil();
		this.host = host;
		this.port = port;
		this.minConn = minConn;
		this.maxConn = maxConn;
		init();
		this.setName(name);
		MemKVManager.getInstance().register(name, this);
	}
	public RedisMemKV(String host,int port,int minConn,int maxConn) {
		serializeUtil = new DefaultSerializeUtil();
		this.host = host;
		this.port = port;
		this.minConn = minConn;
		this.maxConn = maxConn;
		init();
	}
	public void init() {
		if(inited == false) {
			synchronized (this) {
				if(inited == false) {
					jedisPoolConfig = new JedisPoolConfig();
					jedisPoolConfig.setMaxTotal(maxConn);
					jedisPoolConfig.setMinIdle(minConn);
					jedisPool = new JedisPool(jedisPoolConfig, host, port);
					inited = true;
				}
			}
		}
		
	}
	private Jedis getConnection() {
		return jedisPool.getResource();
	}

	@Override
	public boolean set(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		Jedis jedis = getConnection();
		byte[] value = serializeUtil.serialize(object);
		if(expireSeconds != -1) {
			jedis.setex(key.getBytes(), (int) expireSeconds, value);
		} else {
			jedis.set(key.getBytes(), value);
		}
		jedis.close();
		return true;
	}

	@Override
	public boolean setIfAbsent(String key, Object object, long expireSeconds) {
		return set(key,object,expireSeconds);
	}

	@Override
	public Object get(String key) {
		// TODO Auto-generated method stub
		Jedis jedis = getConnection();
		byte[] bytes = jedis.get(key.getBytes());
		if(bytes == null || bytes.length == 0) {
			return null;
		}
		return serializeUtil.unserialize(bytes);
	}

	@Override
	public boolean hset(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		Jedis jedis = getConnection();
		long currentTime = System.currentTimeMillis();
		long expireTime = expireSeconds <0 ? -1 : currentTime + expireSeconds*1000;
		
		CacheObject co = new CacheObject(object, expireTime);
		byte[] bytes = serializeUtil.serialize(co);
		jedis.hset(key.getBytes(), hkey.getBytes(), bytes);
		jedis.close();
		return true;

	}

	@Override
	public boolean hsetIfAbsent(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return hset(key,hkey,object,expireSeconds);
	}

	@Override
	public Object hget(String key, String hkey) {
		Jedis jedis = getConnection();
		long currentTime = System.currentTimeMillis();
		byte[] bytes = jedis.hget(key.getBytes(), hkey.getBytes());
		CacheObject co = (CacheObject) serializeUtil.unserialize(bytes);
		
		long expireTime = co.getExpireTime();
		if(currentTime > expireTime && expireTime != -1) {
			return null;
		}
		jedis.close();
		return co.getValue();
	}

	@Override
	public boolean unsafe_set(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return set(key,object,expireSeconds);
	}

	@Override
	public boolean unsafe_setIfAbsent(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return set(key,object,expireSeconds);
	}

	@Override
	public boolean unsafe_hset(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return hset(key,hkey,object,expireSeconds);
	}

	@Override
	public boolean unsafe_hsetIfAbsent(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return hset(key,hkey,object,expireSeconds);
	}

	@Override
	public Object unsafe_get(String key) {
		// TODO Auto-generated method stub
		return get(key);
	}

	@Override
	public Object unsafe_hget(String key, String hkey) {
		// TODO Auto-generated method stub
		return hget(key,hkey);
	}

	@Override
	public boolean remove(String key) {
		// TODO Auto-generated method stub
		Jedis jedis = getConnection();
		jedis.del(key.getBytes());
		jedis.close();
		return true;
	}

	@Override
	public boolean hremove(String key) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		Jedis jedis = getConnection();
		jedis.del(key.getBytes());
		jedis.close();
		return true;

	}

	@Override
	public boolean hremove(String key, String hkey) {
		// TODO Auto-generated method stub
		Jedis jedis = getConnection();
		jedis.hdel(key.getBytes(), hkey.getBytes());
		jedis.close();
		return true;

	}

	@Override
	public String cacheDump() {
		// TODO Auto-generated method stub
		return null;
	}

	public JedisPool getJedisPool() {
		return jedisPool;
	}

	public void setJedisPool(JedisPool jedisPool) {
		this.jedisPool = jedisPool;
	}

	public JedisPoolConfig getJedisPoolConfig() {
		return jedisPoolConfig;
	}

	public void setJedisPoolConfig(JedisPoolConfig jedisPoolConfig) {
		this.jedisPoolConfig = jedisPoolConfig;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}

