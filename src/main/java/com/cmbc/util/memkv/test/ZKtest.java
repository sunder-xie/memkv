package com.cmbc.util.memkv.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.WatchedEvent;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.RedisMemKV;
import com.cmbc.util.memkv.listener.MemkvZKListener;

public class ZKtest {

	public static void main(String[] args) throws Exception {
		
		MemkvZKListener listener = new MemkvZKListener("172.16.59.129", 2181, "");
		listener.start();
		MemKV memkv = new DefaultMemKV("memory1",false);
		MemKV memkv1= new DefaultMemKV("memory2",false);
		MemKV rm = new RedisMemKV("redis","172.16.59.129", 6379, 100, 200);
		rm.set("jjj", "jfkdkkd", 10);
		rm.hset("111111", "1", "jjj", 10);
		String x = (String) rm.get("jjj");
		System.out.println(x);
		x = (String) rm.hget("111111", "1");
		System.out.println(x);
		memkv.set("1", "123", 10);
		memkv.unsafe_set("2","1111",10);
		memkv.set("3", "jfjjfjfj", 100);
		memkv.set("4", "jkwjek", 50);
		memkv.hset("5", "1", "jekkdkd", 100);
	        //Thread.sleep(1000 * 1000);
		String fuck = "xxx@@fsfsf";
		String[] you = fuck.split("@@");
		System.out.println(you[1]);
		Thread.currentThread().sleep(10000);
		x = (String) rm.get("jjj");
		System.out.println(x);
		x = (String) rm.hget("111111", "1");
		System.out.println(x);

	}
}
