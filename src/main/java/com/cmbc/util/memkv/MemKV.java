package com.cmbc.util.memkv;

public interface MemKV {

	public boolean set(String key, Object object, long expireSeconds);
	public boolean setIfAbsent(String key,Object object,long expireSeconds);
	public Object get(String key);

	public boolean hset(String key, String hkey,Object object, long expireSeconds);
	public boolean hsetIfAbsent(String key, String hkey, Object object, long expireSeconds);
	public Object hget(String key,String hkey);

	public boolean unsafe_set(String key,Object object,long expireSeconds);
	public boolean unsafe_setIfAbsent(String key,Object object,long expireSeconds);
	
	public boolean unsafe_hset(String key,String hkey,Object object,long expireSeconds);
	public boolean unsafe_hsetIfAbsent(String key,String hkey,Object object,long expireSeconds);
	public Object unsafe_get(String key);
	public Object unsafe_hget(String key,String hkey);

	public boolean remove(String key);
	public boolean hremove(String key);
	public boolean hremove(String key,String hkey);
	
	
	public String cacheDump();
}
