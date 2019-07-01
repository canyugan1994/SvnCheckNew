package com.canyugan.interceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandShakeInterceptor extends HttpSessionHandshakeInterceptor 
{
    private final Logger LOGGER = LoggerFactory.getLogger(HandShakeInterceptor.class);
    
    /*
     * 在WebSocket连接建立之前的操作，以鉴权为例
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, 
            WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception 
    {
        LOGGER.info("在websocket连接之前完成握手. ");
        
        // 获取url传递的参数，通过attributes在Interceptor处理结束后传递给WebSocketHandler
        // WebSocketHandler可以通过WebSocketSession的getAttributes()方法获取参数
        ServletServerHttpRequest serverRequest = (ServletServerHttpRequest) request;
        String request_id = serverRequest.getServletRequest().getParameter("request_id");

        if (request_id != null) {
            LOGGER.info("验证通过. WebSocket connecting.... ");
            attributes.put("request_id", request_id);
            return super.beforeHandshake(request, response, wsHandler, attributes);
        } else {
            LOGGER.error("验证失败. WebSocket will not connect. ");
            return false;
        }
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
            WebSocketHandler wsHandler, Exception ex) {
       
    }
}