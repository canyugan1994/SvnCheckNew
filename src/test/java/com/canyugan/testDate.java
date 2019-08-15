package com.canyugan;

import com.canyugan.util.DateUtil;

public class testDate {
	public static void main(String[] args) {
		//String str = "云上卡中心测试文档caorui19941029PJ-LX-2019-云上卡中心";
		//System.out.println(str.split("caorui19941029")[1]);
		//String str = "O2O统一模块维护优化项目（PJ-WH-2019-068）";
		//System.out.println(str.split("（")[0]);
		String date = "2019-08-09 00:00:00.0";
		System.out.println(date.substring(0,10).replace("-",""));
	}
}
