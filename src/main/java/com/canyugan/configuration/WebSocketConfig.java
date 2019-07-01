package com.canyugan.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.canyugan.controller.WebSocketController;
import com.canyugan.interceptor.HandShakeInterceptor;

/**
 * websocket相关配置
 * @author caorui
 *
 */
@Configuration
@EnableWebMvc
@EnableWebSocket
public class WebSocketConfig extends WebMvcConfigurerAdapter implements WebSocketConfigurer 
{

    @Autowired
    private  WebSocketController webSocketController;

    
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) { 
    	registry.addHandler(webSocketController, "/svnCheck/socket/svnLoading")
    			.addInterceptors(new HandShakeInterceptor()).setAllowedOrigins("*"); 
    }

}