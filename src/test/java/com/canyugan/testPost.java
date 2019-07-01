//package com.canyugan;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import okhttp3.MediaType;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import okhttp3.ResponseBody;
//
//public class testPost 
//{
//	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
//	static OkHttpClient client = new OkHttpClient();
//	
//	static String post(String url, String json) throws IOException 
//	{
//	   // RequestBody body = RequestBody.create(JSON, json);
//	    Request request = new Request.Builder().url(url).build();
//	    Response response = client.newCall(request).execute();
//	    if (response.isSuccessful()) {
////	    	Headers headers = response.headers();
////	        int size = headers.size();
////	        for(int index = 0;index < size;index++)
////	        {
////	        	String name = headers.name(index);
////	        	String value = headers.value(index);
////	        	System.out.println("name:" + name + ",value:" + value);
////	        }
//	    	ResponseBody body = response.body();
//	    	System.out.println(body.contentLength());
//	    	BufferedReader bf = new BufferedReader(body.charStream());
//	        while(bf.ready())
//	        {
//	        	System.out.println(bf.readLine());
//	        	try {
//					Thread.sleep(500);
//				} catch (Exception e) {
//				}
//	        }
//	    	return null;
//	    } else {
//	        throw new IOException("Unexpected code " + response);
//	    }
//	}
//	
//	public static void main(String[] args) {
//		try {
//			post("https://blog.csdn.net/u013651026/article/details/79738059", null);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}
