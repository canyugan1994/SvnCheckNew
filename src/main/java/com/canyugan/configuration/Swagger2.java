package com.canyugan.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.annotations.ApiOperation;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Api doc For 交付物检查工具
 * @author caorui
 */
@Configuration
@EnableSwagger2
public class Swagger2 
{
	@Bean
	public Docket createRestApi() 
	{
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.withMethodAnnotation(ApiOperation.class))
				.paths(PathSelectors.any())
				.build();
	}
	
	private ApiInfo apiInfo() 
	{
		return new ApiInfoBuilder()
				.title("交付物检查工具api doc")
				.description("Api docs For 交付物检查工具")
				.version("2.0")
				.build();
	}
}
