package com.canyugan.controller;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.metadata.Table;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.pojo.CalculationModel;
import com.canyugan.pojo.MaintainModel;
import com.canyugan.pojo.developWriterModel;
import com.canyugan.service.SXProjectInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 审计报表下载
 * @author cuilongyun
 *
 */
@Api(value = "审计报表下载", tags = { "审计报表下载" })
@RestController
public class ReportController 
{
	private static final Logger LOG = LoggerFactory.getLogger(ReportController.class);
	@Autowired
	private SXProjectInfoService sxProjectInfoService;
	@Autowired
	private JedisPool JedisPool;//redis连接池

	@ApiModel(value = "DownloadReport", description = "下载审计报表")
	@Data
	static class DownloadReport {
		@ApiModelProperty(value = "用户id", name = "user_id", example = "\"A167347\"")
		private String user_id;
		@ApiModelProperty(value = "审计报表名称", name = "reportName", example = "\"检查报表20190627141548\"")
		private String reportName;
	}
	
	/**
	 * @author cuilongyun
	 * @param request
	 * @param response
	 * @return
	 * @throws IOException
	 */
	@ApiOperation(value="审计报告下载接口", notes="需要两个参数 reportName和user_id")
	@RequestMapping(value = "/svnCheck/downloadReport", produces = {"application/json;charset=utf8" })
	@ResponseBody
	public JSONObject checkoutReport(@RequestBody DownloadReport downloadReport, HttpServletResponse response) throws IOException 
	{
		// 存储接口返回结果
		JSONObject result = new JSONObject();
		// easyexcel映射模型
		try {
			JSONArray jsonArray = sxProjectInfoService.sendServiceRequest();
			List<CalculationModel> callist = new ArrayList<CalculationModel>();
			List<developWriterModel> checkList = new ArrayList<developWriterModel>();
			List<MaintainModel> checkList1 = new ArrayList<MaintainModel>();
			// 按项目组统计
			Map<String, Map<String, List<String>>> classMap = new HashMap<String, Map<String,List<String>>>();
			Map<String, List<String>> develop_teamMap = new HashMap<String, List<String>>();
			Map<String, List<String>> maintain_teamMap = new HashMap<String, List<String>>();
			Map<String, List<Double>> maintain_callist = new HashMap<String, List<Double>>();
			Map<String, List<Double>> develop_callist = new HashMap<String, List<Double>>();
			
			//遍历json数组 获取每一个项目都信息
			JSONObject ob1 = new JSONObject();
			for (int i = 0; i < jsonArray.size(); i++) {
				List<String> temp_list = new LinkedList<String>();
				ob1 = jsonArray.getJSONObject(i);
				String teamclass = ob1.getString("projectCatagory");// 项目类别
				String teamString = ob1.getString("pTeam");// 项目所属团队
				String projectnumber = ob1.getString("projectCodeNum") + "-" + ob1.getString("projectName");// 项目编号
				if (teamclass.contains("立项")) {
					// 立项类项目
					if (classMap.containsKey("LX")) {
						// 已经存在 追加值
						develop_teamMap = classMap.get("LX");
						if (develop_teamMap.containsKey(teamString)) {
							// 这个组已经有项目信息 追加
							temp_list = develop_teamMap.get(teamString);
							temp_list.add(projectnumber);
							develop_teamMap.put(teamString, temp_list);
						} else {
							// 这个组还没有项目信息 新建list
							temp_list.add(projectnumber);
							develop_teamMap.put(teamString, temp_list);
						}
						classMap.put("LX", develop_teamMap);
					} else {
						// 第一次添加
						temp_list.add(projectnumber);
						develop_teamMap.put(teamString, temp_list);
						classMap.put("LX", develop_teamMap);
					}
				} else if (teamclass.contains("维护")) {
					 //维护类项目
					if (classMap.containsKey("WH")) {
						
						maintain_teamMap = classMap.get("WH");
						if (maintain_teamMap.containsKey(teamString)) {
							temp_list = maintain_teamMap.get(teamString);
							temp_list.add(projectnumber);
							maintain_teamMap.put(teamString, temp_list);
						} else {
							temp_list.add(projectnumber);
							maintain_teamMap.put(teamString, temp_list);
						}
						classMap.put("WH", maintain_teamMap);
					}else {
						temp_list.add(projectnumber);
						maintain_teamMap.put(teamString, temp_list);
						classMap.put("WH", maintain_teamMap);
					}
				}
			}
			//LOG.info("-->双速项目接口数据封装：【 " + JSONObject.toJSON(classMap) +  " 】");
			/**
			 * 读取redis完整检查数据 生成审计报表
			 */
			Jedis jedis = this.JedisPool.getResource();
			String report_name = downloadReport.getReportName();//处理前：检查报表20190618092820
			String use_find_name = report_name.substring(4);    //处理后：20190618092820
			LOG.info("-->截取的时间戳：【 " + use_find_name +  " 】");
			String user_id = downloadReport.getUser_id();
			//根据user_id去redis查询对应检查数据 格式：20190618092820_A167347
			Set<String> check_keys = jedis.keys("*" + user_id + "*");
			String result_check_key = "";
			for(String key:check_keys) {
				if(key.contains(use_find_name)) {
					result_check_key = key;
					LOG.info("-->【 查找到的报表key: " + result_check_key + "】");
					break;
				}
			}
			if("".equals(result_check_key)) {
				//未查找到 
				LOG.info("-->【 未找到请求参数reportName:"+ report_name + ",user_id:" + user_id + "对应的报表 】");
				return null;
			}
			String check_value = jedis.get(result_check_key);
			//JSONObject result_json = JSONObject.parseObject(check_value);
			Map<String, Object> result_json = (Map<String, Object>) JSONObject.parse(check_value);
			LOG.info("-->【 开始解析redis的json数据 】");
			Set<Entry<String, Object>> keys = result_json.entrySet();// 所有数据

			int develop_num = 0;
			int maintain_num1 = 0;
			int develop_i = 0;
			int maintain_i = 0;
			double develop_must_num = 0;
			double develop_red_result = 0;
			double develop_option_num = 0;
			double develop_pop_result = 0;
			double maintain_must_num = 0;
			double maintain_red_result = 0;
			double maintain_option_num = 0;
			double maintain_pop_result = 0;
			double team1_red_num = 0;
			double team1_must_num = 0;
			double team1_pop_num = 0;
			double team1_option_num = 0;

			for (Entry<String, Object> entry : keys) 
			{   
				LOG.info("-->【 正在统计项目：" + entry.getKey()  + " 】");
				// keys 所有数据
				Object pronum_num = entry.getKey();// 项目编号
				Map<String, Object> value = (Map<String, Object>) entry.getValue();// 项目编号对应所有数据

				double must_num = 0;
				double red_result = 0;
				double option_num = 0;
				double pop_result = 0;
				
				String proclass_result = (String) value.get("projectClass");// 项目类别
				String proname_result = (String) value.get("projectname");// 项目名称
				Object check_result = value.get("checkTable");
				LOG.info("-->【 解析每个项目的信息 】");
				JSONArray checktable_single = JSONArray.parseArray(check_result.toString());
				LOG.info("-->【 解析每个项目的检查表 】");
				// 研发类检查
				if (proclass_result.equals("研发类")) {
					LOG.info("-->【 研发类 】");
					String probuy_result = (String) value.get("isPurchase");
					int length = checktable_single.size();
					developWriterModel developWriter = new developWriterModel();
					for (int i = 0; i < jsonArray.size(); i++) {
						JSONObject excel_object = jsonArray.getJSONObject(i);
						String excel_name = excel_object.getString("projectName");
						String excel_status = excel_object.getString("projectStatus");
						String excel_manager = excel_object.getString("projectHeader");
						String excel_team = excel_object.getString("pTeam");
						String excel_list = excel_object.getString("projectLists");
						String excel_pline = excel_object.getString("pProdLine");
						if (excel_name.contains(proname_result)) {
							developWriter.setProject_status(excel_status);
							developWriter.setProject_manager(excel_manager);
							developWriter.setProject_team(excel_team);
							developWriter.setProject_set(excel_list);
							developWriter.setProject_prodectLine(excel_pline);
						}
					}
					LOG.info("-->【 处理完 jsonArray 】");
					develop_i = develop_i + 1;
					developWriter.setProject_id(develop_i);
					for (int index = 0; index < length; index++) {
						JSONObject column_single = (JSONObject) checktable_single.get(index);
						developWriter.setProject_name(proname_result);
						developWriter.setProject_num((String) pronum_num);
						developWriter.setProject_class(proclass_result);
						developWriter.setIs_buy(probuy_result);

						if (column_single.getString("templateName").equals("《项目计划书》")) {
							developWriter.setProject_plan(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目周报》")) {
							developWriter.setProject_weekreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").contains("业务需求书")) {
							developWriter.setBusiness_needsbook(column_single.getInteger("exist"));
						} else if (column_single.getString("templateName").equals("《TF》")) {
							developWriter.setTf_report(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《UAT测试计划》")) {
							developWriter.setUat_testplan(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《UAT测试案例》")) {
							developWriter.setUat_testcase(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《UAT测试报告》")) {
							developWriter.setUat_testreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《技术立项申请单》")) {
							developWriter.setTecproject_applicationform(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《立项申请报告》")) {
							developWriter.setProject_applicationreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《立项采购合同》")) {
							developWriter.setProject_procurementcontract(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《概要设计》")) {
							developWriter.setSummary_design(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《数据库设计》")) {
							developWriter.setDatabase_design(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《系统运维手册》")) {
							developWriter.setSystem_maintenancemanual(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《系统应急手册》")) {
							developWriter.setSystem_emergencymanual(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《系统安装手册》")) {
							developWriter.setSystem_installationmanual(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《启动PPT》")) {
							developWriter.setStart_ppt(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《启动会议纪要》")) {
							developWriter.setStart_meetingminutes(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目进度计划.mpp》")) {
							developWriter.setProject_schedule(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目里程碑》")) {
							developWriter.setProject_milestone(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目配置管理计划》")) {
							developWriter.setProject_configurationplan(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《风险列表及跟踪记录》")) {
							developWriter.setRisklist_trackingrecord(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目问题日志》")) {
							developWriter.setProject_problemlog(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目成员技能识别》")) {
							developWriter.setProjectmemberskill_recognition(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《培训记录表》")) {
							developWriter.setTraining_record(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目月报》")) {
							developWriter.setProject_monthlyreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《里程碑报告》")) {
							developWriter.setMilestone_report(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《质量保证》")) {
							developWriter.setQuality_assurance(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《会议纪要》")) {
							developWriter.setMinutes_meeting(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《通讯录》")) {
							developWriter.setAddress_book(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目验收申请表》")) {
							developWriter.setProjectacceptance_applicationform(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目结项申请表》")) {
							developWriter.setProjectcompletion_applicationform(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目总结报告》")) {
							developWriter.setProject_summaryreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目预研报告》")) {
							developWriter.setProject_preresearchreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《详细设计》")) {
							developWriter.setDetailed_design(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《设计评审报告》")) {
							developWriter.setDesignreview_report(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《用户操作手册》")) {
							developWriter.setUseroperation_manual(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《性能测试计划》")) {
							developWriter.setPerformance_testplan(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《性能测试报告》")) {
							developWriter.setPerformance_testreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《压力测试案例》")) {
							//developWriter.setStress_testcase(column_single.getIntValue("exist"));
							developWriter.setStress_testcase(1);
						} else if (column_single.getString("templateName").equals("《压力测试报告》")) {
							developWriter.setStress_testreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《安全测试报告》")) {
							developWriter.setSafety_testreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《测试评审报告》")) {
							developWriter.setTest_reviewreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《数据库规格说明书》")) {
							developWriter.setDatabase_specification(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《变更影响分析》")) {
							developWriter.setChange_impactaanalysis(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《重大模块、功能、批处理等补充说明》")) {
							developWriter.setSupplementary_explanation(column_single.getIntValue("exist"));
						}
						if (column_single.getIntValue("cutResult") == 1) {
							red_result = red_result + 1;
						}
						if (column_single.getIntValue("exist") == 1 && column_single.getIntValue("cutResult") == 1) {
							must_num = must_num + 1;
						}
						if (column_single.getIntValue("cutResult") == 0) {
							pop_result = pop_result + 1;
						}
						if (column_single.getIntValue("exist") == 1 && column_single.getIntValue("cutResult") == 0) {
							option_num = option_num + 1;
						}
						developWriter.setCutResult(column_single.getIntValue("cutResult"));
					}
					LOG.info("-->【 处理完模板 】");
					developWriter.setMust_rate(new DecimalFormat("0.0").format(must_num / red_result * 100) + "%");
					developWriter.setSelected_rate(new DecimalFormat("0.0").format(option_num / pop_result * 100) + "%");

					checkList.add(developWriter);
					develop_num = develop_num + 1;
					develop_must_num = develop_must_num + must_num;
					develop_red_result = develop_red_result + red_result;
					develop_option_num = develop_option_num + option_num;
					develop_pop_result = develop_pop_result + pop_result;
					//按组别计算
					// {WH={运营组=[PJ-WH-2019-032-办公综合管理平台维护优化项目]}, 
					//LX={运营组=[PJ-LX-2019-001-云上卡中心项目], 支付组=[PJ-LX-2019-028-新一代eHR系统维护优化项目]}}
					List<Double> develop_cal = new ArrayList<Double>();
					String projectTeam = developWriter.getProject_team();
					if (develop_callist.containsKey(projectTeam)) {
						List<Double> temp_teamlist = develop_callist.get(projectTeam);
						team1_red_num = temp_teamlist.get(0) + red_result;
						team1_must_num = temp_teamlist.get(1) + must_num;
						team1_pop_num = temp_teamlist.get(2) + pop_result;
						team1_option_num = temp_teamlist.get(3) + option_num;
						develop_cal.add(team1_red_num);
						develop_cal.add(team1_must_num);
						develop_cal.add(team1_pop_num);
						develop_cal.add(team1_option_num);
						develop_callist.put(projectTeam, develop_cal);
					} else {
						team1_red_num = red_result;
						team1_must_num = must_num;
						team1_pop_num = pop_result;
						team1_option_num = option_num;
						develop_cal.add(team1_red_num);
						develop_cal.add(team1_must_num);
						develop_cal.add(team1_pop_num);
						develop_cal.add(team1_option_num);
						develop_callist.put(projectTeam, develop_cal);
					}
					LOG.info("-->【 if处理结束 】");
				} else if (proclass_result.equals("维护类")) {
					LOG.info("-->【 维护类 】");
					int length = checktable_single.size();
					MaintainModel maintainModel = new MaintainModel();
					for (int i = 0; i < jsonArray.size(); i++) {
						JSONObject excel_object = jsonArray.getJSONObject(i);
						String excel_name = excel_object.getString("projectName");
						String excel_status = excel_object.getString("projectStatus");
						String excel_manager = excel_object.getString("projectHeader");
						String excel_team = excel_object.getString("pTeam");
						String excel_list = excel_object.getString("projectLists");
						String excel_pline = excel_object.getString("pProdLine");
						if (excel_name.contains(proname_result)) {
							maintainModel.setProject_status(excel_status);
							maintainModel.setProject_manager(excel_manager);
							maintainModel.setProject_team(excel_team);
							maintainModel.setProject_set(excel_list);
							maintainModel.setProject_prodectLine(excel_pline);
						}
					}
					LOG.info("-->【 处理完 jsonArray 】");
					maintain_i = maintain_i + 1;
					maintainModel.setProject_id(maintain_i);
					for (int index = 0; index < length; index++) {
						JSONObject column_single = (JSONObject) checktable_single.get(index);
						maintainModel.setProject_name(proname_result);
						maintainModel.setProject_num((String) pronum_num);
						maintainModel.setProject_class(proclass_result);

						if (column_single.getString("templateName").equals("《业务需求书》")) {
							//maintainModel.setBusiness_needsbook(column_single.getIntValue("exist"));
							maintainModel.setBusiness_needsbook(1);
						} else if (column_single.getString("templateName").equals("《TF》")) {
							//maintainModel.setTf_report(column_single.getIntValue("exist"));
							maintainModel.setTf_report(1);
						} else if (column_single.getString("templateName").equals("《UAT测试案例》")) {
							maintainModel.setUat_testcase(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《UAT测试报告》")) {
							maintainModel.setUat_testreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《SIT测试案例》")) {
							maintainModel.setSit_testcase(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《SIT测试报告》")) {
							maintainModel.setSit_testreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《里程碑》")) {
							maintainModel.setMilestone(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目配置管理计划》")) {
							maintainModel.setProject_configurationplan(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《风险列表及跟踪记录》")) {
							maintainModel.setRisklist_trackingrecord(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目问题日志》")) {
							maintainModel.setProject_problemlog(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目成员技能识别》")) {
							maintainModel.setProjectmemberskill_recognition(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《培训记录表》")) {
							maintainModel.setTraining_record(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目周报》")) {
							maintainModel.setProject_weekreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《项目月报》")) {
							maintainModel.setProject_monthlyreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《里程碑报告》")) {
							maintainModel.setMilestone_report(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《质量保证》")) {
							maintainModel.setQuality_assurance(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《会议纪要》")) {
							maintainModel.setMinutes_meeting(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《通讯录》")) {
							maintainModel.setAddress_book(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《预研报告》")) {
							maintainModel.setPreresearch_report(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《概要设计》")) {
							maintainModel.setSummary_design(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《详细设计》")) {
							maintainModel.setDetailed_design(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《数据库设计》")) {
							maintainModel.setDatabase_design(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《设计评审报告》")) {
							maintainModel.setDesignreview_report(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《用户操作手册》")) {
							maintainModel.setUseroperation_manual(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《系统运维手册》")) {
							maintainModel.setSystem_maintenancemanual(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《系统应急手册》")) {
							maintainModel.setSystem_emergencymanual(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《系统安装手册》")) {
							maintainModel.setSystem_installationmanual(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《SIT测试计划》")) {
							maintainModel.setSit_testplan(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《UAT测试计划》")) {
							maintainModel.setUat_testplan(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《性能测试计划》")) {
							maintainModel.setPerformance_testplan(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《性能测试报告》")) {
							maintainModel.setPerformance_testreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《压力测试案例》")) {
							maintainModel.setStress_testcase(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《压力测试报告》")) {
							maintainModel.setStress_testreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《安全测试报告》")) {
							maintainModel.setSafety_testreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《测试评审报告》")) {
							maintainModel.setTest_reviewreport(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《数据库规格说明书》")) {
							maintainModel.setDatabase_specification(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《变更影响分析》")) {
							maintainModel.setChange_impactaanalysis(column_single.getIntValue("exist"));
						} else if (column_single.getString("templateName").equals("《重大模块、功能、批处理等补充说明》")) {
							maintainModel.setSupplementary_explanation(column_single.getIntValue("exist"));
						}

						if (column_single.getIntValue("cutResult") == 1) {
							red_result = red_result + 1;
						}
						if (column_single.getIntValue("exist") == 1 && column_single.getIntValue("cutResult") == 1) {
							must_num = must_num + 1;
						}
						if (column_single.getIntValue("cutResult") == 0) {
							pop_result = pop_result + 1;
						}
						if (column_single.getIntValue("exist") == 1 && column_single.getIntValue("cutResult") == 0) {
							option_num = option_num + 1;
						}
						maintainModel.setCutResult(column_single.getIntValue("cutResult"));
					}
					LOG.info("-->【 处理完 模板名 】");
					maintainModel.setMust_rate(new DecimalFormat("0.0").format(must_num / red_result * 100) + "%");
					maintainModel.setSelected_rate(new DecimalFormat("0.0").format(option_num / pop_result * 100) + "%");

					checkList1.add(maintainModel);
					// 维护类总数
					maintain_num1 = maintain_num1 + 1;
					// 必交率计算
					maintain_must_num = maintain_must_num + must_num;
					maintain_red_result = maintain_red_result + red_result;
					// 选交率计算
					maintain_option_num = maintain_option_num + option_num;
					maintain_pop_result = maintain_pop_result + pop_result;
					//按组别计算
					List<Double> maintain_cal = new ArrayList<Double>();
					String projectTeam = maintainModel.getProject_team();
					if (maintain_callist.containsKey(projectTeam)) {
						List<Double> temp_teamlist = maintain_callist.get(projectTeam);
						team1_red_num = temp_teamlist.get(0) + red_result;
						team1_must_num = temp_teamlist.get(1) + must_num;
						team1_pop_num = temp_teamlist.get(2) + pop_result;
						team1_option_num = temp_teamlist.get(3) + option_num;
						maintain_cal.add(team1_red_num);
						maintain_cal.add(team1_must_num);
						maintain_cal.add(team1_pop_num);
						maintain_cal.add(team1_option_num);
						maintain_callist.put(projectTeam, maintain_cal);
					} else {
						team1_red_num = red_result;
						team1_must_num = must_num;
						team1_pop_num = pop_result;
						team1_option_num = option_num;
						maintain_cal.add(team1_red_num);
						maintain_cal.add(team1_must_num);
						maintain_cal.add(team1_pop_num);
						maintain_cal.add(team1_option_num);
						maintain_callist.put(projectTeam, maintain_cal);
					}
				}
				LOG.info("-->【 阶段性处理完毕 】");
			}
			LOG.info("-->【 开始做整体处理 】");
			CalculationModel calculationModel1 = new CalculationModel();
			calculationModel1.setMetric_dimension("[L]交付成果物完整性（研发类项目） ");
			calculationModel1.setProject_num(develop_num);
			calculationModel1.setTarget_value("100%");
			calculationModel1.setMust_rate(new DecimalFormat("0.0").format(develop_must_num / develop_red_result * 100) + "%");
			calculationModel1.setOptional_rate(new DecimalFormat("0.0").format(develop_option_num / develop_pop_result * 100) + "%");
			
			LOG.info("-->【 处理完 calculationModel1 】");
			
			CalculationModel calculationModel_develop = new CalculationModel();
			for (String teamString : develop_callist.keySet()) {
				calculationModel_develop.setMetric_dimension("[L]" + teamString);
				calculationModel_develop.setProject_num(develop_teamMap.get(teamString).size());
				calculationModel_develop.setTarget_value("100%");
				if (develop_callist.get(teamString).get(0) != 0) {
					calculationModel_develop.setMust_rate(new DecimalFormat("0.0").format(develop_callist.get(teamString).get(1)/ develop_callist.get(teamString).get(0)* 100) + "%");
				}else {
					calculationModel_develop.setMust_rate(new DecimalFormat("0.0").format(0) + "%");
				}
				if (develop_callist.get(teamString).get(2) != 0) {
					calculationModel_develop.setOptional_rate(new DecimalFormat("0.0").format(develop_callist.get(teamString).get(3)/ develop_callist.get(teamString).get(2)* 100) + "%");
				}else {
					calculationModel_develop.setOptional_rate(new DecimalFormat("0.0").format(0) + "%");
				}
			}
			LOG.info("-->【 处理完 calculationModel_develop 】");
			CalculationModel calculationModel2 = new CalculationModel();
			calculationModel2.setMetric_dimension("[W]交付成果物完整性（维护类项目） ");
			calculationModel2.setProject_num(maintain_num1);
			calculationModel2.setTarget_value("100%");
			calculationModel2.setMust_rate(new DecimalFormat("0.0").format(maintain_must_num / maintain_red_result * 100) + "%");
			calculationModel2.setOptional_rate(new DecimalFormat("0.0").format(maintain_option_num / maintain_pop_result * 100) + "%");
			
			LOG.info("-->【 处理完 calculationModel2 】");
			
			CalculationModel calculationModel_maintain = new CalculationModel();
			for (String teamString : maintain_callist.keySet()) {
				calculationModel_maintain.setMetric_dimension("[L]" + teamString);
				calculationModel_maintain.setProject_num(maintain_teamMap.get(teamString).size());
				calculationModel_maintain.setTarget_value("100%");
				if (maintain_callist.get(teamString).get(0) != 0) {
					calculationModel_maintain.setMust_rate(new DecimalFormat("0.0").format(maintain_callist.get(teamString).get(1)/ maintain_callist.get(teamString).get(0)* 100) + "%");
				}else {
					calculationModel_maintain.setMust_rate(new DecimalFormat("0.0").format(0) + "%");
				}
				if (maintain_callist.get(teamString).get(2) != 0) {
					calculationModel_maintain.setOptional_rate(new DecimalFormat("0.0").format(maintain_callist.get(teamString).get(3)/ maintain_callist.get(teamString).get(2)* 100) + "%");
				}else {
					calculationModel_maintain.setOptional_rate(new DecimalFormat("0.0").format(0) + "%");
				}
			}
			LOG.info("-->【 处理完 calculationModel_maintain 】");
			callist.add(calculationModel1);
			callist.add(calculationModel_develop);
			callist.add(calculationModel2);
			callist.add(calculationModel_maintain);

			LOG.info("-->【 统计数据计算完成 开始写excel 】");
			//OutputStream out = new FileOutputStream("check.xlsx");

			response.setHeader("Content-Disposition", "attachment; filename=template.xlsx");
			response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
			ExcelWriter writer = new ExcelWriter(response.getOutputStream(), ExcelTypeEnum.XLSX, true);
			Sheet calculation_sheet = new Sheet(1, 1, CalculationModel.class);
			Sheet develop_sheet = new Sheet(2, 1, developWriterModel.class);
			Sheet maintain_sheet = new Sheet(3, 1, MaintainModel.class);
			calculation_sheet.setSheetName("总体情况");
			develop_sheet.setSheetName("研发类项目");
			maintain_sheet.setSheetName("维护类项目");

			writer.write(callist, calculation_sheet, new Table(0));
			writer.write(checkList, develop_sheet, new Table(1));
			writer.write(checkList1, maintain_sheet, new Table(2));
			writer.finish();
			LOG.info("-->【 审计报表：[" + report_name +  "]下载完成 】");
		} catch (Exception e) {
			LOG.info("-->【 审计报表下载出错 错误信息：" + e.getMessage()  + " 】");
		}
		return result;
	}
}
