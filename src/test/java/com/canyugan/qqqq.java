package com.canyugan;

public class qqqq {
	public static void main(String[] args) {
		String s1 = "2019-08-06 13:45";
		System.out.println(s1.substring(0,10).replace("-",""));
		
		String re = "Release-20190806";
		System.out.println(re.split("-")[1]);
	}
}
