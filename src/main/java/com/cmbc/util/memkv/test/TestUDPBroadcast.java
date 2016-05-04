package com.cmbc.util.memkv.test;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.client.UdpBroadcastClient;
import com.cmbc.util.memkv.listener.MemkvUdpListener;

public class TestUDPBroadcast {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		MemKV memkv = new DefaultMemKV("test");
		memkv.set("1", "1", -1);
		MemkvUdpListener listener = new MemkvUdpListener(6666);
		listener.init();
		UdpBroadcastClient client = new UdpBroadcastClient(6666);
		boolean ret = client.invalid("test", "1");
		Thread.sleep(1000);
		String x = (String) memkv.get("1");
		if(x == null) {
			System.out.println("jjj");
		}
		
		listener.destroy();
		
		
	}

}
