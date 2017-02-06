package com.cmbc.util.memkv.test;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;

public class TestContains {

	public TestContains() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MemKV memkv = new DefaultMemKV();
		memkv.set("123", null, 100);
		if(memkv.containsKey("123")) {
			System.out.println("true");
		}
		memkv.hset("345", "456", null, 1000);
		if(memkv.hcontainsKey("345")) {
			System.out.println("true");
		}
		
		if(memkv.hcontainsKey("345","456")) {
			System.out.println("true");
		}
	}

}
