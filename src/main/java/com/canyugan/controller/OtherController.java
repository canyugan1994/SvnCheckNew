package com.canyugan.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.pojo.AuditDownload;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
			JSONArray temp_array = this.sxProjectInfoService.sendServiceRequest();
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
					LOG.info("-->key:" + key);
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
							}
							download.setReportName("检查报表" + key.substring(0, key.indexOf("_")));
							download.setId(++incr_id);
							result_set.add(download);
						}
					}
				}
				result.put("status", "success");
				result.put("data", result_set);
			} else {
				LOG.info("-->【 根据user_id:[" + user_id + "]未检查到对应报表 】");
				result.put("status", "error");
				result.put("message", "根据user_id:[" + user_id + "]未检查到对应报表");
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
	@RequestMapping(value = "/svnCheck/acceptReport", method = RequestMethod.POST, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject acceptReport(HttpServletRequest request, HttpServletResponse response) 
	{
		JSONObject result = new JSONObject();
		Map<String, String[]> param = request.getParameterMap();
		Set<String> paramKey = param.keySet();
		// 打印全部参数值
		for (String key : paramKey) {
			LOG.info("-->【 param:" + key + ",value:" + param.get(key)[0] + "】");
		}
		// 接口文档给出的参数
		String projectName = request.getParameter("projectName");
		String projectID = request.getParameter("projectID");
		String reqName = request.getParameter("reqName");
		String reqcode = request.getParameter("reqcode");
		LOG.info("-->【 projectName:" + projectName + ",projectID:" + projectID + "reqName:" + reqName + ",reqcode:"
				+ reqcode + "】");

		List<MultipartFile> files;
		try {
			files = ((MultipartHttpServletRequest) request).getFiles("file");
		} catch (Exception e) {
			LOG.info("-->【 未发现文件 】");
			result.put("state", "0001");
			result.put("errorMsg", "未曾在请求中发现pdf、doc之类的文档");
			return result;
		}
		if (files.size() > 0) {
			MultipartFile file = files.get(0);
			// 把文件保存进save_path目录下
			String fileName = file.getOriginalFilename();
			try {
				FileOutputStream out = new FileOutputStream(save_path + fileName);

				byte[] fileByte = file.getBytes();
				LOG.info("-->【 file size: " + fileByte.length + " 】");
				// out.write(fileByte);
				// out.flush();
				out.close();
				LOG.info("-->【 " + save_path + fileName + " 文件存储成功 】");
				result.put("state", "0000");
				result.put("errorMsg", null);
			} catch (Exception e) {
				LOG.info("-->【 错误信息：" + e.getMessage() + " 】");
				result.put("state", "0001");
				result.put("errorMsg", "服务器繁忙 请联系卡中心管理员");
			}
		}
		return result;
	}
}
