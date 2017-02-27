package com.cmbc.util.memkv;

public interface MemKV {

	/**将键值对key/object存入缓存，
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * 如果原先已经存在而且未失效，会覆盖，
	 * object会被序列化后存入，必须可序列化**/
	public boolean set(String key, Object object, long expireSeconds);

	/**将键值对key/object存入缓存，
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * allowDirty为true，则会保存一份就数据，可以使用对应方法拿到旧数据
	 * object会被序列化后存入，必须可序列化**/
	public boolean set(String key, Object object, long expireSeconds,boolean allowDirty);
	

	/**将键值对key/object存入缓存，
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * 如果原先已经存在而且未失效，则不会覆盖，返回false，
	 * object会被序列化后存入，必须可序列化**/
	@Deprecated
	public boolean setIfAbsent(String key,Object object,long expireSeconds);
	
	/**取出key对应对object**/
	public Object get(String key);

	/**取出key对应对object,如果已经过期，返回老数据，如果不存在返回null**/
	public Object get(String key,boolean allowDirty);

	/**将键值对key/hkey/object存入缓存,
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * 如果原先已经存在而且未失效，会覆盖，
	 * object会被序列化后存入，必须可序列化对**/
	public boolean hset(String key, String hkey,Object object, long expireSeconds);
	
	/**将键值对key/hkey/object存入缓存,
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * allowDirty为true，则会保存一份就数据，可以使用对应方法拿到旧数据，
	 * 如果原先已经存在而且未失效，会覆盖，
	 * object会被序列化后存入，必须可序列化对**/
	public boolean hset(String key, String hkey,Object object, long expireSeconds,boolean allowDirty);
	
	
	/**将键值对key/hkey/object存入缓存,
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * 如果原先已经存在而且未失效，则不会覆盖，返回false，
	 * object会被序列化后存入，必须可序列化对**/
	@Deprecated
	public boolean hsetIfAbsent(String key, String hkey, Object object, long expireSeconds);
	
	/**取出key/hkey对应对object**/
	public Object hget(String key,String hkey);

	/**取出key/hkey对应对object,如果已经过期，返回老数据，如果不存在返回null**/
	public Object hget(String key,String hkey,boolean allowDirty);

	
	/**将键值对key/object存入缓存，
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * 如果原先已经存在而且未失效，会覆盖，
	 * object不会被序列化，因此放入对仅仅是一个引用，
	 * 其他任何地方对这个对象做了修改都会改变缓存对值！！
	 * 对于RedisMemKV这样对实现不起作用，等同于set**/
	public boolean unsafe_set(String key,Object object,long expireSeconds);

	/**将键值对key/object存入缓存，
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * allowDirty为true，则会保存一份就数据，可以使用对应方法拿到旧数据，
	 * 如果原先已经存在而且未失效，会覆盖，
	 * object不会被序列化，因此放入对仅仅是一个引用，
	 * 其他任何地方对这个对象做了修改都会改变缓存对值！！
	 * 对于RedisMemKV这样对实现不起作用，等同于set**/
	public boolean unsafe_set(String key,Object object,long expireSeconds,boolean allowDirty);

	/**将键值对key/object存入缓存，
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * 如果原先已经存在而且未失效，不会覆盖，返回false，
	 * object不会被序列化，因此放入对仅仅是一个引用，
	 * 其他任何地方对这个对象做了修改都会改变缓存对值！！
	 * 对于RedisMemKV这样对实现不起作用，等同于set**/
	@Deprecated
	public boolean unsafe_setIfAbsent(String key,Object object,long expireSeconds);
	
	/**将键值对key/hkey/object存入缓存，
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * 如果原先已经存在而且未失效，会覆盖，
	 * object不会被序列化，因此放入对仅仅是一个引用，
	 * 其他任何地方对这个对象做了修改都会改变缓存对值！！
	 * 对于RedisMemKV这样对实现不起作用，等同于set**/
	public boolean unsafe_hset(String key,String hkey,Object object,long expireSeconds);
	
	/**将键值对key/hkey/object存入缓存，
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * allowDirty为true，则会保存一份就数据，可以使用对应方法拿到旧数据，
	 * 如果原先已经存在而且未失效，会覆盖，
	 * object不会被序列化，因此放入对仅仅是一个引用，
	 * 其他任何地方对这个对象做了修改都会改变缓存对值！！
	 * 对于RedisMemKV这样对实现不起作用，等同于set**/
	public boolean unsafe_hset(String key,String hkey,Object object,long expireSeconds,boolean allowDirty);

	/**将键值对key/hkey/object存入缓存，
	 * expireSeconds秒后失效,
	 * expireSeconds=-1表示不失效，
	 * 如果原先已经存在而且未失效，不会覆盖，返回false，
	 * object不会被序列化，因此放入对仅仅是一个引用，
	 * 其他任何地方对这个对象做了修改都会改变缓存对值！！
	 * 对于RedisMemKV这样对实现不起作用，等同于set**/
	@Deprecated
	public boolean unsafe_hsetIfAbsent(String key,String hkey,Object object,long expireSeconds);
	
	/**取出key对应的unsafe object**/
	public Object unsafe_get(String key);
	
	/**取出key对应的unsafe object,如果已经过期，返回老数据，如果不存在返回null**/
	public Object unsafe_get(String key,boolean allowDirty);

	/**取出key/hkey对应的unsafe object**/
	public Object unsafe_hget(String key,String hkey);
	
	/**取出key/hkey对应的unsafe object,如果已经过期，返回老数据，如果不存在返回null**/
	public Object unsafe_hget(String key,String hkey,boolean allowDirty);

	public boolean remove(String key);
	public boolean hremove(String key);
	public boolean hremove(String key,String hkey);
	
	public boolean containsKey(String key);
	public boolean hcontainsKey(String key);
	public boolean hcontainsKey(String key, String hkey);
	
	public String cacheDump();
}
