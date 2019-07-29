package com.canyugan.service;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * 双速需求文档信息接口
 * 需求库文档检查需要用到
 * @author caorui
 */
@Service
public class SXNeedService
{
	private static final Logger LOG = LoggerFactory.getLogger(SXNeedService.class);
	@Value("${rqn_host}")
	private String rqn_host;
	
	public JSONArray sendServiceRequest() 
	{
		OkHttpClient client = new OkHttpClient()
                .newBuilder().connectTimeout(25000, TimeUnit.MILLISECONDS)//15s超时
                .readTimeout(25000, TimeUnit.MILLISECONDS).build();
		try 
		{
			Request request = new Request.Builder().url(rqn_host).get().build();
		    Response response = client.newCall(request).execute();
			if (response.isSuccessful()) 
			{
				ResponseBody body = response.body();
				JSONObject result = JSONObject.parseObject(new String(body.bytes()));

				JSONArray need_project = JSONArray.parseArray(result.get("status").toString());
				int length = need_project.size();
				JSONObject temp = new JSONObject();
				JSONArray return_result = new JSONArray();
				for (int index = 0; index < length; index++) 
				{
					temp = (JSONObject) need_project.get(index);
					if ("已归档".equals(temp.get("requirementStatu"))) {
						JSONObject return_result_single = new JSONObject();
						return_result_single.put("project_num", temp.get("requirementId"));//需求编号 RQN-2018-IT039-0039
						return_result_single.put("projectName", temp.get("projectName"));//项目名称  PJ-LX-2018-002-001-人工智能平台运营
						return_result_single.put("projectVersion", temp.get("fixVersion"));//投产窗口 Release-20181117
						return_result.add(return_result_single);
					}
				}
				body.close();//关闭body 防止leak
				return return_result;
			}
			LOG.info("-->【 双速需求文档接口调用失败 】");
			return null;
		} catch (Exception e) {
			LOG.info("-->【 双速需求文档接口调用失败，失败信息：" + e.getMessage() + " 】");
			return null;
		}
	}
}

