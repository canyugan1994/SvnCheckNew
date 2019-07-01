package com.canyugan.pojo;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;
import lombok.Data;

@Data
public class realListMoel
{
    private String jiraProjectName;

    private String projectCodeNum;
    
    private String projectName;
    
    private String projectPerson;
    
    private String projectStatus = "--";
    private String s1 = "--";
    private String s2 = "--";
    private String s3 = "--";
}