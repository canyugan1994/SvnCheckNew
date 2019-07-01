package com.canyugan;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import com.canyugan.controller.WebSocketController;

/**
 * svn文件检测
 * @author caorui
 */
@SpringBootApplication
@MapperScan("com.canyugan.dao")
public class SVNCheck 
{
	public static void main(String[] args) 
	{
		//SpringApplication.run(SVNCheck.class, args);
		SpringApplication springApplication = new SpringApplication(new Object[] { SVNCheck.class });
		ConfigurableApplicationContext configurableApplicationContext = springApplication.run(args);
		WebSocketController.setApplicationContext(configurableApplicationContext);
	}
}
