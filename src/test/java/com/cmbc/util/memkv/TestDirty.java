package com.cmbc.util.memkv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.serialize.SerializeUtil;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestDirty extends TestCase {

	public TestDirty(String testName) {
		super(testName);
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(TestDirty.class);
	}
	public void testSet() {
		MemKV memkv = new DefaultMemKV();
		memkv.set("aaa", "bbb", 1, true);
		String bbb = (String) memkv.get("aaa");
		Assert.assertTrue(bbb.equals("bbb"));
		try {
			Thread.sleep(1001);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bbb = (String) memkv.get("aaa");
		Assert.assertTrue(bbb == null);
		bbb = (String) memkv.get("aaa", true);
		Assert.assertTrue(bbb.equals("bbb"));
		
	}
}
