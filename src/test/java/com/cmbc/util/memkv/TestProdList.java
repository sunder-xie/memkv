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


import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.serialize.SerializeUtil;

import junit.framework.Assert;

public class TestProdList {
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
//		prodList.sort(pc);
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
}
