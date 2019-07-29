package com.canyugan.configuration;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolFactory implements ThreadFactory
{
	private String namePrefix;
	private AtomicInteger nextId = new AtomicInteger(1);

	public ThreadPoolFactory(String whatFeaturOfGroup) {
		namePrefix = whatFeaturOfGroup;
	}
	
	@Override
	public Thread newThread(Runnable task) 
	{
		String name = namePrefix + "-" + nextId.getAndIncrement();
		Thread thread = new Thread(null,task,name,0);
		return thread;
	}
	
}
