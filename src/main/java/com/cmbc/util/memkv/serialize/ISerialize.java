package com.cmbc.util.memkv.serialize;

public interface ISerialize {
	public byte[] serialize(Object object);
	public Object unserialize(byte[] bytes);
}
