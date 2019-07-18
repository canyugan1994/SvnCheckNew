package com.canyugan.controller;

import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.pojo.ProjectInfo;
import com.canyugan.service.SXProjectInfoService;
import com.canyugan.service.SvnCheckService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "同步双速项目信息接口数据至数据库", tags = { "同步双速项目信息接口数据至数据库" })
@RestController
public class SynchronizController 
{
	private static final Logger LOG = LoggerFactory.getLogger(SynchronizController.class);
	@Autowired
	private SXProjectInfoService sxProjectInfoService;
	@Autowired
	private SvnCheckService svnCheckService;
	
	/**
	 * 同步双速的项目信息
	 * @return
	 */
	@ApiOperation("同步双速项目信息至数据库 ")
	@RequestMapping(value = { "/svnCheck/synchronizedProjectInfo" }, method = { RequestMethod.GET }, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject synchronizedProjectInfo() 
	{
		JSONObject result = new JSONObject();
		try {
			JSONArray temp_array = sxProjectInfoService.sendServiceRequest();
			if(temp_array == null) {
				result.put("status", "error");
				result.put("message","双速项目信息接口调用失败 本次无法同步双速项目信息");
				return result;
			}
			//开始同步至数据库
			//step 1 :获取数据库已有项目信息 根据项目编号进行唯一性区分
			List<ProjectInfo> add_to_db_project = new LinkedList<ProjectInfo>();
			List<String> db_projectCode = svnCheckService.getAllProjectCode();//数据库已存在项目的项目编号
			int sx_project_size = temp_array.size();
			JSONObject temp = new JSONObject();
			for(int index = 0;index < sx_project_size;index++) 
			{
				temp = (JSONObject) temp_array.get(index);
				String code = temp.getString("projectCodeNum");
				if(db_projectCode.contains(code)) {
					//如果数据库中包含 跳过新增步骤
					continue;
				}
				LOG.info("-->【 项目编号:[" + temp.getString("projectCodeNum") + "],项目"
						+ "名称:[" + temp.getString("projectName") + "]在数据库中不存在 新增 】");
				//当前编号不存在于数据库 新增
				ProjectInfo projectInfo = new ProjectInfo();
				projectInfo.setPProdLine(temp.getString("pProdLine"));
				projectInfo.setPTeam(temp.getString("pTeam"));
				projectInfo.setProjectCatagory(temp.getString("projectCatagory"));
				projectInfo.setProjectHeader(temp.getString("projectHeader"));
				projectInfo.setProjectStatus(temp.getString("projectStatus"));
				projectInfo.setProjectLists(temp.getString("projectLists"));
				projectInfo.setProjectName(temp.getString("projectName"));
				projectInfo.setProjectId(temp.getString("projectCodeNum"));
				projectInfo.setMigrationStatus(0);
				projectInfo.setMigrationDate(null);
				add_to_db_project.add(projectInfo);
			}
			//step 2:如果新增项目的list集合不为空 就执行插入操作
			if(add_to_db_project.size() > 0) {
				svnCheckService.addNewProjects(add_to_db_project);
				LOG.info("-->【 数据库同步完成 】");
			}
			result.put("status", "success");
			result.put("message", "同步成功");
		} catch (Exception e) {
			LOG.info("-->【 接口同步出错，出错信息:" + e.getMessage() + "】");
			result.put("status", "error");
			result.put("message", "服务器繁忙 请联系管理员");
		}
		return result;
	}
}
