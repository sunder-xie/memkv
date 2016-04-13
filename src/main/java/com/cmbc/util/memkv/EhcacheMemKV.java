package com.cmbc.util.memkv;

import org.ehcache.Cache;

public class EhcacheMemKV implements MemKV {

	private Cache cache;
	@Override
	public boolean set(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setIfAbsent(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object get(String key) {
		// TODO Auto-generated method stub
		return null;
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
		return false;
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
		// TODO Auto-generated method stub
		return null;
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

}
