package com.canyugan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class testThreadpool 
{
	public static void main(String[] args) 
	{
		List<String> nameList = new ArrayList<String>();
		nameList.add("我是1");
		nameList.add("我是2");
		nameList.add("我是3");
		
		System.out.println(nameList.contains("我是1"));
		/*
		//监控线程池状态 || 关闭线程池
		Map<String, ThreadPoolExecutor> monitor = new HashMap<String, ThreadPoolExecutor>();
		ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS,new LinkedBlockingQueue());
		
		//线程1
		Future<String> thread1 = pool.submit(new Callable<String>() 
		{
			@Override
			public String call() throws Exception {
				try {
					System.out.println("...thread1 开始启动");
					Thread.sleep(5000);
				} catch (Exception e) {
					System.out.println("...thread1 异常停止");
					return "error";
				}
				System.out.println("...thread1 执行结束 停止 开始返回");
				return "success";
			}
		});
		
		//线程2
		Future<String> thread2 = pool.submit(new Callable<String>() 
		{
			@Override
			public String call() throws Exception {
				try {
					System.out.println("...thread2 开始启动");
					Thread.sleep(6000);
					int i = 1/0;
				} catch (Exception e) {
					System.out.println("...thread2 异常停止");
					return "error";
				}
				System.out.println("...thread2 执行结束 停止 开始返回");
				return "success";
			}
		});
		
		pool.submit(new Callable<String>() 
		{
			@Override
			public String call() throws Exception {
				try {
					String thread1_result = thread1.get();
					String thread2_result = thread2.get();
					if(thread1_result != null && thread2_result != null) {
						if(thread1_result.equals("success")) {
							System.out.println("...thread1执行成功");
						}else if(thread1_result.equals("error")){
							System.out.println("...thread1执行失败");
						}
						
						if(thread2_result.equals("success")) {
							System.out.println("...thread2执行成功");
						}else if(thread2_result.equals("error")){
							System.out.println("...thread2执行失败");
						}
						System.out.println("...扫尾线程开始关闭线程池");
						pool.shutdown();
					}
				} catch (Exception e) {
					System.out.println("...thread3 异常停止");
					return "error";
				}
				System.out.println("...thread3 执行结束 停止 开始返回");
				return "success";
			}
		});
		monitor.put("admin_pool", pool);
		
//		try {
//			System.out.println("...main start");
//			Thread.sleep(3000);
//		} catch (Exception e) {
//			System.out.println("...main 异常停止");
//		}
//		
//		ThreadPoolExecutor admin_pool = monitor.get("admin_pool");
//		admin_pool.shutdownNow();
//		System.out.println("...main end");
 * 
 */
	}
}
