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
import com.cmbc.util.memkv.MemkvOffheap;
import com.cmbc.util.memkv.serialize.SerializeUtil;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestOffheap extends TestCase{
	
	public TestOffheap(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(TestOffheap.class);

	}
	public void testNullPointer() {
		//MemkvOffheap memkv = new MemkvOffheap 
	}
	
	public void testForever() throws InterruptedException {
		MemkvOffheap memkv = new MemkvOffheap("offheap", false);
		memkv.offheap_set("1", "123", 5);
		String v = (String) memkv.offheap_get("1");
		System.out.println(v);
		Thread.currentThread().sleep(6000);
		v = (String) memkv.offheap_get("1");
		System.out.println(v);
		
	}
	
	public void testMultiThread() {
		final AtomicInteger nulls = new AtomicInteger(0);
		Thread[] threads = new Thread[800];
		final MemkvOffheap cache = new MemkvOffheap();
		for(int i = 0; i < 200; i++) {
			threads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					int rand = new Random().nextInt(2);
					if(rand <= 0) {
						for (int i = 0; i < 1000; i++) {
							Map x = new HashMap();
							x.put("name", "niuxinli");
							x.put("id", "371321198812266138");
							x.put("salary", new BigDecimal("10000000.00"));
							x.put("hight", 188);
							x.put("weight", 130);
							x.put("phone", "110");
							x.put("index", i);
							cache.offheap_set(String.valueOf(i), x, 10);
						}
					}else {
						for (int i = 999; i >= 0; i--) {
							Map x = new HashMap();
							x.put("name", "niuxinli");
							x.put("id", "371321198812266138");
							x.put("salary", new BigDecimal("10000000.00"));
							x.put("hight", 188);
							x.put("weight", 130);
							x.put("phone", "110");
							x.put("index", i);
							cache.offheap_set(String.valueOf(i), x, 10);
						}
					}
				}
			});
		}
		for(int i = 0; i < 200; i++) {
			threads[200+i] = new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					for(int i = 0; i < 1000; i++) {
						Map x = (Map) cache.offheap_get(String.valueOf(i));
						if(x != null) {
						String name = (String) x.get("name");
						String id = (String) x.get("id");
						BigDecimal salary = (BigDecimal) x.get("salary");
						int hight = (Integer) x.get("hight");
						int weight = (Integer) x.get("weight");
						String phone = (String) x.get("phone");
						int index = (Integer) x.get("index");		
						Assert.assertTrue(name.compareTo("niuxinli") == 0);
						Assert.assertTrue(id.compareTo("371321198812266138") == 0);
						Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
						Assert.assertTrue(hight == 188);
						Assert.assertTrue(index == i);
						} else {
							//nulls.incrementAndGet();
						}
					}
				}
			});
		}
		for(int i = 0; i < 200; i++) {
			threads[400+i] = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					int rand = new Random().nextInt(2);
					if(rand <= 0) {
						for (int i = 0; i < 1000; i++) {
							Map x = new HashMap();
							x.put("name", "niuxinli");
							x.put("id", "371321198812266138");
							x.put("salary", new BigDecimal("10000000.00"));
							x.put("hight", 188);
							x.put("weight", 130);
							x.put("phone", "110");
							x.put("index", i);
							String pk = "H" + String.valueOf(i / 20);
							cache.hsetIfAbsent(pk, String.valueOf(i), x, 10);
						}
					} else {
						for(int i = 999; i >= 0; i--) {
							Map x = new HashMap();
							x.put("name", "niuxinli");
							x.put("id", "371321198812266138");
							x.put("salary", new BigDecimal("10000000.00"));
							x.put("hight", 188);
							x.put("weight", 130);
							x.put("phone", "110");
							x.put("index", i);
							String pk = "H"+String.valueOf(i/20);
							cache.hsetIfAbsent(pk,String.valueOf(i), x,10);			
						}
					}
				}
			});
		}
		for(int i = 0; i < 600;i++) {
			threads[i].start();
		}
		for(int i = 0; i < 600; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(int i = 0; i < 200; i++) {
			threads[600+i] = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.currentThread().sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// TODO Auto-generated method stub
					for(int i = 0; i < 1000; i++) {
						String pk = "H"+String.valueOf(i/20);
						Map x = (Map) cache.hget(pk,String.valueOf(i));
						if(x != null) {
						String name = (String) x.get("name");
						String id = (String) x.get("id");
						BigDecimal salary = (BigDecimal) x.get("salary");
						int hight = (Integer) x.get("hight");
						int weight = (Integer) x.get("weight");
						String phone = (String) x.get("phone");
						int index = (Integer) x.get("index");		
						Assert.assertTrue(name.compareTo("niuxinli") == 0);
						Assert.assertTrue(id.compareTo("371321198812266138") == 0);
						Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
						Assert.assertTrue(hight == 188);
						Assert.assertTrue(index == i);
						} else {
							nulls.incrementAndGet();
						}
					}
				}
			});
		}
		for(int i = 600; i < 800; i++) {
			threads[i].start();
		}
		for(int i = 0; i < 800; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(nulls);
	}
}
