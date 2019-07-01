//package com.canyugan.excel;
//
//import com.alibaba.excel.annotation.ExcelProperty;
//import com.alibaba.excel.metadata.BaseRowModel;
//
//import lombok.Data;
//
///**
// * excel读取映射模型
// * @author caorui
// */
//@Data
//public class ReadModel extends BaseRowModel 
//{
//    @ExcelProperty(index = 1)
//    private String projectName;//项目名称
//
//    @ExcelProperty(index = 2)
//    private String ITProjectManager;//it项目经理
//    
////    @ExcelProperty(index = 3)
////    private String childProjectNum;//子项目数量
////    
////    @ExcelProperty(index = 4)
////    private String childProjectNameShuangSu;//子项目名称 双速
////    
////    @ExcelProperty(index = 5)
////    private String childProjectNameZhouBao;//子项目名称 周报
////    
////    @ExcelProperty(index = 6)
////    private String childProjectNameZhanBao;//子项目名称 战报
//    
//    @ExcelProperty(index = 7)
//    private String ITManager;//it负责人
//    
//    @ExcelProperty(index = 8)
//    private String isBuild;//是否已在双速建项
//    
//    @ExcelProperty(index = 9)
//    private String svnUrl;//svn文档库地址
//    
//    @ExcelProperty(index = 10)
//    private String notes;//备注
//}