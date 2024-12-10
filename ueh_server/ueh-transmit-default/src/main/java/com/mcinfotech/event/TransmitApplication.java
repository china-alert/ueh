package com.mcinfotech.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@EnableScheduling
@SpringBootApplication(exclude = {FreeMarkerAutoConfiguration.class})
@EnableFeignClients
public class TransmitApplication{
	public static void main(String[] args){
		SpringApplication.run(TransmitApplication.class);
	}
}
