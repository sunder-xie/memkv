package com.cmbc.util.memkv.test;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;

public class Demo1 {
	
	public static void main(String[] args) throws InterruptedException {
		MemKV memkv = new DefaultMemKV();
		byte[] a = "xxxxx".getBytes();
		byte[] b = "yyyyy".getBytes();
		memkv.set("a", a, 10);//10秒后失效
		memkv.unsafe_set("b", b, 10);
		a[2] = 'y';
		b[2] = 'x';
		byte[] aa = (byte[]) memkv.get("a");
		System.out.println(new String(aa)); //结果为xxxxx
		byte[] bb = (byte[]) memkv.unsafe_get("b");
		System.out.println(new String(bb)); //结果为yyxyy，说明unsafe_set存入的值可以被外界修改
		
		Thread.sleep(10000);
		byte[] aaa = (byte[]) memkv.get("a");
		if(aaa == null) {
			System.out.println("a expired");
		}
		
		((DefaultMemKV)memkv).destroy();
	}
}
