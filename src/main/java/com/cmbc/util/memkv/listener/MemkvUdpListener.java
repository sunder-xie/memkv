package com.cmbc.util.memkv.listener;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.MemKVManager;
import com.cmbc.util.memkv.common.NamedThreadFactory;

public class MemkvUdpListener {

	private int port =8888;
	private DatagramSocket socket;
	private ThreadPoolExecutor handlers;
	private ExecutorService server;
	private int coreThreads = 10;
	private int maxThreads = 100;
	private int liveTime = 10;
	private int queueLength = 100;
	private boolean replyEnabled = false;
	static Logger logger = LoggerFactory.getLogger(MemkvUdpListener.class);
	
	public MemkvUdpListener(int port) {
		// TODO Auto-generated constructor stub
		this.port = port;
	}
	public void init() {
		if(handlers == null) {
			handlers = new ThreadPoolExecutor(coreThreads, maxThreads, liveTime, TimeUnit.SECONDS,
					new ArrayBlockingQueue<Runnable>(queueLength),new NamedThreadFactory("memkv-udp-broadcast-handler"));
		}
		try {
			socket = new DatagramSocket(new InetSocketAddress(port));
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		server = Executors.newSingleThreadExecutor(new NamedThreadFactory("memkv-udp-broadcast-server"));
		server.execute(new Server(socket, handlers));
	}
	public void destroy() {
		if(socket != null) {
			socket.close();
		}
		if(handlers != null) {
			handlers.shutdown();
		}
		if(server != null) {
			server.shutdown();
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
	public static String process(String cmd) {
		String success = "0:";
		String failed = "1:";
		String error = "2:";
		if(cmd == null) {
			return error + "cmd error";
		}
		String[] cmds = cmd.split("&");
		String name = cmds[0];
		MemKV memkv = null;
		if(name != null && !name.isEmpty()) {
			memkv = MemKVManager.getInstance().getMemKV(name);
			if(memkv == null) {
				return error+"memkv of name " + name + " not exist";
			}
			if(! (memkv instanceof DefaultMemKV)) {
				return error+"memkv of name " + name + "is " + memkv.getClass().getSimpleName() + " type";
			}
		} else {
			return error+"name can't be empty";
		}
		
		if(cmds.length < 2) {
			return error+"no cmd specified";
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
				return error+"cmd error";
			}
			if(ret) {
				return success+"success";
			}
		} else if(cmds[1].equals("cacheDump")) {
			String path = memkv.cacheDump();
			return success+path;
		} else if(cmds[1].equals("gc")) {
			
		}
		return error+"failed";
	}
	
}
class Server implements Runnable {

	final private Logger logger = LoggerFactory.getLogger(Server.class);
	private DatagramSocket socket;
	private ThreadPoolExecutor handlers;
	public Server(DatagramSocket socket,ThreadPoolExecutor handlers) {
		this.socket = socket;
		this.handlers = handlers;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		DatagramPacket recvPacket = new DatagramPacket(new byte[512], 512);
		while(true) {
			try {
				socket.receive(recvPacket);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
			}
			byte[] data = Arrays.copyOfRange(recvPacket.getData(), 0, recvPacket.getLength());
			InetAddress addr = recvPacket.getAddress();
			int port = recvPacket.getPort();
			logger.info(addr+":"+port+":"+new String(data));
			System.out.println(addr+":"+port+":"+new String(data));
			handlers.execute(new UdpRequestHandler(addr, port, data, true));
			//new Thread(new UdpRequestHandler(addr, port, data, true) ).start();
		}
	}
}
class UdpRequestHandler implements Runnable {
	private InetAddress addr;
	private byte[] data;
	private int port;
	private boolean reply;
	public UdpRequestHandler(InetAddress addr,int port,byte[] data,boolean reply) {
		this.addr = addr;
		this.data = data;
		this.port = port;
		this.reply = reply;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		String cmd = new String(data);
		String result = MemkvUdpListener.process(cmd);
		String[] rets = result.split(":");
		String retMsg =  "{\"result\":\"" + rets[0] + "\",\"msg\":\"" + rets[1] + "\"}";
	}
}
