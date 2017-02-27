package com.cmbc.util.memkv.event;

public class DefaultMemkvEvent implements MemkvEvent {

	private String key;
	private String hkey;
	private int eventType;
	private String memkvName;
	private String memkvType;
	private boolean safeFlag;
	public DefaultMemkvEvent(String key, String hkey,int eventType,String memkvName,String memkvType,boolean safeflag) {
		// TODO Auto-generated constructor stub
		this.key = key;
		this.hkey = hkey;
		this.eventType = eventType;
		this.memkvName = memkvName;
		this.memkvType = memkvType;
		this.safeFlag = safeflag;
	}

	@Override
	public String getKey() {
		// TODO Auto-generated method stub
		return key;
	}

	@Override
	public String getHkey() {
		// TODO Auto-generated method stub
		return hkey;
	}

	@Override
	public int getEventType() {
		// TODO Auto-generated method stub
		return eventType;
	}

	@Override
	public String getMemkvName() {
		// TODO Auto-generated method stub
		return memkvName;
	}

	@Override
	public String getMemkvType() {
		// TODO Auto-generated method stub
		return memkvType;
	}

	@Override
	public boolean getSafeflag() {
		// TODO Auto-generated method stub
		return safeFlag;
	}

}
