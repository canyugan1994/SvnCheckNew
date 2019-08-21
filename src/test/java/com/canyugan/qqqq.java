package com.canyugan;

import java.net.URLDecoder;

public class qqqq {
	public static void main(String[] args) {
		//String s1 = "2019-08-06 13:45";
		//System.out.println(s1.substring(0,10).replace("-",""));
		
		//String re = "Release-20190806";
		//System.out.println(re.split("-")[1]);
		
		//String projectName = "云上卡中心（PJ-LX）";
		//String projectName = "云上卡中心(PJ-LX)";
		String projectName = "云上卡中心";
		String final_name =  null;
		String decode_name = URLDecoder.decode(projectName);
		int curr_index = -1;
		//tc推送参数项目名会附带() 活着（） 此处进行区分
		for(int index = 0;index < decode_name.length();index++) {
			if(decode_name.charAt(index) == '(') {
				curr_index = index;
				break;
			}else if(decode_name.charAt(index) == '（') {
				curr_index = index;
				break;
			}
		}
		final_name = decode_name.substring(0,curr_index == -1 ? decode_name.length() :curr_index);
		System.out.println(final_name);
	}
}
