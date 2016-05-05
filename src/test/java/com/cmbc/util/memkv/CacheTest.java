//package com.cmbc.util.memkv;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import com.cmbc.util.memkv.DefaultMemKV;
//import com.cmbc.util.memkv.MemKV;
//import com.cmbc.util.memkv.serialize.SerializeUtil;
//
//import junit.framework.Assert;
//import junit.framework.Test;
//import junit.framework.TestCase;
//import junit.framework.TestSuite;
//
//public class CacheTest extends TestCase {
//
//	public CacheTest(String testName) {
//		super(testName);
//	}
//
//	/**
//	 * @return the suite of tests being tested
//	 */
//	public static Test suite() {
//		return new TestSuite(CacheTest.class);
//	}
//	public void testSet() {
//		MemKV cache = new DefaultMemKV();
//		Map x = new HashMap();
//		x.put("name", "niuxinli");
//		x.put("id", "371321918812266138");
//		x.put("salary", new BigDecimal("10000000.00"));
//		x.put("hight", 188);
//		x.put("weight", 130);
//		x.put("phone", "110");
//		x.put("jfjdkdkdjf", "jfksfjsfjsalfjskfjskfjskfjskflaskjgiwoefjsakfjskhgksjfslfjsdkfjsoeifjiwefj");
//		x.put("ksjfksjoegskfjslkfjksfjsafjlskdfjlsjfslajfldkf", "jfslfksdfjewohgskfjakfjsjaflejiofjskfjaldfjalfjslfjalgoerejgoreifjowfjlsfjslfsklfjighslf");
//		cache.set("niuxinli", x, 5);
//
//		long start = System.currentTimeMillis();
//		for(int i = 0; i < 10000;i++) {
//			cache.set(String.valueOf(i/100), x, 10);
//			Map y = (Map) cache.get(String.valueOf(i/100));
//		}
//		long end = System.currentTimeMillis();
//		System.out.println(end-start);
//	}
//	
//	public void testSetIfAbsent() {
//		MemKV cache = new DefaultMemKV();
//		Map x = new HashMap();
//		x.put("name", "niuxinli");
//		x.put("id", "371321918812266138");
//		x.put("salary", new BigDecimal("10000000.00"));
//		x.put("hight", 188);
//		x.put("weight", 130);
//		x.put("phone", "110");
//		cache.set("niuxinli", x, 10);
//		Map y = (Map) cache.get("niuxinli");
//		BigDecimal salary = (BigDecimal) y.get("salary");
//		Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
//		
//		//等6s，未超时，使用setIfAbsent不能放进去
//		try {
//			Thread.currentThread().sleep(6000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		y = new HashMap();
//		y.put("salary", new BigDecimal("100"));
//		boolean ret = cache.setIfAbsent("niuxinli", y, 100);
//		Assert.assertFalse(ret);
//		//取出的数还是老的
//		Map z = (Map) cache.get("niuxinli");
//		salary = (BigDecimal) z.get("salary");
//		Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
//		
//		//再等6s，超时，可以put了
//		try {
//			Thread.currentThread().sleep(6000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		ret = cache.setIfAbsent("niuxinli", y, 100);
//		Assert.assertTrue(ret);
//		z = (Map) cache.get("niuxinli");
//		salary = (BigDecimal) z.get("salary");
//		Assert.assertTrue(salary.compareTo(new BigDecimal("100")) == 0);
//
//
//	}
//	
//	public void testHSet() {
//		MemKV cache = new DefaultMemKV();
//		Map x = new HashMap();
//		x.put("name", "niuxinli");
//		x.put("id", "371321918812266138");
//		x.put("salary", new BigDecimal("10000000.00"));
//		x.put("hight", 188);
//		x.put("weight", 130);
//		x.put("phone", "110");
//		cache.hset("niuxinli", "x", x,10);
//		Map y = (Map) cache.hget("niuxinli","x");
//		BigDecimal salary = (BigDecimal) y.get("salary");
//		Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
//		
//		//等6s，未超时，使用setIfAbsent不能放进去
//		try {
//			Thread.currentThread().sleep(6000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		y = new HashMap();
//		y.put("salary", new BigDecimal("100"));
//		boolean ret = cache.hsetIfAbsent("niuxinli","x", y, 100);
//		Assert.assertFalse(ret);
//		//取出的数还是老的
//		Map z = (Map) cache.hget("niuxinli","x");
//		salary = (BigDecimal) z.get("salary");
//		Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
//		
//	}
//	
//	public void testMultipleSet() {
//		/**1000个线程每个线程写1000次，然后1000个线程每个线程读1000次*/
//		final MemKV cache = new DefaultMemKV();
//		Thread[] threads = new Thread[1000];
//		for(int i = 0; i < 1000; i++) {
//			threads[i] = new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					for(int i = 0; i < 1000; i++) {
//						Map x = new HashMap();
//						x.put("name", "niuxinli");
//						x.put("id", "371321198812266138");
//						x.put("salary", new BigDecimal("10000000.00"));
//						x.put("hight", 188);
//						x.put("weight", 130);
//						x.put("phone", "110");
//						x.put("index", i);
//						cache.setIfAbsent(String.valueOf(i), x,40);			
//					}
//
//				}
//			});
//			threads[i].start();
//		}
//		for(int i = 0; i < 1000; i++) {
//			try {
//				threads[i].join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		for(int i = 0; i < 1000; i++) {
//			threads[i] = new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					for(int i = 0; i < 1000; i++) {
//						Map x = (Map) cache.get(String.valueOf(i));
//						String name = (String) x.get("name");
//						String id = (String) x.get("id");
//						BigDecimal salary = (BigDecimal) x.get("salary");
//						Integer hight = (Integer) x.get("hight");
//						Integer weight = (Integer) x.get("weight");
//						String phone = (String) x.get("phone");
//						int index = (Integer) x.get("index");		
//						Assert.assertTrue(name.compareTo("niuxinli") == 0);
//						Assert.assertTrue(id.compareTo("371321198812266138") == 0);
//						Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
//						Assert.assertTrue(hight == 188);
//						Assert.assertTrue(index == i);
//					}
//				}
//				
//			});
//			threads[i].start();
//		}
//		for(int i = 0; i < 1000; i++) {
//			try {
//				threads[i].join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		try {
//			Thread.currentThread().sleep(1);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		
//	}
//	
//	public void testMemCost() {
//		MemKV cache = new DefaultMemKV();
//
//		long bytes =0;
//		System.gc();
//		System.out.println(Runtime.getRuntime().freeMemory()/1024/1024);
//		String bigStr = ""; //200KB
//		for(int i = 0; i<15000;i++) {
//			bigStr = bigStr + "01234567890123456789";
//		}
//
//		for(int i = 0; i < 1000; i++) {
//			Map x = new HashMap();
//			x.put("name", "niuxinli");
//			x.put("id", "3713219188122fdsfsfasdfsdafafdafsdfsda66138");
//			x.put("idsfs", "37132191sfsfsafsafsfsdfsfsdfsdfdfsfsdfsfsdfds8812266138");
//			x.put("salary", new BigDecimal("10000000.00"));
//			x.put("hight", 188);
//			x.put("weight", 130);
//			x.put("phone", "110");
//			x.put("index", i);
//			x.put("longstr", bigStr);
//			cache.set(String.valueOf(i), x,10);		
//			bytes += SerializeUtil.serialize(x).length;
//		}
//		System.out.println(bytes);
//		try {
//			Thread.currentThread().sleep(3000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println(Runtime.getRuntime().freeMemory()/1024/1024);
//		System.gc();
//		System.out.println(Runtime.getRuntime().freeMemory()/1024/1024);
//		Map y = (Map) cache.get("1");
//		System.out.print(y.get("name"));
//		
//	}
//	
//	public void testSetAndGetTogether() {
//		//200个线程setIfAbsent,200个线程get，200个线程hsetIfAbsent，200个线程hget，得到的值或者为null，或者是正确的值
//		//每个线程set或get1000次
//		final AtomicInteger nulls = new AtomicInteger(0);
//		Thread[] threads = new Thread[800];
//		final MemKV cache = new DefaultMemKV();
//		for(int i = 0; i < 200; i++) {
//			threads[i] = new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					int rand = new Random().nextInt(2);
//					if(rand <= 0) {
//						for (int i = 0; i < 1000; i++) {
//							Map x = new HashMap();
//							x.put("name", "niuxinli");
//							x.put("id", "371321198812266138");
//							x.put("salary", new BigDecimal("10000000.00"));
//							x.put("hight", 188);
//							x.put("weight", 130);
//							x.put("phone", "110");
//							x.put("index", i);
//							cache.set(String.valueOf(i), x, 10);
//						}
//					}else {
//						for (int i = 999; i >= 0; i--) {
//							Map x = new HashMap();
//							x.put("name", "niuxinli");
//							x.put("id", "371321198812266138");
//							x.put("salary", new BigDecimal("10000000.00"));
//							x.put("hight", 188);
//							x.put("weight", 130);
//							x.put("phone", "110");
//							x.put("index", i);
//							cache.set(String.valueOf(i), x, 10);
//						}
//					}
//				}
//			});
//		}
//		for(int i = 0; i < 200; i++) {
//			threads[200+i] = new Thread(new Runnable() {
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					for(int i = 0; i < 1000; i++) {
//						Map x = (Map) cache.get(String.valueOf(i));
//						if(x != null) {
//						String name = (String) x.get("name");
//						String id = (String) x.get("id");
//						BigDecimal salary = (BigDecimal) x.get("salary");
//						int hight = (Integer) x.get("hight");
//						int weight = (Integer) x.get("weight");
//						String phone = (String) x.get("phone");
//						int index = (Integer) x.get("index");		
//						Assert.assertTrue(name.compareTo("niuxinli") == 0);
//						Assert.assertTrue(id.compareTo("371321198812266138") == 0);
//						Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
//						Assert.assertTrue(hight == 188);
//						Assert.assertTrue(index == i);
//						} else {
//							//nulls.incrementAndGet();
//						}
//					}
//				}
//			});
//		}
//		for(int i = 0; i < 200; i++) {
//			threads[400+i] = new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					int rand = new Random().nextInt(2);
//					if(rand <= 0) {
//						for (int i = 0; i < 1000; i++) {
//							Map x = new HashMap();
//							x.put("name", "niuxinli");
//							x.put("id", "371321198812266138");
//							x.put("salary", new BigDecimal("10000000.00"));
//							x.put("hight", 188);
//							x.put("weight", 130);
//							x.put("phone", "110");
//							x.put("index", i);
//							String pk = "H" + String.valueOf(i / 20);
//							cache.hsetIfAbsent(pk, String.valueOf(i), x, 10);
//						}
//					} else {
//						for(int i = 999; i >= 0; i--) {
//							Map x = new HashMap();
//							x.put("name", "niuxinli");
//							x.put("id", "371321198812266138");
//							x.put("salary", new BigDecimal("10000000.00"));
//							x.put("hight", 188);
//							x.put("weight", 130);
//							x.put("phone", "110");
//							x.put("index", i);
//							String pk = "H"+String.valueOf(i/20);
//							cache.hsetIfAbsent(pk,String.valueOf(i), x,10);			
//						}
//					}
//				}
//			});
//		}
//		for(int i = 0; i < 600;i++) {
//			threads[i].start();
//		}
//		for(int i = 0; i < 600; i++) {
//			try {
//				threads[i].join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		for(int i = 0; i < 200; i++) {
//			threads[600+i] = new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						Thread.currentThread().sleep(100);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					// TODO Auto-generated method stub
//					for(int i = 0; i < 1000; i++) {
//						String pk = "H"+String.valueOf(i/20);
//						Map x = (Map) cache.hget(pk,String.valueOf(i));
//						if(x != null) {
//						String name = (String) x.get("name");
//						String id = (String) x.get("id");
//						BigDecimal salary = (BigDecimal) x.get("salary");
//						int hight = (Integer) x.get("hight");
//						int weight = (Integer) x.get("weight");
//						String phone = (String) x.get("phone");
//						int index = (Integer) x.get("index");		
//						Assert.assertTrue(name.compareTo("niuxinli") == 0);
//						Assert.assertTrue(id.compareTo("371321198812266138") == 0);
//						Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
//						Assert.assertTrue(hight == 188);
//						Assert.assertTrue(index == i);
//						} else {
//							nulls.incrementAndGet();
//						}
//					}
//				}
//			});
//		}
//		for(int i = 600; i < 800; i++) {
//			threads[i].start();
//		}
//		for(int i = 0; i < 800; i++) {
//			try {
//				threads[i].join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		System.out.println(nulls);
//		
//	}
//
//	public void testThreads() {
//		Thread[] threads = new Thread[1000];
//		try {
//			Thread.currentThread().sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		final Map map = new ConcurrentHashMap();
//		for(int i =0; i < 1000; i++) {
//			threads[i ]= new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					for(int i = 0; i < 1000; i++) {
//						
//						Map x = new HashMap();
//						x.put("name", "niuxinli");
//						x.put("id", "371321918812266138");
//						x.put("salary", new BigDecimal("10000000.00"));
//						x.put("hight", 188);
//						x.put("weight", 130);
//						x.put("phone", "110");
//						x.put("index", i);
//						map.put(i, x);		
//
//					}
//				}
//			
//		});
//		}
//		for(int i = 0; i < 1000; i++) {
//			try {
//				threads[i].join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		try {
//			Thread.currentThread().sleep(5000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	public void testProdList() {
//		List prodList = new ArrayList();
//		for(int i = 0; i < 300; i++) {
//			Map map = new HashMap();
//			map.put("prodNo", String.valueOf(i));
//			map.put("name", "理财1一号发发发发");
//			map.put("ksjfdj", "jfkdjfdkfjdkjfdkjfdkfjskfjskfjskjksjksfjsdkf");
//			map.put("picUrl", "http://weuwi44j242394823489&(#*#$*#(*U*$JJFKJDfhwojfsdfjskfjskfjskfjskfjksfjskfjslfjslkfskfjslafjietu829442974298(!(&(#&(*#)*");
//			map.put("profit", "0.2839");
//			map.put("jjjjj", "kdjfkd");
//			map.put("skdkkkkkkkkkkkkkkkkkkkkkkkkk", "jfdfksjfksfj");
//			map.put("kdkkkkkkkkkkk", "jjfjdjfjjdjfjjdjfdkfkdkdkdkd");
//			if(i % 50 == 0) {
//				map.put("flag", 1); //flag是1的排在最前面，然后按产品额度再排
//			} else {
//				map.put("flag", 0);
//			}
//			prodList.add(map);
//			
//		}
//		final MemKV cache = new DefaultMemKV();
//		cache.set("prodList", prodList, 3000);
//		cache.set("prod11", prodList, 20);
//		List prodLimit = reloadProdLimit();
//		//cache.set("limitList", prodLimit, 5);
////		sort(prodList, prodLimit);
////		for(int i = 0; i < 100; i++) {
////			Map m = (Map) prodList.get(i);
////			String prdNo = (String) m.get("prodNo");
////			int flag = (int) m.get("flag");
////			BigDecimal limit = (BigDecimal) m.get("limit");
////			String fuck = prdNo+","+flag+","+limit+"\n";
////			System.out.println(fuck);
////		}
//		Thread[] threads = new Thread[1000];
//		for(int i =0; i < 100; i++) {
//			threads[i ]= new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					for(int i = 0; i < 1000; i++) {
//						
//						List prodInfo = (List) cache.get("prodList");
//						List limitList = (List) cache.get("limitList");
//						if(limitList == null) {
//							limitList = reloadProdLimit();
//							cache.setIfAbsent("limitList",limitList, 5);
//						}
//						sort(prodInfo,limitList);
////						for(int j = 0; j < 100; j++) {
////						Map m = (Map) prodInfo.get(j);
////						String prdNo = (String) m.get("prodNo");
////						int flag = (int) m.get("flag");
////						BigDecimal limit = (BigDecimal) m.get("limit");
////						String fuck = prdNo+","+flag+","+limit+"\n";
////						System.out.println(fuck);
////					}
//					}
//				}
//			
//		});
//		}
//		for(int i = 0; i < 100; i++) {
//			threads[i].start();
//		}
//		for(int i = 0; i < 100; i++) {
//			try {
//				threads[i].join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		
//	}
//	//假设prodList和limitList一一对应
//	public void sort(List prodList,List limitList) {
//		prodComparator pc = new prodComparator();
//		for(int i = 0; i < prodList.size(); i++) {
//			Map prod = (Map) prodList.get(i);
//			Map limit = (Map) limitList.get(i);
//			prod.putAll(limit);
//		}
//		//prodList.sort(pc);
//	}
//	class prodComparator implements Comparator {
//
//		@Override
//		public int compare(Object o1, Object o2) {
//			Map m1 = (Map)o1;
//			Map m2 = (Map)o2;
//			int flag1 = (Integer) m1.get("flag");
//			int flag2 = (Integer) m2.get("flag");
//			if(flag1 > flag2) {
//				return -1;
//			} else if(flag1 < flag2) {
//				return 1;
//			}
//			BigDecimal l1 = (BigDecimal) m1.get("limit");
//			BigDecimal l2 = (BigDecimal)m2.get("limit");
//			return l2.compareTo(l1);
//		}
//		
//	}
//	public List reloadProdLimit() {
//		//System.out.println("reload");
//		List limitList = new ArrayList();
//		for(int i = 0; i < 300; i++) {
//			BigDecimal limit = new BigDecimal(new Random().nextInt(10000));
//			Map map = new HashMap();
//			map.put("prodNo", String.valueOf(i));
//			map.put("limit", limit);
//			limitList.add(map);
//		}
//		try {
//			Thread.currentThread().sleep(100);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return limitList;
//	}
//	
//	 
//	public void TestGC() {
//		MemKV memkv = new DefaultMemKV();
//		for(int i = 0; i < 100; i++) {
//			memkv.setIfAbsent(String.valueOf(i), i, (new Random()).nextInt(20));
//			memkv.hsetIfAbsent(String.valueOf(i)+"h", String.valueOf(i), i, (new Random()).nextInt(20));
//		}
//		
//		try {
//			for(int i = 0; i < 20; i++) {
//				
//				Thread.currentThread().sleep(1000);
//				((DefaultMemKV)memkv).cacheDump();
//			}
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//
//	public void testUnsafeSetAndGetTogether() {
//		//200个线程unsafe_setIfAbsent,200个线程get，200个线程unsafe_hsetIfAbsent，200个线程unsafe_hget，得到的值或者为null，或者是正确的值
//		//每个线程set或get1000次
//		final AtomicInteger nulls = new AtomicInteger(0);
//		Thread[] threads = new Thread[800];
//		final MemKV cache = new DefaultMemKV();
//		for(int i = 0; i < 200; i++) {
//			threads[i] = new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					int rand = new Random().nextInt(2);
//					if(rand <= 0) {
//						for (int i = 0; i < 1000; i++) {
//							Map x = new HashMap();
//							x.put("name", "niuxinli");
//							x.put("id", "371321198812266138");
//							x.put("salary", new BigDecimal("10000000.00"));
//							x.put("hight", 188);
//							x.put("weight", 130);
//							x.put("phone", "110");
//							x.put("index", i);
//							cache.unsafe_setIfAbsent(String.valueOf(i), x, 10);
//						}
//					}else {
//						for (int i = 999; i >= 0; i--) {
//							Map x = new HashMap();
//							x.put("name", "niuxinli");
//							x.put("id", "371321198812266138");
//							x.put("salary", new BigDecimal("10000000.00"));
//							x.put("hight", 188);
//							x.put("weight", 130);
//							x.put("phone", "110");
//							x.put("index", i);
//							cache.unsafe_setIfAbsent(String.valueOf(i), x, 10);
//						}
//					}
//				}
//			});
//		}
//		for(int i = 0; i < 200; i++) {
//			threads[200+i] = new Thread(new Runnable() {
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					for(int i = 0; i < 1000; i++) {
//						Map x = (Map) cache.unsafe_get(String.valueOf(i));
//						if(x != null) {
//						String name = (String) x.get("name");
//						String id = (String) x.get("id");
//						BigDecimal salary = (BigDecimal) x.get("salary");
//						int hight = (Integer) x.get("hight");
//						int weight = (Integer) x.get("weight");
//						String phone = (String) x.get("phone");
//						int index = (Integer) x.get("index");		
//						Assert.assertTrue(name.compareTo("niuxinli") == 0);
//						Assert.assertTrue(id.compareTo("371321198812266138") == 0);
//						Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
//						Assert.assertTrue(hight == 188);
//						Assert.assertTrue(index == i);
//						} else {
//							nulls.incrementAndGet();
//						}
//					}
//				}
//			});
//		}
//		for(int i = 0; i < 200; i++) {
//			threads[400+i] = new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//					int rand = new Random().nextInt(2);
//					if(rand <= 0) {
//						for (int i = 0; i < 1000; i++) {
//							Map x = new HashMap();
//							x.put("name", "niuxinli");
//							x.put("id", "371321198812266138");
//							x.put("salary", new BigDecimal("10000000.00"));
//							x.put("hight", 188);
//							x.put("weight", 130);
//							x.put("phone", "110");
//							x.put("index", i);
//							String pk = "H" + String.valueOf(i / 20);
//							cache.unsafe_hsetIfAbsent(pk, String.valueOf(i), x, 10);
//						}
//					} else {
//						for(int i = 999; i >= 0; i--) {
//							Map x = new HashMap();
//							x.put("name", "niuxinli");
//							x.put("id", "371321198812266138");
//							x.put("salary", new BigDecimal("10000000.00"));
//							x.put("hight", 188);
//							x.put("weight", 130);
//							x.put("phone", "110");
//							x.put("index", i);
//							String pk = "H"+String.valueOf(i/20);
//							cache.unsafe_hsetIfAbsent(pk,String.valueOf(i), x,10);			
//						}
//					}
//				}
//			});
//		}
//		for(int i = 0; i < 600;i++) {
//			threads[i].start();
//		}
//		for(int i = 0; i < 600; i++) {
//			try {
//				threads[i].join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		for(int i = 0; i < 200; i++) {
//			threads[600+i] = new Thread(new Runnable() {
//				@Override
//				public void run() {
//					try {
//						Thread.currentThread().sleep(100);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					// TODO Auto-generated method stub
//					for(int i = 0; i < 1000; i++) {
//						String pk = "H"+String.valueOf(i/20);
//						Map x = (Map) cache.unsafe_hget(pk,String.valueOf(i));
//						if(x != null) {
//						String name = (String) x.get("name");
//						String id = (String) x.get("id");
//						BigDecimal salary = (BigDecimal) x.get("salary");
//						int hight = (Integer) x.get("hight");
//						int weight = (Integer) x.get("weight");
//						String phone = (String) x.get("phone");
//						int index = (Integer) x.get("index");		
//						Assert.assertTrue(name.compareTo("niuxinli") == 0);
//						Assert.assertTrue(id.compareTo("371321198812266138") == 0);
//						Assert.assertTrue(salary.compareTo(new BigDecimal("10000000.00")) == 0);
//						Assert.assertTrue(hight == 188);
//						Assert.assertTrue(index == i);
//						} else {
//							nulls.incrementAndGet();
//						}
//					}
//				}
//			});
//		}
//		for(int i = 600; i < 800; i++) {
//			threads[i].start();
//		}
//		for(int i = 0; i < 800; i++) {
//			try {
//				threads[i].join();
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		System.out.println(nulls);
//		
//	}
//}
