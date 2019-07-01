//package com.canyugan.excel;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.InputStream;
//import java.util.List;
//import com.alibaba.excel.ExcelReader;
//import com.alibaba.excel.metadata.Sheet;
//import com.alibaba.excel.support.ExcelTypeEnum;
//
//public class testExcel 
//{
//	public static void main(String[] args) 
//	{
//		ExcelListener listener = new ExcelListener();
//		InputStream is = null;
//		try {
//			is = new FileInputStream(new File("/Users/caorui/Desktop/立项类项目清单.xlsx"));
//			ExcelReader reader = new ExcelReader(is, ExcelTypeEnum.XLSX, null, listener);
//			reader.read(new Sheet(1, 1,ReadModel.class));
//			List<Object> read_result = listener.getDatas();
//			for(Object object:read_result)
//			{
//				ReadModel info = (ReadModel) object;
//				System.out.println(info.getProjectName() + "," + 
//				                   info.getITProjectManager() + "," + 
////						           info.getChildProjectNum() + "," + 
////				                   info.getChildProjectNameShuangSu() + "," +
////						           info.getChildProjectNameZhouBao() + "," +
////				                   info.getChildProjectNameZhanBao() + "," +
////						           info.getChildProjectNameZhanBao() + "," +
//				                   info.getITManager() + "," +
//						           info.getIsBuild() + "," +
//				                   info.getSvnUrl() + "," + 
//						           info.getNotes()
//						           );
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				is.close();
//			} catch (Exception e2) {
//				e2.printStackTrace();
//			}
//		}
//	}
//
//	private static void reader1() {
//		ExcelListener listener = new ExcelListener();
//		InputStream is = null;
//		try {
//			is = new FileInputStream(new File("/Users/caorui/Desktop/立项类项目清单.xlsx"));
//			ExcelReader reader = new ExcelReader(is, ExcelTypeEnum.XLSX, null, listener);
//			reader.read(new Sheet(1, 1,ReadModel.class));
//			List<Object> read_result = listener.getDatas();
//			for(Object object:read_result){
//				ReadModel info = (ReadModel) object;
//				System.out.println(info.getProjectName() + "," + info.getITProjectManager()
//						+ "," + info.getITManager() + "," + info.getSvnUrl());
//			}
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				is.close();
//			} catch (Exception e2) {
//				e2.printStackTrace();
//			}
//		}
//	}
//}
