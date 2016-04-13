package com.cmbc.util.memkv.test;

import java.util.Map;

import com.cmbc.util.memkv.client.HttpBroadcastClient;

public class TestHttpBroadcast {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HttpBroadcastClient client = new HttpBroadcastClient();
		client.setUrlFetcher(new Fetch());
		boolean ret = client.invalid("memory1", "2");
		System.out.println(ret);
		client.destroy();
		
	}

}
