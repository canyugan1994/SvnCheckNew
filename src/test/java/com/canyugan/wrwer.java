package com.canyugan;

import java.util.concurrent.atomic.AtomicInteger;

public class wrwer {
	public static void main(String[] args) {
		AtomicInteger max_person_num = new AtomicInteger(0);
		
		new Thread() {
			public void run() {
				while(max_person_num.get() < 100) {
					System.out.println("t1:" + max_person_num.getAndIncrement());
				}
			};
		}.start();;
		
		new Thread() {
			public void run() {
				while(max_person_num.get() < 100) {
					System.out.println("t2:" + max_person_num.getAndIncrement());
				}
			};
		}.start();;
		

		try {
			Thread.sleep(4000);
		} catch (Exception e) {
			// TODO: handle exception
		}
		System.out.println();
		System.out.println(max_person_num.get());
	}
}
