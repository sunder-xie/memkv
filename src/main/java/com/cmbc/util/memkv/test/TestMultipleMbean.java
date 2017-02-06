package com.cmbc.util.memkv.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import com.cmbc.util.memkv.DefaultMemKV;
import com.cmbc.util.memkv.MemKV;
import com.cmbc.util.memkv.MemKVManager;

public class TestMultipleMbean {

	public TestMultipleMbean() {
		// TODO Auto-generated constructor stub
	}
	public static void main(String[] args) throws InterruptedException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		ClassLoader loader1 = new MyClassLoader();
		ClassLoader loader2 = new MyClassLoader();
		Class a = loader1.loadClass("com.cmbc.util.memkv.MemKVManager");
		 a.newInstance();
		Class b = loader2.loadClass("com.cmbc.util.memkv.MemKVManager");
		 b.newInstance();
		 MemKV memkv = new DefaultMemKV();
		while(true) {
			Thread.sleep(10000);
		}
		
	}

}
class MyClassLoader extends ClassLoader {

	public MyClassLoader() {
		// TODO Auto-generated constructor stub
	}

	public MyClassLoader(ClassLoader arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return super.findClass(name);
	}

	@Override
	protected URL findResource(String name) {
		// TODO Auto-generated method stub
		return super.findResource(name);
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		   Class klass = null;
	        try {
	            klass = findLoadedClass(name); //检查该类是否已经被装载。
	            if (klass != null) {
	            	System.out.println("already loaded");
	            	System.out.println(klass.getClassLoader());
	                return klass;   
	            }
	            
	            byte[] bs = getClassBytes(name);//从一个特定的信息源寻找并读取该类的字节。
	            if (bs != null && bs.length > 0) {
	                klass = defineClass(name, bs, 0, bs.length);   
	            }
	            if (klass == null) { //如果读取字节失败，则试图从JDK的系统API中寻找该类。
	                klass = findSystemClass(name);
	            }
	            if (resolve && klass != null) {
	                resolveClass(klass);   
	            }
	        } catch (IOException e) {
	        	
	            throw new ClassNotFoundException(e.toString());
	           
	        }   
	        System.out.println("klass == " + klass);
	        return klass;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return loadClass(name,true);
	}

	private byte[] getClassBytes(String className) throws IOException {
		
		String path = "/Users/niuxinli/Documents/workspace/memkv/target/classes/";
		path += className.replace('.', '/') + ".class";
		System.out.println(path);
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			System.out.println("fuck");
			System.out.println(e);
			return null; // 如果查找失败，则放弃查找。捕捉这个异常主要是为了过滤JDK的系统API。
		}
		byte[] bs = new byte[fis.available()];
		fis.read(bs);
		return bs;
	}

}
