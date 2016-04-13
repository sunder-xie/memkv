package com.cmbc.util.memkv.client;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;


public class ZKNotificationClient implements NotificationClient {

	private CuratorFramework client;
	private String zkIP;
	private int zkPort;
	private String root;
	public ZKNotificationClient(String zkIP,int zkPort,String root) {
		// TODO Auto-generated constructor stub
		this.zkIP = zkIP;
		this.zkPort = zkPort;
		this.root = root;
		String namespace = "";
		if(root != null && !root.isEmpty()) {
			namespace = root+"/memkv";
		} else {
			namespace = "memkv";
		}
		client = CuratorFrameworkFactory.builder()
				.sessionTimeoutMs(10000)
				.namespace(namespace)
				.retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
				.connectString(zkIP+":"+zkPort)
				.build();
		client.start();
	}
	@Override
	public boolean invalid(String name, String key) {
		// TODO Auto-generated method stub
		try {
			client.setData().forPath("/cmd", (name+"&invalid&"+key).getBytes());
			client.sync().forPath("/cmd");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean hInvalid(String name, String key) {
		try {
			client.setData().forPath("/cmd", (name+"&hinvalid&"+key).getBytes());
			client.sync().forPath("/cmd");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean hInvalid(String name, String key, String hkey) {
		// TODO Auto-generated method stub
		try {
			client.setData().forPath("/cmd", (name+"&hinvalid&"+key+"&"+hkey).getBytes());
			client.sync().forPath("/cmd");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean cacheDump(String name) {
		// TODO Auto-generated method stub
		try {
			client.setData().forPath("/cmd", (name+"&cacheDump").getBytes());
			client.sync().forPath("/cmd");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean gc(String name) {
		// TODO Auto-generated method stub
		try {
			client.setData().forPath("/cmd", (name+"&gc").getBytes());
			client.sync().forPath("/cmd");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

}
