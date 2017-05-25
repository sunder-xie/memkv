package com.cmbc.util.memkv;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbc.util.memkv.common.NamedThreadFactory;
import com.cmbc.util.memkv.event.MemkvEventDispatcher;
import com.cmbc.util.memkv.event.MemkvEventType;
import com.cmbc.util.memkv.serialize.DefaultSerializeUtil;
import com.cmbc.util.memkv.serialize.ISerialize;

/**
 * MemKV的默认实现:轻量级的内存缓存
 * @author niuxinli
 *
 */
public class DefaultMemKV implements MemKV {
	
	private static Logger logger = LoggerFactory.getLogger(DefaultMemKV.class);
	private ISerialize serializeUtil;
	
	static private long GC_PERIOD = 1800; //回收线程启动间隔
	final private ConcurrentHashMap<String,Object> cacheMap = new ConcurrentHashMap<String,Object>(1000);	
	private String cacheDumpDir = "/var/tmp/"; //cacheDump默认存放目录
	private String name;
	ScheduledExecutorService executor;
	public void destroy() {
		if(executor != null && ! executor.isShutdown()) {
			try {
				executor.shutdownNow();
			} catch(Exception e) {
				logger.error(e.getMessage());
			}
		}
	}
	public DefaultMemKV() {
		name = "randomName"+System.nanoTime();
		MemKVManager.getInstance().register(name, this);
		serializeUtil = new DefaultSerializeUtil(); //如果不指定序列化工具，使用默认的Java序列化工具
		executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("memkv-gc-thread",true));
		GC_Thread gcThread = new GC_Thread(this);
		executor.scheduleWithFixedDelay(gcThread, GC_PERIOD, GC_PERIOD, TimeUnit.SECONDS);	
	}

	public DefaultMemKV(String name) {
		serializeUtil = new DefaultSerializeUtil();
		this.name = name;
		MemKVManager.getInstance().register(name, this);
	}
	
	public DefaultMemKV(String name,boolean enableGC) {
		serializeUtil = new DefaultSerializeUtil();
		this.name = name;
		MemKVManager.getInstance().register(name, this);
		if(enableGC) {
			executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("memkv-gc-thread"));
			GC_Thread gcThread = new GC_Thread(this);
			executor.scheduleWithFixedDelay(gcThread, GC_PERIOD, GC_PERIOD, TimeUnit.SECONDS);	
		}
	}
	public DefaultMemKV(String name,boolean enableGC,int gc_period) {
		serializeUtil = new DefaultSerializeUtil();
		this.name = name;
		MemKVManager.getInstance().register(name, this);
		if(enableGC) {
			executor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("memkv-gc-thread"));
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
		MemkvEventDispatcher.addEvent(name, key, null, "Default", MemkvEventType.ADD, true);
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
					MemkvEventDispatcher.addEvent(name, key, null, "Default", MemkvEventType.ADD, true);
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
		
		MemkvEventDispatcher.addEvent(name, key, hkey, "Default", MemkvEventType.ADD, true);
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
					MemkvEventDispatcher.addEvent(name, key, hkey, "Default", MemkvEventType.ADD, true);
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
		MemkvEventDispatcher.addEvent(name, key, null, "Default", MemkvEventType.ADD, false);
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
		MemkvEventDispatcher.addEvent(name, key, hkey, "Default", MemkvEventType.ADD, false);
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
		Object object = cacheMap.get(key);
		if(object == null) {
			MemkvEventDispatcher.addEvent(name, key, null, "Default", MemkvEventType.REMOVE, true);
			return false;
		}
		if(object instanceof Map) {
			Map<String,CacheObject> map = (Map<String,CacheObject>)object;
			for(String k : map.keySet()) {
				CacheObject co = map.get(k);
				try {
					if(!co.isDirtyFlag()) { //不允许使用脏数据
						map.remove(k);
					} else { //允许使用脏数据,可能这个还没超时，这时get还是可以拿到的，故意设成超时
						co.setExpireTime(System.currentTimeMillis()-1000);//
					}
				} catch(Exception e) {
					
					logger.error(e.getMessage());
				}
				
				MemkvEventDispatcher.addEvent(name, key, k, "Default", MemkvEventType.REMOVE, true);
			}
				
			return true;
		}
		CacheObject co = (CacheObject)object;
		if(! co.isDirtyFlag()) {		
			cacheMap.remove(key);
			MemkvEventDispatcher.addEvent(name, key, null, "Default", MemkvEventType.REMOVE, true);
		} else {
			co.setExpireTime(System.currentTimeMillis()-1000);//可能这个还没超时，故意把它弄成超时,
		}
		return true;
	}

	@Override
	public boolean hremove(String key, String hkey) {
		// TODO Auto-generated method stub
		Map map = (Map) cacheMap.get(key);
		if(map == null) {
			MemkvEventDispatcher.addEvent(name, key, hkey, "Default", MemkvEventType.REMOVE, true);
			return false;
		}
		CacheObject co = (CacheObject) map.get(hkey);
		if(!co.isDirtyFlag()) {	
			map.remove(hkey);
		} else {
			co.setExpireTime(System.currentTimeMillis()-1000);
		}
		MemkvEventDispatcher.addEvent(name, key, hkey, "Default", MemkvEventType.REMOVE, true);
		
		return true;		
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
		String tmpDir = cacheDumpDir;
		File dir = new File(cacheDumpDir);
		if( ! (dir.exists() && dir.isDirectory())) {
			tmpDir = ".";
		}
		File dumpFile = new File(tmpDir+"/"+"memkvDump"+System.currentTimeMillis());
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
					sb.append(":");
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
	
	public String cacheDumpAsString() {

		StringBuilder sb = new StringBuilder();
		int i=0;
		for (Object key : cacheMap.keySet()) {
			if(i > 500) {
				break;
			}
			Object cacheObject = cacheMap.get(key);

			if (cacheObject instanceof CacheObject) {
				sb.append(key);
				sb.append(":");
				Object value = ((CacheObject) cacheObject).getValue();
				if (value instanceof byte[]) {
					sb.append(((byte[]) value).length);
				} else {
					sb.append("ref");
				}
				sb.append(":");
				long expireTime = ((CacheObject) cacheObject).getExpireTime();
				if (expireTime > System.currentTimeMillis() || expireTime == -1) {
					sb.append("1");
				} else {
					sb.append("0");
				}
				sb.append("\n");
				i++;
			} else if (cacheObject instanceof Map) {

				Map hMap = (Map) cacheObject;
				for (Object hKey : hMap.keySet()) {
					sb.append(key);
					sb.append("::");
					sb.append(hKey);
					sb.append(":");
					Object hObject = hMap.get(hKey);
					if (hObject instanceof CacheObject) {
						Object hValue = ((CacheObject) hObject).getValue();
						if (hValue instanceof byte[]) {
							sb.append(((byte[]) hValue).length);
						} else {
							sb.append("ref");
						}
						sb.append(":");
						long expireTime = ((CacheObject) hObject).getExpireTime();
						if (expireTime > System.currentTimeMillis() || expireTime == -1) {
							sb.append("1");
						} else {
							sb.append("0");
						}
						sb.append("\n");
					}
					i++;
				}
			}
		}
		return sb.toString();
		
	}
	
	/****************缓存清理相关************************/
	
	/**
	 * 定时线程，清理过期的缓存
	 * @author niuxinli
	 *
	 */
	class GC_Thread implements Runnable {
		DefaultMemKV memkv;
		public GC_Thread(DefaultMemKV memkv) {
			this.memkv = memkv;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println((new Timestamp(System.currentTimeMillis()).toString() + " gc start..(size="+memkv.cacheMap.size())+")");
			if(cacheMap.size() > 0) {
				memkv.removeExpiredObject(memkv.cacheMap);
			}
			//memkv.CacheDump();
			System.out.println((new Timestamp(System.currentTimeMillis()).toString() + " gc end..(size="+memkv.cacheMap.size())+")");
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
		logger.info("DefaultMemKV of name "+ getName() + " start gc...");
		long start = System.currentTimeMillis();
		try {
		
		for(Object key : cacheMap.keySet()) {
			Object value = cacheMap.get(key);
			if(value instanceof CacheObject) {
				CacheObject co = (CacheObject)value;
				long expireTime = co.getExpireTime();
				if(expireTime != -1 && (expireTime + 500) < System.currentTimeMillis()) {
					//logger.info("DefaultMemKV of name "+ getName() + " gc: remove key " + key);;
					if(!co.isDirtyFlag()) {
						cacheMap.remove(key);
					}
				}
			} else if(value instanceof ConcurrentHashMap) {
				ConcurrentHashMap map_value = (ConcurrentHashMap)value;
				for(Object hkey : map_value.keySet()) {
					CacheObject co = (CacheObject) map_value.get(hkey);
					long expireTime = co.getExpireTime();
					if(expireTime != -1 && (expireTime + 500) < System.currentTimeMillis()) {
						if(!co.isDirtyFlag()) {
							map_value.remove(hkey);
						}
						//logger.info("DefaultMemKV of name "+ getName() + " gc: remove hkey " + key + ":" + hkey);;
					}
				}
				if(map_value.size() == 0) {
					cacheMap.remove(key);
				}
				
				
			}
		}
		} catch (Exception e) {
			logger.error("gc error",e);
		}
		logger.info("DefaultMemKV of name " + getName() + " end gc...,consume " + (System.currentTimeMillis()-start) + "ms");
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
		Object object = cacheMap.get(key);
		if(object == null || !(object instanceof Map)) {
			return false;
		}
		Map<String,CacheObject> map = (Map<String,CacheObject>)object;
		for(String k : map.keySet()) {
			CacheObject co = map.get(k);
			try {
				if(!co.isDirtyFlag()) {
					map.remove(k);
				} else {
					co.setExpireTime(System.currentTimeMillis()-1000);
				}
			} catch(Exception e) {
				
				logger.error(e.getMessage());
			}
			MemkvEventDispatcher.addEvent(name, key, k, "Default", MemkvEventType.REMOVE, true);
		}
		return true;
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
					MemkvEventDispatcher.addEvent(name, key, null, "Default", MemkvEventType.ADD, false);
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
					MemkvEventDispatcher.addEvent(name, key, hkey, "Default", MemkvEventType.ADD, false);
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
	
	/**
	 * 获取一个key或者key:hkey对应的CacheObject
	 * @param key
	 * @param hkey
	 * @return
	 */
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

	/**
	 * 把当前的状态返回到一个String里
	 * @return
	 */
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
		String ret =  "serializedObjectCount:" + serializedCount + ",serializedObjectBytes:" + serializedBytes
				+ ",refObjectCount:" + refCount;
		logger.info(ret);
		return ret;

	}
	@Override
	public boolean containsKey(String key) {
		// TODO Auto-generated method stub
		return cacheMap.containsKey(key);
	}
	@Override
	public boolean hcontainsKey(String key) {
		// TODO Auto-generated method stub
		return cacheMap.containsKey(key);
	}
	@Override
	public boolean hcontainsKey(String key, String hkey) {
		// TODO Auto-generated method stub
		Object map = cacheMap.get(key);
		if(map == null ||!( map instanceof Map)) {
			return false;
		}
		Map m = (Map) map;
		return m.containsKey(hkey);
	}
	@Override
	public boolean set(String key, Object object, long expireSeconds, boolean allowDirty) {
		// TODO Auto-generated method stub
		if(!allowDirty) {
			return set(key,object,expireSeconds);
		}
		byte[] bytes = serializeUtil.serialize(object);
		long currentTime = System.currentTimeMillis();
		long expireTime = (expireSeconds <= 0) ? -1 : currentTime+expireSeconds*1000;
		CacheObject co = new CacheObject(bytes,expireTime);
		co.setDirtyFlag(true);
		cacheMap.put(key, co);
		//添加新增事件
		MemkvEventDispatcher.addEvent(name, key, null, "Default", MemkvEventType.ADD, true);
		return true;
	}
	
	@Override
	public Object get(String key, boolean allowDirty) {
		// TODO Auto-generated method stub
		if(!allowDirty) {
			return get(key);
		}
		CacheObject co = (CacheObject) cacheMap.get(key);
		if(co == null) {
			return null;
		}	
		if(!co.isDirtyFlag()) {
			return get(key);
		}
		return serializeUtil.unserialize((byte[]) co.getValue());
	}
	
	@Override
	public boolean hset(String key, String hkey, Object object, long expireSeconds, boolean allowDirty) {
		// TODO Auto-generated method stub
		if(!allowDirty) {
			return hset(key,hkey,object,expireSeconds);
		}
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
		co.setDirtyFlag(true);
		map.put(hkey, co);
		//添加新增事件
		MemkvEventDispatcher.addEvent(name, key, hkey, "Default", MemkvEventType.ADD, true);
		return true;
	}
	@Override
	public Object hget(String key, String hkey, boolean allowDirty) {
		// TODO Auto-generated method stub
		if(!allowDirty) {
			return hget(key,hkey);
		}
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
		if(!co.isDirtyFlag()) {
			return hget(key,hkey);
		}
		return serializeUtil.unserialize((byte[]) co.getValue());

	}
	@Override
	public boolean unsafe_set(String key, Object object, long expireSeconds, boolean allowDirty) {
		// TODO Auto-generated method stub
		if(!allowDirty) {
			return unsafe_set(key,object,expireSeconds);
		}
		long currentTime = System.currentTimeMillis();
		long expireTime = (expireSeconds <= 0) ? -1 : currentTime+expireSeconds*1000;
		CacheObject co = new CacheObject(object,expireTime);
		co.setDirtyFlag(true);
		cacheMap.put(key, co);
		//添加新增事件
		MemkvEventDispatcher.addEvent(name, key, null, "Default", MemkvEventType.ADD, false);
		return true;
	}
	@Override
	public boolean unsafe_hset(String key, String hkey, Object object, long expireSeconds, boolean allowDirty) {
		// TODO Auto-generated method stub
		if(!allowDirty) {
			return unsafe_hset(key,hkey,object,expireSeconds);
		}
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
		co.setDirtyFlag(true);
		map.put(hkey, co);
		
		//添加新增事件
		MemkvEventDispatcher.addEvent(name, key, hkey, "Default", MemkvEventType.ADD, false);
		return true;
	}
	@Override
	public Object unsafe_get(String key, boolean allowDirty) {
		// TODO Auto-generated method stub
		if(!allowDirty) {
			return unsafe_get(key);
		} 
		CacheObject co = (CacheObject) cacheMap.get(key);
		if(co == null) {
			return null;
		}
		if(!co.isDirtyFlag()) {
			return unsafe_get(key);
		}
		long currentTime = System.currentTimeMillis();
		co.setLastAccessTime(currentTime);
//		if(currentTime >  co.getExpireTime() && co.getExpireTime() != -1) {
//			//添加访问时已过期事件
//			MemkvEventDispatcher.addEvent(name, key, null, "Default", MemkvEventType.EXPIRE_ON_ACCESS, false);
//			return co.getValue();
//		}
		return co.getValue();
		
	}
	@Override
	public Object unsafe_hget(String key, String hkey, boolean allowDirty) {
		// TODO Auto-generated method stub
		if(!allowDirty) {
			return unsafe_hget(key,hkey);
		}
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
		if(!co.isDirtyFlag()) {
			return unsafe_hget(key,hkey);
		}
//		if(co.getExpireTime() != -1 && co.getExpireTime() < System.currentTimeMillis()) {
//			//添加访问时已过期事件
//			MemkvEventDispatcher.addEvent(name, key, hkey, "Default", MemkvEventType.EXPIRE_ON_ACCESS, false);
//			return co.getValue();
//		}
		return co.getValue();
	}
	
	public void randomVictim() {
		int size = cacheMap.size();
		Random rand = new Random(System.nanoTime());
		int vic = rand.nextInt(size/2);
		//System.out.println(vic);
		int i = 0;
		for(String key : cacheMap.keySet()) {
			if(i == vic) {
				cacheMap.remove(key);
				//System.out.println(key+" removed");
				break;
			}
			i++;
		}
	}

}
