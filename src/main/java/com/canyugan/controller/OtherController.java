package com.canyugan.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.pojo.AuditDownload;
import com.canyugan.pojo.CheckDataModel;
import com.canyugan.service.SXProjectInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 其它需要的接口
 * @author caorui
 *
 */
@Api(value = "相关接口", tags = { "相关接口" })
@RestController
public class OtherController 
{
	private static final Logger LOG = LoggerFactory.getLogger(OtherController.class);
	@Autowired
	private SXProjectInfoService sxProjectInfoService;
	@Autowired
	private JedisPool JedisPool;
	@Value("${save_path}")
	private String save_path;
	@Value("${template_file}")
	private String template_file;

	@RequestMapping(value = { "/svnCheck/testMap" }, method = { RequestMethod.GET }, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject map() 
	{
		JSONObject result = new JSONObject();
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
					
					LOG.info("final_check_map:【 " + final_check_map + "】");
				}
				return "success";
			}
		});
		
		return result;
	}
	
	/**
	 * 获取双速的项目信息
	 * @return
	 */
	@ApiOperation("获取双速项目信息 过滤项目组为null的单条数据")
	@RequestMapping(value = { "/svnCheck/getAllProjectInfo" }, method = { RequestMethod.GET }, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject getAllProjectInfo() 
	{
		JSONObject result = new JSONObject();
		try {
			JSONArray temp_array = sxProjectInfoService.sendServiceRequest();
			if(temp_array == null) {
				result.put("status", "error");
				result.put("message","双速项目信息接口调用失败 本次无法获取双速项目信息");
				return result;
			}
			result.put("status", "success");
			result.put("data", temp_array);
		} catch (Exception e) {
			LOG.info("-->【 接口调用出错，出错信息:" + e.getMessage() + "】");
			result.put("status", "error");
			result.put("message", "服务器繁忙 请联系管理员");
		}
		return result;
	}

	@ApiModel(value = "AuditReport", description = "审计报表")
	@Data
	static class AuditReport {
		@ApiModelProperty(value = "用户id", name = "user_id", example = "\"A167347\"")
		private String user_id;
		@ApiModelProperty(value = "开始时间", name = "startTime", example = "\"20190620135658\"")
		private String startTime;
		@ApiModelProperty(value = "结束时间", name = "endTime", example = "\"20190620135658\"")
		private String endTime;
	}

	/**
	 * 仅查询已完成的检查报表
	 * @param request
	 * @return
	 */
	@ApiOperation("审计报表查询接口")
	@RequestMapping(value = { "/svnCheck/getAuditReport" }, method = { RequestMethod.POST }, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject getAuditReport(@RequestBody AuditReport request) 
	{
		JSONObject result = new JSONObject();
		try {
			String user_id = request.getUser_id();
			String start_time = request.getStartTime();
			String end_time = request.getEndTime();

			if (user_id == null) {
				result.put("status", "error");
				result.put("message", "user_id核心参数 不可不传");
				return result;
			}
			if ((start_time != null && end_time == null) || (start_time == null && end_time != null)) {
				result.put("status", "error");
				result.put("message", "开始结束时间请成对出现");
				return result;
			}
			if (("".equals(start_time) && !"".equals(end_time)) || (!"".equals(start_time) && "".equals(end_time))) {
				result.put("status", "error");
				result.put("message", "开始结束时间请成对赋值");
				return result;
			}
			Set<AuditDownload> result_set = new HashSet<AuditDownload>();
			Jedis jedis = this.JedisPool.getResource();
			Set<String> keys = jedis.keys("*" + user_id + "*");
			if (keys.size() > 0) 
			{
				Integer incr_id = 0;
				for (String key : keys) 
				{
					if (key.contains(user_id)) 
					{
						String value = jedis.get(key);
						JSONObject parse_value = JSONObject.parseObject(value);
						Set<String> project_keys = parse_value.keySet();
						boolean exit = false;
						for (String project_key : project_keys) 
						{
							JSONObject temp_jsonobject = (JSONObject) parse_value.get(project_key);
							Integer is_done = temp_jsonobject.getInteger("is_done");
							if (is_done.intValue() == 0) {
								exit = true;
								break;
							}
						}
						if (!exit)
						{
							AuditDownload download = new AuditDownload();
							if (!"".equals(start_time) && !"".equals(end_time)) 
							{
								String key_time = key.substring(0, key.lastIndexOf("_"));
								if (Long.valueOf(key_time).longValue() >= Long.valueOf(start_time).longValue()
										&& Long.valueOf(key_time).longValue() <= Long.valueOf(end_time).longValue()) 
								{
									
									download.setReportName("检查报表" + key.substring(0, key.indexOf("_")));
									download.setId(++incr_id);
									result_set.add(download);
								}
							}else {
								download.setReportName("检查报表" + key.substring(0, key.indexOf("_")));
								download.setId(++incr_id);
								result_set.add(download);
							}
						}
					}
				}
				result.put("status", "success");
				result.put("data", result_set);
				LOG.info("-->【 审计报表查询成功，查询的报表数据:" + JSONObject.toJSON(result_set)  + " 】");
			} else {
				LOG.info("-->【 根据user_id:[" + user_id + "]未检查到对应报表 】");
				result.put("status", "error");
				result.put("message", "根据user_id:[" + user_id + "]未检查到对应报表");
				return result;
			}
		} catch (Exception e) {
			LOG.info("-->【 接口发生异常 异常信息:" + e.getMessage() + "】");
			result.put("status", "error");
			result.put("message", "服务器繁忙 请联系管理员");
		}
		return result;
	}

	/**
	 * 服务器上已存在的裁剪模板表
	 * @param request
	 * @param response
	 */
	@ApiOperation(value = "裁剪模板表下载", notes = "裁剪模板表下载")
	@RequestMapping(value = { "/svnCheck/downloadTemplate" }, method = { RequestMethod.GET })
	public void trainModel(HttpServletRequest request, HttpServletResponse response) 
	{
		try {
			InputStream fileIn = new FileInputStream(new File(template_file));
			ServletOutputStream servletOutputStream = response.getOutputStream();
			response.setHeader("Content-Disposition", "attachment; filename=template.xlsx");
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");

			byte[] buff = new byte[2048];
			int readLength = fileIn.read(buff);
			while (readLength != -1) {
				servletOutputStream.write(buff, 0, readLength);
				readLength = fileIn.read(buff);
			}
			LOG.info("-->【 模板表下载成功 】");
			if (fileIn != null) {
				fileIn.close();
			}
		} catch (Exception e) {
			LOG.info("-->【 模板表下载失败，失败信息:" + e.getMessage() + "】");
		}
	}
	
	/**
	 * 提供给TC测试组的上传UAT接口
	 * 接受测试文档 并按照规定的路径存放
	 */
	@ApiOperation("SIT、UAT文档上传")
	@RequestMapping(value = "/svnCheck/acceptReport", method = RequestMethod.POST, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject acceptReport(HttpServletRequest request, HttpServletResponse response) 
	{
		JSONObject result = new JSONObject();
		Map<String, String[]> param = request.getParameterMap();
		Set<String> paramKey = param.keySet();
		// 打印全部参数值
		for (String key : paramKey) {
			LOG.info("-->【 param:" + key + ",value:" + URLDecoder.decode(param.get(key)[0]) + "】");
		}
		// 接口文档给出的参数
		String projectName = request.getParameter("projectName");//项目名称
		String projectID = request.getParameter("projectID");//项目编号
		String reqName = request.getParameter("reqName");//需求名称
		String reqcode = request.getParameter("reqcode");//需求编号
		String department = request.getParameter("department");//部门
		String type = request.getParameter("type");//文档类型 UAT SIT
		String date = request.getParameter("date");//修复的版本
		if(projectName == null || projectID == null ||reqName == null|| reqcode == null ||department == null
				||type == null ||date == null) {
			result.put("state", "0001");
			result.put("errorMsg", "接口文档必传参数为null 请根据接口文档仔细检查");
			return result;
		}
		//检查文件
		List<MultipartFile> files;
		//List<MultipartFile> files1;
		try {
			files = ((MultipartHttpServletRequest) request).getFiles("file");
			//files1 = ((MultipartHttpServletRequest) request).getFiles("file1");
		} catch (Exception e) {
			LOG.info("-->【 未发现文件 】");
			result.put("state", "0001");
			result.put("errorMsg", "未曾在请求中发现pdf、doc之类的文档");
			return result;
		}
//		if(files1.size() > 0) {
//			System.out.println(files1.get(0).getOriginalFilename());
//		}
		if (files.size() > 0) {
			int file_num = files.size();
			//处理每个文件
			for(int index = 0;index < file_num;index++)
			{
				MultipartFile file = files.get(index);
				// 把文件保存进save_path目录下
				String fileName = file.getOriginalFilename();
				try {
					/**
					 * 创建TC的SIT、UAT文档存放目录
					 */
					File final_path = null;
					try {
						// 部门
						File first_path = new File(save_path + department);
						if(!first_path.exists()) {
							first_path.mkdir();
						}
						// 部门/修复的版本
						File second_path = new File(save_path + department + "/" + date);
						if(!second_path.exists()) {
							second_path.mkdir();
						}
						// 部门/修复的版本/需求编号
						File third_path = new File(save_path + department + "/" + date + "/" + reqcode);
						if(!third_path.exists()) {
							third_path.mkdir();
						}
						//获取文件名
						 String file_name = projectID + "-" + projectName + "-" + file.getOriginalFilename();
						// 部门/修复的版本/需求编号/云上卡中心UAT文档
						final_path = new File(save_path + department + "/" + date + "/" + reqcode + "/" + URLEncoder.encode(file_name));
						if(!final_path.exists()) {
							final_path.createNewFile();
						}
						/**
						 * 存储文件
						 */
						FileOutputStream out = new FileOutputStream(final_path);
						byte[] fileByte = file.getBytes();
						LOG.info("-->【 file size: " + fileByte.length + " ,file_name: " + URLEncoder.encode(file_name) + " 】");
						out.write(fileByte);
						out.flush();
						out.close();
						LOG.info("-->【 " + final_path.getName() + " 文件存储成功 】");
						result.put("state", "0000");
						result.put("errorMsg", null);
					} catch (Exception e) {
						LOG.info("-->【 创建文件出错，错误信息：" + e.getMessage() + " 】");
						result.put("state", "0001");
						result.put("errorMsg", "服务器繁忙 请联系卡中心管理员");
						return result;
					}
				} catch (Exception e) {
					LOG.info("-->【 错误信息：" + e.getMessage() + " 】");
					result.put("state", "0001");
					result.put("errorMsg", "服务器繁忙 请联系卡中心管理员");
				}
			}
		}
		return result;
	}
}
