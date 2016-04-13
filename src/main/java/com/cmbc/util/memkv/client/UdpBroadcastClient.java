package com.cmbc.util.memkv.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UdpBroadcastClient implements MemkvBroadcastClient {

	final private static Logger logger = LoggerFactory.getLogger(UdpBroadcastClient.class);
	
	private int port = 8888;
	
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	
	public UdpBroadcastClient(int port) {
		// TODO Auto-generated constructor stub
		this.port = port;
	}
	
	public UdpBroadcastClient() {
		// TODO Auto-generated constructor stub
		this.port = port;
	}
	private boolean sendMsg(String msg) {
		
		byte[] buf = msg.getBytes();
		DatagramPacket packet = null;
		try {
			packet = new DatagramPacket(buf, buf.length,InetAddress.getByName("255.255.255.255"),port);
		} catch (UnknownHostException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			logger.error(e2.getMessage());
			return false;
		}
		DatagramSocket ds = null;
		try {
			ds = new DatagramSocket();
			ds.send(packet);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			logger.error(e1.getMessage(), e1);
			return false;	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return false;
		} finally {
			ds.close();
		}
		return true;
	}
	@Override
	public boolean invalid(String name, String key) {
		return sendMsg(name+"&invalid&"+key);

	}

	@Override
	public boolean hinvalid(String name, String key) {
		// TODO Auto-generated method stub
		return sendMsg(name+"&hinvalid&"+key);
	}

	@Override
	public boolean hinvalid(String name, String key, String hkey) {
		// TODO Auto-generated method stub
		return sendMsg(name+"&hinvalid&"+key+"&"+hkey);
	}
	@Override
	public boolean cacheDump(String name) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean gc(String name) {
		// TODO Auto-generated method stub
		return false;
	}

}
