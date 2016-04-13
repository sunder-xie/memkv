package com.cmbc.util.memkv.test;

import java.io.DataOutput;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.WatchedEvent;

import com.cmbc.util.memkv.client.ZKNotificationClient;

public class ZKClient {

	public static void main(String[] args) throws Exception {
		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString("172.16.59.129:2181")
				.retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
				.namespace("memkv").build();
		client.start();
		
		try {
			client.create().forPath("/cmd");
		} catch(Exception e) {
			if(e instanceof NodeExistsException) {
				System.out.println("jjj");
			}
			System.out.println(e.getClass().getName());
		}
		client.setData().forPath("/cmd","xxx%%invalid%%3".getBytes());
		//client.delete().forPath("/broadcast");
//		client.getData().usingWatcher(new CuratorWatcher() {
//			
//			@Override
//			public void process(WatchedEvent event) throws Exception {
//				System.out.println(event.toString());
//			}
//		}).inBackground().forPath("/broadcast");
		ZKNotificationClient client1 = new ZKNotificationClient("172.16.59.129", 2181, "");
		client1.invalid("xxxx", "1");
		//Thread.currentThread().sleep(10);
		client1.invalid("xxx", "2");
		//client1.hInvalid("wocao", "5","1");
	}
}
