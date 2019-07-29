package com.canyugan.controller;

import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.configuration.ThreadPoolFactory;
import com.canyugan.pojo.CheckDataModel;
import com.canyugan.pojo.ExcelListener;
import com.canyugan.pojo.MetaModel;
import com.canyugan.service.SXNeedService;
import com.canyugan.service.SXProjectInfoService;
import com.canyugan.util.DateUtil;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.http.HttpServletRequest;
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
import org.tmatesoft.sqljet.core.internal.lang.SqlParser.bool_return;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * svn文档检查
 * @author caorui
 */
@Api(value="检查模块",tags={"检查模块"})
@RestController
public class ExcelCheckController 
{
	private static final Logger LOG = LoggerFactory.getLogger(ExcelCheckController.class);

	//作用：监控线程池状态 || 关闭线程池
	private static Map<String, ThreadPoolExecutor> monitor = new ConcurrentHashMap<String, ThreadPoolExecutor>();
	//模拟队列 限制人数
	private static AtomicInteger max_person_num = new AtomicInteger(0);
	@Autowired
	private SXProjectInfoService sxProjectInfoService;
	@Autowired
	private SXNeedService sxNeedService;
	@Autowired
	private JedisPool JedisPool;//redis连接池
	@Value("${dist_svn_username}")
	private String dist_svn_username;
	@Value("${dist_svn_password}")
	private String dist_svn_password;
	@Value("${need_svn_username}")
	private String need_svn_username;
	@Value("${need_svn_password}")
	private String need_svn_password;
	//http://182.180.197.136:8080/svn/appdev_vss/外围组管理文档/2019年项目文档
	@Value("${dist_svn_url}")
	private String dist_svn_url;
	//http://182.180.49.163:8080/svn/RQAL/
	@Value("${need_svn_url}")
	private String need_svn_url;
	@Value("${save_path}")
	private String save_path;
	@Value("${execute_num}")
	private Integer execute_num;//限制同时检查的人数
	private DateUtil dateUtil = new DateUtil();

	/**
	 * 查看用户的任务线程池
	 * @return
	 */
	@ApiOperation("多用户线程池监控")
	@RequestMapping(value = { "/svnCheck/monitorUserPool" }, method = { RequestMethod.GET }, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject monitorUserPool() 
	{
		JSONObject result = new JSONObject();
		try {
			JSONArray temp_array = new JSONArray();
			Set<String> users = monitor.keySet();
			ThreadPoolExecutor pool = null;
			for(String user:users)
			{
				JSONObject temp_user = new JSONObject();
				pool = monitor.get(user);//获取该用户对应的线程池
				int activeCount = pool.getActiveCount();
				long completeCount = pool.getCompletedTaskCount();
				temp_user.put("user_id",user);
				temp_user.put("activeCount",activeCount);
				temp_user.put("completeCount",completeCount);
				
				temp_array.add(temp_user);
			}
			result.put("status", "success");
			result.put("data", temp_array);
		} catch (Exception e) {
			LOG.info("-->【 多用户线程池监控出错，错误信息:" + e.getMessage() + "】");
			result.put("status", "error");
			result.put("message", "服务器繁忙 请联系管理员");
		}
		return result;
	}
	
	@ApiModel(value = "QuitCheck", description = "终止检查")
	@Data
	static class QuitCheck {
		@ApiModelProperty(value = "执行检查任务时返回的唯一编号id", name = "request_id", example = "\"20190627095852_A167347\"")
		private String request_id;
	}
	/**
	 * 终止检查 
	 * 用户id得加上线程池名字
	 */
	@ApiOperation("终止检查任务")
	@RequestMapping(value = "/svnCheck/quitCheck", method = RequestMethod.POST, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject quitCheck(@RequestBody QuitCheck quitCheck) 
	{
		JSONObject result = new JSONObject();
		String user_id = quitCheck.getRequest_id();
		//如果根据用户id获取对应任务线程池存在
		if(monitor.get(user_id) != null) {
			LOG.info("-->【 request_id:" + user_id + "正在终止检查任务 】");
			monitor.get(user_id).shutdownNow();//立即关闭线程池 不做善后处理
			monitor.remove(user_id);//去除该用户的监控
			//去掉占用的坑
			max_person_num.decrementAndGet();
			//设置响应内容
			result.put("status", "success");
			result.put("message", "终止检查任务成功");
		}else {
			result.put("status", "error");
			result.put("message", "该用户似乎没有执行过检查任务");
		}
		return result;
	}
	
	/**
	 * 整体步骤 
	 * 		step 1:预检查excel 校验 通过进行step 2，否则退出 
	 * 		step 2:三个检查点 svn需求文档库 检查需求和TF文档
	 * tc平台接口(给的是测试报告) SIT和UAT报告(给的是需求报告) svn目标库(给的是目标库，包含一个项目的所有阶段文档)
	 * 整体步骤 step 1:预检查excel 校验 通过进行step 2，否则退出 step 2:三个检查点 svn需求文档库 检查需求和TF文档
	 * tc平台接口 SIT和UAT报告 svn目标库
	 * 
	 * @param request
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ApiOperation(value = "svn文档检查", notes = "请求体格式form-data")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "userId", value = "用户id", dataType = "string", example = "A167347"),
			@ApiImplicitParam(name = "projectIds", value = "勾选项目集合", example = "[\"PJ-LX-2018-002-001-人工智能平台运营\",\"PJ-LX-2019-003-002-onesight人工智能平台运营\"]"),
			@ApiImplicitParam(name = "file", value = "MultipartFile", paramType = "MultipartFile") })
	@RequestMapping(value = { "/svnCheck/svnCheckFile" },method={RequestMethod.POST },produces={"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject svnCheckFile(HttpServletRequest request) 
	{
		//响应
		JSONObject result = new JSONObject();
		String user_id = request.getParameter("userId");
		String projectIds = request.getParameter("projectIds");//勾选的项目id信息
		//参数检查
		if (user_id == null || projectIds == null) {
			result.put("status", "error");
			result.put("message", "多么痛的领悟 userId或者projectIds");
			return result;
		}
		//excel检查
		List<MultipartFile> files;
		try {
			files = ((MultipartHttpServletRequest) request).getFiles("file");
		} catch (Exception e) {
			result.put("status", "error");
			result.put("message", "未曾在请求中发现要检查的excel文件 必传");
			return result;
		}
		//获取检查过后封装的数据结构
		JSONObject check_result = new JSONObject();
		checkExcel(check_result, files, JSONArray.parseArray(projectIds));
		Map<String, Map<String, Object>> check_map = new HashMap<String, Map<String, Object>>();
		if (check_result.get("error") != null) {
			String error_message = check_result.getString("error");
			result.put("status", "error");
			result.put("message", error_message);
			return result;
		}else if (check_result.get("success") != null) {
			//检测队列人数 只允许最多两个人执行任务
			Integer current_exe_person_num = max_person_num.get();
			if(current_exe_person_num >= execute_num) {
				result.put("status", "error");
				result.put("message", "当前执行人数已经超过配置的最大执行人数 当前队列内执行任务人数：" + current_exe_person_num);
				LOG.info("-->" + user_id + "【 当前执行人数已经超过配置的最大执行人数 当前队列内执行任务人数：[" + current_exe_person_num + "] 】");
				return result;
			}else {
				check_map = (Map) check_result.get("data");
				LOG.info("-->" + user_id + "【 项目裁剪信息预检查成功,裁剪表检查数据check_map[" + JSONObject.toJSON(check_map).toString() + " ] 】");
			}
		}
		/**
		 * 数据存入redis
		 */
		final String full_key = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_" + user_id;
		String status = this.JedisPool.getResource().set(full_key, JSONObject.toJSON(check_map).toString());
		if ("ok".equals(status) || "OK".equals(status)) {
			LOG.info("-->" + user_id + "【 项目裁剪信息预检查完毕，信息存入redis成功 】");
		} else {
			LOG.info("-->" + user_id + "【 检查信息数据落redis库失败 请稍后重试或者联系管理员");
			result.put("status", "error");
			result.put("message","ip:[" + request.getRemoteAddr() + "] 用户id:[" + user_id + "]的excel检查信息数据落redis库失败 请稍后重试或者联系管理员");
			monitor.remove(user_id);//去除监控
			max_person_num.decrementAndGet();//去掉占用的坑
			return result;
		}
		/**
		 * 开始正式执行svn检查
		 */
		final ThreadPoolExecutor pool = 
				new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,new LinkedBlockingQueue(1),new ThreadPoolFactory("SvnCheck" + "-" + user_id));
		/**
		 * step1:拼装项目和RQN的关系
		 * 形如
		 * PJ-LX-2018-002-001-人工智能平台运营
		 * 		RQN-2018-IT039-0039
		 * 		RQN-2018-IT039-0040
		 * 		RQN-2018-IT039-0041
		 */
		//从双速需求信息接口获取数据 一个项目可能对应多个RQN
		JSONArray need_project = sxNeedService.sendServiceRequest();
		//如果接口请求发生各种错误导致接口响应数据为null 直接给前端返回错误
		if(need_project == null) {
			result.put("status","error");
			result.put("message","双速需求库信息接口调用失败 获取数据为空 请稍后再试 或联系管理员");
			return result;
		}
		final Map<String, Set<String>> need_projectName = new HashMap<String, Set<String>>();
		Set<String> tempSet = null;
		int need_project_length = need_project.size();
		JSONObject temp = new JSONObject();
		for (int need_project_index = 0; need_project_index < need_project_length; need_project_index++) 
		{
			//取出接口中的每一个项目和RQN
			temp = (JSONObject) need_project.get(need_project_index);
			tempSet = need_projectName.get(temp.getString("projectName"));
			if(tempSet == null) {
				//该项目没有被设置过项目和RQN
				tempSet = new HashSet<String>();
			}
			tempSet.add(temp.getString("project_num"));
			need_projectName.put(temp.getString("projectName"), tempSet);
		}
		/**
		 * step2:目标库检查
		 */
		Future<String> dist_thread_result = pool.submit(new Callable<String>() 
		{
			/**
			 * 目标库
			 */
			public List<String> getSvnFileBaseInfoDto(SVNRepository repository, String path, boolean isGetNext)
					throws SVNException {
				List<String> result = new LinkedList<String>();
				listEntries(repository, path, result, isGetNext);
				return result;
			}

			public void listEntries(SVNRepository repository, String path, List<String> result, boolean isGetNext) {
				try {
					Collection entries = repository.getDir(path, -1L, null, (Collection) null);
					Iterator iterator = entries.iterator();
					while (iterator.hasNext()) {
						SVNDirEntry entry = (SVNDirEntry) iterator.next();
						if (entry.getKind() == SVNNodeKind.FILE) {
							result.add(ExcelCheckController.this.dist_svn_url
									+ (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
						}
						if (isGetNext && entry.getKind() == SVNNodeKind.DIR) {
							listEntries(repository, path.equals("") ? entry.getName() : (path + "/" + entry.getName()),result, isGetNext);
						}
					}
				} catch (Exception e) {
					LOG.info("-->" + user_id + " 【 目标库 获取目录下面的文件url失败，失败信息：" + e.getMessage() + " 】");
				}
			}
			
			/**
			 * 需求库
			 */
			public List<String> getSvnFileBaseInfoDto_need(SVNRepository repository, String path, boolean isGetNext)
					throws SVNException {
				List<String> result = new LinkedList<String>();
				listEntries_need(repository, path, result, isGetNext);
				return result;
			}

			public List<String> getSvnFileBaseInfoDto_need(SVNRepository repository, String path) throws SVNException {
				List<String> result = new LinkedList<String>();
				listDirs_need(repository, path, result);
				return result;
			}

			public void listDirs_need(SVNRepository repository, String path, List<String> result) {
				try {
					Collection entries = repository.getDir(path, -1L, null, (Collection) null);
					Iterator iterator = entries.iterator();
					while (iterator.hasNext()) {
						SVNDirEntry entry = (SVNDirEntry) iterator.next();
						if (entry.getKind() == SVNNodeKind.DIR) {
							result.add(ExcelCheckController.this.need_svn_url
									+ (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
						}
					}
				} catch (Exception e) {
					LOG.info("-->" + user_id + " 【 需求库 获取目录下面的目录[" + path + "]失败，失败信息：" + e.getMessage() + " 】");
				}
			}

			public void listEntries_need(SVNRepository repository, String path, List<String> result, boolean isGetNext) {
				try {
					Collection entries = repository.getDir(path, -1L, null, (Collection) null);
					Iterator iterator = entries.iterator();
					while (iterator.hasNext()) {
						SVNDirEntry entry = (SVNDirEntry) iterator.next();
						if (entry.getKind() == SVNNodeKind.FILE) {
							result.add(ExcelCheckController.this.need_svn_url
									+ (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
						}
						if (isGetNext && entry.getKind() == SVNNodeKind.DIR) {
							listEntries(repository, path.equals("") ? entry.getName() : (path + "/" + entry.getName()),
									result, isGetNext);
						}
					}
				} catch (Exception e) {
					LOG.info("-->" + user_id + " 【 需求库 获取目录下面的目录[" + path + "]失败，失败信息：" + e.getMessage() + " 】");
				}
			}

			@SuppressWarnings("deprecation")
			public String call() throws Exception 
			{
				SVNRepository repository = null;
				ISVNAuthenticationManager authManager = null;
				try {
					repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(dist_svn_url));
					authManager = SVNWCUtil.createDefaultAuthenticationManager(dist_svn_username,dist_svn_password);
					repository.setAuthenticationManager(authManager);
				} catch (Exception e) {
					LOG.info("-->" + user_id + " 【 目标库 svn服务器连接失败，失败信息：" + e.getMessage() + " 】");
					return "error";
				}
				
				SVNRepository repository_need = null;
				ISVNAuthenticationManager authManager_need = null;
				try {
					repository_need = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(need_svn_url));
					authManager_need = SVNWCUtil.createDefaultAuthenticationManager(need_svn_username, need_svn_password);
					repository_need.setAuthenticationManager(authManager_need);
				} catch (Exception e) {
					LOG.info("-->" + user_id + " 【 需求库 svn服务器连接失败，失败信息：" + e.getMessage() + " 】");
					return "error";
				}
				//成功标记 false失败 true成功
				boolean need_success = false;
				boolean dist_success = false;
				boolean tc_success = false;
				//获取redis连接
				Jedis jedis = JedisPool.getResource();
				
				try {
					/**
					 * step 1
					 * 开始需求库 需求库仅检查2个字段 为了防止百分比提早计算完毕 先查需求库
					 */
					String year = dateUtil.currentDateTime().substring(0, 4);//获取当前执行年份
					List<String> depart_url = getSvnFileBaseInfoDto_need(repository_need, "");//获取svn根路径下的部门
					for (String depart : depart_url) 
					{
						LOG.info("-->" + user_id + " 【 需求库 正在处理部门：" + depart + " 】");
						//获取例如 http://182.180.49.163:8080/svn/RQAL/业务管理部/2019 的文档目录信息
						List<String> rqn_year = getSvnFileBaseInfoDto_need(repository_need,
								depart.substring(depart.lastIndexOf("/") + 1, depart.length()) + "/" + year);
						for (String rqn : rqn_year) 
						{
							if (rqn.contains("RQN")) 
							{
								Set<String> rqns_needproject_temp = new HashSet<String>();
								//RQN-2019-BIM002-0053-S08
								String rqn_direct = rqn.substring(rqn.lastIndexOf("/") + 1, rqn.length());
								//获取rqn编号下面所有的文档url
								List<String> rqn_all_doc = getSvnFileBaseInfoDto_need(repository_need,
										depart.substring(depart.lastIndexOf("/") + 1, depart.length()) + "/" + year + "/" + rqn_direct,true);
								//项目的key集合 
								Set<String> keys_needProject = need_projectName.keySet();
								/**
								 * 每个RQN在项目中的rqns找一遍 存在就退出循环
								 */
								for(String key_project:keys_needProject)
								{
									//拿出RQN号
									rqns_needproject_temp = need_projectName.get(key_project);
									if (rqns_needproject_temp.contains(rqn_direct)) 
									{
										//从redis中读取
										String redis_check_map = jedis.get(full_key);
										//解析json格式字符串
										JSONObject final_check_map = JSONObject.parseObject(redis_check_map);
										Set<String> redis_project_keySet  = final_check_map.keySet();
										//开始从redis中读取数据
										if (redis_project_keySet.contains(key_project)) {
											LOG.info("-->" + user_id + "【 需求库 项目：[" + key_project + "]和redis中的裁剪表检查项目匹配成功,rqn需求编号：[" + rqn_direct + "] 】");
											//获取项目对应的裁剪表信息
											JSONArray project_checkTable = final_check_map.getJSONObject(key_project).getJSONArray("checkTable");
											int length = project_checkTable.size();
											for (int project_check_table_index = 0; project_check_table_index < length; project_check_table_index++) 
											{
												JSONObject temp = project_checkTable.getJSONObject(project_check_table_index);
												String template_name = temp.getString("templateName");
												template_name = template_name.substring(1, template_name.length() - 1);
												//如果模板名包含TF和需求相关
												if (template_name.contains("TF") || template_name.contains("tf")
													|| template_name.contains("Tf") || template_name.contains("tF")
													|| template_name.contains("需求")) 
												{
													//遍历该rqn下面的所有文档
													for (String rqn_doc_url : rqn_all_doc) 
													{
														//xxx需求分析报告.doc
														String direct_url = rqn_doc_url.substring(rqn_doc_url.lastIndexOf("/") + 1, rqn_doc_url.length());
														if (direct_url.contains(template_name)) {
															LOG.info("-->" + user_id + " 【 需求库 文档：[" + direct_url + "]存在 设置存在标记 】");
															temp.replace("exist", 1);
														}
														temp.replace("done", 1);
														project_checkTable.set(project_check_table_index, temp);
													}
												}
											}
											LOG.info("-->" + user_id + "【 需求库 项目：[" + key_project + "]检查完毕 】");
											String status = jedis.set(full_key, JSONObject.toJSON(final_check_map).toString());
											if ("ok".equals(status) || "OK".equals(status)) {
												LOG.info("-->" + user_id + "【 需求库检查线程 更新项目[" + key_project + "]完成度信息存入redis成功 更新信息：" + 
														JSONObject.toJSON(final_check_map).toString() + " 】");
											}else{
												LOG.info("-->" + user_id + "【 需求库检查线程 更新项目[" + key_project + "]完成度信息存入redis失败 】");
											}
											break;//本次RQN和项目的rqn集合匹配成功 退出在项目中寻找匹配rqn的循环
										}
									}
								}
							}
						}
					}
					LOG.info("-->需求库 检查完毕");
					need_success = true;
				} catch (Exception e) {
					LOG.info("-->需求库 【 比对svn文件信息失败，失败信息：" + e.getMessage() + " 】");
				}
				
				//需求库出错 整体失败 后续检查不再进行
				if(need_success != true) {
					return "error";
				}
				
				try {
					/**
					 * step 2
					 * 开始TC文档检查 tc包含测试文档
					 */
				} catch (Exception e) {
					LOG.info("-->TC文件系统库 【 TC检查失败，失败信息：" + e.getMessage() + " 】");
				}
				
				//TC测试文档检查失败 整体失败 后续目标库不再进行检查
//				if(tc_success != true) {
//					return "error";
//				}
				
				try {
					/**
					 * step 3
					 * 开始目标库
					 */
					String redis_check_map = jedis.get(full_key);
					//解析json格式字符串
					JSONObject final_check_map = JSONObject.parseObject(redis_check_map);
					Set<String> keySet  = final_check_map.keySet();//项目集合
					LOG.info("-->" + user_id + " 【 目标库 准备要处理裁剪表的项目信息:[" + keySet + "] 】");
					for (String svn_project_url : keySet) 
					{
						LOG.info("-->" + user_id + "【 目标库 正在检查项目:[" + svn_project_url + "] 】");
						List<String> urls = new LinkedList<String>();
						if (svn_project_url.contains("LX")) {
							urls = getSvnFileBaseInfoDto(repository, "立项类项目/" + svn_project_url, true);
							//urls = getSvnFileBaseInfoDto(repository, svn_project_url, true);
						} else if (svn_project_url.contains("WH")) {
							//维护类项目暂时不检查
							LOG.info("-->" + user_id + "【 维护类项目本线程暂时不检查 谢谢 】");
							continue;
						}
						//获取每个项目的检查表
						JSONArray project_checkTable = final_check_map.getJSONObject(svn_project_url).getJSONArray("checkTable");
						if(urls.size() > 0) 
						{
							int length = project_checkTable.size();
							for (int project_check_table_index = 0; project_check_table_index < length; project_check_table_index++) 
							{
								JSONObject temp = project_checkTable.getJSONObject(project_check_table_index);
								String template_name = temp.getString("templateName");
								template_name = template_name.substring(1, template_name.length() - 1);
								// 每个项目目录下面所有的文档 任何文档
								for (String url : urls) 
								{
									String direct_url = url.substring(url.lastIndexOf("/") + 1, url.length());
									if (direct_url.contains(template_name)) {
										LOG.info("-->" + user_id + "【 目标库 命中模板:" + template_name + " 设置存在标记为1 】");
										temp.replace("exist", 1);
									}
									//不管在不在 处理过 先设置处理标记为1
									temp.replace("done", 1);
									//重置该裁剪项到原本位置
									project_checkTable.set(project_check_table_index, temp);
								}
							}
							LOG.info("-->" + user_id + "【 项目:[" + svn_project_url + "]检查结束 】");
							String status =jedis.set(full_key,JSONObject.toJSON(final_check_map).toString());
							if ("ok".equals(status) || "OK".equals(status)) {
								LOG.info("-->" + user_id + " 【 目标库检查线程 项目[" + svn_project_url + "]svn文档检查完毕，进度更新信息存入redis成功 "
										+ "更新的数据:" + JSONObject.toJSON(final_check_map).toString()+ " 】");
							}else {
								LOG.info("-->" + user_id + " 【 目标库检查线程 项目[" + svn_project_url + "]svn文档检查完毕，进度更新信息存入redis失败，待后续处理 】");
							}
						}else {
							LOG.info("-->" + user_id + "【 目标库 本次项目下属文档数量为0 略过 】");
						}
						continue;
					}
					LOG.info("-->" + user_id + "【 目标库 检查完毕 】");
					dist_success = true;
				} catch (Exception e) {
					LOG.info("-->" + user_id + "【 目标库 比对svn文件信息失败，失败信息：" + e.getMessage() + " 】");
				}
				//目标库检查
				if(dist_success == true) {
					return "success";
				}else {
					//如果目标库失败 整体失败
					return "error";
				}
			}
		});
		/**
		 * step3:收尾线程
		 * 负责给维护类设置done标记
		 */
		pool.submit(new Callable<String>() 
		{
			public String call() {
				try {
					//获取需求库和目标库的执行结果 阻塞获取
					//String need_result = need_thread_result.get();
					String dist_result = dist_thread_result.get();
					//如果执行完毕且都拿到了返回值 无论success还是error
					if(dist_result != null) 
					{
						//如果都执行成功了
						if("success".equals(dist_result)) 
						{
							LOG.info("-->" + user_id + "【 tc、需求、目标库检测完毕 守护线程开始工作  正在修改项目信息 并关闭线程池");
							Jedis jedis = JedisPool.getResource();
							String redis_check_map = jedis.get(full_key);
							JSONObject final_check_map = JSONObject.parseObject(redis_check_map);
							Set<String> projectKey = final_check_map.keySet();
							
							for (String key : projectKey) {
								JSONArray checkTable = final_check_map.getJSONObject(key).getJSONArray("checkTable");
								int length = checkTable.size();
								for (int index = 0; index < length; index++) {
									JSONObject temp = checkTable.getJSONObject(index);
									if (temp.getIntValue("done") == 0) {
										temp.replace("done", 1);
									}
									checkTable.set(index, temp);
								}
								final_check_map.getJSONObject(key).replace("is_done",1);
							}
							// 处理完毕 存入redis
							String status = jedis.set(full_key,JSONObject.toJSON(final_check_map).toString());
							if ("ok".equals(status) || "OK".equals(status)) {
								LOG.info("-->" + user_id + "【 收尾线程更新项目完成度信息并存入redis的数据：" + JSONObject.toJSON(final_check_map).toString() + " 】成功");
								pool.shutdown();//关闭线程池
								monitor.remove(full_key);//去除当前用户的线程池监控
								max_person_num.decrementAndGet();//去掉占用的坑
								LOG.info("-->" + user_id + "【 收尾线程完成工作：去除当前用户的线程池监控 关闭线程池 去除占用的坑 】");
								jedis.close();
								return "success";
							} else {
								LOG.info("-->" + user_id + "【 收尾线程更新项目完成度信息并存入redis失败 待后续处理 】");
								pool.shutdown();//关闭线程池
								monitor.remove(full_key);//去除当前用户的线程池监控
								max_person_num.decrementAndGet();//去掉占用的坑
								LOG.info("-->" + user_id + "【 收尾线程完成工作：去除当前用户的线程池监控 关闭线程池 去除占用的坑 】");
								jedis.close();
								return "error";
							}
						}else if("error".equals(dist_result)) {
							LOG.info("-->" + user_id + "【 目标库线程执行任务失败 】");
						}
						/**
						 * 遇到了失败情况
						 * 执行之前删除redis中的key 该次检查任务遇到问题 执行失败
						 */
						long status = JedisPool.getResource().del(full_key);
						if(status == 1) {
							LOG.info("-->" + user_id + "【 检查任务执行失败 删除任务key" + full_key + "成功 】");
						}else {
							LOG.info("-->" + user_id + "【 检查任务执行失败 删除任务key" + full_key + "失败,后续可以自行删除 】");
						}
						pool.shutdown();//关闭线程池
						monitor.remove(full_key);//去除当前用户的线程池监控
						max_person_num.decrementAndGet();//去掉占用的坑
						LOG.info("-->" + user_id + "【 去除当前用户的线程池监控 关闭线程池 】");
						return "error";
					}
				} catch (Exception e) {
					LOG.info("-->" + user_id + "【 收尾线程更新项目完成度信息出错，错误信息：" + e.getMessage() + " 】");
					pool.shutdown();//关闭线程池
					monitor.remove(full_key);//去除当前用户的线程池监控
					max_person_num.decrementAndGet();//去掉占用的坑
					LOG.info("-->" + user_id + "【 去除当前用户的线程池监控 关闭线程池 】");
					return "error";
				}
				return "success";
			}
		});
		/**
		 * 设置线程池监控
		 */
		monitor.put(full_key,pool);
		LOG.info("-->" + user_id + "【 用户:" + user_id + "设置线程池监控成功,监控唯一key：" + full_key + " 】");
		//返回响应
		result.put("status", "success");
		result.put("request_id", full_key);
		return result;
	}

	/**
	 * 1:excel校验 2:封装excel数据
	 * @param result
	 * @param files
	 * @author cuilongyun
	 * @return
	 */
	private JSONObject checkExcel(JSONObject result, List<MultipartFile> files,JSONArray project_select) 
	{
		/*
		 * 所有项目的集合 结构： key:projectName value:项目名称 key:checkTable value:项目检测见集合
		 * key:projectCodeNum value:项目编号 下面两个key只有研发类项目存在 据此可以判断研发类和维护类项目 key:isPurchase
		 * value:是否设计采购的结果 || null key:isNewSystem value:是否设计新系统 || null
		 */
		Map<String, Map<String, Object>> check_map = new HashMap<String, Map<String, Object>>();
		/**
		 * 勾选项目
		 */

		JSONArray jsonArray = project_select;
		LOG.info("-->【 勾选项目信息：" + project_select + " 】");
		List<String> LX_pronameList = new ArrayList<String>();
		List<String> LX_pronumList = new ArrayList<String>();
		List<String> WH_pronameList = new ArrayList<String>();
		List<String> wH_pronumList = new ArrayList<String>();
		for (int i = 0; i < jsonArray.size(); i++) {
			String ob1 = (String) jsonArray.get(i);
			String proname1 = ob1.substring(ob1.lastIndexOf("-") + 1, ob1.length());
			String pronum1 = ob1.substring(0, ob1.lastIndexOf("-"));
			if (pronum1.contains("LX")) {
				LX_pronameList.add(proname1);
				LX_pronumList.add(pronum1);
			}
			if (pronum1.contains("WH")) {
				WH_pronameList.add(proname1);
				wH_pronumList.add(pronum1);
			}
		}

		// 处理excel
		System.out.println(files.size());
		if (files.size() > 0) {
			MultipartFile file = (MultipartFile) files.get(0);
			LOG.info("-->【 ExcelfileName: " + file.getOriginalFilename() + "】");
			if (!file.getOriginalFilename().endsWith("xlsx")) {
				result.put("error", "项目裁剪表格式错误！请确认项目裁剪结果并重新上传。");
				return result;
			}
			System.out.println("ddd");
			InputStream is = null;// excel文件流
			ExcelListener meta_listener = new ExcelListener();// metadata监听器
			ExcelReader reader = null;
			try {
				is = new ByteArrayInputStream(file.getBytes());
				reader = new ExcelReader(is, ExcelTypeEnum.XLSX, null, meta_listener);
				// 本次读取仅仅为获取sheet数量
				reader.read(new Sheet(1, 1));// sheetNo headNo
				List<Sheet> sheets = reader.getSheets();
				String sheet_str = "";
				for (Sheet sheet : sheets) {
					sheet_str += "【 " + sheet.getSheetName() + "】 ";
				}
				LOG.info("--------->【 读取到的多个sheetName分别为：" + sheet_str + " 】");
				/**
				 * 现约定sheet1属于研发类项目 sheet2属于维护类项目 ，剩余的sheet按照正常读取规则进行处理
				 */
				for (int index = 2; index <= sheets.size(); index++)// 读取的时候sheet从1开始
				{
					// 获取sheet的时候从0开始 sheets.get(index-1)
					LOG.info("--------->【 开始读取sheetNumber:" + index + ",sheetName:"
							+ sheets.get(index - 1).getSheetName() + " 】");
					if (!sheets.get(1).getSheetName().equals("研发项目裁剪模板")
							|| !sheets.get(2).getSheetName().equals("维护项目裁剪模板")) {
						result.put("error", "裁剪表sheet顺序与裁剪表模板不符");
						return result;
					}
					try {
						boolean isLX = false;
						boolean isWH = false;
						if (index == 2) {
							isLX = true;
							isWH = false;
						} else if (index == 3) {
							isLX = false;
							isWH = true;
						}
						/**
						 * step 1 ：处理metadata并对metadata数据格式进行强校验
						 */
						LOG.info("-->【 step1：开始处理裁剪表元数据 】");
						ExcelListener for_listener = new ExcelListener();
						try {
							is = new ByteArrayInputStream(file.getBytes());
							reader = new ExcelReader(is, ExcelTypeEnum.XLSX, null, for_listener);
							reader.read(new Sheet(index, 0, MetaModel.class));// sheet编号从1开始
						} catch (Exception e) {
							// TODO: handle exception
							LOG.info("发生异常，异常信息：" + e.getMessage());
						}
						/**
						 * 取第1-6行
						 *
						 */
						String project_codeNum = null;
						String project_name = null;
						String project_class = null;
						String project_num = null;
						String project_isPurchase = null;
						String project_isNewSystem = null;

						// 非第三个sheet 走公共逻辑
						if (isLX == true) {
							// 研发类
							List<Object> meta_result = for_listener.getDatas().subList(2, 6);
							for (Object object : meta_result) {
								MetaModel meta = (MetaModel) object;
								if ("项目类别".equals(meta.getKey())) {
									project_class = "研发类";
								}
								if ("项目是否涉及采购".equals(meta.getKey())) {
									project_isPurchase = meta.getValue();
								}
								if ("项目是否涉及新系统".equals(meta.getKey())) {
									project_isNewSystem = meta.getValue();
								}
							}
						} else if (isWH == true) {
							List<Object> meta_result = for_listener.getDatas().subList(2, 4);
							for (Object object : meta_result) {
								MetaModel meta = (MetaModel) object;
								if ("项目类别".equals(meta.getKey())) {
									project_class = "维护类";
								}

							}
						} else if (isLX == false && isWH == false) {
							// 对第三个sheet进行检测校验
							List<Object> meta_result = for_listener.getDatas().subList(0, 6);
							for (Object object : meta_result) {
								MetaModel meta = (MetaModel) object;
								if ("项目编号".equals(meta.getKey())) {
									project_codeNum = meta.getValue();
								}
								if ("项目名称".equals(meta.getKey())) {
									project_name = meta.getValue();
								}
								if ("项目数量".equals(meta.getKey())) {
									project_num = meta.getValue();
								}
								if ("项目类别".equals(meta.getKey())) {
									project_class = meta.getValue();
								}

								if ("项目是否涉及采购".equals(meta.getKey())) {
									project_isPurchase = meta.getValue();
								}
								if ("项目是否涉及新系统".equals(meta.getKey())) {
									project_isNewSystem = meta.getValue();
								}
							}
							if (project_codeNum == null || project_name == null || project_num == null
									|| project_class == null) {
								result.put("error", project_name + " :项目编号、项目名称、项目数量三者未填写或者A列列名与裁剪表模版不相符 请检查。");
								return result;
							}

							// 检测项目编号和项目名称对应是否准确
							JSONArray sx_project = sxProjectInfoService.sendServiceRequest();
							List<String> pronameList = new ArrayList<String>();
							List<String> pronumList = new ArrayList<String>();
							List<String> pronum_nameList = new ArrayList<String>();
							for (int i = 0; i < sx_project.size(); i++) {
								JSONObject ob = sx_project.getJSONObject(i);
								String proname = ob.getString("projectName");
								String pronum = ob.getString("projectCodeNum");
								pronameList.add(proname);
								pronumList.add(pronum);
								pronum_nameList.add(pronum + "-" + proname);
							}

							String[] project_name_single = project_name.split("、");
							String[] project_num_single = project_codeNum.split("、");
							if (project_name_single.length != project_num_single.length
									|| project_name_single.length != Integer.valueOf(project_num)
									|| project_num_single.length != Integer.valueOf(project_num)) {
								result.put("error", project_name + " :请在项目编号和项目名称之间使用顿号隔开，或者项目编号、项目名称、项目数量对应不上 请检查");
								return result;
							}
							for (int i = 0; i < project_name_single.length; i++) {
								if (pronameList.contains(project_name_single[i]) == false) {
									result.put("error", project_name + " :导入的项目名称不一致，请重新确认");
									return result;
								}
								if (pronumList.contains(project_num_single[i]) == false) {
									result.put("error", project_name + " :导入的项目编号不一致，请重新确认");
									return result;
								}
								if (pronum_nameList.contains(project_num_single[i] + "-" + project_name_single[i]) == false) {
									result.put("error", project_name + " :导入的项目编号与名称不一致，请重新确认");
									return result;
								}
							}
							if (project_codeNum.contains("LX") && project_class.equals("维护类")) {
								result.put("error", project_name + " :导入的项目类别不正确，请重新确认");
								return result;
							}
							if (project_codeNum.contains("WH") && project_class.equals("研发类")) {
								result.put("error", project_name + " :导入的项目类别不正确，请重新确认");
								return result;
							}
							// 检测项目类别和是否设计采购、是否设计新系统的对应关系

						}
						// 公共校验
						if ("研发类".equals(project_class)) {
							if (project_isNewSystem == null || project_isPurchase == null) {
								result.put("error", project_name + " :研发类项目未发现填写【项目是否设计采购】和【项目是否设计新系统】或者A列列名与模版提供名称不符合");
								return result;
							}
						}
						if ("维护类".equals(project_class)) {
							if (project_isNewSystem != null || project_isPurchase != null) {
								result.put("error", project_name + " :维护类项目不允许填写【项目是否设计采购】和【项目是否设计新系统】");
								return result;
							}
						}
						// 公共校验部分
						LOG.info("-->【 step2：开始处理裁剪表 】");
						ExcelListener for_check_listener = new ExcelListener();
						is = new ByteArrayInputStream(file.getBytes());
						reader = new ExcelReader(is, ExcelTypeEnum.XLSX, null, for_check_listener);
						if ("研发类".equals(project_class)) {
							reader.read(new Sheet(index, 11, CheckDataModel.class));
						}
						if ("维护类".equals(project_class)) {
							reader.read(new Sheet(index, 9, CheckDataModel.class));
						}
						List<Object> data_develop = for_check_listener.getDatas();
						List<Object> data_maintain = for_check_listener.getDatas();
						if (project_class.equals("研发类") && data_develop.size() >= 59) {
							CheckDataModel d1 = (CheckDataModel) data_develop.get(0);
							CheckDataModel d2 = (CheckDataModel) data_develop.get(1);
							CheckDataModel d3 = (CheckDataModel) data_develop.get(2);
							CheckDataModel d4 = (CheckDataModel) data_develop.get(24);
							CheckDataModel d5 = (CheckDataModel) data_develop.get(31);
							CheckDataModel d6 = (CheckDataModel) data_develop.get(33);
							CheckDataModel d7 = (CheckDataModel) data_develop.get(57);
							CheckDataModel d8 = (CheckDataModel) data_develop.get(58);
							CheckDataModel d9 = (CheckDataModel) data_develop.get(59);

							if (!d1.getTemplateName().contains("技术立项申请单")) {
								result.put("error", project_name + " :裁剪表结构不符合标准，请检查");
								return result;
							}

							if (project_isPurchase.equals("是")) {
								if (d1.getCutResult().equals("0")) {
									if (d1.getReasonDescription() == null) {
										result.put("error", d1.getTemplateName() + " 裁剪结果不合理，请重新确认裁剪结果或补充原因说明");
										return result;
									}
								}
								if (d2.getCutResult().equals("0")) {
									if (d2.getReasonDescription() == null) {
										result.put("error", d2.getTemplateName() + " 裁剪结果不合理，请重新确认裁剪结果或补充原因说明");
										return result;
									}
								}
								if (d3.getCutResult().equals("0")) {
									if (d3.getReasonDescription() == null) {
										result.put("error", d3.getTemplateName() + " 裁剪结果不合理，请重新确认裁剪结果或补充原因说明");
										return result;
									}
								}
							}

							if (project_isNewSystem.equals("是")) {
								if (d4.getCutResult().equals("0")) {
									if (d4.getReasonDescription() == null) {
										result.put("error", d4.getTemplateName() + " 裁剪结果不合理，请重新确认裁剪结果或补充原因说明");
										return result;
									}
								}
								if (d5.getCutResult().equals("0")) {
									if (d5.getReasonDescription() == null) {
										result.put("error", d5.getTemplateName() + " 裁剪结果不合理，请重新确认裁剪结果或补充原因说明");
										return result;
									}
								}
								if (d6.getCutResult().equals("0")) {
									if (d6.getReasonDescription() == null) {
										result.put("error", d6.getTemplateName() + " 裁剪结果不合理，请重新确认裁剪结果或补充原因说明");
										return result;
									}
								}
								if (d7.getCutResult().equals("0")) {
									if (d7.getReasonDescription() == null) {
										result.put("error", d7.getTemplateName() + " 裁剪结果不合理，请重新确认裁剪结果或补充原因说明");
										return result;
									}
								}
								if (d8.getCutResult().equals("0")) {
									if (d8.getReasonDescription() == null) {
										result.put("error", d8.getTemplateName() + " 裁剪结果不合理，请重新确认裁剪结果或补充原因说明");
										return result;
									}
								}
								if (d9.getCutResult().equals("0")) {
									if (d9.getReasonDescription() == null) {
										result.put("error", d9.getTemplateName() + " 裁剪结果不合理，请重新确认裁剪结果或补充原因说明");
										return result;
									}
								}
							}
						}
						if (project_class.equals("维护类")) {
							CheckDataModel d1 = (CheckDataModel) data_maintain.get(0);
							if (!d1.getTemplateName().contains("进度计划")) {
								result.put("error", project_name + " :裁剪表结构不符合标准，请检查");
								return result;
							}
						}

						for (Object object : data_develop) {
							// 裁剪结果校验
							CheckDataModel develop = (CheckDataModel) object;
							if (!develop.getTemplateName().startsWith("《") || !develop.getTemplateName().endsWith("》")) {
								result.put("error", develop.getTemplateName() +" :裁剪表项目模板填写不规范，请检查");
								return result;
							}
							if (develop.getCutGuide() == null) {
								result.put("error", develop.getTemplateName() +" :项目裁剪指南填写不完整，请补充");
								return result;
							}
							if (develop.getCutResult() == null) {
								result.put("error", project_name + " :项目裁剪结果填写不完整，请补充");
								return result;
							}
							if (develop.getCutGuide().equals("1") && develop.getCutResult() != 2) {
								if (develop.getReasonDescription() == null) {
									if (!"TC平台管理".equals(develop.getNotes()) && !"双速平台管理".equals(develop.getNotes())) {
										result.put("error", project_name + " :裁剪结果不合理，请重新确认裁剪结果或补充原因说明");
										return result;
									}
								}
							}
						}
						LOG.info("-->【 开始check_map 】");
						// 封装check_map
						String[] p_num = null;
						String[] p_name = null;

						if (isLX == true) {
							p_num = LX_pronumList.toArray(new String[LX_pronumList.size()]);
							p_name = LX_pronameList.toArray(new String[LX_pronameList.size()]);
							if (p_num.length > 1 || p_name.length > 1) {
								if (p_num.length != p_name.length) {
									result.put("error", project_name +" :项目编号、项目名称、项目数量对应不上 请检查");
									return result;
								}
							}
						} else if (isWH == true) {
							p_num = wH_pronumList.toArray(new String[wH_pronumList.size()]);
							p_name = WH_pronameList.toArray(new String[WH_pronameList.size()]);
							if (p_num.length > 1 || p_name.length > 1) {
								if (p_num.length != p_name.length) {
									result.put("error",  project_name +" :项目编号、项目名称、项目数量对应不上 请检查");
									return result;
								}
							}
						} else if (isLX == false && isWH == false) {
							p_num = project_codeNum.split("、");
							p_name = project_name.split("、");
							if (p_num.length > 1 || p_name.length > 1) {
								if (p_num.length != p_name.length || p_num.length != Integer.valueOf(project_num)) {
									result.put("error", project_name + " :项目编号、项目名称、项目数量对应不上 请检查");
									return result;
								}
							}
						}
						for (int project_index = 0; project_index < p_num.length; project_index++) {
							Map<String, Object> innerData = new HashMap<String, Object>();
							innerData.put("checkTable", data_develop);
							check_map.put(p_num[project_index] + "-" + p_name[project_index], innerData);
							// 项目名
							innerData.put("projectnum", p_num[project_index]);
							innerData.put("projectname", p_name[project_index]);
							// 项目类别
							innerData.put("projectClass", project_class);
							// 其它属性
							if ("研发类".equals(project_class)) {
								innerData.put("isPurchase", project_isPurchase);
								innerData.put("isNewSystem", project_isNewSystem);
							}
							innerData.put("is_done", 0);
						}
						// 返回封装后的数据
						result.put("success", "excel数据封装完毕");
						result.put("data", check_map);

					} catch (IOException e) {
						LOG.info("-->【 文件检测发生异常，信息：" + e.getMessage() + " 】");
						result.put("error", "文件检测发生异常 请联系管理员");
						return result;
					}

				}
			} catch (Exception e) {
				LOG.info("-->【 文件检测发生异常，信息：" + e.getMessage() + " 】");
				result.put("error", "文件检测发生异常 请联系管理员");
				return result;
			}
		} else {
			result.put("error", "项目裁剪表错误！/n 请上传标准项目裁剪表，标准项目裁剪表可直接点击【裁剪表下载】获取。");
			return result;
		}
		return result;
	}
}


