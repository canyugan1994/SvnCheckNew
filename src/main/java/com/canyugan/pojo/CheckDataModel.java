package com.canyugan.pojo;

import org.tmatesoft.sqljet.core.internal.lang.SqlParser.bool_return;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

import lombok.Data;
import lombok.ToString;

/**
 * excel读取映射模型
 * @author caorui
 */
@Data
@ToString
public class CheckDataModel extends BaseRowModel 
{
	//编号从0开始
    @ExcelProperty(index = 0)
    private String No;

    @ExcelProperty(index = 1)
    private String classify;
    
    @ExcelProperty(index = 2)
    private String directoryStruct;
    
    @ExcelProperty(index = 3)
    private String templateName;
    
    @ExcelProperty(index = 4)
    private Integer cutGuide;
    
    @ExcelProperty(index = 5)
    private Integer cutResult;
    
    @ExcelProperty(index = 6)
    private String reasonDescription;
    
    @ExcelProperty(index = 7)
    private String notes;
    
    private Integer exist = 0;
    private Integer done = 0;
    
}