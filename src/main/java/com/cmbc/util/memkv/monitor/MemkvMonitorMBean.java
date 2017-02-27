package com.cmbc.util.memkv.monitor;

import java.util.Date;

public interface MemkvMonitorMBean {

	public String listMemkv();
	public String stat(String name);
	public String cacheDump(String name);
	public String gc(String name);
	public Object get(String name,String key);
	public Object unsafe_get(String name,String key);
	public Object hget(String name,String key,String hkey);
	public Object unsafe_hget(String name,String key,String hkey);
	public Object invalid(String name,String key);
	public Object hinvalid(String name,String key,String hkey);
	public Object hinvalid(String name,String key);
	public Date expireTime(String name,String key);
	public Date expireTime(String name,String key,String hkey);
	public String addEvent(String memory,String key, String hkey, int eventType);
}
