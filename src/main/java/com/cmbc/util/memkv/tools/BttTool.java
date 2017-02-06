package com.cmbc.util.memkv.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Set;

import javax.management.InstanceNotFoundException;
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

public class BttTool {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		FileReader fr = new FileReader("servers.txt");
		String line = "";
		BufferedReader br = new BufferedReader(fr);
		while((line=br.readLine()) != null) {
			String url = "service:jmx:rmi:///jndi/rmi://" + line + "/jmxrmi";
			System.out.print(line+":");
			try {
				refresh(url);
			} catch(Exception e) {
				System.out.println("failed");
			}
		}
		
	}
	
	public static void refresh(String url,String serverName, String key) throws IOException {
		JMXServiceURL jmxUrl = new JMXServiceURL(url);
		JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl);
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

		Set<ObjectName> beanSet = mbsc.queryNames(null, null);
		 
		//System.out.println("mbeans in domain com.cmbc.util.memkv.monitor");
		String bttServer = "";
		for(ObjectName name : beanSet) {
			if(name.toString().contains("com.cmbc.util.memkv.monitor") && name.toString().contains(serverName)) {
				
				bttServer = name.toString();
			}
		}
		//System.out.println(url+":");
		invokeMethod(mbsc, bttServer, "invalid", "memory",key);
				
	}
	public static void refresh(String url) throws IOException {
		JMXServiceURL jmxUrl = new JMXServiceURL(url);
		JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl);
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

		Set<ObjectName> beanSet = mbsc.queryNames(null, null);
		 
		//System.out.println("mbeans in domain com.cmbc.util.memkv.monitor");
		String bttServer = "";
		for(ObjectName name : beanSet) {
			if(name.toString().contains("com.cmbc.util.memkv.monitor") && name.toString().contains("bttServer")) {
				
				bttServer = name.toString();
			}
		}
		//System.out.println(url+":");
		invokeMethod(mbsc, bttServer, "invalid", "memory","top5mer");
				
	}

	
		static void invokeMethod(MBeanServerConnection mbsc,String mbeanName, String method,String arg1,String arg2) {
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
			params[0] = arg1;
			params[1] = arg2;
		
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
			} catch (MalformedObjectNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MBeanException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(result);	
			
		}
}


