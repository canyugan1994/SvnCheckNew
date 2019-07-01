//package com.canyugan;
//
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.concurrent.TimeUnit;
//import org.apache.http.HttpHost;
//import org.elasticsearch.action.search.SearchRequest;
//import org.elasticsearch.action.search.SearchResponse;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.common.unit.TimeValue;
//import org.elasticsearch.index.query.QueryBuilders;
//import org.elasticsearch.search.SearchHit;
//import org.elasticsearch.search.SearchHits;
//import org.elasticsearch.search.builder.SearchSourceBuilder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * ES查询
// * @author caorui
// *
// */
//public class ESUtil 
//{
//	private static final Logger LOG;
//	private static RestHighLevelClient client; 
//	static {
//		client = new RestHighLevelClient(RestClient.builder(new HttpHost("172.16.209.174", 9200, "http")));
//		LOG = LoggerFactory.getLogger(ESUtil.class);
//	}
//	 
//	
//	public static void main(String[] args) 
//	{
//		Map<String, Map<String, String>> result = queryResultByTitle("caorui");
//		System.out.println("查询到结果数量：" + result.size());
//		System.out.println(result.get("0").toString());
//	}
//
//	private static Map<String, Map<String, String>> queryResultByTitle(String title) 
//	{
//		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();
//		try{
//			LOG.info("-->【 ES查询开始】");
//            // 1、创建search请求
//            SearchRequest searchRequest = new SearchRequest("document_splider"); 
//            searchRequest.types("doc");
//            
//            // 2、用SearchSourceBuilder来构造查询请求体 ,请仔细查看它的方法，构造各种查询的方法都在这。
//            SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); 
//            sourceBuilder.query(QueryBuilders.matchQuery("title", title));
//            sourceBuilder.timeout(new TimeValue(4, TimeUnit.SECONDS)); 
//            searchRequest.source(sourceBuilder);
//            
//            //3、发送请求        
//            SearchResponse searchResponse = client.search(searchRequest);
//            
//            //4、处理搜索命中文档结果
//            SearchHits hits = searchResponse.getHits();
//            long totalHits = hits.getTotalHits();
//            SearchHit[] searchHits = hits.getHits();
//            LOG.info("-->【 命中查询数量：" + totalHits + "】");
//
//            if(totalHits == 0){
//            	Map<String, String> article = new HashMap<>();
//            	article.put("title", "没有合适的答案");
//            	article.put("content", "亲 本机器人宝宝没有查询到您想要的结果");
//            	result.put("0",article);
//            	return result;
//            }else if(totalHits > 5){
//            	LOG.info("-->【 查询结果大于5条 】");
//            	//大于5条 只取5条
//            	int count = 0;
//            	for (SearchHit hit : searchHits)
//                {
//            		Map<String, String> article = new HashMap<>();
//                    //取_source字段值
//            		float score = hit.getScore();//取评分
//                    Map<String, Object> sourceAsMap = hit.getSourceAsMap(); // 取成map对象
//                    
//                    if(score > 6){
//                    	article.put("title",(String) sourceAsMap.get("title"));
//                        article.put("content",(String) sourceAsMap.get("content"));
//                        result.put(String.valueOf(count),article);
//                        
//                        LOG.info("-->结果title：【 " +  article.get("title") + ",评分：" + score + " 】");
//                        
//                        if(++count >= 5){
//                        	break;
//                        }
//                    }
//                }
//            }else{
//            	LOG.info("-->【 查询结果少于5条 】");
//            	int count = 0;
//            	for (SearchHit hit : searchHits)
//                {
//            		 Map<String, String> article = new HashMap<>();
//                    //取_source字段值
//            		float score = hit.getScore();//取评分
//                    Map<String, Object> sourceAsMap = hit.getSourceAsMap(); // 取成map对象
//                    
//                    if(score > 6){
//                    	article.put("title",(String) sourceAsMap.get("title"));
//                        article.put("content",(String) sourceAsMap.get("content"));
//                        result.put(String.valueOf(count++),article);
//                        
//                        LOG.info("-->结果title：【 " +  article.get("title") + " 】");
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//		LOG.info("-->【 ES查询结束】");
//		if(result.size() == 0){
//			//没有合适的结果
//			Map<String, String> article = new HashMap<>();
//        	article.put("title", "没有合适的答案");
//        	article.put("content", "亲 本机器人宝宝没有查询到您想要的结果");
//        	result.put("0",article);
//		}
//		return result;
//	}
//}
