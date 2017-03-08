package com.cmbc.util.memkv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbc.util.memkv.event.MemkvEventDispatcher;
import com.cmbc.util.memkv.event.MemkvEventHandler;

public class MemKVManager {

	private static Logger logger = LoggerFactory.getLogger(MemKVManager.class);
	private ConcurrentHashMap<String,MemKV> managerMap;
	public MemKVManager() {
		managerMap = new ConcurrentHashMap<String, MemKV>();
	}
	private static MemKVManager instance; 
	static {
		instance = new MemKVManager();
//        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();   
//        
//        // 新建MBean ObjectName, 在MBeanServer里标识注册的MBean   
//        ObjectName name = null;
//       
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
//        String type = "MemkvMonitor"+sdf.format(new Date());
//		try {
//			name = new ObjectName("com.cmbc.util.memkv.monitor:type="+type);
//		} catch (MalformedObjectNameException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error(e.getMessage());
//		}   
//        // 创建MBean   
//        MemkvMonitor mbean = null;
//        try {
//        	mbean = new MemkvMonitor();          
//        } catch(Exception e) {
//        	logger.error(e.getMessage());
//        }
//        // 在MBeanServer里注册MBean, 标识为ObjectName(com.tenpay.jmx:type=Echo)   
//        try {
//			mbs.registerMBean(mbean, name);
//		} catch (InstanceAlreadyExistsException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error(e.getMessage());
//		} catch (MBeanRegistrationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error(e.getMessage());
//		} catch (NotCompliantMBeanException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			logger.error(e.getMessage());
//		}   
 
	}
	
	public static MemKVManager getInstance() {
		return instance;
	}
	public static void destroy() {
		MemKVManager ins = MemKVManager.getInstance();
		for(String name : ins.managerMap.keySet()) {
			MemKV mkv = ins.managerMap.get(name);
			if(mkv instanceof DefaultMemKV) {
				((DefaultMemKV)mkv).destroy();
			}
		}
		MemkvEventDispatcher.destroy();
	}
	public boolean register(String name,MemKV memkv) {
		
		MemKV value =  managerMap.putIfAbsent(name, memkv);
		if(value == null) {
			return false;
		}
		return true;
	}
	public void unregister(String name,MemKV memkv) {
		managerMap.remove(name);
	}
	public MemKV getMemKV(String name) {
		return managerMap.get(name);
	}
	public boolean registered(String name) {
		return managerMap.get(name) != null;
	}
	public List<String> getMemKvNames() {
		List<String> names = new ArrayList<String>();
		for(String name : managerMap.keySet()) {
			names.add(name);
		}
		return names;
	}
	/**
	 * 添加一个事件的handler
	 * @param memkvName
	 * @param key
	 * @param hkey
	 * @param handler
	 * @param eventType
	 * @return
	 */
	public static boolean addHandler(String memkvName,String key,String hkey, MemkvEventHandler handler,int eventType) {
		return MemkvEventDispatcher.addHandler(memkvName, key, hkey, handler, eventType);
	}
	/**
	 * 添加一个事件
	 * @param memkvName
	 * @param key
	 * @param hkey
	 * @param type
	 * @return
	 */
	public static boolean addEvent(String memkvName,String key,String hkey,int type) {
		return MemkvEventDispatcher.addEvent(memkvName, key, hkey, "Default", type,true);
	}
}
