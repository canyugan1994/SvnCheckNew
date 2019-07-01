package com.canyugan.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

import lombok.Data;

/**
 * excel读取映射模型
 * @author caorui
 */
@Data
public class MetaModel extends BaseRowModel 
{
	//编号从0开始
    @ExcelProperty(index = 0)
    private String key;

    @ExcelProperty(index = 1)
    private String value;
}