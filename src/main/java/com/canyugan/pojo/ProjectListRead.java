package com.canyugan.pojo;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;

public class ProjectListRead {
	public static void main(String[] args) {
		ExcelListener list_listener = new ExcelListener();
		InputStream list_is = null;
		try {
			FileInputStream fis = new FileInputStream(new File("D:\\项目清单.xlsx"));
			try {
				byte[] buffer = new byte[fis.available() + 1];
				fis.read(buffer, 0, fis.available());
				list_is = new ByteArrayInputStream(buffer);

				ExcelReader list_reader = new ExcelReader(list_is, ExcelTypeEnum.XLSX, null, list_listener);
				list_reader.read(new Sheet(1, 1));
				
				List<Object> read_result = list_listener.getDatas();
				for (Object object : read_result) {
					ProjectListModel listinfo = (ProjectListModel) object;
					System.out.println(listinfo.getJiraProjectName() + "," + listinfo.getProjectCodeNum() + ","
						+ listinfo.getProjectName() + "," + listinfo.getProjectPerson());
					String text = JSON.toJSONString(listinfo);  
			        //System.out.println("toJsonString()方法：text=" + text);  
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				list_is.close();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}
}
