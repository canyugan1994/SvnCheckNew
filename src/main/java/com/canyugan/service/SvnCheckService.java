package com.canyugan.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.canyugan.dao.SvnCheckDao;
import com.canyugan.pojo.ProjectInfo;
import com.canyugan.pojo.Request;

/**
 * 交付物检查项目业务层
 * @author caorui
 */
@Service
public class SvnCheckService
{
	private static final Logger LOG = LoggerFactory.getLogger(SvnCheckService.class);
	@Autowired
	private SvnCheckDao svnCheckDao;
	
	//获取插入过后的主键id
	@Transactional
	public Integer createRequest_id(Request request_mapp) {
		return svnCheckDao.createRequest_id(request_mapp);
	}
	
	public Integer getLoading(Integer request_id) {
		return svnCheckDao.getLoading(request_id);
	}
	
	//双速项目编号相关信息
	public List<String> getAllProjectCode(){
		return svnCheckDao.getAllProjectCode();
	}
	
	//双速项目全部信息
	public List<ProjectInfo> getAllProject(){
		return svnCheckDao.getAllProject();
	}

	//新增项目
	@Transactional
	public void addNewProjects(List<ProjectInfo> add_to_db_project) {
		svnCheckDao.batchInsertProject(add_to_db_project);
	}
	
	//更改项目的迁移状态
	@Transactional
	public void updateProjectMoveStatusAndDate(String project_code_name,String status,String date) {
		svnCheckDao.updateProjectMoveStatusAndDate(project_code_name,status,date);
	}
}
