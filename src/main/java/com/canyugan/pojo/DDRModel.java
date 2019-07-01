package com.canyugan.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

@Data
public class DDRModel extends BaseRowModel {

	// 编号从0开始
	@ExcelProperty(index = 0)
	private String project_num;

	@ExcelProperty(index = 1)
	private String project_status;

	@ExcelProperty(index = 2)
	private String projectName;

	@ExcelProperty(index = 3)
	private String projectVersion;

}
