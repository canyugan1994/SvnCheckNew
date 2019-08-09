package com.canyugan.dao;


import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.canyugan.pojo.ProjectInfo;
import com.canyugan.pojo.Request;

/**
 * 数据库相关操作
 * @author caorui
 *
 */
public interface SvnCheckDao
{
	public Integer createRequest_id(Request request_mapp);

	public Integer getLoading(@Param("request_id")Integer request_id);
	
	public List<ProjectInfo> getProjectInfo();
	public Integer getProjectInfoTotal();
	
	//双速项目编号相关信息
	public List<String> getAllProjectCode();
	//新增项目
	public void batchInsertProject(@Param("projects")List<ProjectInfo> add_to_db_project);
	//获取项目全部信息
	public List<ProjectInfo> getAllProject();
	
	public void updateProjectMoveStatusAndDate(@Param("project_code_name")String project_code_name, 
											   @Param("status")String status, 
											   @Param("date")String date);
}
