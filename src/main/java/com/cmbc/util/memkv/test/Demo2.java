package com.cmbc.util.memkv.test;

import java.util.List;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.MemKVManager;

public class Demo2 {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MemKV memkv1 = new DefaultMemKV("memkv1");
		MemKV memkv2 = new DefaultMemKV("memkv2");
		
		MemKV memkv = MemKVManager.getInstance().getMemKV("memkv1");
		memkv.set("1", "123", 100);
		
		String x = (String) memkv.get("1");
		System.out.println(x); //结果123
		
		List<String> names = MemKVManager.getInstance().getMemKvNames();
		System.out.println(names); //结果[memkv1,memkv2]
		
	}

}
