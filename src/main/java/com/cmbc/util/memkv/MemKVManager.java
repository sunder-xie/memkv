package com.cmbc.util.memkv;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
}
