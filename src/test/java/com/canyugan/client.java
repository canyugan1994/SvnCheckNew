//package com.canyugan;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.Socket;
//import java.net.UnknownHostException;
//
//import com.canyugan.nio.file;
//
///**
// * 客户端向向服务端申请保存文件
// * @author caorui
// *
// */
//public class client 
//{
//	public static void main(String[] args) 
//	{
//		try 
//		{
//			//连接制定的服务器
//			Socket socket = new Socket("127.0.0.1",1994);
//			//输出流
//			OutputStream outputStream = socket.getOutputStream();
//			//获取文件流
//			FileInputStream fileStream = new FileInputStream(new File("/Users/caorui/Desktop/2020张宇高等数学18讲02.pdf"));
//			//暂存读取的2048字节大小的流
//			byte[] buffer = new byte[2048];//2kb
//
//			int readLength = fileStream.read(buffer);
//			while(readLength != -1 && fileStream.available() > 0){
//				outputStream.write(buffer,0,readLength);
//				readLength = fileStream.read(buffer);
//			}
//			System.out.println("【 i'm send file data done 】");
//			
//			//接收服务端处理结果
//			byte[] response = new byte[2048];
//			//获取输入流
//			InputStream inputStream = socket.getInputStream();
//			int length = inputStream.read(response);
//			StringBuilder sb = new StringBuilder();
//			while(length != -1){
//				sb.append(new String(response, 0, length));
//				length = inputStream.read(response);
//			}
//			//输出服务端的响应
//			System.out.println(sb.toString());
//			
//			/**
//			 * tips
//			 * 1:只要不关闭输入输出流和socket 服务端和客户端可以无限制的互发消息
//			 * 2:web开发的本质是服务端（一般是某种编程语言的socketServer）和浏览器（socketClient）互发消息
//			 *   互相协作来完成数据传输和显示
//			 * 3:web开发精髓就在于socket，无socket无web开发
//			 */
//			
//			//逐个关闭
//			inputStream.close();
//			outputStream.close();
//			fileStream.close();
//			socket.close();
//		} catch (UnknownHostException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}
