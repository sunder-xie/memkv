package com.cmbc.util.memkv.listener;

import com.cmbc.util.memkv.MemKV;

public interface NotificationListener {
	public void waitForNotification();
	public void invalid(MemKV memkv,String key);
	public void hInvalid(MemKV memkv,String key);
	public void hInvalid(MemKV memkv,String key,String hkey);
	public void cacheDump(MemKV memkv);
}
