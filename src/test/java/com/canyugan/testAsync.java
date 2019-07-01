//package com.canyugan;
//
//import java.io.IOException;
//
//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.Headers;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//
//public class testAsync 
//{
//	  static final OkHttpClient client = new OkHttpClient();
//
//	  static public void run() throws Exception 
//	  {
//	    Request request = new Request.Builder().url("https://blog.csdn.net/u013651026/article/details/79738059").build();
//	
//	    client.newCall(request).enqueue(new Callback() 
//	    {
//		      @Override 
//		      public void onFailure(Call call, IOException e) {
//		        e.printStackTrace();
//		      }
//		
//		      @Override 
//		      public void onResponse(Call call, Response response) throws IOException 
//		      {
//		        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
//		        Headers responseHeaders = response.headers();
//		        for (int i = 0, size = responseHeaders.size(); i < size; i++) 
//		        {
//		          System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
//		        }
//		
//		        //System.out.println(response.body().string());
//		      }
//	    });
//	  }
//	  
//	  public static void main(String[] args) {
//		try {
//			run();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}
