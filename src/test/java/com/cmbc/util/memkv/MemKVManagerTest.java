package com.cmbc.util.memkv;

import com.cmbc.util.memkv.monitor.JmxMonitor;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class MemKVManagerTest extends TestCase {
	public MemKVManagerTest(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(MemKVManagerTest.class);

	}
	
	public void test() throws InterruptedException {
		MemKV memkv = new DefaultMemKV("memkv",true);
		MemKV fuck = MemKVManager.getInstance().getMemKV("memkv");
		JmxMonitor monitor = new JmxMonitor("app");
		monitor.init();
		fuck.set("1", "2", 3);
		//Thread.sleep(100000);
	}
}
