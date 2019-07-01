//package com.canyugan;
//
//public class testmain 
//{
//	public static void main(String[] args) 
//	{
//		String fileName = "group:group1;path:M00/00/00/rBDRrly0M56AOLnCAANjILNfqZ8092.png";
//		
//		String group = fileName.substring(0, fileName.lastIndexOf(";"));
//		group = group.substring(group.lastIndexOf(":") + 1,group.length());
//		
//		String path = fileName.substring(fileName.lastIndexOf(";") + 1, fileName.length());
//		path = path.substring(path.lastIndexOf(":") + 1, path.length());
//		
//		System.out.println(group);
//		System.out.println(path);
//		
//		path = path.substring(path.lastIndexOf("/"),path.length());
//		System.out.println(path);
//	}
//}
