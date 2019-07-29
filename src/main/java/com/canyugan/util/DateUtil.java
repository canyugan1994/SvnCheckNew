package com.canyugan.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 时间工具类
 * @author caorui
 */
public class DateUtil 
{
	private static final ThreadLocal<DateFormat> df = new ThreadLocal<DateFormat>() 
	{
		@Override
		protected DateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	};
	
	public String currentDateTime()
	{
		return df.get().format(new Date());
	}
}
