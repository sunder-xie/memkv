package com.cmbc.util.memkv.monitor;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmxMonitor {

	private String appName;
	private static Logger logger = LoggerFactory.getLogger(JmxMonitor.class);
	public JmxMonitor(String appName) {
		// TODO Auto-generated constructor stub
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String rand = sdf.format(new Date());
		this.appName = appName+rand;
	}

	public JmxMonitor() {
		
	}
	public void init() {
		if(appName == null) {
			appName = String.valueOf(System.currentTimeMillis());
		}
		
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();   
        
        // 新建MBean ObjectName, 在MBeanServer里标识注册的MBean   
        ObjectName name = null;
       
		try {
			name = new ObjectName("com.cmbc.util.memkv.monitor:type="+appName);
		} catch (MalformedObjectNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		}   
        // 创建MBean   
        MemkvMonitor mbean = null;
        try {
        	mbean = new MemkvMonitor();          
        } catch(Exception e) {
        	logger.error(e.getMessage());
        }
        // 在MBeanServer里注册MBean, 标识为ObjectName(com.tenpay.jmx:type=Echo)   
        try {
			mbs.registerMBean(mbean, name);
		} catch (InstanceAlreadyExistsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (MBeanRegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (NotCompliantMBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		}   
	}
	public void destroy() {
	       MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();   
	        
	        // 新建MBean ObjectName, 在MBeanServer里标识注册的MBean   
	        ObjectName name = null;
	       
			try {
				name = new ObjectName("com.cmbc.util.memkv.monitor:type="+appName);
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
			}   
			
			try {
				mbs.unregisterMBean(name);
			} catch (MBeanRegistrationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
			} catch (InstanceNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
			}

	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String rand = sdf.format(new Date());
		this.appName = appName+rand;
	}
}
