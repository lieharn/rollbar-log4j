package com.github.rollbar.log4j;

import org.apache.log4j.Logger;

public class FooBarTest {

	static Logger logger = Logger.getLogger(FooBarTest.class);

	public static void main(String[] args) {
		logger.info("our own appender succeeded");
	}

}
