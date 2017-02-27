package com.cmbc.util.memkv.test;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.event.MemkvEvent;
import com.cmbc.util.memkv.event.MemkvEventDispatcher;
import com.cmbc.util.memkv.event.MemkvEventHandler;
import com.cmbc.util.memkv.event.MemkvEventType;

public class EventTester {

	public EventTester() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MemkvEventDispatcher.addHandler("memory", "fuck", null, new MemkvEventHandler() {
			
			@Override
			public void handle(MemkvEvent event) {
				// TODO Auto-generated method stub
				System.out.println("wocao");
			}
		}, MemkvEventType.ADD);
		MemkvEventDispatcher.addHandler("memory", "fuck1", null, new MemkvEventHandler() {
			
			@Override
			public void handle(MemkvEvent event) {
				// TODO Auto-generated method stub
				System.out.println("wocao1");
			}
		}, MemkvEventType.SELF_DEFINE1);
		MemKV memkv = new DefaultMemKV("memory");
		memkv.set("fuck", "fuck", 20);
		memkv.set("fuck", "fuck", 20);
		memkv.set("fuck1", "fuck", 20);
		memkv.set("wocao", "jjj", 30);
		MemkvEventDispatcher.addEvent("memory", "fuck1", null, "Default", MemkvEventType.SELF_DEFINE1, true);
		
		
		
	}

}
