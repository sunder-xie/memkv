package com.cmbc.util.memkv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.cmbc.util.memkv.serialize.DefaultSerializeUtil;
import com.cmbc.util.memkv.serialize.ISerialize;

/**
 * MemKV的默认实现:轻量级的内存缓存
 * @author niuxinli
 *
 */
public class MemkvOffheap implements MemKV {
	
	private static Logger logger = Logger.getLogger(MemkvOffheap.class);
	private ISerialize serializeUtil;
	static {
		System.load("/Users/niuxinli/cworkspace/memkv/memkv/memkv/memkv.dylib");
	}
	static private long GC_PERIOD = 1800; //回收线程启动间隔
	final private ConcurrentHashMap<String,Object> cacheMap = new ConcurrentHashMap<String,Object>(1000);	
	private String cacheDumpDir = "."; //cacheDump默认存放目录
	private String name;
	public MemkvOffheap() {
		name = "randomName"+System.nanoTime();
		MemKVManager.getInstance().register(name, this);
		serializeUtil = new DefaultSerializeUtil(); //如果不指定序列化工具，使用默认的Java序列化工具
		ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		GC_Thread gcThread = new GC_Thread(this);
		executor.scheduleWithFixedDelay(gcThread, GC_PERIOD, GC_PERIOD, TimeUnit.SECONDS);	
	}

	public MemkvOffheap(String name) {
		serializeUtil = new DefaultSerializeUtil();
		this.name = name;
		MemKVManager.getInstance().register(name, this);
	}
	public MemkvOffheap(String name,boolean enableGC) {
		serializeUtil = new DefaultSerializeUtil();
		this.name = name;
		MemKVManager.getInstance().register(name, this);
		if(enableGC) {
			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
			GC_Thread gcThread = new GC_Thread(this);
			executor.scheduleWithFixedDelay(gcThread, GC_PERIOD, GC_PERIOD, TimeUnit.SECONDS);	

		}
	}
	public MemkvOffheap(String name,boolean enableGC,int gc_period) {
		serializeUtil = new DefaultSerializeUtil();
		this.name = name;
		MemKVManager.getInstance().register(name, this);
		if(enableGC) {
			ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
			GC_Thread gcThread = new GC_Thread(this);
			executor.scheduleWithFixedDelay(gcThread, gc_period, gc_period, TimeUnit.SECONDS);	

		}
	}


	/**
	 * 如果该key不存在则set，存在就覆盖，expireSeconds是超时时间，单位为s，-1表示不超时
	 */
	@Override
	public boolean set(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		byte[] bytes = serializeUtil.serialize(object);
		long currentTime = System.currentTimeMillis();
		long expireTime = (expireSeconds <= 0) ? -1 : currentTime+expireSeconds*1000;
		CacheObject co = new CacheObject(bytes,expireTime);
		cacheMap.put(key, co);
		return true;
	}

	/**
	 * 如果该key不存在或者超时则set并返回true，否则返回false，expireSeconds是超时时间，单位为s，-1表示不超时
	 */
	@Override
	public boolean setIfAbsent(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		long expireTime = (expireSeconds <= 0) ? -1 : currentTime+expireSeconds*1000;
		CacheObject co = (CacheObject) cacheMap.get(key);
		if(co == null || (co.getExpireTime() != -1 && co.getExpireTime() < currentTime)) {
			synchronized (this) {
				co = (CacheObject) cacheMap.get(key);
				if(co == null || co.getExpireTime() < currentTime) {
					byte[] bytes = serializeUtil.serialize(object);
					co = new CacheObject(bytes,expireTime);
					cacheMap.put(key, co);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Object get(String key) {
		// TODO Auto-generated method stub
		CacheObject co = (CacheObject) cacheMap.get(key);
		if(co == null) {
			return null;
		}
		long currentTime = System.currentTimeMillis();
		if(currentTime >  co.getExpireTime() && co.getExpireTime() != -1) {
			return null;
		}
		//co.setLastAccessTime(currentTime);
		return serializeUtil.unserialize((byte[]) co.getValue());

	}

	@Override
	public boolean hset(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		long expireTime = expireSeconds <0 ? -1 : currentTime + expireSeconds*1000;
		Map<String,CacheObject> map = (Map) cacheMap.get(key);
		if(map == null) {
			synchronized (this) {
				map = (Map<String, CacheObject>) cacheMap.get(key);
				if(map == null) {
					map = new ConcurrentHashMap<String,CacheObject>();
					cacheMap.put(key, map);
				}
			}
		}
		byte[] bytes = serializeUtil.serialize(object);
		CacheObject co = new CacheObject(bytes, expireTime);
		map.put(hkey, co);
		return true;

	}

	@Override
	public boolean hsetIfAbsent(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		long expireTime = expireSeconds <0 ? -1 : currentTime + expireSeconds*1000;
		Map<String,CacheObject> map = (Map) cacheMap.get(key);
		if(map == null) {
			synchronized (this) {
				map = (Map<String, CacheObject>) cacheMap.get(key);
				if(map == null) {
					map = new ConcurrentHashMap<String,CacheObject>();
					cacheMap.put(key, map);
				}
			}
		}
		CacheObject co = map.get(hkey);
		if(co == null || co.getExpireTime() < System.currentTimeMillis()) {
			synchronized (this) {
				co = map.get(hkey);
				if(co == null || (co.getExpireTime() != -1 && co.getExpireTime() < currentTime)) {
					byte[] bytes = serializeUtil.serialize(object);
					co = new CacheObject(bytes,expireTime);
					map.put(hkey, co);
					return true;
				}
			}
		}
		return false;

	}

	@Override
	public Object hget(String key, String hkey) {
		// TODO Auto-generated method stub
		Object o = cacheMap.get(key);
		if(o == null) {
			return null;
		}
		if(! (o instanceof Map)) {
			throw new RuntimeException();
		}
		Map map = (Map)o;
		
		CacheObject co = (CacheObject) map.get(hkey);
		if(co == null) {
			return null;
		}
		if(co.getExpireTime() != -1 && co.getExpireTime() < System.currentTimeMillis()) {
			return null;
		}
		return serializeUtil.unserialize((byte[]) co.getValue());
		
	}

	@Override
	public boolean unsafe_set(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		long expireTime = (expireSeconds <= 0) ? -1 : currentTime+expireSeconds*1000;
		CacheObject co = new CacheObject(object,expireTime);
		cacheMap.put(key, co);
		return true;
	}

	@Override
	public boolean unsafe_hset(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		long expireTime = expireSeconds <0 ? -1 : currentTime + expireSeconds*1000;
		Map<String,CacheObject> map = (Map) cacheMap.get(key);
		if(map == null) {
			synchronized (this) {
				map = (Map<String, CacheObject>) cacheMap.get(key);
				if(map == null) {
					map = new ConcurrentHashMap<String,CacheObject>();
					cacheMap.put(key, map);
				}
			}
		}
		CacheObject co = new CacheObject(object, expireTime);
		map.put(hkey, co);
		return true;
	}

	@Override
	public Object unsafe_get(String key) {
		// TODO Auto-generated method stub
		CacheObject co = (CacheObject) cacheMap.get(key);
		if(co == null) {
			return null;
		}
		long currentTime = System.currentTimeMillis();
		if(currentTime >  co.getExpireTime() && co.getExpireTime() != -1) {
			return null;
		}
		co.setLastAccessTime(currentTime);
		return co.getValue();
	}

	@Override
	public boolean remove(String key) {
		// TODO Auto-generated method stub
		return cacheMap.remove(key)==null? false:true;
	}

	@Override
	public boolean hremove(String key, String hkey) {
		// TODO Auto-generated method stub
		Map map = (Map) cacheMap.get(key);
		if(map == null) {
			return false;
		}
		return map.remove(hkey)==null? false:true;
		
	}

	
	public Set getHKeys(String key) {
		Map map = (Map) cacheMap.get(key);
		if(map == null) {
			return null;
		}
		return map.keySet();
	}
	public int getHlen(String key) {
		Map map = (Map) cacheMap.get(key);
		if(map == null) {
			return 0;
		}
		return map.size();
	}
	public Set getKeys() {
		return cacheMap.keySet();
	}
	public int getSize() {
		return cacheMap.size();
	}
	
	@Override
	public String cacheDump() {
		File dumpFile = new File(cacheDumpDir+"/"+"memkvDump"+System.currentTimeMillis());
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(dumpFile));
			StringBuilder sb = new StringBuilder();
			for(Object key : cacheMap.keySet()) {
				Object cacheObject = cacheMap.get(key);
				
				if(cacheObject instanceof CacheObject) {
					sb.append(key);
					sb.append(":");
					Object value = ((CacheObject) cacheObject).getValue();
					if(value instanceof byte[]) {
						sb.append(((byte[])value).length);
					} else {
						sb.append("ref");
					}
					sb.append(":");
					long expireTime = ((CacheObject)cacheObject).getExpireTime();
					if(expireTime > System.currentTimeMillis() || expireTime == -1) {
						sb.append("1");
					} else {
						sb.append("0");
					}
					sb.append("\n");
					bw.write(sb.toString());
					sb.setLength(0);				
				} else if(cacheObject instanceof Map) {
					
					Map hMap = (Map) cacheObject;
					for(Object hKey : hMap.keySet()) {
						sb.append(key);
						sb.append("::");
						sb.append(hKey);
						sb.append(":");
						Object hObject = hMap.get(hKey);
						if(hObject instanceof CacheObject) {
							Object hValue = ((CacheObject) hObject).getValue();
							if(hValue instanceof byte[]) {
								sb.append(((byte[]) hValue).length);
							} else {
								sb.append("ref");
							}
							sb.append(":");
							long expireTime = ((CacheObject) hObject).getExpireTime();
							if(expireTime > System.currentTimeMillis() || expireTime == -1) {
								sb.append("1");
							} else {
								sb.append("0");
							}
							sb.append("\n");
						}
						bw.write(sb.toString());
						sb.setLength(0);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "cache dump failed";
		} finally {
			if(bw != null) {
				try {
					bw.flush();
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return dumpFile.getAbsolutePath();
		
		
	}
	public String getCacheDumpDir() {
		return cacheDumpDir;
	}
	public void setCacheDumpDir(String cacheDumpDir) {
		this.cacheDumpDir = cacheDumpDir;
	}
	
	
	/****************缓存清理相关************************/
	
	/**
	 * 定时线程，清理过期的缓存
	 * @author niuxinli
	 *
	 */
	class GC_Thread implements Runnable {
		MemkvOffheap memkv;
		public GC_Thread(MemkvOffheap memkv) {
			this.memkv = memkv;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println((new Timestamp(System.currentTimeMillis()).toString() + " gc start..(size="+memkv.cacheMap.size()));
			if(cacheMap.size() > 0) {
				memkv.removeExpiredObject(memkv.cacheMap);
			}
			//memkv.CacheDump();
			System.out.println((new Timestamp(System.currentTimeMillis()).toString() + " gc end..(size="+memkv.cacheMap.size()));
		}		
	}

	/**
	 * 回收过期的缓存
	 *
	 */
	public void gc() {
		removeExpiredObject(cacheMap);
	}
	private void removeExpiredObject(ConcurrentHashMap cacheMap) {
		logger.info("DefaultMemKV start gc...");
		long start = System.currentTimeMillis();
		for(Object key : cacheMap.keySet()) {
			Object value = cacheMap.get(key);
			if(value instanceof CacheObject) {
				long expireTime = ((CacheObject) value).getExpireTime();
				if(expireTime != -1 && expireTime < System.currentTimeMillis()) {
					logger.info("gc: remove key " + key);;
					cacheMap.remove(key);
				}
			} else if(value instanceof ConcurrentHashMap) {
				ConcurrentHashMap map_value = (ConcurrentHashMap)value;
				for(Object hkey : map_value.keySet()) {
					CacheObject co = (CacheObject) map_value.get(hkey);
					long expireTime = co.getExpireTime();
					if(expireTime != -1 && expireTime < System.currentTimeMillis()) {
						map_value.remove(hkey);
						logger.info("gc: remove hkey " + key + ":" + hkey);;
					}
				}
				if(map_value.size() == 0) {
					cacheMap.remove(key);
				}
				
				
			}
		}
		logger.info("DefaultMemKV end gc...,consume " + (System.currentTimeMillis()-start) + "ms");
	}
	/**
	 * 检查一下当前有没有必要回收内存
	 * @return
	 */
	private boolean checkGC() {
		//检查当前缓存个数，包含过期的和未过期的
		int size = cacheMap.size();
		//统计占用的空间大小		
		//获取系统的内存大小
		return false;
	}	
	/**************缓存清理 end***************************/

	/**
	 * 使一个hash全部实效，如果存在则返回true，不存在返回false
	 * @param key
	 */
	@Override
	public boolean hremove(String key) {
		// TODO Auto-generated method stub
		return cacheMap.remove(key)==null?false:true;
	}

	/**
	 * 放入一个未序列化的value，expireSeconds是超时秒数，-1表示不超时，如果已经存在并且未超时，则返回null，否则放入缓存中
	 * @param key
	 * @param object
	 * @param expireSeconds
	 *
	 */
	@Override
	public boolean unsafe_setIfAbsent(String key, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		long expireTime = (expireSeconds <= 0) ? -1 : currentTime+expireSeconds*1000;
		CacheObject co = (CacheObject) cacheMap.get(key);
		if(co == null || (co.getExpireTime() != -1 && co.getExpireTime() < currentTime)) {
			synchronized (this) {
				co = (CacheObject) cacheMap.get(key);
				if(co == null || co.getExpireTime() < currentTime) {
					co = new CacheObject(object,expireTime);
					cacheMap.put(key, co);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 放入一个未序列化的hash value，expireSeconds是超时秒数，-1表示不超时，如果已经存在并且未超时，则返回null，否则放入缓存中
	 * @param key
	 * @param hkey
	 * @param object
	 * @param expireSeconds
	 */
	@Override
	public boolean unsafe_hsetIfAbsent(String key, String hkey, Object object, long expireSeconds) {
		// TODO Auto-generated method stub
		long currentTime = System.currentTimeMillis();
		long expireTime = expireSeconds <0 ? -1 : currentTime + expireSeconds*1000;
		Map<String,CacheObject> map = (Map) cacheMap.get(key);
		if(map == null) {
			synchronized (this) {
				map = (Map<String, CacheObject>) cacheMap.get(key);
				if(map == null) {
					map = new ConcurrentHashMap<String,CacheObject>();
					cacheMap.put(key, map);
				}
			}
		}
		CacheObject co = map.get(hkey);
		if(co == null || co.getExpireTime() < System.currentTimeMillis()) {
			synchronized (this) {
				co = map.get(hkey);
				if(co == null || (co.getExpireTime() < currentTime && co.getExpireTime() != -1)) {
					
					co = new CacheObject(object,expireTime);
					map.put(hkey, co);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * 获取一个未序列化的hash value
	 * @param key
	 * @param hkey
	 */
	@Override
	public Object unsafe_hget(String key,String hkey) {
		// TODO Auto-generated method stub
		Object o = cacheMap.get(key);
		if(o == null) {
			return null;
		}
		if(! (o instanceof Map)) {
			throw new RuntimeException();
		}
		Map map = (Map)o;
		
		CacheObject co = (CacheObject) map.get(hkey);
		if(co == null) {
			return null;
		}
		if(co.getExpireTime() != -1 && co.getExpireTime() < System.currentTimeMillis()) {
			return null;
		}
		return co.getValue();
		
	}

	public ISerialize getSerializeUtil() {
		return serializeUtil;
	}

	public void setSerializeUtil(ISerialize serializeUtil) {
		this.serializeUtil = serializeUtil;
	}



	public String getName() {
		return name;
	}



	public void setName(String name) {
		this.name = name;
	}
	
	/**提供给Monitor使用**/
	public CacheObject getCacheObject(String key) {
		Object object = cacheMap.get(key);
		if(object == null) {
			return null;
		}
		if(object instanceof CacheObject) {
			return (CacheObject) object;
		} else if(object instanceof ConcurrentHashMap) {
			throw new RuntimeException("hash set");
		} else {
			throw new RuntimeException("unknown type");
		}
	}
	public CacheObject getCacheObject(String key,String hkey) {
		Object object = cacheMap.get(key);
		if(object == null) {
			return null;
		}
		if(object instanceof CacheObject) {
			throw new RuntimeException("not hash set");
		} else if(object instanceof ConcurrentHashMap) {
			CacheObject co = (CacheObject) ((Map)object).get(hkey);
			return co;
		} else {
			throw new RuntimeException("unknown type");
		}
	}

	public String stat() {

		int serializedCount = 0;
		int refCount = 0;
		int serializedBytes = 0;
		for (Object key : cacheMap.keySet()) {
			Object cacheObject = cacheMap.get(key);
			if (cacheObject instanceof CacheObject) {
				Object value = ((CacheObject) cacheObject).getValue();
				if (value instanceof byte[]) {
					serializedCount++;
					serializedBytes += ((byte[]) value).length;
				} else {
					refCount++;
				}
			} else if (cacheObject instanceof Map) {

				Map hMap = (Map) cacheObject;
				for (Object hKey : hMap.keySet()) {

					Object hObject = hMap.get(hKey);
					if (hObject instanceof CacheObject) {
						Object hValue = ((CacheObject) hObject).getValue();
						if (hValue instanceof byte[]) {
							serializedCount++;
							serializedBytes += ((byte[]) hValue).length;
						} else {
							refCount++;
						}

					}
				}
			}
		}
		return "serializedObjectCount:" + serializedCount + ",serializedObjectBytes:" + serializedBytes
				+ ",refObjectCount:" + refCount;

	}

	public boolean offheap_set(String key,Object object,long expireSeconds){
		byte[] bytes = serializeUtil.serialize(object);
		return offheap_set_native(key.getBytes(), bytes, expireSeconds);
	}
	public Object offheap_get(String key){
		byte[] bytes = offheap_get_native(key.getBytes());
		if(bytes == null) {
			return bytes;
		}
		return serializeUtil.unserialize(bytes);
	}
	public boolean offheap_remove(String key) {
		return offheap_remove_native(key.getBytes());
	}
	public boolean offheap_hset(String key,String hkey,Object object,long expireSeconds) {
		if(key == null || hkey == null || object == null) {
			return false;
		}
		byte[] bytes = serializeUtil.serialize(object);
		return offheap_hset_native(key.getBytes(), hkey.getBytes(),bytes, expireSeconds);
	}
	public Object offheap_hget(String key,String hkey) {
		return null;
	}
//	private native boolean offheap_set_native(String key,byte[] bytes,long expireSeconds);
//	private native byte[] offheap_get_native(String key);
//	private native boolean offheap_remove_native(String key);
	private native boolean offheap_set_native(byte[] key,byte[] value,long expireSeconds);
	private native byte[] offheap_get_native(byte[] key);
	private native boolean offheap_remove_native(byte[] key);
	private native boolean offheap_hset_native(byte[] key,byte[] hkey,byte[] value,long expireSeconds);
	private native byte[] offheap_hget_native(byte[] key,byte[] hkey);
	private native boolean offheap_hremove_native(byte[] key,byte[] hkey);
	private native boolean offheap_hremove_native(byte[] key);


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

	@Override
	public boolean set(String key, Object object, long expireSeconds, boolean allowDirty) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object get(String key, boolean allowDirty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hset(String key, String hkey, Object object, long expireSeconds, boolean allowDirty) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object hget(String key, String hkey, boolean allowDirty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean unsafe_set(String key, Object object, long expireSeconds, boolean allowDirty) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean unsafe_hset(String key, String hkey, Object object, long expireSeconds, boolean allowDirty) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object unsafe_get(String key, boolean allowDirty) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object unsafe_hget(String key, String hkey, boolean allowDirty) {
		// TODO Auto-generated method stub
		return null;
	}


	
}
