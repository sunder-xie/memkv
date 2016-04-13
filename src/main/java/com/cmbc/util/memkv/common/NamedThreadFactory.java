package com.cmbc.util.memkv.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {

	private String name;
	private AtomicInteger index = new AtomicInteger(0);
	public NamedThreadFactory(String name) {
		this.name = name;
	}
	@Override
	public Thread newThread(Runnable r) {
		// TODO Auto-generated method stub
		return new Thread(r,name+"-"+index.getAndIncrement());
	}

}
