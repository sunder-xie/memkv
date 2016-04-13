package com.cmbc.util.memkv;

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
	
	public void test() {
		MemKV memkv = new DefaultMemKV("memkv");
		MemKV fuck = MemKVManager.getInstance().getMemKV("memkv");
		fuck.set("1", "2", 3);
	}
}
