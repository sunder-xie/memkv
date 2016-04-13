package com.cmbc.util.memkv.test;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.MemkvOffheap;

public class TestMBean {

	public static void main(String[] args) throws InterruptedException {
		MemKV memkv = new DefaultMemKV("memory");
		MemKV memkv1 = new DefaultMemKV("memory1");
		memkv.set("a", "aaa", 1000);
		Map map = new HashMap();
		memkv.set("b", map, 1000);
		memkv.unsafe_set("c","c",1000);
		memkv.unsafe_set("d", map, 1000);
		memkv.hset("1", "1", "1", 5000);
		memkv.unsafe_hset("2", "2", "2", 30);
		memkv.set("xxx", "xxx", -1);
		MemkvOffheap memkv2 = new MemkvOffheap("wocao");
		memkv2.offheap_set("22", "wocao", 1000);
		memkv2.offheap_set("12", "jjj", 1);
		String a = (String) memkv2.offheap_get("22");
		String b = (String) memkv2.offheap_get("12");
		System.out.println(a);
		System.out.println(b);
		Thread.currentThread().sleep(2000);
		a = (String) memkv2.offheap_get("22");
		b = (String) memkv2.offheap_get("12");
		System.out.println(a);
		System.out.println(b);
		Thread.currentThread().sleep(2000);
		a = (String) memkv2.offheap_get("22");
		b = (String) memkv2.offheap_get("12");
		System.out.println(a);
		System.out.println(b);
		String x = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < 1000; i++) {
			sb.append(x);
		}
		x = sb.toString();
		System.out.println(new Timestamp(System.currentTimeMillis()));
		for(int i = 0; i < 10000; i++) {
			memkv2.set(String.valueOf(i), x, 100);
		}
		System.out.println("fuck");
		System.out.println(new Timestamp(System.currentTimeMillis()));

		Thread.currentThread().sleep(20000);
		for(int i = 0; i < 10000; i++) {
			memkv2.remove(String.valueOf(i));
		}
		System.out.println(new Timestamp(System.currentTimeMillis()));
		for(int i = 0; i < 10000; i++) {
			memkv2.set(String.valueOf(i), x, 100);
		}
		System.out.println("fuck");
		System.out.println(new Timestamp(System.currentTimeMillis()));
		for(int i = 0; i < 10000; i++) {
			memkv2.remove(String.valueOf(i));
		}
		System.out.println(new Timestamp(System.currentTimeMillis()));
		for(int i = 0; i < 10000; i++) {
			memkv2.set(String.valueOf(i), x, 100);
		}
		System.out.println("fuck");
		System.out.println(new Timestamp(System.currentTimeMillis()));

		Thread.currentThread().sleep(100000000);
	}
}
