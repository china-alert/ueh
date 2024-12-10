package com.mcinfotech.event.probe.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cn.mcinfotech.data.service.util.DataSourceUtils;

/**
 * DataSource管理
 * 

 */
@Configuration
public class DataSourceConfig {

	@Value("${config.jdbc.url}")
	private String jdbcUrl;
	@Value("${config.jdbc.driverClassName}")
	private String driverClassName;
	@Value("${config.jdbc.databaseType}")
	private String databaseType;
	@Value("${config.jdbc.username}")
	private String username;
	@Value("${config.jdbc.password}")
	private String password;
	@Value("${config.jdbc.initialsize}")
	private int initialSize;
	@Value("${config.jdbc.maxtotal}")
	private int maxTotal;
	@Value("${config.jdbc.minidle}")
	private int minIdle;
	@Value("${config.jdbc.maxWaitSeconds}")
	private int maxWaitSeconds;
	
	@Bean
	public DataSource buildDataSource() {
		//String url="jdbc:postgresql://127.0.0.1:5432/ueh?currentSchema=ueh_ctrl&TimeZone=PRC&useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=GMT";
		//String driverClassName="org.postgresql.Driver";
		Properties prop=new Properties();
		prop.put("initial.size", new Integer(initialSize).toString());
		prop.put("max.total", new Integer(maxTotal).toString());
		prop.put("min.idle", new Integer(minIdle).toString());
		prop.put("max.wait.seconds", new Integer(maxWaitSeconds).toString());
		DataSource dataSource = DataSourceUtils.setupDataSource(jdbcUrl,driverClassName,databaseType,username,password,prop);
		return dataSource;
	}
	/*public DataSource rebuild(){
		Properties prop=new Properties();
		prop.put("initial.size", new Integer(initialSize).toString());
		prop.put("max.total", new Integer(maxTotal).toString());
		prop.put("min.idle", new Integer(minIdle).toString());
		prop.put("max.wait.seconds", new Integer(maxWaitSeconds).toString());
		return DataSourceUtils.setupDataSource(jdbcUrl,driverClassName,databaseType,username,password,prop);
	}*/
}
