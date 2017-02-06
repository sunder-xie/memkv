package com.cmbc.util.memkv.test;

import java.util.Random;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.EhcacheMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.MemKVManager;

public class Memcost {

	public Memcost() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		final MemKV memkv = new EhcacheMemKV("fuck");
		long x = 0;
		new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				long x = 0;
				try {
					Thread.sleep(300000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				while(true) {
					int count = 0;
					for(int i = 10000000; i < 10000300; i++) {
						String key = String.valueOf(x) + "_" + String.valueOf(i);
						String value = (String) memkv.unsafe_get(key);
						if(value != null) {
							count++;
						}
					}
					System.out.println(count);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					x++;
					}
			}
			
		}).start();
		
		while(true) {
		for(int i = 10000000; i < 10000300; i++) {
			String key = String.valueOf(x) + "_" + String.valueOf(i);
			byte[] rand = new byte[2048];
			new Random().nextBytes(rand);
			String value = new String(rand);
			
			memkv.unsafe_set(key, value, 300);
			
		}
		Thread.sleep(1000);
		x++;
		}
	}

}
