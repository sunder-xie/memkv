package com.cmbc.util.memkv.client;

public interface MemkvBroadcastClient {

	public boolean invalid(String name,String key);
	public boolean hinvalid(String name,String key);
	public boolean hinvalid(String name,String key,String hkey);
	public boolean cacheDump(String name);
	public boolean gc(String name);
	public void startPipe();
	public void abortPipe();
	public boolean commitPipe();
}
