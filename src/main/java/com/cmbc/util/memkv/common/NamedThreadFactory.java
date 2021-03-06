package com.cmbc.util.memkv.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private String name;
	private boolean daemon = false;
	private AtomicInteger index = new AtomicInteger(0);
	public NamedThreadFactory(String name) {
		this.name = name;
	}
	public NamedThreadFactory(String name,boolean daemon) {
		this.name = name;
		this.daemon = daemon;
	}
	@Override
	public Thread newThread(Runnable r) {
		// TODO Auto-generated method stub
		Thread t =  new Thread(r,name+"-"+index.getAndIncrement());
		if(daemon) {
			t.setDaemon(true);
		}
		return t;
	}
	
	
	
}
