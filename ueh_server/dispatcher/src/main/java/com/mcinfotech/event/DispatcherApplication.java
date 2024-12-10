package com.mcinfotech.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication(exclude = {FreeMarkerAutoConfiguration.class})
public class DispatcherApplication{
	public static void main(String[] args){
		SpringApplication.run(DispatcherApplication.class,args);
	}
}
