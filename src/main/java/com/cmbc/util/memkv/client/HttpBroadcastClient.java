package com.cmbc.util.memkv.client;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cmbc.util.memkv.common.NamedThreadFactory;

public class HttpBroadcastClient extends PipeableClient implements MemkvBroadcastClient {
	
	final  static Logger logger = LoggerFactory.getLogger(HttpBroadcastClient.class);
	//private ExecutorService executors = Executors.newFixedThreadPool(10);
	private ThreadPoolExecutor executors= null;
	private UrlFetcher urlFetcher;
	

	public HttpBroadcastClient() {
		// TODO Auto-generated constructor stub	
	}
	
	public void destroy() {
		if(executors != null) {
			executors.shutdown();
		}	
		logger.info("httpbroadcastclient destroyed");
	}
	public HttpBroadcastClient (UrlFetcher urlFectcher) {
		this.urlFetcher = urlFectcher;
	}
	private volatile boolean inited = false;
	public void init() {
		if(inited == false) {
			synchronized (this) {
				if(inited == false) {
					//executors = Executors.newFixedThreadPool(10, new MemKVBroadcastThreadFactory());
					if(executors == null) {
						executors = new ThreadPoolExecutor(10, 20, 10, TimeUnit.SECONDS, 
								new ArrayBlockingQueue<Runnable>(100), new NamedThreadFactory("memkv-broadcast-thread"));
					}
					inited = true;
				}
			}
		}	
	}
	//url是一个基础的url，然后把request里的参数拼到这个url上
	//比如url 是 http://192.168.1.10:8080/mb/memkvrefresher.do,request里面name="memory",cmd="invalid"，key="auth"
	//拼成http://192.168.1.10:8080/mb/memkvrefresher.do?name=memory&cmd=invalid&key=auth
	//之行结果有三种
	//0 成功使某个key失效
	//1 成功执行了，但是对应的key本来就失效了或者不存在
	//2 任何其他错误
	public static Map sendRequest(String url, Map request) {
		// TODO Auto-generated method stub
		HttpClient hc = new DefaultHttpClient();
		String name = (String) request.get("name");
		String cmd = (String) request.get("cmd");
		String key = (String) request.get("key");
		String hkey = (String) request.get("hkey");
		url = url + "?name="+name+"&cmd="+cmd+"&key="+key;
		if(hkey != null) {
			url = url + "&hkey=" + hkey;
		}
		HttpGet get = new HttpGet(url);
		Map result = new HashMap();
		HttpResponse response = null;
		logger.info("start sending request:"+url);
		try {
			response = hc.execute(get);
			if(response == null) {
				logger.info("response is null for url:"+url);
				result.put("result", "2");
			} else {
				result.put("result", fetchResult(url,response));
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			result.put("result", "2");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			result.put("result", "2");
		} finally {
			logger.info("release connection for url:"+url);
			hc.getConnectionManager().shutdown();
		}
		return result;
	}

	/**
	 * 0 成功
	 * 1 之行成功，但是key本来就是null
	 * 2 错误
	 * @param response
	 * @return
	 */
	public static String fetchResult(String url,HttpResponse response) {
		InputStream is;
		try {
			is = response.getEntity().getContent();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			return "2";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			return "2";
		}
		byte[] buffer = new byte[1024];
		int len = 0;
		StringBuilder sb = new StringBuilder();
		try {
			while((len = is.read(buffer,0,1024)) != -1) {
				char[] tmp = new char[len];
				for(int i = 0; i < len; i++) {
					tmp[i] = (char) buffer[i];
				}
				sb = sb.append(tmp);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
			return "2";
		}
		String body = sb.toString();
		logger.info("response body from "+url +":"+body);
		int index = body.indexOf("result:");
		String result = null;
		if(index == -1) {
			index = body.indexOf("result\":");
			if(index == -1) {
				return "2";
			} else {
				result = body.substring(index+8,index+9);
				if(result != null && result.equals("\"")) {
					result = body.substring(index+9,index+10);
				}
			}
		} else {
			result = body.substring(index+7, index+8);
			if(result != null && result.equals("\"")) {
				result = body.substring(index+8,index+9);
			}

		}
		if(result == null || result.isEmpty()) {
			return "2";
		}
		if(result.equals("1") || result.equals("0")) {
			return result;
		}
		return "2";
	}
	
	public List<String> getUrls() {
		// TODO Auto-generated method stub
		List<String> urls = urlFetcher.fetchUrls();
		return urls;
	}
	
	/**向获取的url发送请求，同时发出去，然后等待结果，把结果以url为key放到Map里**/
	public Map broadCast(Map request,List<String> urls) {
		if(!inited) {
			init();
		}
		Map<String,Future<Map>> results = new HashMap<String,Future<Map>>();
		Map<String,String> ret = new HashMap<String,String>();
		for(String url : urls) {
			Future<Map> future = executors.submit(new NotifyTask(url,request));
			results.put(url, future);
		}
		for(String key : results.keySet()) {
			Future<Map> future = results.get(key);
			try {
				Map result = future.get(10, TimeUnit.SECONDS); //最长等待10s
				if(result == null) {
					logger.error("can't get result from future");
					ret.put(key, "2");
				} else {
					String code = (String) result.get("result");
					if(code == null) {
						ret.put(key, "2");
					} else {
						ret.put(key, code);
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
				ret.put(key, "2");
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
				ret.put(key, "2");
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
				ret.put(key, "2");
			}
		}
		return ret;
	}

	@Override
	public boolean invalid(String name, String key) {
		// TODO Auto-generated method stub
		Map req = new HashMap();
		req.put("name", name);
		req.put("cmd", "invalid");
		req.put("key", key);
		return broadCastNtimes(req, 3);	
	}

	@Override
	public boolean hinvalid(String name, String key) {
		// TODO Auto-generated method stub
		Map req = new HashMap();
		req.put("name", name);
		req.put("cmd", "hinvalid");
		req.put("key", key);
		return broadCastNtimes(req, 3);
	}

	@Override
	public boolean hinvalid(String name, String key, String hkey) {
		// TODO Auto-generated method stub
		Map req = new HashMap();
		req.put("name", name);
		req.put("cmd", "hinvalid");
		req.put("key", key);
		req.put("hkey",hkey);
		return broadCastNtimes(req, 3);
	}
	
	public boolean broadCastNtimes(Map req,int n) {
		List<String> urls = getUrls();
		if(urls == null || urls.isEmpty()) {
			logger.info("urls are empty");
			return false;
		}
		Map ret = null;
		for(int i = 0; i < n; i++) {
			logger.info("broadcast for the "+(i+1)+" time");
			ret = broadCast(req,urls);
			List<String> failedUrls = new ArrayList<String>();
			for(String url : urls) {
				String result = (String) ret.get(url);
				if(result != null && !result.equals("0")) {
					failedUrls.add(url);
				}
			}
			if(failedUrls.isEmpty()) {
				break;
			} else {
				urls = failedUrls;
			}
		}
		if(urls.isEmpty()) {
			return true;
		} else {
			
			for(String url : urls) {
				String result = (String) ret.get(url);
				if(result == null || result.equals("2")) {
					return false;
				}
			}
			return true;
		}
	}
	
	public static void main(String[] args) throws ClientProtocolException, IOException {
		HttpClient hc = new DefaultHttpClient();
		ClientConnectionManager manager = hc.getConnectionManager();
		HttpGet get = new HttpGet("http://www.baidu.com");
		HttpResponse response = hc.execute(get);
		InputStream is = response.getEntity().getContent();
		byte[] buffer = new byte[1024];
		int len = 0;
		StringBuilder sb = new StringBuilder();
		while((len = is.read(buffer,0,1024)) != -1) {
			sb = sb.append(new String(buffer));
		}
		System.out.println(sb.toString());
		
		
	}

	public UrlFetcher getUrlFetcher() {
		return urlFetcher;
	}

	public void setUrlFetcher(UrlFetcher urlFetcher) {
		this.urlFetcher = urlFetcher;
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


	@Override
	public boolean commitPipe() {
		// TODO Auto-generated method stub
		return false;
	}
}
/**
 * 发送请求，返回结果，放到Map里,result字段是结果
 * @author niuxinli
 *
 */
class NotifyTask implements Callable<Map> {	
	private String url;
	private Map request;
	public NotifyTask(String url,Map request) {
		this.url = url;
		this.request = request;
	}
	@Override
	public Map call() throws Exception {
		// TODO Auto-generated method stub
		return HttpBroadcastClient.sendRequest(url, request);
	}
}

