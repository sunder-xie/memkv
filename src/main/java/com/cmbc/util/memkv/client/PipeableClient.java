package com.cmbc.util.memkv.client;

abstract public class PipeableClient implements MemkvBroadcastClient {

	private ThreadLocal<Boolean> inPipe = new ThreadLocal<Boolean>(); //用来判断该线程是否处于一个pipe操作中
	private ThreadLocal<String> pipeCmds = new ThreadLocal<String>();
	
	/**
	 * 判断是否在pipe中
	 * @return
	 */
	public boolean inPipe() {
		Boolean isInPipe = inPipe.get();
		if(isInPipe != null && isInPipe == true) {
			return true;
		}
		return false;
	}
	
	
	
	@Override
	public void startPipe() {
		// TODO Auto-generated method stub
		if(inPipe()) {
			throw new RuntimeException("already in pipe");
		}
		inPipe.set(true);
	}



	@Override
	public void abortPipe() {
		// TODO Auto-generated method stub
		if(inPipe()) {
			pipeCmds.remove();
			inPipe.set(false);
		} else {
			throw new RuntimeException("not in pipe");
		}
	}


	public String getPipeCmds() {
		return pipeCmds.get();
	}
	
	public void p_invalid(String name,String key ) {
		if(!inPipe()) {
			throw new RuntimeException("not in pipe");
		}
		String cmds = pipeCmds.get();
		String cmd = name+"&invalid&"+key;
		if(cmds == null || cmds.isEmpty()) {
			cmds = cmd;
		} else {
			cmds = cmds + "|" + cmd;
		}
		pipeCmds.set(cmds);
	}
	
	public void p_hinvalid(String name,String key,String hkey) {
		if(!inPipe()) {
			throw new RuntimeException("not in pipe");
		}
		String cmd = name+"&hinvalid&"+key;
		if(hkey != null)  {
			cmd = cmd + "&" + hkey;
		}
		String cmds = pipeCmds.get();
		if(cmds == null || cmds.isEmpty()) {
			cmds = cmd;
		} else {
			cmds = cmds + "|" + cmd;
		}
		pipeCmds.set(cmds);
	}
	
}
