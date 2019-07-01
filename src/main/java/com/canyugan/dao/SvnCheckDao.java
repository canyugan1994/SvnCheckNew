package com.canyugan.dao;


import org.apache.ibatis.annotations.Param;

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
}
