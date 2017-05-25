package com.cmbc.util.memkv;

import com.cmbc.util.memkv.monitor.JmxMonitor;

public class TestMonitor {

	public TestMonitor() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		fuck();
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void fuck() {
		MemKV memkv = new DefaultMemKV("memory",true);
		MemKV fuck = MemKVManager.getInstance().getMemKV("memory");
		JmxMonitor monitor = new JmxMonitor("bttServer");
		monitor.init();
		fuck.unsafe_set("bttlog", "1", -1);
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//monitor.destroy();
	}
}
