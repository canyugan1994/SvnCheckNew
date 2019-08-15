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
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
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
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
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
	@Value("${dist_svn_username}")
	private String dist_svn_username;
	@Value("${dist_svn_password}")
	private String dist_svn_password;
	//http://182.180.197.136:8080/svn/appdev_vss/外围组管理文档/2019年项目文档
	@Value("${dist_svn_url}")
	private String dist_svn_url;
	@Autowired
	private SvnCheckService svnCheckService;
	@Autowired
	private SXNeedService sxNeedService;
	private String splitString = "caorui19941029";
	
	@ApiOperation(value = "[获取双速项目需求类信息]")
	@RequestMapping(value = { "/svnCheck/getAllNeedInfo" }, method = { RequestMethod.GET }, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject getAllNeedInfo() 
	{
		JSONObject result = new JSONObject();
		try {
			JSONArray temp_array = sxNeedService.sendServiceRequest();
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
	public JSONObject getAuditReport(@RequestBody AuditReport request,HttpServletRequest remoteRequest) 
	{
		LOG.info("-->【 查询请求来自["+remoteRequest.getRemoteAddr()+"] 】");
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
	 * 提交到svn
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
			LOG.info("-->【 key["+key+"] decode value["+URLDecoder.decode(param.get(key)[0])+"] encode value["+param.get(key)[0]+"] 】");
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
			if(uploadFile.length == 0) {
				LOG.info("-->【 未发现文件 】");
				result.put("state", "0001");
				result.put("errorMsg", "文档数量为0");
				return result;
			}
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
						File first_path = new File(save_path+URLDecoder.decode(department));
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
						LOG.info("-->【 开始解压文件["+file_name+"] 】");
						//解压文件
						ZipFile zipFile = null;
						try 
						{
							LOG.info("-->【 压缩文件所在目录[" + final_path.getParentFile().getPath() + "] 】");
							zipFile = new ZipFile(final_path,Charset.forName(code_style));
							Enumeration<?> entries = zipFile.entries();
							while(entries.hasMoreElements()) 
							{
								ZipEntry entry = (ZipEntry) entries.nextElement();
								if(!entry.isDirectory())
								{
									try {
										//项目编号-项目名称+分隔符+文档名
										String final_name = 
												final_path.getParentFile().getPath()+"/"
												+projectID+"-"+URLDecoder.decode(projectName).split("（")[0]
												+splitString+entry.getName();
										File tergetFile = new File(final_name);
										tergetFile.createNewFile();//创建新文件
										InputStream is = zipFile.getInputStream(entry);//获取原始文件io流
										FileOutputStream fos = new FileOutputStream(tergetFile);//目标输出流
										byte[] file_buffer = new byte[is.available()+1];
										int len;
										while((len = is.read(file_buffer)) != -1) {
											fos.write(file_buffer,0,len);
										}
										fos.close();
										is.close();
										LOG.info("-->【 解压文件["+entry.getName()+"]成功 解压后的文件名["+tergetFile.getName()+"] 】");
									} catch (Exception e) {
										LOG.info("-->【 解压文件["+entry.getName()+"] 失败】");
										result.put("state", "0001");
										result.put("errorMsg", "服务器繁忙 请联系卡中心管理员");
										return result;
									}
								}
							}
						} catch (Exception e) {
							LOG.info("-->【 解压文件["+file_name+"]失败 失败信息["+e.getMessage()+"] 】");
						}
						try {
							if(final_path.delete()) {
								LOG.info("-->【 删除原始压缩文件["+file_name+"]成功 】");
							}
						} catch (Exception e) {//删除不成功 无需理会
							LOG.info("-->【 删除原始压缩文件["+file_name+"]失败,失败信息["+e.getMessage()+"] 】");
						}
						/**
						 * 迁移到目标库
						 */
						String message = pushSvn(new File(final_path.getParentFile().getPath()),reqcode,date);//迁移文档所在路径 需求编号 日期[修复的版本]
						if(message.equals("success")) {//防止提交文件到svn失败 改为同步
							LOG.info("-->【 文档提交到svn成功 返回client端成功信息 】");
							result.put("state", "0000");
							result.put("errorMsg", null);
						}else {
							LOG.info("-->【 文档提交到svn失败 返回client端失败信息 】");
							result.put("state", "0001");
							result.put("errorMsg", "服务器繁忙 请联系卡中心管理员");
						}
						//返回TC响应
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
			result.put("state", "0001");
			result.put("errorMsg", "服务器繁忙 请联系卡中心管理员");
			return result;
		}
		return result;
	}

	/**
	 * svn相关
	 * @param repository
	 * @param path
	 * @param type
	 * @param source
	 * @return
	 * @throws SVNException
	 */
	public List<String> getSvnBaseInfo(SVNRepository repository, String path,String type,String source) throws SVNException {
		List<String> result = new LinkedList<String>();
		list(repository, path, result,type,source);
		return result;
	}
	public void list(SVNRepository repository, String path, List<String> result,String type,String source) {
		try {
			Collection entries = repository.getDir(path, -1L, null, (Collection) null);
			Iterator iterator = entries.iterator();
			while (iterator.hasNext()) {
				SVNDirEntry entry = (SVNDirEntry) iterator.next();
				if (entry.getKind() == SVNNodeKind.DIR) {
					if(type.equals("dir")) {
						result.add(dist_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
					}
				}else if(entry.getKind() == SVNNodeKind.FILE) {
					if(type.equals("file")) {
						result.add(dist_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
					}
				}
			}
		} catch (Exception e) {
			LOG.info("-->【 获取目录下面的目录或者文件["+path+"]失败 失败信息["+e.getMessage()+" 】");
		}
	}
	
	/**
	 * 迁移
	 * @param final_path
	 * @param reqcode
	 * @param date
	 * @return
	 */
	@SuppressWarnings("deprecation")
	private String pushSvn(File final_path, String reqcode, String date) 
	{
		 SVNClientManager dist_clientManager = null;//目标库
		 SVNRepository    dist_repository = null;
		 ISVNAuthenticationManager dist_authManager = null;
		 try {
			 //目标库
			 DAVRepositoryFactory.setup();
		     ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
		     dist_clientManager = SVNClientManager.newInstance((DefaultSVNOptions)options,dist_svn_username,dist_svn_password);
		     //low level api for list url
		     dist_repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(dist_svn_url));
			 dist_authManager = SVNWCUtil.createDefaultAuthenticationManager(dist_svn_username, dist_svn_password);
			 dist_repository.setAuthenticationManager(dist_authManager);
		 } catch (Exception e) {
			LOG.info("-->【 目标库svn服务器["+dist_svn_url+"]连接失败 失败信息["+e.getMessage()+"] 】");
			return "error";
		 }
		 try 
		 {
			 //开始进行TC迁移
		 	if(!final_path.exists()) {//如果根目录被删
				LOG.info("-->【 TC文档所在的根目录["+final_path+"]不存在 请检查目录是否存在 迁移作废  】");
				return "error";//快速失败
			}
			SVNCommitClient dist_commitClient = dist_clientManager.getCommitClient();//目标库提交文档、创建目录客户端
			for(File file:final_path.listFiles())
			{
				String project_info = file.getName();
				project_info = project_info.split(splitString)[0];//获得项目编号-项目名
				LOG.info("-->【 待迁移的文档所属项目信息["+project_info+"] 】");
				/**
				 * svn相关
				 */
				if(project_info.contains("LX")) {//立项类项目
					List<String> lx_project_url = getSvnBaseInfo(dist_repository,"立项类项目","dir","dist");
					boolean project_exist = false;//判断 [项目跟目录] 是否存在
					for(String lx_project:lx_project_url) {
						if(lx_project.contains(project_info)) {
							project_exist = true;
							break;
						}
					}
					if(!project_exist) {
						LOG.info("-->【 项目["+project_info+"]在svn上不存在 创建 】");
						dist_commitClient.doMkDir(new SVNURL[]{SVNURL.parseURIEncoded(dist_svn_url+"/立项类项目/"+project_info)},null);
					}
					List<String> lx_project_second_urls=getSvnBaseInfo(dist_repository,"立项类项目/"+project_info,"dir","dist");
					boolean project_doc_exist = false;//判断 [项目文档] 目录存在
					for(String url:lx_project_second_urls)
					{
						if(url.contains("项目文档")) {
							project_doc_exist = true;
							break;
						}
					}
					String url = "02 项目文档";
					if(!project_doc_exist) {//不存在02 项目文档则帮忙创建
						LOG.info("-->【 项目["+project_info+"]不存在[02 项目文档]目录 创建 】");
						dist_commitClient.doMkDir(new SVNURL[] {SVNURL.parseURIEncoded(
										dist_svn_url+"/立项类项目/"+project_info+"/"+url)},null);
					}
					List<String> lx_project_urls_final = //获取02 项目目录的下级目录
							getSvnBaseInfo(dist_repository, "立项类项目/"+project_info+"/"+url,"dir","dist");
					boolean test_exist = false;//判断06-测试目录存在与否
					for(String final_url:lx_project_urls_final) {
						if(final_url.contains("测试")) {
							test_exist = true;
							break;
						}
					}
					if(!test_exist) {//如果不存在06 测试目录 则创建
						LOG.info("-->【 项目["+project_info+"]不存在[06 测试]目录 创建 】");
						dist_commitClient.doMkDir(new SVNURL[] {SVNURL.parseURIEncoded(
										dist_svn_url+"/立项类项目/"+project_info+"/"+url+"/"+"06 测试")},null);
					}
					if(file.isFile())//避免迁移恶意创建的目录
					{
		    			try {//迁移每个文档到目标库的测试下
		    				LOG.info("-->【 迁移到的目录["+dist_svn_url+"/立项类项目/"+project_info+"/"+url+"/06 测试] 】");
							dist_commitClient.doImport(file,//文件在磁盘文件的路径
									SVNURL.parseURIEncoded(dist_svn_url+"/立项类项目/"+project_info+"/"+url+"/06 测试/"+file.getName().split(splitString)[1]),null,false);
							LOG.info("-->【 TC的文档["+file+"]迁移到目标库立项类项目["+project_info+"]下属目录06 测试目录 迁移完成】");
				    	} catch (Exception e) {//可能是上传了重复文档 
							LOG.info("-->【 上传文档到svn目标库目录["+dist_svn_url+"/立项类项目/"+project_info+"/"+
									url+"/06 测试]失败 失败信息["+e.getMessage()+"] ");
						}
		    		}
				}else if(project_info.contains("WH")){//维护类项目
					List<String> wh_project_urls = getSvnBaseInfo(dist_repository,"维护优化项目","dir","dist");
					boolean project_exist = false;//判断 [项目根目录] 是否存在
					for(String wh_project:wh_project_urls) {
						if(wh_project.contains(project_info)) {
							project_exist = true;
							break;
						}
					}
					if(!project_exist) {
						LOG.info("-->【 项目["+project_info+"]在svn上不存在 创建 】");
						dist_commitClient.doMkDir(new SVNURL[] {SVNURL.parseURIEncoded(dist_svn_url+"/维护优化项目/"+project_info)},null);
					}
					List<String> project_release_urls = getSvnBaseInfo(dist_repository,"维护优化项目/"+project_info,"dir","dist");
					//拆解date得到日期 如果是非常规发布 不做任何截取
					boolean non_routine = false;
					if(!date.contains("非常规发布")) {
						date = date.substring(0,10).replace("-","");//从2019-08-09 00:00:00.0到20190809
					}else {
						non_routine = true;
					}
					boolean release_exist = false;
					for(String release_url:project_release_urls) {
						if(release_url.contains(date)) {
							release_exist = true;
							break;
						}
					}
					String release_final = null;
					if(non_routine) {
						release_final = date;
					}else {
						release_final = "Release-"+date;
					}
					if(!release_exist) {
						LOG.info("-->【 项目["+project_info+"]在svn上不存在修复的版本["+date+"] 创建 】");
						dist_commitClient.doMkDir(new SVNURL[]{
								SVNURL.parseURIEncoded(dist_svn_url+"/维护优化项目/"+project_info+"/"+release_final)},null);
					}
					List<String> project_rqn_urls = getSvnBaseInfo(dist_repository,"维护优化项目/"+project_info+"/"+release_final,"dir","dist");
					boolean rqn_exist = false;
					for(String rqn_url:project_rqn_urls) {
						if(rqn_url.contains(reqcode)) {
							rqn_exist = true;
							break;
						}
					}
					if(!rqn_exist) {
						LOG.info("-->【 项目["+project_info+"]在svn上不存在修复的版本["+release_final+"]的需求编号["+reqcode+"] 创建 】");
						dist_commitClient.doMkDir(new SVNURL[]{
								SVNURL.parseURIEncoded(dist_svn_url+"/维护优化项目/"+project_info+"/"+release_final+"/"+reqcode)},null);
					}
					List<String> project_final_urls = getSvnBaseInfo(dist_repository,"维护优化项目/"+project_info+"/"+release_final+"/"+reqcode,"dir","dist");
					boolean test_exist = false;
					for(String final_url:project_final_urls) {
						if(final_url.contains("测试")) {
							test_exist = true;
							break;
						}
					}
					if(!test_exist) {
						LOG.info("-->【 项目["+project_info+"]在svn上不存在修复的版本["+release_final+"]的需求编号["+reqcode+"]的[03-测试]目录 创建 】");
						dist_commitClient.doMkDir(new SVNURL[]{
								SVNURL.parseURIEncoded(dist_svn_url+"/维护优化项目/"+project_info+"/"+release_final+"/"+reqcode+"/03-测试")},null);
					}
					if(file.isFile())//排除恶意的人创建目录 做无谓迁移
					{
		    			try {//迁移每个文档到目标库的测试下
		    				LOG.info("-->【 迁移到的目录["+dist_svn_url+"/维护优化项目/"+project_info+"/"+release_final+"/"+reqcode+"/03-测试] 】");
							dist_commitClient.doImport(
									file,//文件在磁盘文件的路径
									SVNURL.parseURIEncoded(
											dist_svn_url+"/维护优化项目/"+project_info+"/"+
											release_final+"/"+reqcode+"/03-测试/"+file.getName().split(splitString)[1]),null,false);
							LOG.info("-->【 tc文档["+file+"]迁移到目标库维护类目录["+dist_svn_url+"/维护优化项目/"+
												    project_info+"/"+release_final+"/"+reqcode+"/03-测试]迁移完成】");
				    	} catch (Exception e) {
							LOG.info("-->【 迁移tc文档到目标库项目["+project_info+"]目录"+"["+dist_svn_url+"/维护优化项目/"+
									project_info+"/"+release_final+"/"+reqcode+"/03-测试]失败 失败信息:"+e.getMessage()+" 】");
						}
					}
				}
			}
		} catch (Exception e) {
			LOG.info("-->【 发生错误 错误信息["+e.getMessage()+"] 返回error信息 】");
			return "error";
		}
		return "success";
	}
}
