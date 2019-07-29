package com.canyugan.controller;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.canyugan.pojo.CalculationModel;
import com.canyugan.pojo.MaintainModel;
import com.canyugan.pojo.developWriterModel;
import com.canyugan.service.SXProjectInfoService;
import com.canyugan.util.StyleExcelHandler;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
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
	public JSONObject checkoutReport(@RequestBody DownloadReport downloadReport, HttpServletResponse response)
			throws IOException 
	{
		// 存储接口返回结果
		JSONObject result = new JSONObject();
		// easyexcel映射模型
		try {
			JSONArray jsonArray = null;
			try {
				jsonArray = sxProjectInfoService.sendServiceRequest();
			} catch (Exception e) {
				LOG.info("双速接口发生异常，异常信息：" + e.getMessage());
				return null;
			}
			List<CalculationModel> callist = new ArrayList<CalculationModel>();
			List<developWriterModel> checkList = new ArrayList<developWriterModel>();
			List<MaintainModel> checkList1 = new ArrayList<MaintainModel>();
			//按项目组统计
			Map<String, List<Double>> maintain_callist = new LinkedHashMap<String, List<Double>>();
			Map<String, List<Double>> develop_callist = new LinkedHashMap<String, List<Double>>();
			//裁剪结果封装
			Map<String, Map<Object,Map<String, Object>>> structer = new LinkedHashMap<String, Map<Object,Map<String,Object>>>();
			Map<Object,Map<String, Object>> develop_sortResult = new LinkedHashMap<Object, Map<String,Object>>();
			Map<Object,Map<String, Object>> maintain_sortResult = new LinkedHashMap<Object, Map<String,Object>>();
			//遍历json数组 获取每一个项目都信息
			String[] totalTeam = {"基础平台开发组","移动互联应用开发组", "运营及业务开发组","支付应用开发组", "主机系统组","大数据组", "数据仓库组", "数据分析组", "云平台组", "质量管理组", "信息安全及综合管理组", "技术维护组", "测试组"};
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
//				String report_name = request.getParameter("reportName");// 全量检查报表20190618092820
//				String user_id = request.getParameter("user_id");
//				String key = "20190712135850_iiii";
//
//				Jedis jedis = JedisPool.getResource();
//
//				String check_value = jedis.get(key);
//				LOG.info("-->check_value from redis:【 " + check_value + " 】");
//				JSONObject result_json = JSONObject.parseObject(check_value);
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
		int deve_i = 0;
		int main_i = 0;
		double[][] develop_dataTeam = new double[13][4];
		double[][] maintain_dataTeam = new double[13][4];
		int[] develop_sumnum = new int[13];
		int[] maintain_sumnum = new int[13];
		int teamNum = 0;

		Set<String> develop_temple_list = new HashSet<String>();
		Set<String> maintain_temple_list = new HashSet<String>();

		for (Entry<String, Object> entry : keys) {
			Map<String, Object> maintain_templeResult = new LinkedHashMap<String, Object>();
			Map<String, Object> develop_templeResult = new LinkedHashMap<String, Object>();
			/// 每一个项目
			// keys 所有数据
			Object pronum_num = entry.getKey();// 项目编号
			System.out.println("number" + pronum_num);
			LOG.info("-->【 key：" + pronum_num + "  】");
			Map<String, Object> value = (Map<String, Object>) entry.getValue();// 项目编号对应所有数据

			double must_num = 0;
			double red_result = 0;
			double option_num = 0;
			double pop_result = 0;

			String proclass_result = (String) value.get("projectClass");// 项目类别
			if ("研发类".equals(proclass_result)) {
				deve_i = deve_i + 1;
				structer.put("研发类", develop_sortResult);
				develop_sortResult.put(deve_i, develop_templeResult);
			}
			if ("维护类".equals(proclass_result)) {
				main_i = main_i + 1;
				structer.put("维护类", maintain_sortResult);
				maintain_sortResult.put(main_i, maintain_templeResult);
			}

			String proname_result = (String) value.get("projectname");
			String project_numberString = (String)value.get("projectnum");// 项目名称
			Object check_result = value.get("checkTable");
			JSONArray checktable_single = JSONArray.parseArray(check_result.toString());

			// 研发类检查

			if (proclass_result.equals("研发类")) {
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
				develop_i = develop_i + 1;
				developWriter.setProject_id(develop_i);
				for (int index = 0; index < length; index++) {
					JSONObject column_single = (JSONObject) checktable_single.get(index);
					String temp_checkString = column_single.getString("templateName");
//				if(!temp_checkString.startsWith("《") && !temp_checkString.endsWith("》")) {
//					result.put("error", "模板名称不规范，请检查");
//					return result;
//				}
					developWriter.setProject_name(proname_result);
					developWriter.setProject_num(project_numberString);
					developWriter.setProject_class(proclass_result);
					developWriter.setIs_buy(probuy_result);

					if (column_single.getString("templateName").equals("《项目计划书》")) {
						developWriter.setProject_plan(column_single.getIntValue("exist"));
						develop_templeResult.put("10", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目周报》")) {
						developWriter.setProject_weekreport(column_single.getIntValue("exist"));
						develop_templeResult.put("11", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").contains("《业务需求书》")) {
						developWriter.setBusiness_needsbook(column_single.getInteger("exist"));
						develop_templeResult.put("12", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《TF》")) {
						developWriter.setTf_report(column_single.getIntValue("exist"));
						develop_templeResult.put("13", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《UAT测试计划》")) {
						developWriter.setUat_testplan(column_single.getIntValue("exist"));
						develop_templeResult.put("14", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《UAT测试案例》")) {
						developWriter.setUat_testcase(column_single.getIntValue("exist"));
						develop_templeResult.put("15", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《UAT测试报告》")) {
						developWriter.setUat_testreport(column_single.getIntValue("exist"));
						develop_templeResult.put("16", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《版本发布申请表》")) {
						developWriter.setVersion_application(column_single.getIntValue("exist"));
						develop_templeResult.put("17", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《变更切换步骤》")) {
						developWriter.setChange_step(column_single.getIntValue("exist"));
						develop_templeResult.put("18", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《应用变更申请表》")) {
						developWriter.setApplication_Request(column_single.getIntValue("exist"));
						develop_templeResult.put("19", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《技术立项申请单》")) {
						developWriter.setTecproject_applicationform(column_single.getIntValue("exist"));
						develop_templeResult.put("20", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《立项申请报告》")) {
						developWriter.setProject_applicationreport(column_single.getIntValue("exist"));
						develop_templeResult.put("21", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《立项采购合同》")) {
						developWriter.setProject_procurementcontract(column_single.getIntValue("exist"));
						develop_templeResult.put("22", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《概要设计》")) {
						developWriter.setSummary_design(column_single.getIntValue("exist"));
						develop_templeResult.put("23", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《数据库设计》")) {
						developWriter.setDatabase_design(column_single.getIntValue("exist"));
						develop_templeResult.put("24", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目总结报告》")) {
						developWriter.setProject_summary(column_single.getIntValue("exist"));
						develop_templeResult.put("25", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《系统运维手册》")) {
						developWriter.setSystem_maintenancemanual(column_single.getIntValue("exist"));
						develop_templeResult.put("26", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《系统应急手册》")) {
						developWriter.setSystem_emergencymanual(column_single.getIntValue("exist"));
						develop_templeResult.put("27", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《系统安装手册》")) {
						developWriter.setSystem_installationmanual(column_single.getIntValue("exist"));
						develop_templeResult.put("28", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《启动PPT》")) {
						developWriter.setStart_ppt(column_single.getIntValue("exist"));
						develop_templeResult.put("29", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《启动会议纪要》")) {
						developWriter.setStart_meetingminutes(column_single.getIntValue("exist"));
						develop_templeResult.put("30", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目进度计划》")) {
						developWriter.setProject_schedule(column_single.getIntValue("exist"));
						develop_templeResult.put("31", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目里程碑》")) {
						developWriter.setProject_milestone(column_single.getIntValue("exist"));
						develop_templeResult.put("32", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目配置管理计划》")) {
						developWriter.setProject_configurationplan(column_single.getIntValue("exist"));
						develop_templeResult.put("33", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《风险列表及跟踪记录》")) {
						developWriter.setRisklist_trackingrecord(column_single.getIntValue("exist"));
						develop_templeResult.put("34", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目问题日志》")) {
						developWriter.setProject_problemlog(column_single.getIntValue("exist"));
						develop_templeResult.put("35", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目成员技能识别》")) {
						developWriter.setProjectmemberskill_recognition(column_single.getIntValue("exist"));
						develop_templeResult.put("36", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《培训记录表》")) {
						developWriter.setTraining_record(column_single.getIntValue("exist"));
						develop_templeResult.put("37", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目月报》")) {
						developWriter.setProject_monthlyreport(column_single.getIntValue("exist"));
						develop_templeResult.put("38", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《里程碑报告》")) {
						developWriter.setMilestone_report(column_single.getIntValue("exist"));
						develop_templeResult.put("39", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《质量保证》")) {
						developWriter.setQuality_assurance(column_single.getIntValue("exist"));
						develop_templeResult.put("40", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《会议纪要》")) {
						developWriter.setMinutes_meeting(column_single.getIntValue("exist"));
						develop_templeResult.put("41", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《通讯录》")) {
						developWriter.setAddress_book(column_single.getIntValue("exist"));
						develop_templeResult.put("42", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目验收申请表》")) {
						developWriter.setProjectacceptance_applicationform(column_single.getIntValue("exist"));
						develop_templeResult.put("43", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目结项申请表》")) {
						developWriter.setProjectcompletion_applicationform(column_single.getIntValue("exist"));
						develop_templeResult.put("44", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目预研报告》")) {
						developWriter.setProjectpreresearch_report(column_single.getIntValue("exist"));
						develop_templeResult.put("45", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《详细设计》")) {
						developWriter.setDetailed_design(column_single.getIntValue("exist"));
						develop_templeResult.put("46", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《设计评审报告》")) {
						developWriter.setDesignreview_report(column_single.getIntValue("exist"));
						develop_templeResult.put("47", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《用户操作手册》")) {
						developWriter.setUseroperation_manual(column_single.getIntValue("exist"));
						develop_templeResult.put("48", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《SIT测试计划》")) {
						developWriter.setSit_testplan(column_single.getIntValue("exist"));
						develop_templeResult.put("49", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《SIT测试案例》")) {
						developWriter.setSit_testcase(column_single.getIntValue("exist"));
						develop_templeResult.put("50", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《SIT测试报告》")) {
						developWriter.setSit_testreport(column_single.getIntValue("exist"));
						develop_templeResult.put("51", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《性能测试计划》")) {
						developWriter.setPerformance_testplan(column_single.getIntValue("exist"));
						develop_templeResult.put("52", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《性能测试报告》")) {
						developWriter.setPerformance_testreport(column_single.getIntValue("exist"));
						develop_templeResult.put("53", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《压力测试案例》")) {
						developWriter.setStress_testcase(column_single.getIntValue("exist"));
						develop_templeResult.put("54", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《压力测试报告》")) {
						developWriter.setStress_testreport(column_single.getIntValue("exist"));
						develop_templeResult.put("55", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《安全测试报告》")) {
						developWriter.setSafety_testreport(column_single.getIntValue("exist"));
						develop_templeResult.put("56", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《测试评审报告》")) {
						developWriter.setTest_reviewreport(column_single.getIntValue("exist"));
						develop_templeResult.put("57", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《数据库规格说明书》")) {
						developWriter.setDatabase_specification(column_single.getIntValue("exist"));
						develop_templeResult.put("58", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《变更影响分析》")) {
						developWriter.setChange_impactaanalysis(column_single.getIntValue("exist"));
						develop_templeResult.put("59", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《重大模块、功能、批处理等补充说明》")) {
						developWriter.setSupplementary_explanation(column_single.getIntValue("exist"));
						develop_templeResult.put("60", column_single.getIntValue("cutResult"));
						develop_temple_list.add(column_single.getString("templateName"));
					}

					if (develop_temple_list.contains(column_single.getString("templateName"))) {

						if (column_single.getIntValue("cutResult") == 2) {
							red_result = red_result + 1;
						}
						if (column_single.getIntValue("exist") == 1
								&& column_single.getIntValue("cutResult") == 2) {
							must_num = must_num + 1;
						}
					}
					if (develop_temple_list.contains(column_single.getString("templateName"))) {
						if (column_single.getIntValue("cutResult") == 0) {
							pop_result = pop_result + 1;
						}
						if (column_single.getIntValue("exist") == 1
								&& column_single.getIntValue("cutResult") == 0) {
							option_num = option_num + 1;
						}
					}

					developWriter.setCutResult(column_single.getIntValue("cutResult"));
				}
				
				developWriter.setMust_target((int)red_result);
				developWriter.setMust_now((int)must_num);
				developWriter.setOption_target((int)pop_result);
				developWriter.setOption_now((int)option_num);

				if (red_result == 0) {
					developWriter.setMust_rate("0.0%");
				} else {
					developWriter.setMust_rate(new DecimalFormat("0.0").format(must_num * 100 / red_result) + "%");
				}
				if (pop_result == 0) {
					developWriter.setSelected_rate("0.0%");
				} else {
					developWriter
							.setSelected_rate(new DecimalFormat("0.0").format(option_num * 100 / pop_result) + "%");
				}

				checkList.add(developWriter);

				develop_num = develop_num + 1;
				develop_must_num = develop_must_num + must_num;
				develop_red_result = develop_red_result + red_result;
				develop_option_num = develop_option_num + option_num;
				develop_pop_result = develop_pop_result + pop_result;
				// 按组别计算
				String projectTeam = developWriter.getProject_team();
				// String[] totalTeam = {"基础平台开发组","移动互联应用开发组", "运营及业务开发组","支付应用开发组",
				// "主机系统组","大数据组", "数据仓库组", "数据分析组", "云平台组", "质量管理组", "信息安全及综合管理组", "技术维护组",
				// "测试组"};
				for (int i = 0; i < totalTeam.length; i++) {
					if (totalTeam[i].equals(projectTeam)) {
						teamNum = i;
						break;
					}
				}
				develop_dataTeam[teamNum][0] += red_result;
				develop_dataTeam[teamNum][1] += must_num;
				develop_dataTeam[teamNum][2] += pop_result;
				develop_dataTeam[teamNum][3] += option_num;
				develop_sumnum[teamNum] += 1;

			} else if (proclass_result.equals("维护类")) {
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
				maintain_i = maintain_i + 1;
				maintainModel.setProject_id(maintain_i);
				for (int index = 0; index < length; index++) {
					JSONObject column_single = (JSONObject) checktable_single.get(index);
					String temp_checkString = column_single.getString("templateName");
//				if(!temp_checkString.startsWith("《") && !temp_checkString.endsWith("》")) {
//					result.put("error", "模板名称不规范，请检查");
//					return result;
//				}						
					maintainModel.setProject_name(proname_result);
					maintainModel.setProject_num(project_numberString);
					maintainModel.setProject_class(proclass_result);

					if (column_single.getString("templateName").equals("《进度计划》")) {
						maintainModel.setSchedule(column_single.getIntValue("exist"));
						maintain_templeResult.put("9", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目人力成本评估报告》")) {
						maintainModel.setProject_laborreport(column_single.getIntValue("exist"));
						maintain_templeResult.put("10", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《业务需求书》")) {
						maintainModel.setBusiness_needsbook(column_single.getIntValue("exist"));
						maintain_templeResult.put("11", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《TF》")) {
						maintainModel.setTf_report(column_single.getIntValue("exist"));
						maintain_templeResult.put("12", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《UAT测试案例》")) {
						maintainModel.setUat_testcase(column_single.getIntValue("exist"));
						maintain_templeResult.put("13", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《UAT测试报告》")) {
						maintainModel.setUat_testreport(column_single.getIntValue("exist"));
						maintain_templeResult.put("14", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《版本发布申请表》")) {
						maintainModel.setVersion_application(column_single.getIntValue("exist"));
						maintain_templeResult.put("15", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《变更切换步骤》")) {
						maintainModel.setChange_step(column_single.getIntValue("exist"));
						maintain_templeResult.put("16", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《应用变更申请表》")) {
						maintainModel.setApplication_Request(column_single.getIntValue("exist"));
						maintain_templeResult.put("17", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《里程碑》")) {
						maintainModel.setMilestone(column_single.getIntValue("exist"));
						maintain_templeResult.put("18", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目配置管理计划》")) {
						maintainModel.setProject_configurationplan(column_single.getIntValue("exist"));
						maintain_templeResult.put("19", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《风险列表及跟踪记录》")) {
						maintainModel.setRisklist_trackingrecord(column_single.getIntValue("exist"));
						maintain_templeResult.put("20", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目问题日志》")) {
						maintainModel.setProject_problemlog(column_single.getIntValue("exist"));
						maintain_templeResult.put("21", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目成员技能识别》")) {
						maintainModel.setProjectmemberskill_recognition(column_single.getIntValue("exist"));
						maintain_templeResult.put("22", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《培训记录表》")) {
						maintainModel.setTraining_record(column_single.getIntValue("exist"));
						maintain_templeResult.put("23", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目周报》")) {
						maintainModel.setProject_weekreport(column_single.getIntValue("exist"));
						maintain_templeResult.put("24", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《项目月报》")) {
						maintainModel.setProject_monthlyreport(column_single.getIntValue("exist"));
						maintain_templeResult.put("25", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《里程碑报告》")) {
						maintainModel.setMilestone_report(column_single.getIntValue("exist"));
						maintain_templeResult.put("26", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《质量保证》")) {
						maintainModel.setQuality_assurance(column_single.getIntValue("exist"));
						maintain_templeResult.put("27", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《会议纪要》")) {
						maintainModel.setMinutes_meeting(column_single.getIntValue("exist"));
						maintain_templeResult.put("28", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《通讯录》")) {
						maintainModel.setAddress_book(column_single.getIntValue("exist"));
						maintain_templeResult.put("29", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《预研报告》")) {
						maintainModel.setPreresearch_report(column_single.getIntValue("exist"));
						maintain_templeResult.put("30", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《概要设计》")) {
						maintainModel.setSummary_design(column_single.getIntValue("exist"));
						maintain_templeResult.put("31", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《详细设计》")) {
						maintainModel.setDetailed_design(column_single.getIntValue("exist"));
						maintain_templeResult.put("32", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《数据库设计》")) {
						maintainModel.setDatabase_design(column_single.getIntValue("exist"));
						maintain_templeResult.put("33", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《设计评审报告》")) {
						maintainModel.setDesignreview_report(column_single.getIntValue("exist"));
						maintain_templeResult.put("34", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《用户操作手册》")) {
						maintainModel.setUseroperation_manual(column_single.getIntValue("exist"));
						maintain_templeResult.put("35", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《系统运维手册》")) {
						maintainModel.setSystem_maintenancemanual(column_single.getIntValue("exist"));
						maintain_templeResult.put("36", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《系统应急手册》")) {
						maintainModel.setSystem_emergencymanual(column_single.getIntValue("exist"));
						maintain_templeResult.put("37", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《系统安装手册》")) {
						maintainModel.setSystem_installationmanual(column_single.getIntValue("exist"));
						maintain_templeResult.put("38", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《SIT测试计划》")) {
						maintainModel.setSit_testplan(column_single.getIntValue("exist"));
						maintain_templeResult.put("39", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《SIT测试案例》")) {
						maintainModel.setSit_testcase(column_single.getIntValue("exist"));
						maintain_templeResult.put("40", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《SIT测试报告》")) {
						maintainModel.setSit_testreport(column_single.getIntValue("exist"));
						maintain_templeResult.put("41", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《UAT测试计划》")) {
						maintainModel.setUat_testplan(column_single.getIntValue("exist"));
						maintain_templeResult.put("42", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《性能测试计划》")) {
						maintainModel.setPerformance_testplan(column_single.getIntValue("exist"));
						maintain_templeResult.put("43", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《性能测试报告》")) {
						maintainModel.setPerformance_testreport(column_single.getIntValue("exist"));
						maintain_templeResult.put("44", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《压力测试案例》")) {
						maintainModel.setStress_testcase(column_single.getIntValue("exist"));
						maintain_templeResult.put("45", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《压力测试报告》")) {
						maintainModel.setStress_testreport(column_single.getIntValue("exist"));
						maintain_templeResult.put("46", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《安全测试报告》")) {
						maintainModel.setSafety_testreport(column_single.getIntValue("exist"));
						maintain_templeResult.put("47", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《测试评审报告》")) {
						maintainModel.setTest_reviewreport(column_single.getIntValue("exist"));
						maintain_templeResult.put("48", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《数据库规格说明书》")) {
						maintainModel.setDatabase_specification(column_single.getIntValue("exist"));
						maintain_templeResult.put("49", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《变更影响分析》")) {
						maintainModel.setChange_impactaanalysis(column_single.getIntValue("exist"));
						maintain_templeResult.put("50", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					} else if (column_single.getString("templateName").equals("《重大模块、功能、批处理等补充说明》")) {
						maintainModel.setSupplementary_explanation(column_single.getIntValue("exist"));
						maintain_templeResult.put("51", column_single.getIntValue("cutResult"));
						maintain_temple_list.add(column_single.getString("templateName"));
					}
					if (maintain_temple_list.contains(column_single.getString("templateName"))) {

						if (column_single.getIntValue("cutResult") == 2) {
							red_result = red_result + 1;
						}
						if (column_single.getIntValue("exist") == 1
								&& column_single.getIntValue("cutResult") == 2) {
							must_num = must_num + 1;
						}
					}
					if (maintain_temple_list.contains(column_single.getString("templateName"))) {
						if (column_single.getIntValue("cutResult") == 0) {
							pop_result = pop_result + 1;
						}
						if (column_single.getIntValue("exist") == 1
								&& column_single.getIntValue("cutResult") == 0) {
							option_num = option_num + 1;
						}
					}
					maintainModel.setCutResult(column_single.getIntValue("cutResult"));
				}
				maintainModel.setMust_target((int)red_result);
				maintainModel.setMust_now((int)must_num);
				maintainModel.setOption_target((int)pop_result);
				maintainModel.setOption_now((int)option_num);
				if (red_result == 0) {
					maintainModel.setMust_rate("0.0%");
				} else {
					maintainModel.setMust_rate(new DecimalFormat("0.0").format(must_num * 100 / red_result) + "%");
				}
				if (pop_result == 0) {
					maintainModel.setSelected_rate("0.0%");
				} else {
					maintainModel
							.setSelected_rate(new DecimalFormat("0.0").format(option_num * 100 / pop_result) + "%");
				}
				checkList1.add(maintainModel);
				// 维护类总数
				maintain_num1 = maintain_num1 + 1;
				// 必交率计算
				maintain_must_num = maintain_must_num + must_num;
				maintain_red_result = maintain_red_result + red_result;
				// 选交率计算
				maintain_option_num = maintain_option_num + option_num;
				maintain_pop_result = maintain_pop_result + pop_result;
				// 按组别计算
				String projectTeam = maintainModel.getProject_team();
				for (int i = 0; i < totalTeam.length; i++) {
					if (totalTeam[i].equals(projectTeam)) {
						teamNum = i;
						break;
					}
				}
				maintain_dataTeam[teamNum][0] += red_result;
				maintain_dataTeam[teamNum][1] += must_num;
				maintain_dataTeam[teamNum][2] += pop_result;
				maintain_dataTeam[teamNum][3] += option_num;
				maintain_sumnum[teamNum] += 1;
			}
		}
		// lx
		if (develop_num != 0) {
			developWriterModel lx_note = new developWriterModel();
			lx_note.setProject_id("说明：\r\n" + "1. 红色背景——代表项目裁剪结果是必须提交的文档；\r\n" + "2. 蓝色背景——代表项目裁剪结果是选交的文档；\r\n"
					+ "3. 数字1——代表已提交文档；\r\n" + "4. 数字0——代表未提交文档；");
			checkList.add(lx_note);
		}
		if (maintain_num1 !=0 ) {
			MaintainModel wh_note = new MaintainModel();
			wh_note.setProject_id("说明：\r\n" + "1. 红色背景——代表项目裁剪结果是必须提交的文档；\r\n" + "2. 蓝色背景——代表项目裁剪结果是选交的文档；\r\n"
					+ "3. 数字1——代表已提交文档；\r\n" + "4. 数字0——代表未提交文档；");
			checkList1.add(wh_note);
		}

		for (int j = 0; j < 13; j++) {
			List<Double> develop_cal = new ArrayList<Double>();
			List<Double> maintain_cal = new ArrayList<Double>();
			develop_cal.add(develop_dataTeam[j][0]);
			develop_cal.add(develop_dataTeam[j][1]);
			develop_cal.add(develop_dataTeam[j][2]);
			develop_cal.add(develop_dataTeam[j][3]);
			develop_callist.put(totalTeam[j], develop_cal);
			maintain_cal.add(maintain_dataTeam[j][0]);
			maintain_cal.add(maintain_dataTeam[j][1]);
			maintain_cal.add(maintain_dataTeam[j][2]);
			maintain_cal.add(maintain_dataTeam[j][3]);
			maintain_callist.put(totalTeam[j], maintain_cal);
		}
//	System.out.println("develop_temple_list" + develop_temple_list + "size: " + develop_temple_list.size());
//	System.out.println("maintain_temple_list" + maintain_temple_list + "size: " + maintain_temple_list.size());

		CalculationModel calculationModel1 = new CalculationModel();
		calculationModel1.setMetric_dimension("[L]交付成果物完整性（研发类项目） ");
		calculationModel1.setProject_num(develop_num);
		calculationModel1.setTarget_value("100%");
		if (develop_red_result == 0) {
			calculationModel1.setMust_rate("0.0%");
		} else {
			calculationModel1.setMust_rate(
					new DecimalFormat("0.0").format(develop_must_num * 100 / develop_red_result) + "%");
		}

		if (develop_pop_result == 0) {
			calculationModel1.setOptional_rate("0.0%");
		} else {
			calculationModel1.setOptional_rate(
					new DecimalFormat("0.0").format(develop_option_num * 100 / develop_pop_result) + "%");
		}
		callist.add(calculationModel1);
		int j = -1;
		for (String teamString : develop_callist.keySet()) {
			j = j + 1;
			CalculationModel calculationModel_develop = new CalculationModel();
			calculationModel_develop.setMetric_dimension("[L]" + teamString);
			calculationModel_develop.setProject_num(develop_sumnum[j]);
			calculationModel_develop.setTarget_value("100%");
			if (develop_callist.get(teamString).get(0) != 0) {
				calculationModel_develop.setMust_rate(new DecimalFormat("0.0").format(
						develop_callist.get(teamString).get(1) * 100 / develop_callist.get(teamString).get(0))
						+ "%");
			} else {
				calculationModel_develop.setMust_rate(new DecimalFormat("0.0").format(0) + "%");
			}
			if (develop_callist.get(teamString).get(2) != 0) {
				calculationModel_develop.setOptional_rate(new DecimalFormat("0.0").format(
						develop_callist.get(teamString).get(3) * 100 / develop_callist.get(teamString).get(2))
						+ "%");
			} else {
				calculationModel_develop.setOptional_rate(new DecimalFormat("0.0").format(0) + "%");
			}
			callist.add(calculationModel_develop);
		}

		CalculationModel calculationModel2 = new CalculationModel();
		calculationModel2.setMetric_dimension("[W]交付成果物完整性（维护类项目） ");
		calculationModel2.setProject_num(maintain_num1);
		calculationModel2.setTarget_value("100%");
		if (maintain_red_result == 0) {
			calculationModel2.setMust_rate("0.0%");
		} else {
			calculationModel2.setMust_rate(
					new DecimalFormat("0.0").format(maintain_must_num * 100 / maintain_red_result) + "%");
		}
		if (maintain_pop_result == 0) {
			calculationModel2.setOptional_rate("0.0%");
		} else {
			calculationModel2.setOptional_rate(
					new DecimalFormat("0.0").format(maintain_option_num * 100 / maintain_pop_result) + "%");
		}
		callist.add(calculationModel2);

		j = -1;
		for (String teamString : maintain_callist.keySet()) {
			j = j + 1;
			CalculationModel calculationModel_maintain = new CalculationModel();
			calculationModel_maintain.setMetric_dimension("[W]" + teamString);
			calculationModel_maintain.setProject_num(maintain_sumnum[j]);
			calculationModel_maintain.setTarget_value("100%");
			if (maintain_callist.get(teamString).get(0) != 0) {
				calculationModel_maintain.setMust_rate(new DecimalFormat("0.0").format(
						maintain_callist.get(teamString).get(1) * 100 / maintain_callist.get(teamString).get(0))
						+ "%");
			} else {
				calculationModel_maintain.setMust_rate(new DecimalFormat("0.0").format(0) + "%");
			}
			if (maintain_callist.get(teamString).get(2) != 0) {
				calculationModel_maintain.setOptional_rate(new DecimalFormat("0.0").format(
						maintain_callist.get(teamString).get(3) * 100 / maintain_callist.get(teamString).get(2))
						+ "%");
			} else {
				calculationModel_maintain.setOptional_rate(new DecimalFormat("0.0").format(0) + "%");
			}
			callist.add(calculationModel_maintain);

		}
		LOG.info("-->【 开始写入 】");
		//LOG.info("structer" + structer);
		
        Sheet calculation_sheet = new Sheet(1, 1, CalculationModel.class);
		Sheet develop_sheet = new Sheet(2, 1, developWriterModel.class);
		Sheet maintain_sheet = new Sheet(3, 1, MaintainModel.class);
		calculation_sheet.setSheetName("审计总体情况");
		develop_sheet.setSheetName("研发类项目");
		maintain_sheet.setSheetName("维护类项目");

		StyleExcelHandler handler = new StyleExcelHandler(structer);
		//				InputStream in = new FileInputStream("check.xlsx");
		//OutputStream out = new FileOutputStream("check_result.xlsx");
		ExcelWriter writer = new ExcelWriter(null, response.getOutputStream(), ExcelTypeEnum.XLSX, true,handler);
		
		writer.write(callist, calculation_sheet);
		writer.write(checkList, develop_sheet);
		writer.write(checkList1, maintain_sheet);
		writer.finish();
		} catch (Exception e) {
			LOG.info("发生异常，异常信息：" + e.getMessage());
		}
		
		return result;
	}
}
