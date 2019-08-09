package com.canyugan.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.pojo.AuditDownload;
import com.canyugan.pojo.ProjectInfo;
import com.canyugan.service.SXNeedService;
import com.canyugan.service.SvnCheckService;
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
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
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
	private JedisPool JedisPool;
	@Value("${save_path}")
	private String save_path;
	@Value("${template_file}")
	private String template_file;
	@Value("${code_style}")
	private String code_style;
	@Autowired
	private SvnCheckService svnCheckService;
	@Autowired
	private SXNeedService sxNeedService;
	private String splitString = "_caorui_";
	
	@ApiOperation(value = "[获取双速项目需求类信息]")
	@RequestMapping(value = { "/svnCheck/getAllNeedInfo" }, method = { RequestMethod.GET }, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject getAllNeedInfo() 
	{
		JSONObject result = new JSONObject();
		try {
			JSONArray temp_array = sxNeedService.sendServiceRequest();
			//从数据库中获取项目信息
			if(temp_array == null) {
				result.put("status", "error");
				result.put("message","双速项目信息接口调用失败 本次无法获取双速项目信息");
			}else {
				result.put("status", "success");
				result.put("data", temp_array);
			}
		} catch (Exception e) {
			LOG.info("-->【 接口调用出错，出错信息[" + e.getMessage() + "] 】");
			result.put("status", "error");
			result.put("message", "服务器繁忙 请联系管理员");
		}
		return result;
	}
	
	/**
	 * 获取双速的项目信息
	 * @return
	 */
	@ApiOperation(value = "[获取双速项目信息] new_version:form database",notes = "迁移状态：0待迁移 1迁移中 2迁移完成 3迁移失败")
	@RequestMapping(value = { "/svnCheck/getAllProjectInfo" }, method = { RequestMethod.GET }, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject getAllProjectInfo() 
	{
		JSONObject result = new JSONObject();
		try {
			List<ProjectInfo> temp_array = svnCheckService.getAllProject();
			List<ProjectInfo> result_array = new LinkedList<ProjectInfo>();
			if(temp_array.size() > 0)
			{
				int length = temp_array.size();
				for(int index = 0;index < length;index++) {
					ProjectInfo pro = temp_array.get(index);
					pro.setProjectCodeNum(pro.getProjectId());
					result_array.add(pro);
				}
			}
			result.put("status", "success");
			result.put("data", result_array);
		} catch (Exception e) {
			LOG.info("-->【 数据库查询出错，出错信息[" + e.getMessage() + "] 】");
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
				/**
				 * 对set集合内的元素进行排序
				 */
				Set<AuditDownload> sort_result  = new TreeSet<AuditDownload>(new Comparator<AuditDownload>() {
					@Override
					public int compare(AuditDownload o1, AuditDownload o2) {
						//降序排序
						return o2.getReportName().compareTo(o1.getReportName());
					}
				});
				sort_result.addAll(result_set);
				result.put("status", "success");
				result.put("data", sort_result);
				LOG.info("-->【 审计报表查询成功，查询的报表数据[" + JSONObject.toJSON(sort_result)  + "] 】");
			} else {
				LOG.info("-->【 根据user_id[" + user_id + "]未检查到对应报表 】");
				result.put("status", "error");
				result.put("message", "根据user_id[" + user_id + "]未检查到对应报表");
				return result;
			}
		} catch (Exception e) {
			LOG.info("-->【 接口发生异常 异常信息[" + e.getMessage() + "] 】");
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
	public void templateDownload(HttpServletRequest request, HttpServletResponse response) 
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
			LOG.info("-->【 模板表下载失败，失败信息[" + e.getMessage() + "] 】");
		}
	}
	
	/**
	 * 提供给TC测试组的上传UAT接口
	 * 接受测试文档 并按照规定的路径存放
	 */
	@ApiOperation("SIT、UAT文档上传")
	@RequestMapping(value = "/svnCheck/acceptReport", method = RequestMethod.POST, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject acceptReport(@RequestParam("file") MultipartFile[] uploadFile, 
			HttpServletRequest request,
			HttpServletResponse response) 
	{
		JSONObject result = new JSONObject();
		Map<String, String[]> param = request.getParameterMap();
		Set<String> paramKey = param.keySet();
		// 打印全部参数值
		for (String key : paramKey) {
			LOG.info("-->【 key[" + key + "],decode value[" + URLDecoder.decode(param.get(key)[0]) + "],encode value[" + param.get(key)[0] + "] 】");
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
		try 
		{
			for(MultipartFile file:uploadFile) 
			{
				// 把文件保存进save_path目录下
				try {
					//创建TC的SIT、UAT文档存放目录
					File final_path = null;
					try {
						/**
						 * 创建文件存储的目录
						 */
						// 部门
						File first_path = new File(save_path + 
											  URLDecoder.decode(department));
						if(!first_path.exists()) {
							first_path.mkdir();
						}
						// 部门/修复的版本
						File second_path = new File(save_path + 
								              URLDecoder.decode(department) + "/" + 
								              URLDecoder.decode(date));
						if(!second_path.exists()) {
							second_path.mkdir();
						}
						// 部门/修复的版本/需求编号
						File third_path = new File(save_path + 
								              URLDecoder.decode(department) + "/" + 
								              URLDecoder.decode(date) + "/" + 
								              URLDecoder.decode(reqcode));
						if(!third_path.exists()) {
							third_path.mkdir();
						}
						//获取压缩文件的文件名
						String file_name = file.getOriginalFilename();
						// 部门/修复的版本/需求编号/云上卡中心UAT文档
						final_path = new File(save_path + 
								              URLDecoder.decode(department) + "/" + 
								              URLDecoder.decode(date) + "/" + 
								              URLDecoder.decode(reqcode) + "/" + 
								              file_name);
						if(!final_path.exists()) {
							final_path.createNewFile();
						}
						//存储文件
						FileOutputStream out = new FileOutputStream(final_path);
						byte[] fileByte = file.getBytes();
						out.write(fileByte);
						out.flush();
						out.close();
						LOG.info("-->【 开始解压文件[" + file_name + "] 】");
						//解压文件
						ZipFile zipFile = null;
						try {
							LOG.info("-->【 压缩文件所在目录[" + final_path.getParentFile().getPath() + "] 】");
							zipFile = new ZipFile(final_path,Charset.forName(code_style));
							Enumeration<?> entries = zipFile.entries();
							while(entries.hasMoreElements()) {
								ZipEntry entry = (ZipEntry) entries.nextElement();
								//仅解压文件
								if(!entry.isDirectory()) {
									try {
										File tergetFile = new File(
												final_path.getParentFile().getPath() + "/" + 
										        entry.getName());
										tergetFile.createNewFile();//创建新文件
										InputStream is = zipFile.getInputStream(entry);//获取原始文件io流
										FileOutputStream fos = new FileOutputStream(tergetFile);//目标输出流
										byte[] file_buffer = new byte[is.available() + 1];
										int len;
										while((len = is.read(file_buffer)) != -1) {
											fos.write(file_buffer,0,len);
										}
										fos.close();
										is.close();
									} catch (Exception e) {
										LOG.info("-->【 解压文件[" + entry.getName() + "] 失败】");
									}
									LOG.info("-->【 解压文件[" + entry.getName() + "] 成功】");
								}
							}
						} catch (Exception e) {
							LOG.info("-->【 解压文件[" + file_name + "]失败，失败信息[" + e.getMessage() + "] 】");
						}
						//返回成功信息
						LOG.info("-->【 返回client端成功信息 】");
						result.put("state", "0000");
						result.put("errorMsg", null);
						//删除原文件
						try {
							if(final_path.delete()) {
								LOG.info("-->【 删除原始压缩文件[" + file_name + "]成功 】");
							}
						} catch (Exception e) {
							LOG.info("-->【 删除原始压缩文件["+file_name+"]失败,失败信息["+e.getMessage()+"] 】");
						}
						return result;
					} catch (Exception e) {
						LOG.info("-->【 创建文件出错，错误信息[" + e.getMessage() + "] 】");
						result.put("state", "0001");
						result.put("errorMsg", "服务器繁忙 请联系卡中心管理员");
						return result;
					}
				} catch (Exception e) {
					LOG.info("-->【 错误信息[" + e.getMessage() + "] 】");
					result.put("state", "0001");
					result.put("errorMsg", "服务器繁忙 请联系卡中心管理员");
					return result;
				}
			}
		} catch (Exception e) {
			LOG.info("-->【 未发现文件 】");
			result.put("state", "0001");
			result.put("errorMsg", "未曾在请求中发现pdf、doc之类的文档");
			return result;
		}
		return result;
	}
}
