package com.cmbc.util.memkv;

import java.io.Serializable;

/**
 * Object Wrapper
 *
 */
public class CacheObject implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public CacheObject(Object value) {
		long currentTime = System.currentTimeMillis();
		this.putTime = currentTime;
		this.updateTime = currentTime;
		this.lastAccessTime = currentTime;
		this.expireTime = -1;
		this.value = value;
	}
	public CacheObject(Object value,long expireTime) {
		long currentTime = System.currentTimeMillis();
		this.putTime = currentTime;
		this.updateTime = currentTime;
		this.lastAccessTime = currentTime;
		this.expireTime = expireTime;
		this.value = value;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public long getPutTime() {
		return putTime;
	}
	public void setPutTime(long putTime) {
		this.putTime = putTime;
	}
	public long getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(long updateTime) {
		this.updateTime = updateTime;
	}
	public long getLastAccessTime() {
		return lastAccessTime;
	}
	public void setLastAccessTime(long lastAccessTime) {
		this.lastAccessTime = lastAccessTime;
	}
	public long getExpireTime() {
		return expireTime;
	}
	public void setExpireTime(long expireTime) {
		this.expireTime = expireTime;
	}
	private Object value;
	private Object oldValue;
	private boolean dirtyFlag = false;
	
	public Object getOldValue() {
		return oldValue;
	}
	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}
	public boolean isDirtyFlag() {
		return dirtyFlag;
	}
	public void setDirtyFlag(boolean dirtyFlag) {
		this.dirtyFlag = dirtyFlag;
	}
	private long putTime;
	private long updateTime;
	private long lastAccessTime;
	private long expireTime;
}