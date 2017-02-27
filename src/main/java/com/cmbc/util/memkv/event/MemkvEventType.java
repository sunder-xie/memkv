package com.cmbc.util.memkv.event;

public class MemkvEventType {
	
	public static final int ADD = 1; 
	public static final int REMOVE = 2;
	public static final int EXPIRE_ON_ACCESS = 4;
	public static final int SELF_DEFINE1 = 32;
	public static final int SELF_DEFINE2 = 64;
	public static final int ALL = 65535; 
}
