package com.canyugan.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

import lombok.Data;
import lombok.ToString;
@Data
@ToString

public class MaintainModel extends BaseRowModel{
	
	 @ExcelProperty(value = "序号" ,index = 0)
	    private Integer project_id;

	    @ExcelProperty(value = "项目名称",index = 1)
	    private String project_name;

	    @ExcelProperty(value = "项目编号",index = 2)
	    private String project_num;

	    @ExcelProperty(value = "项目类别",index = 3)
	    private String project_class;

	    @ExcelProperty(value = "项目状态",index = 4)
	    private String project_status;

	    @ExcelProperty(value = "项目负责人",index = 5)
	    private String project_manager;

	    @ExcelProperty(value = "项目所属团队",index = 6)
	    private String project_team;
	    
	    @ExcelProperty(value = "项目所属产品线" ,index = 7)
	    private String project_prodectLine;

	    @ExcelProperty(value = "所属项目集",index = 8)
	    private String project_set;
	    
	    @ExcelProperty(value = "业务需求书" ,index = 9)
	    private Integer business_needsbook;

	    @ExcelProperty(value = "TF",index = 10)
	    private Integer tf_report;
	    
	    @ExcelProperty(value = "UAT测试案例",index = 11)
	    private Integer uat_testcase;
	    
	    @ExcelProperty(value = "UAT测试报告",index = 12)
	    private Integer uat_testreport;
	    
	    @ExcelProperty(value = "SIT测试案例",index = 13)
	    private Integer sit_testcase;
	    
	    @ExcelProperty(value = "SIT测试报告",index = 14)
	    private Integer sit_testreport;
	    
	    @ExcelProperty(value = "里程碑",index = 15)
	    private Integer milestone;
	    
	    @ExcelProperty(value = "项目配置管理计划",index = 16)
	    private Integer project_configurationplan;
	    
	    @ExcelProperty(value = "风险列表及跟踪记录",index = 17)
	    private Integer risklist_trackingrecord;

	    @ExcelProperty(value = "项目问题日志",index = 18)
	    private Integer project_problemlog;
	    
	    @ExcelProperty(value = "项目成员技能识别" ,index = 19)
	    private Integer projectmemberskill_recognition;

	    @ExcelProperty(value = "培训记录表",index = 20)
	    private Integer training_record;
	    
	    @ExcelProperty(value = "项目周报",index = 21)
	    private Integer project_weekreport;
	    
	    @ExcelProperty(value = "项目月报",index = 22)
	    private Integer project_monthlyreport;
	    
	    @ExcelProperty(value = "里程碑报告",index = 23)
	    private Integer milestone_report;

	    @ExcelProperty(value = "质量保证",index = 24)
	    private Integer quality_assurance;

	    @ExcelProperty(value = "会议纪要",index = 25)
	    private Integer minutes_meeting;

	    @ExcelProperty(value = "通讯录",index = 26)
	    private Integer address_book;
	    
	    @ExcelProperty(value = "预研报告",index = 27)
	    private Integer preresearch_report;
	    
	    @ExcelProperty(value = "概要设计",index = 28)
	    private Integer summary_design;
	    
	    @ExcelProperty(value = "详细设计",index = 29)
	    private Integer detailed_design;
	    
	    @ExcelProperty(value = "数据库设计",index = 30)
	    private Integer database_design;
	    
	    @ExcelProperty(value = "设计评审报告",index = 31)
	    private Integer designreview_report;

	    @ExcelProperty(value = "用户操作手册",index = 32)
	    private Integer useroperation_manual;
	    
	    @ExcelProperty(value = "系统运维手册",index = 33)
	    private Integer system_maintenancemanual;

	    @ExcelProperty(value = "系统应急手册",index = 34)
	    private Integer system_emergencymanual;

	    @ExcelProperty(value = "系统安装手册",index = 35)
	    private Integer system_installationmanual;
	    
	    @ExcelProperty(value = "SIT测试计划",index = 36)
	    private Integer sit_testplan;
	    
	    @ExcelProperty(value = "UAT测试计划",index = 37)
	    private Integer uat_testplan;
	    
	    @ExcelProperty(value = "性能测试计划",index = 38)
	    private Integer performance_testplan;
	    
	    @ExcelProperty(value = "性能测试报告" ,index = 39)
	    private Integer performance_testreport;

	    @ExcelProperty(value = "压力测试案例",index = 40)
	    private Integer stress_testcase;

	    @ExcelProperty(value = "压力测试报告",index = 41)
	    private Integer stress_testreport;

	    @ExcelProperty(value = "安全测试报告",index = 42)
	    private Integer safety_testreport;

	    @ExcelProperty(value = "测试评审报告",index = 43)
	    private Integer test_reviewreport;
	    
	    @ExcelProperty(value = "数据库规格说明书" ,index = 44)
	    private Integer database_specification;

	    @ExcelProperty(value = "变更影响分析",index = 45)
	    private Integer change_impactaanalysis;

	    @ExcelProperty(value = "重大模块、功能、批处理等补充说明",index = 46)
	    private Integer supplementary_explanation;

	    @ExcelProperty(value = "必交文档提交率",index = 47)
	    private String must_rate;

	    @ExcelProperty(value = "选交文档提交率",index = 48)
	    private String selected_rate;

	    @ExcelProperty(value = "备注",index = 49)
	    private String remarks;
	    
	    private Integer cutResult;

	
}
