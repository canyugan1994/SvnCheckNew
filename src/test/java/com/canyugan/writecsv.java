//package com.canyugan;
//
//import java.io.FileOutputStream;
//import java.io.OutputStreamWriter;
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVPrinter;
//
//public class writecsv 
//{
//	public static void main(String[] args) {
//		try {
//			new writecsv().testWrite();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
//	 public void testWrite() throws Exception {
//	        FileOutputStream fos = new FileOutputStream("/Users/caorui/Desktop/ModelTrainData_standard.csv");
//	        OutputStreamWriter osw = new OutputStreamWriter(fos,"UTF-8");
//
//	        CSVFormat csvFormat = CSVFormat.DEFAULT.withHeader("标准问", "标准答");
//	        CSVPrinter csvPrinter = new CSVPrinter(osw, csvFormat);
//
//	        csvPrinter.printRecord("你早上吃了么","问好");
//	        csvPrinter.printRecord("分布式集群怎么搭建","集群搭建");
//
//	        csvPrinter.flush();
//	        csvPrinter.close();
//	        System.out.println("write over");
//	    }
//}
