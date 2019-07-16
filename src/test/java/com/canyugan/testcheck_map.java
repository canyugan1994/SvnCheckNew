package com.canyugan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.pojo.CheckDataModel;

public class testcheck_map 
{
	public static void main(String[] args)
	{
		Logger LOG = LoggerFactory.getLogger(test_check_map.class);
		Map<String, Map<String, Object>> check_map = new HashMap<String, Map<String,Object>>();
		Map<String, Object> inner_map1 = new HashMap<String, Object>();
		List<CheckDataModel> inner_list1 = new LinkedList<CheckDataModel>();
		CheckDataModel model = new CheckDataModel();
		model.setExist(0);model.setClassify("项目管理文档");model.setDirectoryStruct("立项采购合同");model.setNotes("如涉及采购，需提交，反之，可以不提交");
		model.setCutGuide(0);model.setTemplateName("《技术立项申请单》");model.setCutResult(2);model.setDone(0);
		CheckDataModel model2 = new CheckDataModel();
		model2.setExist(0);model2.setDirectoryStruct("启动");model2.setCutGuide(0);model2.setTemplateName("《启动PPT》");model2.setCutResult(2);model2.setDone(0);
		CheckDataModel model_1 = new CheckDataModel();
		model_1.setExist(0);model_1.setNotes("设计新系统要求有");model_1.setCutGuide(0);model_1.setTemplateName("《系统安装手册》");model_1.setCutResult(2);model_1.setDone(0);
		CheckDataModel model2_1 = new CheckDataModel();
		model2_1.setExist(0);model2_1.setNotes("设计新系统要求有");model2_1.setCutGuide(0);model2_1.setTemplateName("《系统应急手册》");model2_1.setCutResult(2);model2_1.setDone(0);
		inner_list1.add(model);inner_list1.add(model2);inner_list1.add(model_1);inner_list1.add(model2_1);
		inner_map1.put("checkTable",inner_list1);
		check_map.put("PJ-LX-2019-001-云上卡中心项目", inner_map1);
		
		Map<String, Object> inner_map2 = new HashMap<String, Object>();
		List<CheckDataModel> inner_list2 = new LinkedList<CheckDataModel>();
		CheckDataModel model3_1 = new CheckDataModel();
		model3_1.setExist(0);model3_1.setClassify("项目管理文档");model3_1.setDirectoryStruct("立项采购合同");model3_1.setNotes("如涉及采购，需提交，反之，可以不提交");
		model3_1.setCutGuide(0);model3_1.setTemplateName("《技术立项申请单》");model3_1.setCutResult(2);model3_1.setDone(0);
		CheckDataModel model4_1 = new CheckDataModel();
		model4_1.setExist(0);model4_1.setDirectoryStruct("启动");model4_1.setCutGuide(0);model4_1.setTemplateName("《启动PPT》");model4_1.setCutResult(2);model4_1.setDone(0);
		CheckDataModel model3 = new CheckDataModel();
		model3.setExist(0);model3.setNotes("设计新系统要求有");model3.setCutGuide(0);model3.setTemplateName("《系统安装手册》");model3.setCutResult(2);model3.setDone(0);
		CheckDataModel model4 = new CheckDataModel();
		model4.setExist(0);model4.setNotes("设计新系统要求有");model4.setCutGuide(0);model4.setTemplateName("《系统应急手册》");model4.setCutResult(2);model4.setDone(0);
		inner_list2.add(model3);inner_list2.add(model4);inner_list2.add(model3_1);inner_list2.add(model4_1);
		inner_map2.put("checkTable",inner_list2);
		check_map.put("PJ-LX-2019-002-运营业务监控项目",inner_map2);
		
		final Map<String, Map<String, Object>> final_check_map = check_map;
		final ThreadPoolExecutor pool = new ThreadPoolExecutor(4, 4, 0, TimeUnit.MILLISECONDS,new LinkedBlockingQueue());
		/**
		 * 逻辑
		 */
		Future<String> test_result = pool.submit(new Callable<String>() {
			@Override
			public String call() throws Exception {
				CheckDataModel temp = new CheckDataModel();
				List<CheckDataModel> project_check_table = new LinkedList<CheckDataModel>();
				//检查每个项目
				for (String svn_project_url : final_check_map.keySet()) 
				{
					LOG.info("-->【 目标库 正在检查项目：" + svn_project_url + " 】");
					project_check_table = (List<CheckDataModel>) final_check_map.get(svn_project_url).get("checkTable");
					LOG.info("-->【 目标库 根据项目名[" + svn_project_url + "]取出来的checkTable:" + project_check_table + " 】");
					int length = project_check_table.size();
					for (int project_check_table_index = 0; project_check_table_index < length; project_check_table_index++) 
					{
						temp = project_check_table.get(project_check_table_index);
						String template_name = temp.getTemplateName();
						template_name = template_name.substring(1, template_name.length() - 1);
						
						if(template_name.equals("技术立项申请单") && svn_project_url.contains("云上卡中心")) {
							LOG.info("-->【 云上卡中心 技术立项申请单 】");
							temp.setExist(1);
							temp.setDone(1);
						}
						if(template_name.equals("系统安装手册") && !svn_project_url.contains("云上卡中心")) {
							LOG.info("-->【 非云上卡中心 系统安装手册 】");
							temp.setExist(1);
							temp.setDone(1);
						}
						
					}
					LOG.info("-->【 目标库 该项目[" + svn_project_url + "]检查完毕 ，检查表：" + JSONObject.toJSON(project_check_table) + "】");
					
					System.out.println(final_check_map);
					System.out.println();
				}
				return "success";
			}
		});
		
		try {
			if(test_result.get() != null) {
				System.out.println();
				System.out.println("...end");
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
