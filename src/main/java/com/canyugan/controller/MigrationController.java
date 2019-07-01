package com.canyugan.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.service.SXNeedService;
import com.canyugan.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * svn文档迁移
 * @author caorui
 *
 */
@Api(value = "文档迁移相关接口", tags = { "文档迁移相关接口" })
@RestController
public class MigrationController 
{
	private static final Logger LOG = LoggerFactory.getLogger(MigrationController.class);
	//模拟队列 限制人数
	private static AtomicInteger max_person_num = new AtomicInteger(0);
	@Autowired
	private SXNeedService sxNeedService;
	@Value("${execute_num}")
	private Integer execute_num;//同时迁移的人数
	
	@ApiModel(value = "Migration", description = "")
	@Data
	static class Migration {
		@ApiModelProperty(value = "用户id", name = "user_id", example = "\"A167347\"")
		private String user_id;
		//@ApiImplicitParam(name = "projectIds", value = "勾选项目集合", example = "[\"PJ-LX-2018-002-001-人工智能平台运营\",\"PJ-LX-2019-003-002-onesight人工智能平台运营\"]"),
		@ApiModelProperty(value =  "projectIds" ,name = "勾选项目集合" ,example = "[\"PJ-LX-2018-002-001-人工智能平台运营\",\"PJ-LX-2019-003-002-onesight人工智能平台运营\"]")
		private JSONArray projectIds;
	}

	/**
	 * 暂时不用寻找需求库和tc与目标库的文件异同
	 * 只要迁移 直接把该项目对应的文件迁移到目标库
	 * @param request
	 * @return
	 */
	@ApiOperation("svn文档迁移接口")
	@RequestMapping(value = { "/svnCheck/svnFileMigration" }, method = { RequestMethod.POST }, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject getAuditReport(@RequestBody Migration request) 
	{
		JSONObject result = new JSONObject();
		//做前置校验
		Integer exe_num = max_person_num.get();
		if(exe_num >= execute_num) {
			result.put("status", "error");
			result.put("message", "当前队列内还有" + exe_num + "人在执行任务 请耐心等候");
			return result;
		}
		String user_id = request.getUser_id();
		try {
			JSONArray project_array = request.getProjectIds();//PJ-LX-2018-002-001-人工智能平台运营
			/**
			 * 需求编号 RQN-2018-IT039-0039
			   项目名称  PJ-LX-2018-002-001-人工智能平台运营
			   投产窗口 Release-20181117
			 */
			JSONArray need_project = sxNeedService.sendServiceRequest();//从双速需求信息接口获取数据
			//区分开研发类和维护类项目
			
		}catch (Exception e) {
			LOG.info("-->【 svn文档迁移接口发生异常，异常信息：" + e.getMessage() + " 】");
			result.put("status","error");
			result.put("message","服务器繁忙 请连续管理员");
			return result;
		}
		return result;
	}
}
