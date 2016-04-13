package com.cmbc.util.memkv.test;

import java.util.ArrayList;
import java.util.List;

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

}
