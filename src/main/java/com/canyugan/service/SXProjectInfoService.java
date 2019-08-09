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
 * 双速项目信息接口
 * @author caorui
 */
@Service
public class SXProjectInfoService
{
	private static final Logger LOG = LoggerFactory.getLogger(SXProjectInfoService.class);
	@Value("${project_host}")
	private String project_host;
	@Value("${current_year}")
	private Integer current_year;
	
	public JSONArray sendServiceRequest() 
	{
		OkHttpClient client = new OkHttpClient()
                .newBuilder().connectTimeout(25000, TimeUnit.MILLISECONDS)//15s超时
                .readTimeout(25000, TimeUnit.MILLISECONDS).build();

		try {
			Request request = new Request.Builder().url(project_host).get().build();
		    Response response = client.newCall(request).execute();
			if (response.isSuccessful()) 
			{
				ResponseBody body = response.body();
				JSONObject result = JSONObject.parseObject(new String(body.bytes()));
				//双速接口返回数据
				JSONArray projectAttr = JSONArray.parseArray(result.get("status").toString());
				int length = projectAttr.size();
				JSONObject temp = new JSONObject();
				//封装返回数据
				JSONArray return_result = new JSONArray();
				for (int index = 0; index < length; index++) 
				{
					temp = (JSONObject) projectAttr.get(index);
					//过滤非当年项目
					if(!temp.getString("projectId").contains(String.valueOf(current_year))) {
						continue;
					}
					JSONObject return_result_single = new JSONObject();
					return_result_single.put("pProdLine", temp.get("pProdLine"));
					//项目组
					return_result_single.put("pTeam", temp.get("pTeam"));
					return_result_single.put("projectCatagory", temp.get("projectCatagory"));
					return_result_single.put("projectHeader", temp.get("projectHeader"));
					return_result_single.put("projectStatus", temp.get("projectStatus"));
					return_result_single.put("projectLists", temp.get("projectLists"));
					//项目名
					return_result_single.put("projectName", temp.get("projectName"));
					//项目编号
					return_result_single.put("projectCodeNum", temp.get("projectId"));
					return_result.add(return_result_single);
				}
				body.close();//关闭body 防止leak
				return return_result;
			}
			if(LOG.isInfoEnabled()) {
				LOG.info("-->【 双速项目信息接口失败 】");
			}
		} catch (Exception e) {
			if(LOG.isInfoEnabled()) {
				LOG.info("-->【 双速项目信息接口失败，失败信息:" + e.getMessage() + " 】");
			}
		}
		return null;
	}
}

