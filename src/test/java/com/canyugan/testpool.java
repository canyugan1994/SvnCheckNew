package com.canyugan;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.canyugan.configuration.ThreadPoolFactory;

public class testpool 
{
	public static void main(String[] args) 
	{
		ThreadPoolExecutor pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,new LinkedBlockingQueue(1),new ThreadPoolFactory("JiaoFu"));
		pool.execute(new Runnable() 
		{
			@Override
			public void run() {
				while(true) {
					try {
						Thread.sleep(1000);
						System.out.println(Thread.currentThread().getName());
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
		});
	}
}
