package com.canyugan.pojo;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ProjectInfo 
{
	/**
	 * pProdLine varchar(50),
       pTeam varchar(50) ,
       projectCatagory varchar(50),
       projectHeader varchar(50),
       projectStatus varchar(50),
       projectLists varchar(50),
       projectName varchar(50),
       projectId varchar(50),
	 */
	
	private String pProdLine;
	private String pTeam;
	private String projectCatagory;
	private String projectHeader;
	private String projectStatus;
	private String projectLists;
	private String projectName;
	private String projectId;
	private String projectCodeNum;
	//0:待迁移 1:正在迁移中 2:迁移完成 
	private Integer migrationStatus;
	//上次迁移完成日期
	private String migrationDate;
}
