//package com.canyugan.excel;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import com.alibaba.excel.context.AnalysisContext;
//import com.alibaba.excel.event.AnalysisEventListener;
//
//@SuppressWarnings("rawtypes")
//public class ExcelListener extends AnalysisEventListener 
//{
//    private List<Object> datas = new ArrayList<>();
//
//    //通过 AnalysisContext 对象还可以获取当前 sheet，当前行等数据
//    @Override
//    public void invoke(Object object, AnalysisContext context) 
//    {
//        //数据存储到list，供批量处理，或后续自己业务逻辑处理。
//        datas.add(object);
//    }
//
//    @Override
//    public void doAfterAllAnalysed(AnalysisContext context) {
//    	
//    }
//    
//    public void clearDttas(){
//    	datas.clear();
//    }
//
//    public List<Object> getDatas() {
//        return datas;
//    }
//
//    public void setDatas(List<Object> datas) {
//        this.datas = datas;
//    }
//}
