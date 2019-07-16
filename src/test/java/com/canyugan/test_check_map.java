package com.canyugan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.pojo.CheckDataModel;

public class test_check_map 
{
	public static void main(String[] args) 
	{
		Map<String, Map<String, Object>> check_map = new HashMap<String, Map<String, Object>>();
		
		List<CheckDataModel> project_check_table = new LinkedList<CheckDataModel>();
		Map<String, Object> temp_map = new HashMap<String, Object>();
		//init
		CheckDataModel model1 = new CheckDataModel();
		model1.setExist(0);model1.setCutGuide(1);model1.setCutResult(1);
		model1.setDone(0);model1.setNotes("i am model1 note");
		CheckDataModel model2 = new CheckDataModel();
		model2.setExist(1);model2.setCutGuide(1);model2.setCutResult(1);
		model2.setDone(1);model2.setNotes("i am model2 note");
		project_check_table.add(model1);
		project_check_table.add(model2);
		temp_map.put("checkTable",project_check_table);
		check_map.put("project1",temp_map);
		
		List<CheckDataModel> project_check_table_2 = new LinkedList<CheckDataModel>();
		Map<String, Object> temp_map_2 = new HashMap<String, Object>();
		CheckDataModel model3 = new CheckDataModel();
		model3.setExist(0);model3.setCutGuide(1);model3.setCutResult(1);
		model3.setDone(0);model3.setNotes("i am model3 note");
		CheckDataModel model4 = new CheckDataModel();
		model4.setExist(1);model4.setCutGuide(1);model4.setCutResult(1);
		model4.setDone(1);model4.setNotes("i am model4 note");
		project_check_table_2.add(model3);
		project_check_table_2.add(model4);
		temp_map_2.put("checkTable",project_check_table_2);
		check_map.put("project2",temp_map_2);
		
		System.out.println(JSONObject.toJSON(check_map));
		
		//change
		Set<String> keys = check_map.keySet();
		CheckDataModel temp = new CheckDataModel();
		for(String key:keys) {
			List<CheckDataModel> temp_project_table = (List<CheckDataModel>) check_map.get(key).get("checkTable");
			//System.out.println(temp_project_table.hashCode());
			for(int index = 0;index < temp_project_table.size();index++) {
				temp = temp_project_table.get(index);
				System.out.println(temp.hashCode());
				if(temp.getDone() == 0) {
					temp.setDone(1);
					temp.setNotes("originly is 0 ,now is 1");
				}
				temp_project_table.set(index, temp);
			}
			//check_map.get(key).put("checkTable", temp_project_table);
		}
		
		//System.out.println(JSONObject.toJSON(check_map));
	}
}
