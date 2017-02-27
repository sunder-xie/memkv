package com.cmbc.util.memkv.event;

public interface MemkvEvent {

	public String getKey();
	public String getHkey();
	public int getEventType();
	public String getMemkvName();
	public String getMemkvType();
	public boolean getSafeflag();
	
}
