package com.cmbc.util.memkv.listener;

import java.util.Map;

import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.MemKVManager;

public class MemkvCmdProcessor {

	public static String process(Map params) {
		String ret = process1(params);
		String[] msg = ret.split(":");
		String json = "";
		if(msg[0].equals("ERROR")) {
			json = "{\"result\":\"2\",\"msg\":\"" + msg[1] + "\"";
		} else if(msg[0].equals("SUCCESS")) {
			json = "{\"result\":\"0\",\"msg\":\"" + msg[1] + "\"";
		} else {
			json = "{\"result\":\"1\",\"msg\":\"" + msg[1] + "\"";
		}
		return json;
	}
	
	public static String process1(Map params) {
		String name = (String) params.get("name");
		if(name == null || name.isEmpty()) {
			return "ERROR:name can't be empty";
		}
		MemKV memkv = MemKVManager.getInstance().getMemKV(name);
		if(memkv == null) {
			return "ERROR:memkv of name " + name + " not exist";
		}

		String cmd = (String)params.get("cmd");
		if(cmd == null || cmd.isEmpty()) {
			return "ERROR:cmd can't be empty";
		}
		
		if(cmd.equals("invalid")) {
			String key = (String) params.get("key");
			if(key == null || key.isEmpty()) {
				return "ERROR:key can't be empty for cmd invalid";
			}
			boolean ret = memkv.remove(key);
			if(ret) {
				return "SUCCESS:invalid key " + key + " success";
			} else {
				return "FAILED:the value of key " + key + " is null, please confirm";
			}
			
		} else if(cmd.equals("hinvalid")) {
			String key = (String) params.get("key");
			if(key == null || key.isEmpty()) {
				return "ERROR:key can't be empty for cmd hinvalid";
			}
			String hkey = (String)params.get("hkey");
			boolean ret = false;
			if(hkey == null || hkey.isEmpty()) {
				ret = memkv.hremove(key);
			} else {
				ret = memkv.hremove(key, hkey);
			}
			if(ret) {
				return "SUCCESS:hinvalid key:hkey " + key+":"+hkey + " success";
			} else {
				return "FAILED:the value of key:hkey " + key+":"+hkey + " is null, please confirm";
			}

		} else {
			return "ERROR:cmd " + cmd + " not correct";
		}
	}
}
