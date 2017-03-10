package com.cmbc.util.memkv.monitor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.cmbc.util.memkv.CacheObject;
import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.MemKVManager;
import com.cmbc.util.memkv.event.MemkvEventDispatcher;

public class MemkvMonitor implements MemkvMonitorMBean {

	@Override
	public String listMemkv() {
		// TODO Auto-generated method stub
		List<String> names =  MemKVManager.getInstance().getMemKvNames();
		String result = "";
		for(String name : names) {
			MemKV memkv = MemKVManager.getInstance().getMemKV(name);
			result = result + "Name:"+name+",Type:"+memkv.getClass().getSimpleName()+"\n";
		}
		return result;
	}


	
	@Override
	public String stat(String name) {
		// TODO Auto-generated method stub
		MemKV memkv = MemKVManager.getInstance().getMemKV(name);
		if(memkv != null) {
			if(memkv instanceof DefaultMemKV) {
				return ((DefaultMemKV) memkv).stat();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public String cacheDump(String name) {
		// TODO Auto-generated method stub
		MemKV memkv = MemKVManager.getInstance().getMemKV(name);
		if(memkv != null) {
			return memkv.cacheDump();
			
		}
		return "null";
	}

	@Override
	public Object get(String name, String key) {
		// TODO Auto-generated method stub
		return MemKVManager.getInstance().getMemKV(name).get(key);
	}

	@Override
	public Object unsafe_get(String name, String key) {
		// TODO Auto-generated method stub
		return MemKVManager.getInstance().getMemKV(name).unsafe_get(key);
	}

	@Override
	public Object hget(String name, String key, String hkey) {
		// TODO Auto-generated method stub
		return MemKVManager.getInstance().getMemKV(name).hget(key,hkey);
	}

	@Override
	public Object unsafe_hget(String name, String key, String hkey) {
		// TODO Auto-generated method stub
		return MemKVManager.getInstance().getMemKV(name).unsafe_hget(key,hkey);
	}

	@Override
	public Object invalid(String name, String key) {
		// TODO Auto-generated method stub
		boolean ret = MemKVManager.getInstance().getMemKV(name).remove(key);
		if(ret) {
			return "success";
		} else {
			return "not exist";
		}
	}

	@Override
	public Object hinvalid(String name, String key, String hkey) {
		// TODO Auto-generated method stub
		boolean ret = MemKVManager.getInstance().getMemKV(name).hremove(key,hkey);
		if(ret) {
			return "success";
		} else {
			return "not exist";
		}
	}

	@Override
	public Object hinvalid(String name, String key) {
		// TODO Auto-generated method stub
		boolean ret = MemKVManager.getInstance().getMemKV(name).hremove(key);
		if(ret) {
			return "success";
		} else {
			return "not exist";
		}
	}

	@Override
	public Date expireTime(String name, String key) {
		// TODO Auto-generated method stub
		MemKV memkv = MemKVManager.getInstance().getMemKV(name);
		if(memkv == null) {
			throw new RuntimeException("memkv of "+name+" not exist");
		}
		if(memkv instanceof DefaultMemKV) {
			CacheObject co = ((DefaultMemKV)memkv).getCacheObject(key);
			if(co != null) {
				long expireTime = co.getExpireTime();
				if(expireTime == -1) {
					expireTime = Long.MAX_VALUE;
				}
				return new Date(expireTime);
			} else {
				throw new RuntimeException("value of "+key+" not exist");
			}
		} else {
			return null;
		}
	}

	@Override
	public Date expireTime(String name, String key, String hkey) {
		MemKV memkv = MemKVManager.getInstance().getMemKV(name);
		if(memkv == null) {
			throw new RuntimeException("memkv of "+name+" not exist");
		}
		if(memkv instanceof DefaultMemKV) {
			CacheObject co = ((DefaultMemKV)memkv).getCacheObject(key,hkey);
			if( co != null) {
				long expireTime = co.getExpireTime();
				if(expireTime == -1) {
					expireTime = Long.MAX_VALUE;
				}
				return new Date(expireTime);
			} else {
				throw new RuntimeException("value of "+key+" not exist");
			}
		} else {
			return null;
		}
	}

	@Override
	public String gc(String name) {
		// TODO Auto-generated method stub
		MemKV memkv = MemKVManager.getInstance().getMemKV(name);
		if(memkv == null) {
			return "memkv of "+name+" not exist";
		}
		if(memkv instanceof DefaultMemKV) {
			((DefaultMemKV)memkv).gc();
			return "success";
		} else {
			return "not DefaultMemKV";
		}
	}



	@Override
	public String addEvent(String memory, String key, String hkey, int eventType) {
		// TODO Auto-generated method stub
		MemkvEventDispatcher.addEvent(memory, key, hkey, "Default", eventType, false);
		return "success";
	}



	@Override
	public String addEvent(String memory, String key, int eventType) {
		MemkvEventDispatcher.addEvent(memory, key, null, "Default", eventType, false);
		return "success";
	}


}
