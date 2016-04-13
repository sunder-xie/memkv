package com.cmbc.util.memkv.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.serialize.SerializeUtil;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestForeverValid extends TestCase{
	
	public TestForeverValid(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(TestForeverValid.class);

	}
	
	public void testForever() throws InterruptedException {
		MemKV cache = new DefaultMemKV("memory",true,10);
		cache.set("1", 1, -1);
		cache.setIfAbsent("2", 2, -1);
		cache.unsafe_set("3", 2, -1);
		cache.unsafe_setIfAbsent("4", 5, -1);
		cache.hset("5", "5", 5,-1);
		cache.hsetIfAbsent("6", "6", 6,-1);
		cache.unsafe_hset("7", "7", 7, -1);
		cache.unsafe_hsetIfAbsent("8", "8", 8, -1);
		for(int i = 0; i < 100; i++) {
			Thread.currentThread().sleep(1000);
			try {
				
			cache.set(String.valueOf((i+1)*100+new Random().nextInt(100)), "x", 1+(new Random().nextInt(10)));
			cache.unsafe_set(String.valueOf((i+1)*100+new Random().nextInt(100)), "x", 1+(new Random().nextInt(10)));
			cache.set(String.valueOf((i+1)*100+new Random().nextInt(100)), "x", 1+(new Random().nextInt(10)));
			cache.unsafe_setIfAbsent(String.valueOf((i+1)*100+new Random().nextInt(100)), "x", 1+(new Random().nextInt(10)));
			} catch(Exception e) {
				e.printStackTrace();
			}
			int size = ((DefaultMemKV)cache).getSize();
			assertTrue(size >= 8);
			assertNotNull(cache.get("1"));
			assertNotNull(cache.get("2"));
			assertNotNull(cache.unsafe_get("3"));
			assertNotNull(cache.unsafe_get("4"));
			assertNotNull(cache.hget("5","5"));
			assertNotNull(cache.hget("6","6"));
			assertNotNull(cache.unsafe_hget("6","6"));
			assertNotNull(cache.unsafe_hget("6","6"));
			System.out.println("size = "+size);
		}
		Thread.currentThread().sleep(20000);
		int size = ((DefaultMemKV)cache).getSize();
		assertTrue(size >= 8);
		assertNotNull(cache.get("1"));
		assertNotNull(cache.get("2"));
		assertNotNull(cache.unsafe_get("3"));
		assertNotNull(cache.unsafe_get("4"));
		assertNotNull(cache.hget("5","5"));
		assertNotNull(cache.hget("6","6"));
		assertNotNull(cache.unsafe_hget("6","6"));
		assertNotNull(cache.unsafe_hget("6","6"));
		System.out.println("size = "+size);
		cache.cacheDump();

	}
	

}
