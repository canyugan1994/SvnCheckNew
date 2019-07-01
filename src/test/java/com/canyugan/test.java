package com.canyugan;

import java.util.concurrent.atomic.AtomicInteger;

public class test {
	public static void main(String[] args) {
		AtomicInteger integer = new AtomicInteger(0);
		integer.incrementAndGet();
		System.out.println(integer.decrementAndGet());
	}
}
