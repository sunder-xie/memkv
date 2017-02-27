package com.cmbc.util.memkv.event;

public class MemkvEventConsumer implements Runnable {

	private MemkvEventHandler handler;
	private MemkvEvent event;
	public MemkvEventConsumer(MemkvEventHandler handler, MemkvEvent event) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		this.event = event;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		handler.handle(event);
	}

}
