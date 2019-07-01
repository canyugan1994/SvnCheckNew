package com.canyugan.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * svn文档检查进度
 * @author caorui
 *
 */
@RestController
public class WebSocketController implements WebSocketHandler 
{
	private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketController.class);
	@Autowired
	private JedisPool JedisPool;//redis连接池
	private static ApplicationContext applicationContext;
	private static final List<WebSocketSession> sessions = new LinkedList<WebSocketSession>();
	private static Set<String> request_ids = new HashSet<String>();
	
	/**
	 * 解决注入失败问题
	 */
	public static void setApplicationContext(ApplicationContext applicationContext) {
        WebSocketController.applicationContext = applicationContext;
    }

	/**
	 * 连接建立以后
	 */
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
	    sessions.add(session);
	    //request_id的作用是为了关闭websocket的时候 可以停止handMessage发送进度行为 
	    String request_id = (String)session.getAttributes().get("request_id");
	    request_ids.add(request_id);
	    LOGGER.info("request_id[" + request_id + "] 打开了一个websocket连接,time:" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date()));
	}

	/**
	 * 给前端发送消息
	 */
	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception 
	{
		String request_id = (String) session.getAttributes().get("request_id");
		LOGGER.info("收到一条消息，来自于reques_id[" + request_id + "],message:" + message.toString());

		JSONObject result = new JSONObject();
		if (request_ids.contains(request_id)) 
		{
			double loading = 0;
			Jedis jedis = null;

			/**
			 * 考虑任务执行失败 任务key被删除的情况
			 */
			try {
				jedis = this.JedisPool.getResource();
				String value = jedis.get(request_id);
				
				//如果value为空 可能是检查任务执行失败 任务key被删除
				if(value == null) {
					//任务key被删了 可能遇到了失败 结束websocket
					LOGGER.info("-->【 根据request_id：[" + request_id +  "]获得的value为null 可能遇到了任务执行失败的情况 关闭websocket】");
					result.put("status", "error");
					result.put("message", "任务执行失败 进度不再提供");
					TextMessage resp_error = new TextMessage(result.toJSONString().getBytes());
					session.sendMessage(resp_error);
					session.close();
					sessions.remove(session);
				}else {
					JSONObject parse_value = JSONObject.parseObject(value);
					float project_sum = 0;//项目模板检查总数量
					Set<String> keys = parse_value.keySet();
					for (String key : keys) {
						JSONObject temp_jsonobject = (JSONObject) parse_value.get(key);
						JSONArray temp_array = temp_jsonobject.getJSONArray("checkTable");
						int length = temp_array.size();
						project_sum += length;
					}
					//	如果比例达到100% || 手动关闭了websocket ，那么就不继续执行了
					while (loading < 100 && request_ids.contains(request_id)) {
						float aleady_done = 0;
						value = jedis.get(request_id);
						/**
						 * 任务执行失败 key可能被删了 
						 */
						if(value == null) {
							//任务key被删了 可能遇到了失败 结束websocket
							LOGGER.info("-->【 根据request_id：[" + request_id +  "]获得的value为null 可能遇到了任务执行失败的情况 关闭websocket】");
							result.put("status", "error");
							result.put("message", "任务执行失败 进度不再提供");
							TextMessage resp_error = new TextMessage(result.toJSONString().getBytes());
							session.sendMessage(resp_error);
							session.close();
							sessions.remove(session);
						}else {
							parse_value = JSONObject.parseObject(value);
							keys = parse_value.keySet();
							for (String key : keys) {
								JSONObject temp_jsonobject = (JSONObject) parse_value.get(key);
								JSONArray temp_array = temp_jsonobject.getJSONArray("checkTable");
								int length = temp_array.size();
								for (int index = 0; index < length; index++) {
									if (temp_array.getJSONObject(index).getInteger("done").intValue() == 1) {
										aleady_done++;
									}
								}
							}
							//计算比例
							loading = (aleady_done / project_sum) * 100;
							LOGGER.info("-->request_id为" + request_id + "的进度信息 【 模板总数:" + project_sum
									+ ",已完成:" + aleady_done + ",进度:" + loading + "% 】");

							result.put("status", "success");
							result.put("message", String.valueOf(loading));
							TextMessage resp_success = new TextMessage(result.toJSONString().getBytes());
							session.sendMessage(resp_success);

							try {
								Thread.sleep(2000);
							} catch (Exception e) {
								LOGGER.info("-->【 sleep异常，异常信息：" + e.getMessage() + " 】");
								result.put("status", "error");
								result.put("message", "服务器异常 请联系管理员");
								TextMessage resp_error = new TextMessage(result.toJSONString().getBytes());
								session.sendMessage(resp_error);
								session.close();
								sessions.remove(session);
							}
						}
					}
					//退出轮询 
					LOGGER.info("-->【 request_id:[" + request_id + "] 的websocket长连接由于被手动取消或者任务执行比例达到100%关闭 】");
					session.close();
					sessions.remove(session);
				}
			} catch (Exception e) {
				LOGGER.info("-->【 加载进度失败，失败信息:" + e.getMessage() + " 】");
				result.put("status", "error");
				result.put("message", "服务器异常 请联系管理员");
				TextMessage resp_error = new TextMessage(result.toJSONString().getBytes());
				session.sendMessage(resp_error);
				session.close();
				sessions.remove(session);
			}
		} else {
			LOGGER.info("不存在该request_id对应的进度信息");
			result.put("status", "error");
			result.put("message", "不存在该request_id对应的进度信息");
			TextMessage resp_error = new TextMessage(result.toJSONString().getBytes());
			session.sendMessage(resp_error);
			session.close();
			sessions.remove(session);
		}
	}

	/**
	 * websocket连接出现初三那会是异常
	 */
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		String request_id = (String) session.getAttributes().get("request_id");
		LOGGER.info("-->【 request_id [" + request_id + "]发生了传输异常 】");

		JSONObject result = new JSONObject();
		result.put("status", "error");
		result.put("message", "websocket传输错误 不可恢复 请重新连接");
		TextMessage resp_error = new TextMessage(result.toJSONString().getBytes());
		session.sendMessage(resp_error);

		session.close();
		sessions.remove(session);
	}

	/**
	 * websokcet关闭 删除request_id 停止sendMessage
	 */
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		String request_id = (String) session.getAttributes().get("request_id");
		LOGGER.info("被request_id为[" + request_id + "]关闭了websocket");
		request_ids.remove(request_id);
		sessions.remove(session);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}
