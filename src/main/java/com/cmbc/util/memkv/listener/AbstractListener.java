package com.cmbc.util.memkv.listener;

import com.cmbc.util.memkv.MemKV;

public abstract class AbstractListener implements NotificationListener {

	@Override
	public void invalid(MemKV memkv, String key) {
		// TODO Auto-generated method stub
		memkv.remove(key);
	}

	@Override
	public void hInvalid(MemKV memkv, String key) {
		// TODO Auto-generated method stub
		memkv.hremove(key);
	}

	@Override
	public void hInvalid(MemKV memkv, String key, String hkey) {
		// TODO Auto-generated method stub
		memkv.hremove(key, hkey);
	}

	@Override
	public void cacheDump(MemKV memkv) {
		// TODO Auto-generated method stub
		memkv.cacheDump();
	}

	@Override
	abstract public void waitForNotification();

}
