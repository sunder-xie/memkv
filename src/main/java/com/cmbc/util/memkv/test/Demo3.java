package com.cmbc.util.memkv.test;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;

public class Demo3 {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		DefaultMemKV memkv1 = new DefaultMemKV("memkv1",true); //默认半小时清理一次
		DefaultMemKV memkv2 = new DefaultMemKV("memkv2",true,6); //指定6s清理一次
		memkv1.set("1", "1", 5);
		memkv1.unsafe_set("2", "2", 5);
		memkv1.hset("3", "3", "3", 5);
		memkv2.set("1", "1", 5);
		memkv2.unsafe_set("2", "2", 5);
		memkv2.hset("3", "3", "3", 5);
		
		while(true) {
			System.out.println("cache dump of memkv1");
			System.out.println(memkv1.cacheDumpAsString());
			System.out.println("cache dump of memkv1");
			System.out.println(memkv2.cacheDumpAsString());
			Thread.sleep(10000);
		}
	}
}
