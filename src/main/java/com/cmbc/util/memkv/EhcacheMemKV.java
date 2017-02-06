package com.cmbc.util.memkv;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.exceptions.CacheWritingException;

public class EhcacheMemKV implements MemKV {

	private Cache<String,CacheObject> cache;
	public EhcacheMemKV(String name) {
		CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
		          .withCache(name,
		               CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, CacheObject.class)
		               .build())
		          .build(true);
			
		     cache = cacheManager.getCache(name, String.class, CacheObject.class);
	}
	
	@Override
	public boolean set(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		
		long currentTime = System.currentTimeMillis();
		long expireTime = (expireSeconds <= 0) ? -1 : currentTime+expireSeconds*1000;
		CacheObject co = new CacheObject(object,expireTime);
		
		try {
			cache.put(key, co);
		} catch(CacheWritingException e) {
			return false;
		}
		return true;
	}

	@Override
	public boolean setIfAbsent(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		long expireTime = (expireSeconds <= 0) ? -1 : currentTime+expireSeconds*1000;
		CacheObject co = new CacheObject(object,expireTime);
		cache.put(key, co);
		return true;
	}

	@Override
	public Object get(String key) {
		// TODO Auto-generated method stub
		CacheObject co = cache.get(key);
		if(co == null) {
			return null;
		}
		long currentTime = System.currentTimeMillis();
		if(currentTime >  co.getExpireTime() && co.getExpireTime() != -1) {
			return null;
		}
		//co.setLastAccessTime(currentTime);
		return co.getValue();
	}

	@Override
	public boolean hset(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hsetIfAbsent(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object hget(String key, String hkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean unsafe_set(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		long expireTime = (expireSeconds <= 0) ? -1 : currentTime+expireSeconds*1000;
		CacheObject co = new CacheObject(object,expireTime);
		cache.put(key, co);
		return true;
	}

	@Override
	public boolean unsafe_setIfAbsent(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unsafe_hset(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unsafe_hsetIfAbsent(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object unsafe_get(String key) {
		CacheObject co = cache.get(key);
		if(co == null) {
			return null;
		}
		long currentTime = System.currentTimeMillis();
		if(currentTime >  co.getExpireTime() && co.getExpireTime() != -1) {
			return null;
		}
		//co.setLastAccessTime(currentTime);
		return co.getValue();
	}

	@Override
	public Object unsafe_hget(String key, String hkey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean remove(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hremove(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hremove(String key, String hkey) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String cacheDump() {
		return null;
		// TODO Auto-generated method stub

	}



	@Override
	public boolean containsKey(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hcontainsKey(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hcontainsKey(String key, String hkey) {
		// TODO Auto-generated method stub
		return false;
	}

}
