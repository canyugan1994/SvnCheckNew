package com.canyugan.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

import lombok.Data;
@Data
public class CalculationModel extends BaseRowModel {

    @ExcelProperty(value = "度量维度" ,index = 0)
    private String metric_dimension;

    @ExcelProperty(value = "项目数量",index = 1)
    private Integer project_num;

    @ExcelProperty(value = "目标值（关键成果物）",index = 2)
    private String target_value;

    @ExcelProperty(value = "必交文档提交率",index = 3)
    private String must_rate;

    @ExcelProperty(value = "可选文档提交率",index = 4)
    private String optional_rate;
    
    @ExcelProperty(value = "备注",index = 4)
    private String remarks;
}

