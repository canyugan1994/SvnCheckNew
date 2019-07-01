package com.canyugan;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSONObject;

//@RunWith(SpringJUnit4ClassRunner.class)
//public class MockTest 
//{
//	
//	private final static String url = "http://localhost:9102";
//	private RestTemplate restTemplate = new RestTemplate();
//	
//	@Test
//	public void query()
//	{
//		try {
//			//请求头
//			HttpHeaders headers = new HttpHeaders();
//			headers.add("Cookie", "UUMS=1qaz2wsx");
//			
//			JSONObject body = new JSONObject();
//			body.put("pageIndex",1);
//			body.put("pageNum", 20);
//			HttpEntity httpEntity = new HttpEntity<>(body,headers);
//			ResponseEntity<String> responseEntity = 
//					restTemplate.exchange(url + "/mlanxin/getAllClassify",
//						HttpMethod.POST, 
//						httpEntity, 
//						String.class);
//			System.out.println("--->" + responseEntity.getBody().toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//}
