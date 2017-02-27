package com.cmbc.util.memkv.event;

public class MemkvEventHandlerWrapper {

	public String getMemkvName() {
		return memkvName;
	}
	public void setMemkvName(String memkvName) {
		this.memkvName = memkvName;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getHkey() {
		return hkey;
	}
	public void setHkey(String hkey) {
		this.hkey = hkey;
	}
	public int getEventType() {
		return eventType;
	}
	public void setEventType(int eventType) {
		this.eventType = eventType;
	}
	public MemkvEventHandler getHandler() {
		return handler;
	}
	public void setHandler(MemkvEventHandler handler) {
		this.handler = handler;
	}
	private String memkvName;;
	private String key;
	private String hkey;
	private int eventType;
	private MemkvEventHandler handler;
	public MemkvEventHandlerWrapper(String memkvname,String key,String hkey,int eventType,MemkvEventHandler handler) {
		// TODO Auto-generated constructor stub
		this.memkvName = memkvname;
		this.key = key;
		this.hkey = hkey;
		this.eventType = eventType;
		this.handler = handler;
	}

}
