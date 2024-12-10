package com.mcinfotech.event.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mcinfotech.event.domain.ProjectInfo;

/**
 * 从配置读取Project信息

 *
 */
@Configuration
public class ProjectInfoConfig {
	
	@Value("${project.id}")
	private int id;
	@Value("${project.name}")
	private String name;	
	@Value("${project.source}")
	private String source;
	
	@Bean
	public ProjectInfo buildProjectInfo() {
		ProjectInfo info=new ProjectInfo();
		info.setId(id);
		info.setName(name);
		info.setSource(source);
		return info;
	}
}
