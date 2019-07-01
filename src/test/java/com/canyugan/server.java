//package com.canyugan;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.ServerSocket;
//import java.net.Socket;
//
///**
// * 服务端的功能是负责保存客户端传过来的文件
// * @author caorui
// */
//public class server 
//{
//	public static void main(String[] args) 
//	{
//		try 
//		{
//			ServerSocket server = new ServerSocket(1994);
//			System.out.println("【 I'm server,i began to work 】");
//			Socket socket = null;
//			//监听客户端请求
//			while((socket = server.accept())!= null )
//			{
//				//获得客户端的一个连接请求 传给一个线程去处理
//				new Handler(socket).start();
//			}
//			//服务端关闭
//			server.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}
//class Handler extends Thread
//{
//	Socket socket;
//	//构造函数初始化
//	public Handler(Socket socket) 
//	{
//		this.socket = socket;
//	}
//	
//	@Override
//	public void run() 
//	{
//		try {
//			//获取输入流
//			InputStream is = socket.getInputStream();
//			//虚伪的欢迎
//			System.out.println("收到客户端【" + socket.getRemoteSocketAddress() + "】的消息");
//			File file = new File("/Users/caorui/Desktop/2020张宇高等数学18讲02_new.pdf");
//			//判断文件是否存在 不存在就新建
//			if(!file.exists()){
//				file.createNewFile();
//			}
//			FileOutputStream fileOutputStream = new FileOutputStream(file);
//			//暂存从输入流读取的2048字节大小的流
//			byte[] buffer = new byte[2048];//2kb
//			int readLength = is.read(buffer);
//			//read方法获取不到字节时会返回-1 但是网络通信时需要借助is输入流可获取字节数来判断是否读取结束
//			while(readLength != -1 && is.available() > 0){
//				fileOutputStream.write(buffer,0,readLength);
//				readLength = is.read(buffer);
//			}
//			
//			//获取输出流
//			OutputStream os = socket.getOutputStream();
//			//向客户端回送处理成功响应
//			os.write(("【 hi," + socket.getRemoteSocketAddress() 
//					+",i'm handle success with your request 】").getBytes());
//			os.flush();
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
//			fileOutputStream.close();
//			is.close();
//			os.close();
//			socket.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//}