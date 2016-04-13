package com.cmbc.util.memkv.listener;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.data.Stat;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.MemKVManager;

public class MemkvZKListener {

	private ExecutorService executors;
	private String zkIp;
	private int port;
	private String root;
	private CuratorFramework client;
	private String cmdPath = "/cmd";
	private String replyPath = "/reply";
	private String cacheNodes = "/node";
	private NodeCache cmdNodeCache;
	public MemkvZKListener(String zkIp,int port,String root) {
		// TODO Auto-generated constructor stub
		this.zkIp = zkIp;
		this.port = port;
		this.root = root;

		
	}
	public void start() throws Exception {
		
		String namespace = "";
		if(root != null && !root.isEmpty()) {
			namespace = root+"/memkv";
		} else {
			namespace = "memkv";
		}
		client = CuratorFrameworkFactory.builder()
				.sessionTimeoutMs(10000)
				.namespace(namespace)
				.retryPolicy(new RetryNTimes(Integer.MAX_VALUE, 1000))
				.connectString(zkIp+":"+port)
				.build();
		client.start();

		executors = Executors.newFixedThreadPool(3,new ZkWatcherThreadFactory());	
		cmdNodeCache = new NodeCache(client, cmdPath, false);
		cmdNodeCache.start(true);
		cmdNodeCache.getListenable().addListener(
            new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                	String cmd = new String(cmdNodeCache.getCurrentData().getData());   
                	System.out.println(cmd);
                    String result = process(cmd);
                    
        			Stat stat = client.checkExists().forPath("/node/"+MemkvZKListener.getHostInfo()+"/lastcmd");
        			if(stat == null) {
        				try {
        					client.create().forPath("/node/"+MemkvZKListener.getHostInfo()+"/lastcmd");
        				} catch(Exception e) {
        					if(e instanceof NodeExistsException) {
        						System.out.println("node created by others");
        					}
        					else {
        						throw e;
        					}
        				}			
        			}
        			String data = "cmd : "+cmd + "\nret : "+result;
        			client.setData().forPath("/node/"+MemkvZKListener.getHostInfo()+"/lastcmd",data.getBytes()); 
        			
                }
            }, 
            executors
        );
		
		ExecutorService poster = Executors.newSingleThreadExecutor(new ZkWatcherThreadFactory());
		poster.execute(new Poster(client));
		
	}

	public void stop() {
		if(cmdNodeCache != null) {
			try {
				cmdNodeCache.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!executors.isShutdown()) {
			executors.shutdown();
		}
	}

	/**
	 * memkv/cmd
	 * 省略name则表示全部缓存都操作
	 * 1. name%%invalid%%key
	 * 2. name%%hinvalid%%key
	 * 3. name%%hinvalid%%key%%hkey
	 * 4. name%%cachedump
	 * 5. name%%gc
	 * 
	 * @param cmd
	 * @return
	 */
	public  String process(String cmd) {
		String success = "A";
		String failed = "B";
		if(cmd == null) {
			return failed + "cmd error";
		}
		String[] cmds = cmd.split("&");
		String name = cmds[0];
		MemKV memkv = null;
		if(name != null && !name.isEmpty()) {
			memkv = MemKVManager.getInstance().getMemKV(name);
			if(memkv == null) {
				return failed+"memkv of name " + name + " not exist";
			}
			if(! (memkv instanceof DefaultMemKV)) {
				return failed+"memkv of name " + name + "is " + memkv.getClass().getSimpleName() + " type";
			}
		} else {
			return "name can't be empty";
		}
		
		if(cmds.length < 2) {
			return failed+"no cmd specified";
		}
		if(cmds[1].equals("invalid")) {
			if(cmds.length > 2 && cmds[2].isEmpty() == false) {
				boolean ret = memkv.remove(cmds[2]);
				if(ret) {
					return success+"success";
				}
			}
		} else if(cmds[1].equals("hInvalid")) {
			boolean ret;
			if(cmds.length == 4 && !cmds[2].isEmpty() && !cmds[3].isEmpty()) {
				ret = memkv.hremove(cmds[2],cmds[3]);
			} else if(cmds.length == 3 && !cmds[2].isEmpty()) {
				ret = memkv.hremove(cmds[1]);
			} else {
				return failed+"cmd error";
			}
			if(ret) {
				return success+"success";
			}
		} else if(cmds[1].equals("cacheDump")) {
			String path = memkv.cacheDump();
			return success+path;
		} else if(cmds[1].equals("gc")) {
			
		}
		return failed+"failed";
	}
	
	public static String getHostInfo() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "unknown_host";
		}	
		String ip=addr.getHostAddress().toString();//获得本机IP
		String hostname=addr.getHostName().toString();//获得本机名称
		return ip+"_"+hostname;

	}
}

class Poster implements Runnable {

	private CuratorFramework client;
	public Poster(CuratorFramework client) {
		this.client = client;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		Stat stat = null;
		try {
			//检查／node结点是否存在，如不存在则创建
			stat = client.checkExists().forPath("/node");
			if(stat == null) {
				try {
					client.create().forPath("/node");
				} catch(Exception e) {
					if(e instanceof NodeExistsException) {
						System.out.println("node created by others");
					}
					else {
						throw e;
					}
				}
			}
			//检查/node/ip_host结点是否存在，如不存在则创建
			stat = client.checkExists().forPath("/node/"+MemkvZKListener.getHostInfo());
			if(stat == null) {
				try {
					client.create().forPath("/node/"+MemkvZKListener.getHostInfo());
				} catch(Exception e) {
					if(e instanceof NodeExistsException) {
						System.out.println("node created by others");
					}
					else {
						throw e;
					}
				}
				
			}
			//检查/node/ip_host/cache是否存在，如果不存在则创建
			stat = client.checkExists().forPath("/node/"+MemkvZKListener.getHostInfo()+"/cache");
			if(stat == null) {
				try {
					client.create().forPath("/node/"+MemkvZKListener.getHostInfo()+"/cache");
				} catch(Exception e) {
					if(e instanceof NodeExistsException) {
						System.out.println("node created by others");
					}
					else {
						throw e;
					}
				}			
			}
			//检查/node/ip_host/lastcmd是否存在，如果不存在则创建
			stat = client.checkExists().forPath("/node/"+MemkvZKListener.getHostInfo()+"/lastcmd");
			if(stat == null) {
				try {
					client.create().forPath("/node/"+MemkvZKListener.getHostInfo()+"/lastcmd");
				} catch(Exception e) {
					if(e instanceof NodeExistsException) {
						System.out.println("node created by others");
					}
					else {
						throw e;
					}
				}			
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			//todo
		}
		while(true) {
			
			List<String> names  = MemKVManager.getInstance().getMemKvNames();
			for(String name : names) {
				String path = "/node/"+MemkvZKListener.getHostInfo()+"/cache/"+name;
				try {
					stat = client.checkExists().forPath(path);
					if(stat == null) {
						client.create().forPath(path);
					}
					MemKV memkv = MemKVManager.getInstance().getMemKV(name);
					String type = memkv.getClass().getName();
					String status = null;
					String kv = null;
					if(memkv instanceof DefaultMemKV) {
						status = ((DefaultMemKV)memkv).stat();
						kv = ((DefaultMemKV)memkv).cacheDumpAsString();
					} else {
						status = memkv.getClass().getName();
						kv = status;
					}
					stat = client.checkExists().forPath(path+"/type");
					if(stat == null) {
						client.create().forPath(path+"/type");
					}
					client.setData().forPath(path+"/type", type.getBytes());
					
					stat = client.checkExists().forPath(path+"/status");
					if(stat == null) {
						client.create().forPath(path+"/status");
					}
					client.setData().forPath(path+"/status", status.getBytes());
					
					stat = client.checkExists().forPath(path+"/keyvalue");
					if(stat == null) {
						client.create().forPath(path+"/keyvalue");
					}
					client.setData().forPath(path+"/keyvalue", kv.getBytes());

				} catch (Exception e) {
				// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}
	
}
class ZkWatcherThreadFactory implements ThreadFactory {

	private static AtomicInteger index = new AtomicInteger(0);
	
	@Override
	public Thread newThread(Runnable r) {
		// TODO Auto-generated method stub
		Thread thread = new Thread(r, "memkv-zk-watcher-"+index.getAndIncrement());
		return thread;
	}
	
}
