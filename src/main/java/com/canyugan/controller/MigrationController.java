package com.canyugan.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.service.SXNeedService;
import com.canyugan.service.SvnCheckService;
import com.canyugan.util.DateUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import java.io.File;
import java.text.SimpleDateFormat;
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
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
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
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNUpdateClient;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * svn文档迁移
 * @author caorui
 */
@Api(value = "文档迁移相关接口", tags = { "文档迁移相关接口" })
@RestController
public class MigrationController 
{
	private static final Logger LOG = LoggerFactory.getLogger(MigrationController.class);
	@Autowired
	private SXNeedService sxNeedService;
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
	@Value("${execute_num}")
	private Integer execute_num;//同时迁移的人数
	@Value("${svn_save_path}")
	private String svn_save_path;
	@Autowired
	private SvnCheckService svnCheckService;
	@Autowired
	private JedisPool JedisPool;//redis连接池
	private DateUtil dateUtil = new DateUtil();
	
	@ApiModel(value = "Migration", description = "")
	@Data
	static class Migration 
	{
		@ApiModelProperty(value = "userId", name = "用户id", dataType = "string", example = "A167347")
		private String user_id;
		@ApiModelProperty(value =  "projectIds" ,name = "勾选项目集合" ,
				example = "[\"PJ-LX-2018-002-001-人工智能平台运营\",\"PJ-LX-2019-003-002-onesight人工智能平台运营\"]")
		private JSONArray projectIds;
	}

	/**
	 * 暂时不用寻找需求库和tc文档与目标库的文件异同
	 * 只要迁移 直接把该项目对应的文件迁移到目标库
	 * @param request
	 * @return
	 */
	@ApiOperation(value = "svn文档迁移接口",notes = "四种状态 0待迁移 1迁移中 2迁移完成 3迁移失败")
	@RequestMapping(value = { "/svnCheck/svnFileMigration" }, method = { RequestMethod.POST }, produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject svnFileMigration(@RequestBody Migration request) 
	{
		JSONObject result = new JSONObject();
		try {
			JSONArray project_array = request.getProjectIds();//["PJ-LX-2018-002-001-人工智能平台运营","PJ-LX-2019-001-002-运维知识库"]
			LOG.info("-->【 用户勾选的需要迁移的项目：" + project_array.toJSONString()  + " 】");
			/**
			 * 需求编号 RQN-2018-IT039-0039
			   项目名称  PJ-LX-2018-002-001-人工智能平台运营
			   投产窗口 Release-20181117
			 */
			JSONArray need_project = sxNeedService.sendServiceRequest();//从双速需求信息接口获取数据
			if(need_project == null) {
				result.put("status","error");
				result.put("message","双速需求库接口调用失败 本次无法进行后续迁移");
				return result;
			}
			/**
			 * 过滤出需要迁移的项目
			 * 一个项目可能对应很多个需求编号
			 * example：
			 * 	PJ-LX-2018-002-001-人工智能平台运营
			 *     RQN-2018-IT039-0039
			 *     RQN-2018-IT039-0040
			 *     ...
			 */
			Map<String,Set<Map<String, String>>> all_project_needMove = new HashMap<String, Set<Map<String, String>>>();
			Set<Map<String, String>> info_rqn_project_set = null;
			Map<String, String> info_rqn_project = null;
			JSONObject temp = new JSONObject();
			String current_select_project = null;
			
			int project_array_len = project_array.size();
			int need_project_len = need_project.size();
			for(int index_project = 0;index_project < project_array_len;index_project++) {
				current_select_project = project_array.getString(index_project);
				info_rqn_project_set = new HashSet<Map<String,String>>();
				for(int index_need_project = 0;index_need_project < need_project_len;index_need_project++) {
					temp = need_project.getJSONObject(index_need_project);
					if(temp.getString("projectName") != null && temp.getString("projectName").equals(current_select_project)) {
						info_rqn_project = new HashMap<String, String>();
						info_rqn_project.put("project_num",temp.getString("project_num"));//需求编号
						info_rqn_project.put("projectVersion",temp.getString("projectVersion"));//投产窗口
						info_rqn_project_set.add(info_rqn_project);
					}
				}
				all_project_needMove.put(current_select_project,info_rqn_project_set);
			}
			LOG.info("-->【 双速需求文档库信息接口和用户勾选迁移项目信息匹配后的项目信息：" + JSONObject.toJSON(all_project_needMove) + " 】");
			if(all_project_needMove.size() == 0) {
				result.put("status","error");
				result.put("message","勾选项目和校验库的项目信息匹配成功数量为0 无法进行后续迁移");
				return result;
			}
			/**
			 * 维护一个hash结构的数据 存储在redis中
			 * 20190722113027userid
			 * 	project_key1 0/1
			 *  project_key2 0/1
			 *  ...
			 *  0待迁移 1迁移中 2迁移完成 3迁移失败
			 */
			Map<String, String> hash_project_migration = new HashMap<String, String>();
			Set<String> p_key = all_project_needMove.keySet();
			Iterator<String> iterator_key = p_key.iterator();
			while(iterator_key.hasNext()) {
				hash_project_migration.put(iterator_key.next(), "1");
			}
			//当前用户本次迁移redis key
			final String migration_key = dateUtil.currentDateTime() + request.getUser_id();
			Jedis jedis = null;
			try {
				jedis = JedisPool.getResource();
				String resp_redis = jedis.hmset(migration_key, hash_project_migration);//设置每个项目的初始迁移状态
				if(!"OK".equals(resp_redis)) {
					result.put("status","error");
					result.put("message","当前用户迁移项目的项目状态保存至redis失败 请联系管理员");
					jedis.close();
					return result;
				}
				LOG.info("-->【 当前用户迁移项目的项目状态保存至redis成功 】");
				jedis.close();
			} catch (Exception e) {
				LOG.info("-->【 项目状态保存至redis出错，错误信息：" + e.getMessage() + "】");
				result.put("status","error");
				result.put("message","项目状态保存至redis出错 请联系管理员");
				return result;
			}
			
			//迁移线程池
			final ThreadPoolExecutor pool = new ThreadPoolExecutor(6, 6, 0, TimeUnit.MILLISECONDS,new LinkedBlockingQueue());
			/**
			 * 迁移需求库到目标库立项类
			 * 	本线程 负责立项类项目迁移
			 */
			Future<String> lx_thread_result = pool.submit(new Callable<String>()
			{
				public List<String> getSvnBaseInfo(SVNRepository repository, String path,String type,String source) throws SVNException {
					List<String> result = new LinkedList<String>();
					list(repository, path, result,type,source);
					return result;
				}
				//打印对应目录或者文件
				public void list(SVNRepository repository, String path, List<String> result,String type,String source) {
					try {
						Collection entries = repository.getDir(path, -1L, null, (Collection) null);
						Iterator iterator = entries.iterator();
						while (iterator.hasNext()) {
							SVNDirEntry entry = (SVNDirEntry) iterator.next();
							if (entry.getKind() == SVNNodeKind.DIR) {
								if(type.equals("dir")) {
									if(source.equals("need")) {
										result.add(need_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}else if(source.equals("dist")) {
										result.add(dist_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}
								}
							}else if(entry.getKind() == SVNNodeKind.FILE) {
								if(type.equals("file")) {
									if(source.equals("need")) {
										result.add(need_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}else if(source.equals("dist")) {
										result.add(dist_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}
								}
							}
						}
					} catch (Exception e) {
						LOG.info("-->立项类项目迁移线程【 获取目录或者文件[" + path + "]失败，失败信息：" + e.getMessage() + " 】");
					}
				}

				@SuppressWarnings("deprecation")
				@Override
				public String call() throws Exception 
				{
				     SVNClientManager need_clientManager = null;//需求库
					 SVNRepository    need_repository = null;ISVNAuthenticationManager need_authManager = null;
					 SVNClientManager dist_clientManager = null;//目标库
					 SVNRepository    dist_repository = null;ISVNAuthenticationManager dist_authManager = null;
					 try {
						 //需求库
						 DAVRepositoryFactory.setup();
					     ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
					     need_clientManager = SVNClientManager.newInstance((DefaultSVNOptions)options,need_svn_username,need_svn_password);
					     //low level api ; for list file or dir
					     need_repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(need_svn_url));
						 need_authManager = SVNWCUtil.createDefaultAuthenticationManager(need_svn_username, need_svn_password);
						 need_repository.setAuthenticationManager(need_authManager);
					 } catch (Exception e) {
						LOG.info("-->立项类项目迁移线程【 svn服务器连接失败，失败信息：" + e.getMessage() + " 】");
						return "error";
					 }
					 try {
						 //目标库
						 DAVRepositoryFactory.setup();
					     ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
					     dist_clientManager = SVNClientManager.newInstance((DefaultSVNOptions)options,dist_svn_username,dist_svn_password);
					     //low level api ; for list file or dir
					     dist_repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(dist_svn_url));
						 dist_authManager = SVNWCUtil.createDefaultAuthenticationManager(dist_svn_username, dist_svn_password);
						 dist_repository.setAuthenticationManager(dist_authManager);
					 } catch (Exception e) {
						LOG.info("-->立项类项目迁移线程【 svn服务器连接失败，失败信息：" + e.getMessage() + " 】");
						return "error";
					 }
					//立项类项目迁移逻辑
					Set<String> project_keys = all_project_needMove.keySet();//待迁移项目的集合
					Set<Map<String, String>> temp_map_list = new HashSet<Map<String,String>>();//每个项目对应的RQN集合
					try {
						SVNUpdateClient need_updateClient = need_clientManager.getUpdateClient();//需求库下载客户端
						SVNCommitClient dist_commitClient = dist_clientManager.getCommitClient();//目标库提交文档客户端
						//遍历勾选的项目PJ-LX-云上卡中心-2019-001
						for(String project_key:project_keys) 
						{
							if(!project_key.contains("LX")) {
								LOG.info("-->立项类项目迁移线程【 抱歉，非LX立项项目，本线程不负责迁移 】");
								continue;
							}
							String year = dateUtil.currentDateTime().substring(0, 4);//获取当前执行年份
							List<String> depart_url = getSvnBaseInfo(need_repository, "","dir","need");//获取svn根路径下的部门路径
							for (String depart : depart_url) 
							{
								/**
								 * step 1: 需求库
								 */
								depart = depart.substring(depart.lastIndexOf("/")+1,depart.length());
								LOG.info("-->立项类项目迁移线程【 正在处理部门:" + depart + " 】");
								//获取例如 【 业务管理部/2019 】的RQN目录信息
								List<String> rqn_year = getSvnBaseInfo(need_repository,depart+"/"+year,"dir","need");
								/**
								 * RQN-2018-IT039-0039
								 * RQN-2018-IT039-0040
								 * ...
								 */
								for(String rqn:rqn_year)
								{
									if (rqn.contains("RQN"))//有的目录不是以RQN开头 忽略 加快迁移速度
									{
										temp_map_list = all_project_needMove.get(project_key);//获取项目编号+项目名称对应的rqn和投产窗口等信息
										//RQN-2018-IT039-0039
										String rqn_direct = rqn.substring(rqn.lastIndexOf("/") + 1, rqn.length());
										Map<String, String> temp_map = null;
										//遍历每个项目下面的所以RQN编号
										for(Map<String, String> temp_map_single:temp_map_list) 
										{
											temp_map = temp_map_single;
											//项目的此rqn编号和svn的rqn编号能匹配上
											if(temp_map.get("project_num").equals(rqn_direct)) 
											{
												LOG.info("-->立项类项目迁移线程【 svn上的RQN编号和项目的RQN编号匹配成功,RQN["+rqn_direct+"],projectName["+project_key+"] 】");
												List<String> rqn_dirs = getSvnBaseInfo(need_repository,depart+"/"+year+"/"+rqn_direct,"dir","need");
												for(String rqn_dir:rqn_dirs) //01-需求 || 02-TF目录
												{
													String rqn_dir_direct = rqn_dir.substring(rqn_dir.lastIndexOf("/"), rqn_dir.length());
													/**
													 * step 2: 目标库
													 */
													List<String> lx_project_urls = getSvnBaseInfo(dist_repository, "立项类项目","dir","dist");
													if(rqn_dir_direct.contains("TF") || rqn_dir_direct.contains("需求"))//02-TF
													{
														for(String lx_project_url:lx_project_urls)//PJ-LX-2018-002-001-人工智能平台运营 此类集合
														{
															//匹配立项类项目的项目名和要迁移的项目 
															String lx_project_url_direct = lx_project_url.substring(lx_project_url.lastIndexOf("/")+1,lx_project_url.length());
															if(lx_project_url_direct.equals(project_key)) //PJ-LX-2018-002-001-人工智能平台运营
															{
																LOG.info("-->立项类项目迁移线程【 svn项目目录和待迁移的项目一致 [" + lx_project_url_direct + "],[" + project_key +  "] 】");
																//PJ-LX-2019-001-001-PDA中间件系统重构上云 匹配 svnurl【PJ-LX-2019-001-001-PDA中间件系统重构上云】
																List<String> lx_project_urls_other = 
																		getSvnBaseInfo(dist_repository,"立项类项目/"+lx_project_url_direct,"dir","dist");
																for(String lx_project_urls_other_single:lx_project_urls_other) //01项目管理文档 02项目文档 03其它文档
																{
																	//拆解得到 02项目文档 【 需求和TF文档存放处 】
																	String other_direct = lx_project_urls_other_single.substring(
																			lx_project_urls_other_single.lastIndexOf("/")+1,lx_project_urls_other_single.length());
																	if(other_direct.contains("项目文档")) 
																	{
																		List<String> find_tf_dirs = getSvnBaseInfo(
																						dist_repository,"立项类项目/"+lx_project_url_direct+"/"+other_direct,"dir","dist"); 
																		for(String find_tf_dir:find_tf_dirs)//02需求 03TF
																		{
																			String tf_dir = find_tf_dir.substring(find_tf_dir.lastIndexOf("/")+1, find_tf_dir.length());
																			//目标库的需求或者TF目录一定要和此刻需求库的对应
																			String temp_tf_dir = null;
																			if(tf_dir.contains("TF")) {//是TF目录
																				temp_tf_dir = "TF";
																			}else if(tf_dir.contains("需求")) {//是需求目录
																				temp_tf_dir = "需求";
																			}
																			if(temp_tf_dir != null && rqn_dir_direct.contains(temp_tf_dir)) 
																			{
																				//匹配到目标库项目对应的tf目录
																				String path = svn_save_path+rqn_direct+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
																				File move_path = new File(path);
																				if(!move_path.exists()) 
																				{
																					boolean success = move_path.mkdir();//创建目录
																					if(success) 
																					{
																						LOG.info("-->立项类项目迁移线程【 磁盘上创建目录成功 ，正在从svn目录[" 
																					      + need_svn_url + depart + "/" + year 
																					      + "/" + rqn_direct + "/" + rqn_dir_direct + "]上下载文件】");
																						//把需求库 [需求] [TF] 文档下载到创建的目录
																					    need_updateClient.setIgnoreExternals(false);
																					    need_updateClient.doExport(SVNURL.parseURIEncoded(
																					      need_svn_url + depart + "/" + year + 
																					      "/" + rqn_direct + "/" + rqn_dir_direct), 
																					      move_path, SVNRevision.HEAD, SVNRevision.HEAD, "", true, true);
																					    //如果目标库存在文档 则迁移单位选择目录会导致全部失败 ，故本次迁移目标为单个文档
																					    File[] files = move_path.listFiles();
																					    if(files.length !=0) {
																					    	for(File single_file:files) 
																						    {
																					    		//迁移单位：文件非目录
																					    		if(single_file.isFile()) {
																					    			try {
																							    		//迁移每个文档到目标库的需求或者TF目录下
																					    				LOG.info("-->立项类项目迁移线程【 迁移到的目录:" + 
																					    						(dist_svn_url+"/立项类项目/"+lx_project_url_direct+"/"+other_direct+"/"+tf_dir+"/"+single_file.getName()) + "】");
																										dist_commitClient.doImport(
																												single_file,//文件在磁盘文件的路径
																												SVNURL.parseURIEncoded(
																														dist_svn_url+"/立项类项目/"+lx_project_url_direct+
																														"/"+other_direct+"/"+tf_dir+"/"+single_file.getName()),null,false);
																										LOG.info("-->立项类项目迁移线程【 文档：" + single_file + " 迁移完成】");
																										/**
																										 * 设置redis中的项目迁移状态
																										 *  0待迁移 1迁移中 2迁移完成 3迁移失败
																										 */
																										Jedis jedis = JedisPool.getResource();
																										jedis.hset(migration_key, project_key, "2");
																							    	} catch (Exception e) {
																										LOG.info("-->立项类项目迁移线程【 迁移需求库文档到目标库失败，失败信息:" + e.getMessage() +  " 】");
																									}
																					    		}
																						    }
																					    	LOG.info("-->立项类项目迁移线程【 开始删除文件 】");
																					    	//开始删除文件
																					    	for(File delete_file:files) {
																					    		try {
																									delete_file.delete();
																								} catch (Exception e) {
																									LOG.info("-->立项类项目迁移线程【 文件" + delete_file + "删除失败 】");
																								}
																					    	}
																					    	move_path.delete();
																					    	LOG.info("-->立项类项目迁移线程【 结束删除文件 】");
																					    }else {
																					    	LOG.info("-->立项类项目迁移线程【 磁盘文件系统的目录下不存在文件 】");
																					    }
																					}else {
																						LOG.info("-->立项类项目迁移线程【 在服务器文件系统上创建目录失败，目录：" + path + " 】");
																					}
																				}
																			}
																		}
																	}
																}
															}
														}
													}else {
														LOG.info("-->立项类项目迁移线程【 匹配成功的项目不存在需求目录和TF目录 请仔细检查svn 】");
													}
												}
												break;//退出本次拿需求库svn RQN匹配项目的RQN列表的过程 进行下次svn RQN匹配
											}
										}
									}
								}
							}
						}
					} catch (Exception e) {
						LOG.info("-->立项类项目迁移线程【 发生错误，错误信息：" + e.getMessage()  + " 】返回守护线程error信息");
						return "error";
					}
					return "success";
				}
			});
			
			/**
			 * 迁移需求库到目标库维护类
			 * 本线程 负责维护类项目迁移
			 */
			Future<String> wh_thread_result = pool.submit(new Callable<String>()
			{
				public List<String> getSvnBaseInfo(SVNRepository repository, String path,String type,String source) throws SVNException {
					List<String> result = new LinkedList<String>();
					list(repository, path, result,type,source);
					return result;
				}
				//打印对应目录
				public void list(SVNRepository repository, String path, List<String> result,String type,String source) {
					try {
						Collection entries = repository.getDir(path, -1L, null, (Collection) null);
						Iterator iterator = entries.iterator();
						while (iterator.hasNext()) {
							SVNDirEntry entry = (SVNDirEntry) iterator.next();
							if (entry.getKind() == SVNNodeKind.DIR) {
								if(type.equals("dir")) {
									if(source.equals("need")) {
										result.add(need_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}else if(source.equals("dist")) {
										result.add(dist_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}
								}
							}else if(entry.getKind() == SVNNodeKind.FILE) {
								if(type.equals("file")) {
									if(source.equals("need")) {
										result.add(need_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}else if(source.equals("dist")) {
										result.add(dist_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}
								}
							}
						}
					} catch (Exception e) {
						LOG.info("-->维护类项目迁移线程【 获取目录下面的目录或者文件[" + path + "]失败，失败信息：" + e.getMessage() + " 】");
					}
				}
				
				@SuppressWarnings("deprecation")
				@Override
				public String call() throws Exception 
				{
					 SVNClientManager need_clientManager = null;//需求库
					 SVNRepository    need_repository = null;ISVNAuthenticationManager need_authManager = null;
					 SVNClientManager dist_clientManager = null;//目标库
					 SVNRepository    dist_repository = null;ISVNAuthenticationManager dist_authManager = null;
					 try {
						//需求库
						 DAVRepositoryFactory.setup();
					     ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
					     need_clientManager = SVNClientManager.newInstance((DefaultSVNOptions)options,need_svn_username,need_svn_password);
					     //low level api
					     need_repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(need_svn_url));
						 need_authManager = SVNWCUtil.createDefaultAuthenticationManager(need_svn_username, need_svn_password);
						 need_repository.setAuthenticationManager(need_authManager);
					 } catch (Exception e) {
						LOG.info("-->维护类项目迁移线程 【 svn服务器连接失败，失败信息：" + e.getMessage() + " 】");
						return "error";
					 }
					 try {
						//目标库
						 DAVRepositoryFactory.setup();
					     ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
					     dist_clientManager = SVNClientManager.newInstance((DefaultSVNOptions)options,dist_svn_username,dist_svn_password);
					     //low level api
					     dist_repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(dist_svn_url));
						 dist_authManager = SVNWCUtil.createDefaultAuthenticationManager(dist_svn_username, dist_svn_password);
						 dist_repository.setAuthenticationManager(dist_authManager);
					 } catch (Exception e) {
						LOG.info("-->维护类项目迁移线程 【 svn服务器连接失败，失败信息：" + e.getMessage() + " 】");
						return "error";
					 }
					 
					//迁移逻辑
					Set<String> project_keys = all_project_needMove.keySet();//待迁移项目的集合
					Set<Map<String, String>> temp_map_list = new HashSet<Map<String,String>>();//每个项目对应的RQN集合
					try {
						SVNUpdateClient need_updateClient = need_clientManager.getUpdateClient();//需求库下载客户端
						SVNCommitClient dist_commitClient = dist_clientManager.getCommitClient();//目标库提交文档、创建目录客户端 
						for(String project_key:project_keys) 
						{
							if(!project_key.contains("WH")) {
								LOG.info("-->维护类项目迁移线程【 抱歉，非WH维护项目，本线程不负责迁移 】");
								continue;
							}
							String year = dateUtil.currentDateTime().substring(0, 4);//获取当前执行年份
							List<String> depart_url = getSvnBaseInfo(need_repository, "","dir","need");//获取svn根路径下的部门路径
							for (String depart : depart_url) 
							{
								/**
								 * step 1: 需求库
								 */
								depart = depart.substring(depart.lastIndexOf("/")+1,depart.length());
								LOG.info("-->维护类项目迁移线程【 正在处理部门:" + depart + " 】");
								//获取例如http://182.180.49.163:8080/svn/RQAL/业务管理部/2019的文档目录信息
								List<String> rqn_year = getSvnBaseInfo(need_repository,depart+"/"+year,"dir","need");
								for(String rqn:rqn_year) 
								{
									if (rqn.contains("RQN"))//有的目录不是以RQN开头 忽略 加快迁移速度
									{
										temp_map_list = all_project_needMove.get(project_key);//获取项目编号+项目名称对应的rqn和投产窗口等信息
										String rqn_direct = rqn.substring(rqn.lastIndexOf("/") + 1, rqn.length());//RQN-2018-IT039-0039
										Map<String, String> temp_map = null;
										//遍历每个项目下面的所以RQN编号
										for(Map<String, String> temp_map_single:temp_map_list) 
										{
											temp_map = temp_map_single;
											//项目的rqn编号和svn的rqn编号能匹配上 
											if(temp_map.get("project_num").equals(rqn_direct))
											{
												LOG.info("-->维护类项目迁移线程【 svn上的RQN编号和项目的RQN编号匹配成功,RQN["+rqn_direct+"],projectName["+project_key+"] 】");
												List<String> rqn_dirs = getSvnBaseInfo(need_repository,depart+"/"+year+"/"+rqn_direct,"dir","need");
												for(String rqn_dir:rqn_dirs) //01-需求 || 02-TF目录
												{
													String rqn_dir_direct = rqn_dir.substring(rqn_dir.lastIndexOf("/"), rqn_dir.length());
													/**
													 * step 2: 目标库
													 */
													List<String> wh_project_urls = getSvnBaseInfo(dist_repository, "维护优化项目","dir","dist");
													if(rqn_dir_direct.contains("TF") || rqn_dir_direct.contains("需求"))//02-TF
													{
														LOG.info("-->维护类项目迁移线程【 正在处理子目录[" + rqn_dir_direct + "] 】");
														for(String wh_project_url:wh_project_urls)//PJ-WH-2018-002-001-人工智能平台运营 此类集合
														{
															//匹配维护类项目的项目名和要迁移的项目
															String wh_project_url_direct = wh_project_url.substring(wh_project_url.lastIndexOf("/")+1,wh_project_url.length());
															if(wh_project_url_direct.equals(project_key)) //PJ-LX-2018-002-001-人工智能平台运营
															{
																LOG.info("-->维护类项目迁移线程 【 svn项目目录和待迁移的项目一致 [" + wh_project_url_direct + "] 】");
																/**
																 * 开始创建目录
																 * projectName 项目编号+项目名称
																	  project_num 需求编号
																	  projectVersion 投产窗口
																 */
																try {
																	//获取该项目的投产窗口 需求编号 以及例如01-需求 02-TF此类从需求库获得的目录
																	String project_Version = temp_map.get("projectVersion");//Release-20181117
																	String project_num  = temp_map.get("project_num");//RQN-2018-IT039-0039
																	String child_dir = rqn_dir_direct;//01-需求
																	LOG.info("-->维护类项目迁移线程【 投产窗口[" + project_Version + "]需求编号[" + project_num + "],子目录[" + child_dir + "] 】");
																	/*
																	 * 检查投产窗口目录
																	 */
																	LOG.info("-->维护类项目迁移线程【 正在检查投产窗口目录 】");
																	List<String> temp_list1 = 
																			getSvnBaseInfo(dist_repository,"维护优化项目/"+project_key, "dir", "dist");
																	boolean pv_exist = false;
																	String temp_px = null;
																	for(String temp_l:temp_list1) {//查看对应投产窗口目录是否存在
																		temp_px = temp_l.substring(temp_l.lastIndexOf("/")+1,temp_l.length());
																		if(project_Version.equals(temp_px)) {
																			pv_exist = true;//投产窗口目录已经存在
																		}
																	}
																	if(!pv_exist) {//不存在 创建
																		dist_commitClient.doMkDir(new SVNURL[] {
																			SVNURL.parseURIEncoded(dist_svn_url+"/维护优化项目/"+project_key+"/"+project_Version)
																	    },null);
																	}
																	/*
																	 * 检查需求编号目录
																	 */
																	LOG.info("-->维护类项目迁移线程【 正在检查需求编号目录 】");
																	List<String> temp_list2 = 
																			getSvnBaseInfo(dist_repository,
																					"维护优化项目/"+project_key+"/"+project_Version, "dir", "dist");
																	boolean rqn_exist = false;
																	String temp_rqn = null;
																	for(String temp_l2:temp_list2) {//查看需求编号是否存在
																		temp_rqn = temp_l2.substring(temp_l2.lastIndexOf("/")+1, temp_l2.length());
																		if(project_num.equals(temp_rqn)) {
																			rqn_exist = true;
																		}
																	}
																	if(!rqn_exist) {//不存在 创建
																		dist_commitClient.doMkDir(new SVNURL[] {
																				SVNURL.parseURIEncoded(
																						dist_svn_url+"/维护优化项目/"+project_key+"/"+project_Version+"/"+project_num)
																		},null);
																	}
																	/*
																	 * 检查01-需求 final目录
																	 */
																	List<String> temp_list3 = 
																			getSvnBaseInfo(dist_repository,
																					"维护优化项目/"+project_key+"/"+project_Version+"/"+project_num, "dir", "dist");
																	boolean final_dir_exixt = false;
																	String temp_final_dir = null;
																	for(String temp_l3:temp_list3) {
																		temp_final_dir = temp_l3.substring(temp_l3.lastIndexOf("/")+1,temp_l3.length());
																		if(child_dir.contains(temp_final_dir)) {
																			final_dir_exixt = true;
																		}
																	}
																	if(!final_dir_exixt) {
																		dist_commitClient.doMkDir(new SVNURL[] {
																				SVNURL.parseURIEncoded(
																						dist_svn_url+"/维护优化项目/"+project_key+"/"+project_Version+"/"+project_num+"/"+child_dir)
																		    },null);
																	}
																	LOG.info("-->维护类项目迁移线程【 目录创建完毕 开始迁移 】");
																	//目录创建完毕 开始迁移
																	String path = svn_save_path+rqn_direct+new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
																	File move_path = new File(path);
																	if(!move_path.exists()) 
																	{
																		boolean success = move_path.mkdir();//创建目录
																		if(success) 
																		{
																			LOG.info("-->维护类项目迁移线程【 磁盘上创建目录成功 ，正在从svn目录[" 
																				      + need_svn_url + depart + "/" + year 
																				      + "/" + rqn_direct + "/" + rqn_dir_direct + "]上下载文件】");
																			//把需求库 [需求] [TF] 文档下载到创建的目录
																		    need_updateClient.setIgnoreExternals(false);
																			need_updateClient.doExport(SVNURL.parseURIEncoded(
																				      need_svn_url + depart + "/" + year + 
																				      "/" + rqn_direct + "/" + rqn_dir_direct), 
																				      move_path, SVNRevision.HEAD, SVNRevision.HEAD, "", true, true);
																			//如果目标库存在文档 则迁移单位选择目录会导致全部失败 ，故本次迁移目标为单个文档
																		    File[] files = move_path.listFiles();
																		    if(files.length !=0) {
																		    	for(File single_file:files) 
																			    {
																		    		//迁移单位：文件非目录
																		    		if(single_file.isFile()) {
																		    			try {
																				    		//迁移每个文档到目标库的需求或者TF目录下
																		    				LOG.info("-->维护类项目迁移线程【 迁移到的目录:" + 
																		    						(dist_svn_url+"/维护优化项目/"+project_key+"/"+project_Version+"/"+
																		    							project_num+"/"+child_dir+"/"+single_file.getName()) + "】");
																							dist_commitClient.doImport(
																									single_file,//文件在磁盘文件的路径
																									SVNURL.parseURIEncoded(
																											dist_svn_url+"/维护优化项目/"+project_key+"/"+project_Version+"/"+project_num+"/"+
																									child_dir+"/"+single_file.getName()),null,false);
																							LOG.info("-->【 文档：" + single_file + " 迁移完成】");
																				    	} catch (Exception e) {
																							LOG.info("-->【 迁移需求库文档到目标库失败，失败信息:" + e.getMessage() +  " 】");
																						}
																		    		}
																			    }
																		    	LOG.info("-->维护类项目迁移线程【 开始删除文件 】");
																		    	//开始删除文件
																		    	for(File delete_file:files) {
																		    		try {
																						delete_file.delete();
																					} catch (Exception e) {
																						LOG.info("-->【 文件" + delete_file + "删除失败 】");
																					}
																		    	}
																		    	move_path.delete();
																		    	LOG.info("-->维护类项目迁移线程【 结束删除文件 】");
																		    }
																		}
																	}else {
																		LOG.info("-->维护类项目迁移线程【 在服务器文件系统上创建目录失败，目录：" + path + " 】");
																	}
																} catch (Exception e) {
																	LOG.info("-->维护类项目迁移线程【 目标库创建项目svn目录发生异常，异常信息：" + e.getMessage() + " 】");
																}
															}
														}
													}
												}
												break;
											}
										}
									}
								}
							}
						}
					}catch (Exception e) {
						LOG.info("-->维护类项目迁移线程【 线程发生错误，错误信息：" + e.getMessage()  + " 】返回守护线程error信息");
						return "error";
					}
					return "success";
				}
			});
			
			/**
			 * 迁移Tc库到目标库维护类
			 */
			Future<String> tc_thread = pool.submit(new Callable<String>()
			{
				public List<String> getSvnBaseInfo(SVNRepository repository, String path,String type,String source) throws SVNException {
					List<String> result = new LinkedList<String>();
					list(repository, path, result,type,source);
					return result;
				}
				//打印对应目录
				public void list(SVNRepository repository, String path, List<String> result,String type,String source) {
					try {
						Collection entries = repository.getDir(path, -1L, null, (Collection) null);
						Iterator iterator = entries.iterator();
						while (iterator.hasNext()) {
							SVNDirEntry entry = (SVNDirEntry) iterator.next();
							if (entry.getKind() == SVNNodeKind.DIR) {
								if(type.equals("dir")) {
									if(source.equals("need")) {
										result.add(need_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}else if(source.equals("dist")) {
										result.add(dist_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}
								}
							}else if(entry.getKind() == SVNNodeKind.FILE) {
								if(type.equals("file")) {
									if(source.equals("need")) {
										result.add(need_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}else if(source.equals("dist")) {
										result.add(dist_svn_url + (path.equals("") ? entry.getName() : (path + "/" + entry.getName())));
									}
								}
							}
						}
					} catch (Exception e) {
						LOG.info("-->维护类项目迁移线程【 获取目录下面的目录或者文件[" + path + "]失败，失败信息：" + e.getMessage() + " 】");
					}
				}
				
				@Override
				public String call() throws Exception 
				{
					 SVNClientManager dist_clientManager = null;//目标库
					 SVNRepository    dist_repository = null;
					 ISVNAuthenticationManager dist_authManager = null;
					 try {
						 //目标库
						 DAVRepositoryFactory.setup();
					     ISVNOptions options = SVNWCUtil.createDefaultOptions(true);
					     dist_clientManager = SVNClientManager.newInstance((DefaultSVNOptions)options,dist_svn_username,dist_svn_password);
					     //low level api
					     dist_repository = SVNRepositoryFactory.create(SVNURL.parseURIEncoded(dist_svn_url));
						 dist_authManager = SVNWCUtil.createDefaultAuthenticationManager(dist_svn_username, dist_svn_password);
						 dist_repository.setAuthenticationManager(dist_authManager);
					 } catch (Exception e) {
						LOG.info("-->TC文档维护类迁移线程【 svn服务器连接失败，失败信息：" + e.getMessage() + " 】");
						return "error";
					 }
					
					try {
						/**
						 * PJ-LX-2018-002-001-人工智能平台运营
						 *     RQN-2018-IT039-0039
						 *     RQN-2018-IT039-0040
						 */
						//迁移逻辑
						Set<String> project_keys = all_project_needMove.keySet();//待迁移项目的集合
						Set<Map<String, String>> temp_map_list = new HashSet<Map<String,String>>();//每个项目对应的RQN集合
						SVNCommitClient dist_commitClient = dist_clientManager.getCommitClient();//目标库提交文档、创建目录客户端 
						
						for(String project_key:project_keys) 
						{
							if(!project_key.contains("WH")) {
								LOG.info("-->TC文档维护类迁移线程【 抱歉，非WH维护项目，本线程不负责迁移 】");
								continue;
							}
							
						}
					} catch (Exception e) {
						LOG.info("-->TC文档维护类迁移线程【 线程发生错误，错误信息：" + e.getMessage()  + " 】返回守护线程error信息");
						return "error";
					}
					return "success";
				}
			});
			
			//收尾线程
			pool.submit(new Callable<String>()
			{
				@Override
				public String call() throws Exception 
				{
					String message_lx = lx_thread_result.get();
					String message_wh = wh_thread_result.get();
					if(message_lx.equals("success") && message_wh.equals("success")) {
						LOG.info("-->【 立项类、维护类迁移成功 】");
					}else if(message_lx.equals("error")){
						LOG.info("-->【 立项类迁移失败 】");
					}else if(message_wh.equals("error")) {
						LOG.info("-->【 维护类迁移失败 】");
					}else {
						LOG.info("-->【 TC文档迁移失败 】");
					}
					/**
					 * 勾选项目重新设置数据库迁移状态和迁移时间
					 */
					pool.shutdown();//关闭线程池
					LOG.info("-->【 线程池关闭 欢迎下次再次进行迁移 】");
					return "success";
				}
			});
		}catch (Exception e) {
			LOG.info("-->【 svn文档迁移接口发生异常，异常信息：" + e.getMessage() + " 】");
			result.put("status","error");
			result.put("message","服务器繁忙 请连续管理员");
			return result;
		}
		result.put("status","success");
		//待迁移 迁移中 迁移完成 迁移失败
		result.put("message","迁移任务提交成功 后续可以刷新本页面进行迁移进度查询");
		return result;
	}
}
