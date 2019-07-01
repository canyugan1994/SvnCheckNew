//package com.canyugan;
//
//import java.io.IOException;
//
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//
//public class testGet 
//{
//	public static void main(String[] args) 
//	{
//		OkHttpClient client = new OkHttpClient();
//		Request request = new Request.Builder().url("http://localhost:9102/mlanxin/?timestamp=873526143&signature=1qaz2wsx3edc&nonce=123&echostr=qwertyasdfg&msgcontent=<xml><ToUserName>canyugan</ToUserName><FromUserName>caorui</FromUserName><CreateTime>1348831860</CreateTime><MsgType>text</MsgType><Content>曹瑞</Content><MsgId>1qaz2wsx3edc4rfv5tgb6yhn</MsgId></xml>").build();
//	    //Response response;
//	    int count = 2000;
//		try {
//			while(count >=0){
//				client.newCall(request).execute();
//				try {
//					count--;
//					Thread.sleep(100);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}
