package com.cmbc.util.memkv.event;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbc.util.memkv.common.NamedThreadFactory;

public class MemkvEventDispatcher {

	public static Map<String,Map<String,MemkvEventHandlerWrapper>> handlerMap = 
			new ConcurrentHashMap<String, Map<String,MemkvEventHandlerWrapper>>();
	public static BlockingQueue<MemkvEvent> eventQueue = new LinkedBlockingQueue<MemkvEvent>(2000);
	public static ThreadPoolExecutor consumerThreadPool = new ThreadPoolExecutor(5, 20, 20, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(1000), new NamedThreadFactory("memkv-event-consumer-pool"));
	public static ExecutorService dispatcher = Executors.newSingleThreadExecutor(new NamedThreadFactory("memkv-event-dispatcher"));
	public static volatile boolean stop = false;
	final private static Logger logger = LoggerFactory.getLogger(MemkvEventDispatcher.class);
	
	static {
		dispatcher.execute(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(!stop) {
					try {
						MemkvEvent event = eventQueue.take();
						String memkvName = event.getMemkvName();
						String key = event.getKey();
						Map<String, MemkvEventHandlerWrapper> map = handlerMap.get(memkvName);
						if(map == null) {
							continue;
						}
						MemkvEventHandlerWrapper handlerWrapper = map.get(key);
						if(handlerWrapper == null) {
							continue;
						}
						consumerThreadPool.execute(new MemkvEventConsumer(handlerWrapper.getHandler(),event));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						logger.info("memkveventdispatcher received interupt signal, stopping...",e.getMessage());
						break;
					}
				}
			}
			
		});
	}
	
	public static void destroy() {
		stop = true;
		dispatcher.shutdownNow();
		//dispatcher.shutdown();
		consumerThreadPool.shutdownNow();
	}
	public MemkvEventDispatcher() {
		// TODO Auto-generated constructor stub
	}
	
	public static boolean keyListened(String memkvName,String key,String hkey,int memkvEventType) {
		Map<String,MemkvEventHandlerWrapper> map = handlerMap.get(memkvName);
		if(map == null) {
			return false;
		}
		MemkvEventHandlerWrapper wrapper = map.get(key);
		if(wrapper == null) {
			return false;
		}
		if((hkey != null && wrapper.getHkey() != null && hkey.equals(wrapper.getHkey())) ||
				(hkey == null && wrapper.getHkey() == null) ){
			if((wrapper.getEventType() & memkvEventType) != 0) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean addEvent(String memkvName,String key,String hkey,String memkvType,int type,boolean safeflag) {
		if(!keyListened(memkvName,key,hkey,type)) {
			return true;
		}
		MemkvEvent event = new DefaultMemkvEvent(key, hkey, type, memkvName, memkvType,safeflag);
		return eventQueue.offer(event);
	}
	
	/**
	 * 
	 * @param memkvName memkv名字
	 * @param key 一级key
	 * @param hkey 二级key，可为空
	 * @param handler 
	 * @param eventType 监听的事件类型，例如MemkvEventType.ADD, MemkvEventType.ADD|MemkvEventType.REMOVE，或者MemkvEventType.ALL
	 * @return
	 */
	public static boolean addHandler(String memkvName,String key,String hkey, MemkvEventHandler handler,int eventType) {
		if(memkvName != null && key != null && !memkvName.isEmpty() && !key.isEmpty()) {
			MemkvEventHandlerWrapper wrapper = new MemkvEventHandlerWrapper(memkvName, key, hkey, eventType, handler);
			Map map = handlerMap.get(memkvName);
			if(map == null) {
				map = new ConcurrentHashMap<String,MemkvEventHandlerWrapper>();
				handlerMap.put(memkvName, map);
			}
			map.put(key, wrapper);
		}
		return false;
	}
}
