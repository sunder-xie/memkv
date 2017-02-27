package com.cmbc.util.memkv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.cmbc.util.memkv.serialize.SerializeUtil;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class UnsafeTest extends TestCase{
	
	public UnsafeTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(UnsafeTest.class);

	}
	
	public void testUnsafeSet() {
		MemKV cache = new DefaultMemKV();
		Map x = new HashMap();
		x.put("name", "niuxinli");
		x.put("id", "371321918812266138");
		x.put("salary", new BigDecimal("10000000.00"));
		x.put("hight", 188);
		x.put("weight", 130);
		x.put("phone", "110");
		cache.unsafe_set("niuxinli", x, 5);
		Map y = (Map) cache.unsafe_get("niuxinli");
		BigDecimal salary = (BigDecimal) y.get("salary");
		Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
		try {
			Thread.currentThread().sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		y = (Map)cache.unsafe_get("niuxinli");
		Assert.assertEquals(true, y == null);
		cache.unsafe_set("niuxinli", x, -1);
		y = (Map) cache.unsafe_get("niuxinli");
		salary = (BigDecimal) y.get("salary");
		Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
		cache.remove("niuxinli");
		y = (Map) cache.get("niuxinli");
		Assert.assertTrue(y==null);
	}
	
	public void testUnsafeSetIfAbsent() {
		MemKV cache = new DefaultMemKV();
		Map x = new HashMap();
		x.put("name", "niuxinli");
		x.put("id", "371321918812266138");
		x.put("salary", new BigDecimal("10000000.00"));
		x.put("hight", 188);
		x.put("weight", 130);
		x.put("phone", "110");
		cache.unsafe_set("niuxinli", x, 10);
		Map y = (Map) cache.unsafe_get("niuxinli");
		BigDecimal salary = (BigDecimal) y.get("salary");
		Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
		
		//等6s，未超时，使用setIfAbsent不能放进去
		try {
			Thread.currentThread().sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		y = new HashMap();
		y.put("salary", new BigDecimal("100"));
		boolean ret = cache.unsafe_setIfAbsent("niuxinli", y, 100);
		Assert.assertFalse(ret);
		//取出的数还是老的
		Map z = (Map) cache.unsafe_get("niuxinli");
		salary = (BigDecimal) z.get("salary");
		Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
		
		//再等6s，超时，可以put了
		try {
			Thread.currentThread().sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ret = cache.unsafe_setIfAbsent("niuxinli", y, 100);
		Assert.assertTrue(ret);
		z = (Map) cache.unsafe_get("niuxinli");
		salary = (BigDecimal) z.get("salary");
		Assert.assertTrue(salary.compareTo(new BigDecimal("100")) == 0);


	}
	
	public void testUnsafeHset() {
		MemKV cache = new DefaultMemKV();
		Map x = new HashMap();
		x.put("name", "niuxinli");
		x.put("id", "371321918812266138");
		x.put("salary", new BigDecimal("10000000.00"));
		x.put("hight", 188);
		x.put("weight", 130);
		x.put("phone", "110");
		cache.unsafe_hset("niuxinli", "x", x,10);
		Map y = (Map) cache.unsafe_hget("niuxinli","x");
		BigDecimal salary = (BigDecimal) y.get("salary");
		Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
		
		//等6s，未超时，使用setIfAbsent不能放进去
		try {
			Thread.currentThread().sleep(6000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		y = new HashMap();
		y.put("salary", new BigDecimal("100"));
		boolean ret = cache.unsafe_hsetIfAbsent("niuxinli","x", y, 100);
		Assert.assertFalse(ret);
		//取出的数还是老的
		Map z = (Map) cache.unsafe_hget("niuxinli","x");
		salary = (BigDecimal) z.get("salary");
		Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
		
	}
	
	public void testMultipleUnsafeSet() {
		/**1000个线程每个线程写1000次，然后1000个线程每个线程读1000次*/
		final MemKV cache = new DefaultMemKV();
		Thread[] threads = new Thread[1000];
		for(int i = 0; i < 1000; i++) {
			threads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					for(int i = 0; i < 1000; i++) {
						Map x = new HashMap();
						x.put("name", "niuxinli");
						x.put("id", "371321198812266138");
						x.put("salary", new BigDecimal("10000000.00"));
						x.put("hight", 188);
						x.put("weight", 130);
						x.put("phone", "110");
						x.put("index", i);
						cache.unsafe_setIfAbsent(String.valueOf(i), x,40);			
					}

				}
			});
			threads[i].start();
		}
		for(int i = 0; i < 1000; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		for(int i = 0; i < 1000; i++) {
			threads[i] = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					for(int i = 0; i < 1000; i++) {
						Map x = (Map) cache.unsafe_get(String.valueOf(i));
						String name = (String) x.get("name");
						String id = (String) x.get("id");
						BigDecimal salary = (BigDecimal) x.get("salary");
						Integer hight = (Integer) x.get("hight");
						Integer weight = (Integer) x.get("weight");
						String phone = (String) x.get("phone");
						int index = (Integer) x.get("index");		
						Assert.assertTrue(name.compareTo("niuxinli") == 0);
						Assert.assertTrue(id.compareTo("371321198812266138") == 0);
						Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
						Assert.assertTrue(hight == 188);
						Assert.assertTrue(index == i);
					}
				}
				
			});
			threads[i].start();
		}
		for(int i = 0; i < 1000; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			Thread.currentThread().sleep(1);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	
	public void testSetAndGetTogether() {
		//200个线程setIfAbsent,200个线程get，200个线程hsetIfAbsent，200个线程hget，得到的值或者为null，或者是正确的值
		//每个线程set或get1000次
		final AtomicInteger nulls = new AtomicInteger(0);
		Thread[] threads = new Thread[800];
		final MemKV cache = new DefaultMemKV();
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
							cache.unsafe_setIfAbsent(String.valueOf(i), x, 10);
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
							cache.unsafe_setIfAbsent(String.valueOf(i), x, 10);
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
						Map x = (Map) cache.unsafe_get(String.valueOf(i));
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
							cache.unsafe_hsetIfAbsent(pk, String.valueOf(i), x, 10);
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
							cache.unsafe_hsetIfAbsent(pk,String.valueOf(i), x,10);			
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
						Map x = (Map) cache.unsafe_hget(pk,String.valueOf(i));
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

	
	

	 
	public void TestGC() {
		MemKV memkv = new DefaultMemKV();
		
		for(int i = 0; i < 100; i++) {
			memkv.setIfAbsent(String.valueOf(i), i, (new Random()).nextInt(20));
			memkv.hsetIfAbsent(String.valueOf(i)+"h", String.valueOf(i), i, (new Random()).nextInt(20));
			memkv.unsafe_setIfAbsent(String.valueOf(i)+"unsafe", i, (new Random()).nextInt(20));
			memkv.unsafe_hsetIfAbsent(String.valueOf(i)+"unsafeh", String.valueOf(i), i, (new Random()).nextInt(20));
		}
		
		try {
			for(int i = 0; i < 20; i++) {
				
				Thread.currentThread().sleep(1000);
				((DefaultMemKV)memkv).cacheDump();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	public void testUnsafeSetAndGetTogether() {
		//200个线程unsafe_setIfAbsent,200个线程get，200个线程unsafe_hsetIfAbsent，200个线程unsafe_hget，得到的值或者为null，或者是正确的值
		//每个线程set或get1000次
		final AtomicInteger nulls = new AtomicInteger(0);
		Thread[] threads = new Thread[800];
		final MemKV cache = new DefaultMemKV();
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
							cache.unsafe_setIfAbsent(String.valueOf(i), x, 10);
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
							cache.unsafe_setIfAbsent(String.valueOf(i), x, 10);
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
						Map x = (Map) cache.unsafe_get(String.valueOf(i));
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
							cache.unsafe_hsetIfAbsent(pk, String.valueOf(i), x, 10);
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
							cache.unsafe_hsetIfAbsent(pk,String.valueOf(i), x,10);			
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
						Map x = (Map) cache.unsafe_hget(pk,String.valueOf(i));
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
