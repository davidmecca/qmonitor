package com.hps.mayo.servlet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ImportResource;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
@ImportResource("classpath*:/spring/*.xml")
@EnableAutoConfiguration(exclude = { DataSourceAutoConfiguration.class })
public class MasterdataQueueMonitorApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(MasterdataQueueMonitorApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(MasterdataQueueMonitorApplication.class, args);
	}
}
