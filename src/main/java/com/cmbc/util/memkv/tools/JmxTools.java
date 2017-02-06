package com.cmbc.util.memkv.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.server.Operation;
import java.util.Scanner;
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
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

public class JmxTools {



	public static void main(String[] args) throws IOException, MalformedObjectNameException, InstanceNotFoundException, IntrospectionException, ReflectionException {
		
		
		
		Scanner scanner = new Scanner(System.in);
		System.out.println("input jvm ip:");
		String ip = scanner.nextLine();
		System.out.println("input jmx port");
		String port = scanner.nextLine();
		String url = "service:jmx:rmi:///jndi/rmi://" + ip + ":" + port + "/jmxrmi";
		
		JMXServiceURL jmxUrl = new JMXServiceURL(url);
		JMXConnector jmxc = JMXConnectorFactory.connect(jmxUrl);
		MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
	
		Set<ObjectName> beanSet = mbsc.queryNames(null, null);
		 
		System.out.println("mbeans in domain com.cmbc.util.memkv.monitor");
		for(ObjectName name : beanSet) {
			if(name.toString().contains("com.cmbc.util.memkv.monitor")) {
				
				System.out.println(name);
			}
		}
		System.out.println("input choice: 1.display mbean info 2.invoke mbean operation");
		
		while(true) {
			String choice = scanner.nextLine();
			if(choice.equals("1")) {
				System.out.println("input mbean full name");
				String name = scanner.nextLine();
				getMBeanInfo(mbsc, name);
			} else {
				System.out.println("input mbean full name");
				String mbeanName = scanner.nextLine();
				System.out.println("input method name");
				String methodName = scanner.nextLine();
				invokeMethod(mbsc, mbeanName, methodName, scanner);
				
			}
			System.out.println("input choice: 1.display mbean info 2.invoke mbean operation");
		}
		

	}
	static void getMBeanInfo(MBeanServerConnection mbsc, String name) {
		MBeanInfo mbean = null;
		try {
			mbean = mbsc.getMBeanInfo(new ObjectName("com.cmbc.util.memkv.monitor:type=app20160714103143520"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		for(MBeanOperationInfo oper : mbean.getOperations()) {
			System.out.println(oper);
		}
	}
	static void invokeMethod(MBeanServerConnection mbsc,String mbeanName, String method,Scanner scanner) {
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
		int count = oper.getSignature().length;
		System.out.println("please input " + oper.getSignature().length + " parameters");
		Object[] params = new Object[count];
	
		for(int i = 0; i < count; i++) {
			params[i] = scanner.nextLine();
		}
		String[] sign = new String[count];
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
