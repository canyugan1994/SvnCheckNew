//package com.canyugan.test;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//
//public class testspring 
//{
//	
//	static class Tree
//	{
//		Tree left;
//		Tree right;
//		String value;
//		
//		public void setValue(String value) {
//			this.value = value;
//		}
//		
//		public String getValue() {
//			return value;
//		}
//		
//		public void setLeft(Tree left) {
//			this.left = left;
//		}
//		public void setRight(Tree right) {
//			this.right = right;
//		}
//		
//		public Tree getLeft() {
//			return left;
//		}
//		public Tree getRight() {
//			return right;
//		}
//	}
//	
//	public void createBinTree() {
//		/**
//		 *            root
//		 *    left          right
//		 *  left1 left2        right
//		 */
//		Tree root = new Tree();
//		
//		Tree left = new Tree();
//		left.setValue("left1");
//		Tree right = new Tree();
//		right.setValue("right1");
//		
//		root.setLeft(left);
//		root.setRight(right);
//		
//		System.out.println(root.getRight().getValue());
//		
//	}
//	
//	public void writeByte() throws whrexception
//	{
//		throw new whrexception();
//	}
//	
//	public  void testSelfException() {
//		try {
//			new testspring().writeByte();
//		} catch (whrexception e) {
//			System.out.println(e.exceptionName());
//		}
//	}
//	
//	
//	public static void main(String[] args) 
//	{
//		OutputStream fileOutputStream  = null;
//		try {
//			InputStream fileInputStream = new FileInputStream("/Users/caorui/Desktop/Kafka源码设计与实现.pdf");
//			
//			File file = new File("/Users/caorui/Desktop/Kafka源码设计与实现_new.pdf");
//			if(!file.exists()){
//				file.createNewFile();
//			}
//			fileOutputStream = new FileOutputStream("/Users/caorui/Desktop/Kafka源码设计与实现_new.pdf");
//			
//			//
//			byte[] buffer = new byte[4096];
//			int readlen = fileInputStream.read(buffer);
//			while(readlen != -1)
//			{
//				fileOutputStream.write(buffer, 0, buffer.length);
//				readlen = fileInputStream.read(buffer);
//			}
//			//
//			
//			fileOutputStream.close();
//			fileInputStream.close();
//		} catch (FileNotFoundException e) {
//			System.out.println("文件不存在 ");
//			e.printStackTrace();
//			
//		} catch (IOException e) {
//			System.err.println("文件创建失败");
//			e.printStackTrace();
//			try {
//				fileOutputStream = new FileOutputStream("/Users/caorui");
//			} catch (FileNotFoundException e1) {
//				e1.printStackTrace();
//			}
//		}
//		System.out.println("....我还在");
//	}
//}
