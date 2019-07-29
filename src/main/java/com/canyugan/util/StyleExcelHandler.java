package com.canyugan.util;


import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.excel.event.WriteHandler;

public class StyleExcelHandler implements WriteHandler 
{
	private static final Logger LOG = LoggerFactory.getLogger(StyleExcelHandler.class);
	/**
	 * LX WH
	 *   template cutResult
	 */
	Map<String, Map<Object, Map<String, Object>>> structer;
	int total_lx = 0;
	int total_wh = 0;
	public StyleExcelHandler(Map<String, Map<Object, Map<String, Object>>> structer) {
		this.structer = structer;
		this.total_lx = total_lx;
		this.total_wh = total_wh;
	}
//	private static AtomicInteger lx_num = new AtomicInteger(1);
//	private static AtomicInteger wh_num = new AtomicInteger(1);
	
	private static Map<String, Object> project_template_kv = null;
	int lx_num = 0;
	int wh_num = 0;
    @Override
    public void sheet(int i, Sheet sheet) {
    	
    }

    @Override
    public void row(int i, Row row) {
    	/**
    	 * 第二个sheet 
    	 * 按lx_num取出单个项目的全部模板kv结果
    	 */
    	if(row.getSheet().getSheetName().equals("审计总体情况")) {
    		return;
    	}
    	if(i == 0) {
    		return;
    	}
    	
    	try {
    		if(row.getSheet().getSheetName().equals("研发类项目")) {
    			lx_num += 1;
    			project_template_kv = structer.get("研发类").get(lx_num);
//    			lx_num.incrementAndGet();
    			if (i == structer.get("研发类").size() + 1) {
					row.setHeight((short)2500);
				}
    			
    		}else if(row.getSheet().getSheetName().equals("维护类项目")){
    			wh_num += 1;
    			project_template_kv = structer.get("维护类").get(wh_num);
    			if (i == structer.get("维护类").size() + 1) {
					row.setHeight((short)2500);
				}
    		}
		} catch (Exception e) {
			LOG .info("row exception:" + e.getMessage());
		}
    	
    }
    
	@Override
    public void cell(int i, Cell cell) {
		
		Workbook workbook = cell.getSheet().getWorkbook();
    	CellStyle cellStyle = createStyle(workbook);
    	Font totalFont = workbook.createFont();
    	totalFont.setFontHeightInPoints((short)10);
    	totalFont.setFontName("宋体");
    	
    	if ("审计总体情况".equals(cell.getSheet().getSheetName())) {
//    		 cellStyle.setBorderBottom(BorderStyle.THIN);
//		     cellStyle.setBorderLeft(BorderStyle.THIN);
//		     cellStyle.setBorderTop(BorderStyle.THIN);
//		     cellStyle.setBorderRight(BorderStyle.THIN);
//		     cellStyle.setAlignment(HorizontalAlignment.CENTER);
//		     cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    		if (cell.getRowIndex() == 0) {
    	        cellStyle.setAlignment(HorizontalAlignment.CENTER);
    	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    			cellStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            	totalFont.setColor(IndexedColors.WHITE.getIndex());            	
			}else if (cell.getRowIndex() != 0 && i == 0) {
    			cellStyle.setAlignment(HorizontalAlignment.LEFT);
     	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			}else if (cell.getRowIndex() != 0 && i != 0) {
				cellStyle.setAlignment(HorizontalAlignment.CENTER);
    	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			}
    		if (cell.getRowIndex() == 1) {
				totalFont.setBold(true);
			}
    		if (cell.getRowIndex() == 15) {
				totalFont.setBold(true);
			}
		}else if ("研发类项目".equals(cell.getSheet().getSheetName())) {
			
    		if (cell.getRowIndex() == 0) {
//    			cellStyle.setBorderBottom(BorderStyle.THIN);
// 		        cellStyle.setBorderLeft(BorderStyle.THIN);
// 		        cellStyle.setBorderTop(BorderStyle.THIN);
// 		        cellStyle.setBorderRight(BorderStyle.THIN);
// 		        cellStyle.setAlignment(HorizontalAlignment.CENTER);
// 		        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    			totalFont.setColor(IndexedColors.WHITE.getIndex());
    			
    			if (i <= 9 | i == 67) {
    				cellStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.getIndex());
                	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);                	 
				}
    			if (i >= 10 && i <= 19) {
    				cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
                	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				}
    			if (i >= 20 && i <= 28) {
    				cellStyle.setFillForegroundColor(IndexedColors.LAVENDER.getIndex());
                	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				}
    			if (i >= 29 && i <= 60) {
    				cellStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
                	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				}
    			if (i >= 61 && i <=66) {
    				cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				}				
			}else {
				if (cell.getRowIndex() == structer.get("研发类").size() + 1) {
    				cellStyle.setBorderBottom(BorderStyle.NONE);
     		        cellStyle.setBorderLeft(BorderStyle.NONE);
     		        cellStyle.setBorderTop(BorderStyle.THIN);
     		        cellStyle.setBorderRight(BorderStyle.NONE);
     		        cellStyle.setAlignment(HorizontalAlignment.LEFT);
     		        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
     		        
     		       
				}else if (cell.getRowIndex() == structer.get("研发类").size() + 2 ) {
    		        cellStyle.setAlignment(HorizontalAlignment.LEFT);
    		        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
				}
//    			
				if (cell.getRowIndex() == -1) {
					cellStyle.setBorderBottom(BorderStyle.THICK);
					
					
				}
    			if (i == 1 || i == 2 || i == 7 || i == 8) {
    				cellStyle.setAlignment(HorizontalAlignment.LEFT);
         	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
				}
    			try {
        			Integer cut_value = null;
        			try {
        				cut_value = (Integer) project_template_kv.get(String.valueOf(i));
					} catch (ClassCastException e) {
						LOG.info("-->class研发类【 获取具体项目的某个位置的数字出错 cut_value:" + (cut_value == null) + "】");
					}catch (NullPointerException e) {
						LOG.info("-->null研发类【 获取具体项目的某个位置的数字出错 cut_value:" + (cut_value == null) + "】");
					}
            		if(i == 10 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 11 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 12 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 13 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 14 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 15 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 16 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 17 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 18 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 19 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 20 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 21 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 22 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 23 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 24 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 25 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 26 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 27 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 28 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 29 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 30 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 31 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 32 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 33 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 34 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 35 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 36 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 37 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 38 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 39 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 40 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 41 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 42 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 43 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 44 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 45 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 46 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 47 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 48) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 49) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 50 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 51 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 52 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 53 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 54 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 55 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 56) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 57) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 58) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 59) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 60) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}
    			} catch (Exception e) {
    				System.out.println("-->【 涂色异常：" + e.getMessage() + " 】");
    			}
    			
    		}
    		
//    		CellRangeAddress region = new CellRangeAddress(structer.get("研发类").size() + 1, structer.get("研发类").size() + 1, 0, 60);
//	        cell.getSheet().addMergedRegion(region);
//    		if (cell) {
//    			cellStyle.setBorderBottom(BorderStyle.THICK);
//    	        cellStyle.setBorderLeft(BorderStyle.NONE);
//    	        cellStyle.setBorderTop(BorderStyle.NONE);
//    	        cellStyle.setBorderRight(BorderStyle.NONE);
//    	        cellStyle.setAlignment(HorizontalAlignment.LEFT);
//    	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
//			}
    		    		
		}else if ("维护类项目".equals(cell.getSheet().getSheetName())) {
    		if (cell.getRowIndex() == 0) {
    			cellStyle.setBorderBottom(BorderStyle.THIN);
    	        cellStyle.setBorderLeft(BorderStyle.THIN);
    	        cellStyle.setBorderTop(BorderStyle.THIN);
    	        cellStyle.setBorderRight(BorderStyle.THIN);
    	        cellStyle.setAlignment(HorizontalAlignment.CENTER);
    	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    			totalFont.setColor(IndexedColors.WHITE.getIndex());
    			if (i <= 8 | i == 58) {
    				cellStyle.setFillForegroundColor(IndexedColors.DARK_YELLOW.getIndex());
                	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);                	 
				}
    			if (i >= 9 && i <= 17) {
    				cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
                	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				}
    			
    			if (i >= 18 && i <= 51) {
    				cellStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
                	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				}
    			if (i >= 52 && i <=57) {
    				cellStyle.setFillForegroundColor(IndexedColors.ORANGE.getIndex());
                	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				}				
			}else {
    			if (cell.getRowIndex() == structer.get("维护类").size() + 1) {
				cellStyle.setBorderBottom(BorderStyle.NONE);
 		        cellStyle.setBorderLeft(BorderStyle.NONE);
 		        cellStyle.setBorderTop(BorderStyle.THIN);
 		        cellStyle.setBorderRight(BorderStyle.NONE);
 		        cellStyle.setAlignment(HorizontalAlignment.LEFT);
 		        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);		       
			}else if (cell.getRowIndex() == structer.get("维护类").size() + 2 ) {
		        cellStyle.setAlignment(HorizontalAlignment.LEFT);
		        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
			}
    			if (i == 1 || i == 2 || i == 7 || i == 8) {
    				cellStyle.setAlignment(HorizontalAlignment.LEFT);
         	        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
				}
    			try {
					Integer cut_value = null;
    				try {
            			 cut_value = (Integer) project_template_kv.get(String.valueOf(i));
					} catch (ClassCastException e) {
						LOG.info("-->class维护类【 获取具体项目的某个位置的数字出错 cut_value:" + (cut_value == null) + "】");
					}catch (NullPointerException e) {
						LOG.info("-->null维护类【 project_kv:" + project_template_kv + ",index:" + i + "】");
					}
        			if(i == 9 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 10 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 11 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 12 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 13 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 14 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 15 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 16 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 17 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 18 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 19 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 20 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 21 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 22 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 23 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 24 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 25 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 26 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 27 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 28 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 29 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 30 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 31 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 32 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 33 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 34 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 35 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 36 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 37 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 38 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 39 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 40 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 41 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 42 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 43 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 44 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 45 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 46 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 47 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 48) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 49) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 50 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}            			
            		}else if(i == 51 ) {
            			if (cut_value == 2) {
            				cellStyle.setFillForegroundColor(IndexedColors.CORAL.getIndex());
                        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 0) {
							cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}else if (cut_value == 3) {
							cellStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
	                    	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
						}
            		}
    			}catch (Exception e) {
    				System.out.println("-->【 涂色异常：" + e.getMessage() + " 】");
    			}
            		
		}
		} 
    	cellStyle.setFont(totalFont);
        cell.getRow().getCell(i).setCellStyle(cellStyle);
    	
    }


    /**
      * 实际中如果直接获取原单元格的样式进行修改, 最后发现是改了整行的样式, 因此这里是新建一个样式
      */
    private CellStyle createStyle(Workbook workbook) {
        CellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setBorderBottom(BorderStyle.THIN);
        cellStyle.setBorderLeft(BorderStyle.THIN);
        cellStyle.setBorderTop(BorderStyle.THIN);
        cellStyle.setBorderRight(BorderStyle.THIN);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        return cellStyle;
    }
}