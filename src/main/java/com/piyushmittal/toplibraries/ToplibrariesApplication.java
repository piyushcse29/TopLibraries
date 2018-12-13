package com.piyushmittal.toplibraries;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;

@SpringBootApplication
public class ToplibrariesApplication implements CommandLineRunner {

	String google_key = "";
	String google_cx = "";

	@Autowired
	TopLibrariesAnalytics analytics;
	public static void main(String[] args) {
		SpringApplication.run(ToplibrariesApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		analytics.webCrawler(args[0],google_key,google_cx);
	}
}
