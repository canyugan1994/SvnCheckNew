package com.canyugan;

import java.util.Set;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class testredis_map 
{
	public static void main(String[] args) {
		String redis_check_map = "{\"PJ-LX-2019-001-云上卡中心项目\":{\"checkTable\":[{\"exist\":0,\"classify\":\"项目管理文档\",\"directoryStruct\":\"立项采购合同\",\"notes\":\"如涉及采购，需提交，反之，可以不提交\",\"cutGuide\":0,\"templateName\":\"《技术立项申请单》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《立项申请报告》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《立项采购合同》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"directoryStruct\":\"启动\",\"cutGuide\":0,\"templateName\":\"《启动PPT》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"启动会议纪要\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"项目计划\",\"cutGuide\":1,\"templateName\":\"《项目计划书》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《项目进度计划.mpp》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《项目里程碑》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"配置管理\",\"cutGuide\":0,\"templateName\":\"《项目配置管理计划》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"风险管理\",\"cutGuide\":0,\"templateName\":\"《风险列表及跟踪记录》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"问题管理\",\"cutGuide\":0,\"templateName\":\"《项目问题日志》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"培训管理\",\"cutGuide\":0,\"templateName\":\"《项目成员技能识别》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《培训记录表》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"资源申请\",\"cutGuide\":0,\"templateName\":\"《项目人力成本评估报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《资源申请表》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"项目状态报告\",\"cutGuide\":1,\"templateName\":\"《项目周报》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《项目月报》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《里程碑报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《项目度量》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《质量保证》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"会议纪要\",\"cutGuide\":0,\"templateName\":\"《会议纪要》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"组织架构及通讯录\",\"cutGuide\":0,\"templateName\":\"《通讯录》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"项目结项\",\"notes\":\"涉及到采购合同未付款\",\"cutGuide\":0,\"templateName\":\"《项目验收申请表》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《项目结项申请表》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《项目总结报告》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"classify\":\"项目过程文档\",\"directoryStruct\":\"项目预研（POC）\",\"cutGuide\":0,\"templateName\":\"《项目预研报告V》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"需求\",\"cutGuide\":1,\"templateName\":\"《业务需求书》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《业务需求评审报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《需求变更管理表》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"TF\",\"cutGuide\":1,\"templateName\":\"《TF》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《TF评审报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"设计\",\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《概要设计》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《详细设计》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《数据库设计》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《设计评审报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"开发\",\"cutGuide\":0,\"templateName\":\"《代码评审报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《单元测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《软件版本说明》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《用户操作手册》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"测试\",\"cutGuide\":0,\"templateName\":\"《SIT测试计划》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《SIT测试案例》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《SIT测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"notes\":\"TC平台管理\",\"cutGuide\":1,\"templateName\":\"《UAT测试计划》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"notes\":\"TC平台管理\",\"cutGuide\":1,\"templateName\":\"《UAT测试案例》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":1,\"templateName\":\"《UAT测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《性能测试计划》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《性能测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《压力测试案例》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《压力测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《安全测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《测试评审报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"投产\",\"notes\":\"双速平台管理\",\"cutGuide\":1,\"templateName\":\"《版本发布申请表》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"notes\":\"双速平台管理\",\"cutGuide\":1,\"templateName\":\"《变更切换步骤》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《数据库规格说明书》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《变更影响分析》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《重大模块、功能、批处理等补充说明》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"运维\",\"notes\":\"双速平台管理\",\"cutGuide\":1,\"templateName\":\"《应用变更申请表》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《系统运维手册》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《系统应急手册》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《系统安装手册》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0}],\"projectname\":\"云上卡中心项目\",\"is_done\":0,\"isPurchase\":\"是\",\"projectClass\":\"研发类\",\"isNewSystem\":\"是\"},\"PJ-LX-2019-002-运营业务监控项目\":{\"checkTable\":[{\"exist\":0,\"classify\":\"项目管理文档\",\"directoryStruct\":\"立项采购合同\",\"notes\":\"如涉及采购，需提交，反之，可以不提交\",\"cutGuide\":0,\"templateName\":\"《技术立项申请单》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《立项申请报告》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《立项采购合同》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"directoryStruct\":\"启动\",\"cutGuide\":0,\"templateName\":\"《启动PPT》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"启动会议纪要\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"项目计划\",\"cutGuide\":1,\"templateName\":\"《项目计划书》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《项目进度计划.mpp》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《项目里程碑》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"配置管理\",\"cutGuide\":0,\"templateName\":\"《项目配置管理计划》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"风险管理\",\"cutGuide\":0,\"templateName\":\"《风险列表及跟踪记录》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"问题管理\",\"cutGuide\":0,\"templateName\":\"《项目问题日志》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"培训管理\",\"cutGuide\":0,\"templateName\":\"《项目成员技能识别》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《培训记录表》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"资源申请\",\"cutGuide\":0,\"templateName\":\"《项目人力成本评估报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《资源申请表》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"项目状态报告\",\"cutGuide\":1,\"templateName\":\"《项目周报》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《项目月报》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《里程碑报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《项目度量》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《质量保证》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"会议纪要\",\"cutGuide\":0,\"templateName\":\"《会议纪要》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"组织架构及通讯录\",\"cutGuide\":0,\"templateName\":\"《通讯录》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"项目结项\",\"notes\":\"涉及到采购合同未付款\",\"cutGuide\":0,\"templateName\":\"《项目验收申请表》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《项目结项申请表》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《项目总结报告》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"classify\":\"项目过程文档\",\"directoryStruct\":\"项目预研（POC）\",\"cutGuide\":0,\"templateName\":\"《项目预研报告V》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"需求\",\"cutGuide\":1,\"templateName\":\"《业务需求书》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《业务需求评审报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《需求变更管理表》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"TF\",\"cutGuide\":1,\"templateName\":\"《TF》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《TF评审报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"设计\",\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《概要设计》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《详细设计》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《数据库设计》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《设计评审报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"开发\",\"cutGuide\":0,\"templateName\":\"《代码评审报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《单元测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《软件版本说明》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《用户操作手册》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"测试\",\"cutGuide\":0,\"templateName\":\"《SIT测试计划》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《SIT测试案例》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《SIT测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"notes\":\"TC平台管理\",\"cutGuide\":1,\"templateName\":\"《UAT测试计划》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"notes\":\"TC平台管理\",\"cutGuide\":1,\"templateName\":\"《UAT测试案例》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":1,\"templateName\":\"《UAT测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《性能测试计划》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《性能测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《压力测试案例》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《压力测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《安全测试报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《测试评审报告》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"投产\",\"notes\":\"双速平台管理\",\"cutGuide\":1,\"templateName\":\"《版本发布申请表》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"notes\":\"双速平台管理\",\"cutGuide\":1,\"templateName\":\"《变更切换步骤》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《数据库规格说明书》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《变更影响分析》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"cutGuide\":0,\"templateName\":\"《重大模块、功能、批处理等补充说明》\",\"cellStyleMap\":{},\"cutResult\":0,\"done\":0},{\"exist\":0,\"directoryStruct\":\"运维\",\"notes\":\"双速平台管理\",\"cutGuide\":1,\"templateName\":\"《应用变更申请表》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《系统运维手册》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《系统应急手册》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0},{\"exist\":0,\"notes\":\"涉及新系统要求有\",\"cutGuide\":0,\"templateName\":\"《系统安装手册》\",\"cellStyleMap\":{},\"cutResult\":2,\"done\":0}],\"projectname\":\"运营业务监控项目\",\"is_done\":0,\"isPurchase\":\"是\",\"projectClass\":\"研发类\",\"isNewSystem\":\"是\"}}";
		JSONObject final_check_map = JSONObject.parseObject(redis_check_map);
		Set<String> keySet  = final_check_map.keySet();
		for(String key:keySet) {
			JSONArray checkTable = final_check_map.getJSONObject(key).getJSONArray("checkTable");
			int length = checkTable.size();
			for(int index = 0;index < length;index++) {
				JSONObject temp = checkTable.getJSONObject(index);
				if(temp.getString("templateName").equals("《应用变更申请表》") && key.contains("云上卡中")) {
					temp.replace("exist", 1);
					temp.replace("done", 1);
					checkTable.set(index, temp);
				}
				if(temp.getString("templateName").equals("《变更影响分析》") && !key.contains("云上卡中")) {
					temp.replace("exist", 1);
					temp.replace("done", 1);
					checkTable.set(index, temp);
				}
			}
			System.out.println(JSONObject.toJSON(final_check_map));
		}
	}
}