package com.cmbc.util.memkv.client;

public interface NotificationClient {

	public boolean invalid(String name,String key);
	public boolean hInvalid(String name,String key);
	public boolean hInvalid(String name,String key,String hkey);
	public boolean cacheDump(String name);
	public boolean gc(String name);
}
