package com.cmbc.util.memkv.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.cmbc.util.memkv.client.UrlFetcher;

public class Fetch implements UrlFetcher {

	@Override
	public List<String> fetchUrls() {
		// TODO Auto-generated method stub
		String url =  "http://127.0.0.1:8080/firefly/memkv.do";
		String url1 = "http://127.0.0.1:8080/firefly1/memkv.do";
		List<String> urls = new ArrayList<String>();
		urls.add(url1);
		urls.add(url);
		return urls;
	}
	public static void main(String[] args) {
		Map map = new ConcurrentHashMap();
		map.put("1", "1");
		map.put("2", "2");
		System.out.println(map.keySet());
		map.remove("2");
		System.out.println(map.keySet());
		map.put("2", "2");
		//System.out.println(map.keySet());
		for(Object key : map.keySet()) {
			System.out.println(key);
		}
	}

}
