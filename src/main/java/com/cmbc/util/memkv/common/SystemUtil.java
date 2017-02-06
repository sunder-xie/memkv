package com.cmbc.util.memkv.common;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

public class SystemUtil {

	private static  MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();   
    private static MemoryUsage heapUsage = memorymbean.getHeapMemoryUsage();  
    private static MemoryUsage nonHeapUsage = memorymbean.getNonHeapMemoryUsage();
	public SystemUtil() {
		// TODO Auto-generated constructor stub
	}

	public static long getUsedHeap() {
		return heapUsage.getUsed();
	}
	public static long getInitHeap() {
		return heapUsage.getInit();
	}
	public static long getMaxHeap() {
		return heapUsage.getMax();
	}
	public static long getUsedNonHeap() {
		return nonHeapUsage.getUsed();
	}
	public static long getInitNonHeap() {
		return nonHeapUsage.getInit();
	}
	public static long getMaxNonHeap() {
		return nonHeapUsage.getMax();
	}
	public static long getPhysicalMem() {
		return 0;
	}
	public static long getInitPerm() {
		return 0;
	}
	public static long getMaxPerm() {
		return 0;
	}
	public static long getFreePerm() {
		return 0;
	}
	public static String getOs() {
		return "linux";
	}

}
