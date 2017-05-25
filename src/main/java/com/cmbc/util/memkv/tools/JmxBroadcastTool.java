package com.cmbc.util.memkv.tools;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxBroadcastTool {

	public JmxBroadcastTool() {
		// TODO Auto-generated constructor stub
	}
	
	//对一个urlList进行广播
	public static String broadcast(List<String> uriList,String appName,String memkvName,String key) {
		String result = "";
		for(String uri: uriList) {
			try {
				String r = refresh(uri,appName,memkvName,key);
				result = result + uri +"=>"+ r + "\n";
			} catch (Exception e) {
				result += result + uri + "=>" + "BBBBBBB:"+e.getMessage().substring(0,20);
			}
		}
		return result;
	}
	
	public static String refresh(String uri,String appName,String memkvName,String key) throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException {
			
		String url = "service:jmx:rmi:///jndi/rmi://" + uri + "/jmxrmi";
		
		JMXServiceURL jmxUrl = new JMXServiceURL(url);
		JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl);
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
	
		Set<ObjectName> beanSet = mbsc.queryNames(null, null);
		 
		//System.out.println("mbeans in domain com.cmbc.util.memkv.monitor");
		
		String beanName = "";
		for(ObjectName name : beanSet) {
			if(name.toString().contains("com.cmbc.util.memkv.monitor") && name.toString().contains(appName)) {
				beanName = name.toString();
				String result = invokeMethod(mbsc, beanName, "invalid", memkvName,key);
				return result;
			}
		}
		if(beanName.isEmpty()) {
			return "BBBBBBB:can't find app " + appName;
		}
		return "BBBBBBB:other error";
		//invokeMethod(mbsc, mbeanName, methodName, scanner);
				
		
		

	}

	static String invokeMethod(MBeanServerConnection mbsc,String mbeanName, String method, String memkvName,String key) {
		MBeanInfo mbean = null;
		try {
			mbean = mbsc.getMBeanInfo(new ObjectName(mbeanName));
		} catch (Exception e) {
			e.printStackTrace();
		}
		MBeanOperationInfo oper = null;
		for(MBeanOperationInfo oper1 : mbean.getOperations()) {
				if(oper1.getName().compareTo(method) == 0) {
					oper = oper1;
					break;
				}
		}
		
		Object[] params = new Object[2];
	
		params[0] = memkvName;
		params[1] = key;
		String[] sign = new String[2];
		int j = 0;
		for(MBeanParameterInfo info : oper.getSignature()) {
			sign[j++] = info.getType();
		}
		Object result = null;
		try {
			result = mbsc.invoke(new ObjectName(mbeanName), method, params,sign);
		} catch (InstanceNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "BBBBBBB";
		} catch (MalformedObjectNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "BBBBBBB";
		} catch (MBeanException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "BBBBBBB";
		} catch (ReflectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "BBBBBBB";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "BBBBBBB";
		}
		
		System.out.println(result);	
		return "AAAAAAA:"+result;
		
	}


	public JmxUrlFetcher getUrlfetcher() {
		return null;
	}


	public void setUrlfetcher(JmxUrlFetcher urlfetcher) {
	}
	
	public static void main(String[] args) {
		List<String> list = new ArrayList<String>();
		list.add("127.0.0.1:1234");
		list.add("127.0.0.1:1235");
		System.out.println(broadcast(list, "btt", "memory", "fuck"));
		
	}
}
