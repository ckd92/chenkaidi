package com.fitech.account.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;

public class ExcelUtils<T> {
	

	public static String createExcel(List<List<String>> columheader,
			String sheetName, String templatePath,
			List<Integer> downRows,List<List<String>> downData) {
		String path = templatePath;
		Object inStream = null;
		FileOutputStream fout = null;
		File file = null;
		HSSFWorkbook wb = null;
		try {
			createDir(path);
			path = path + sheetName + ".xls";
			wb = new HSSFWorkbook();
			HSSFSheet e = wb.createSheet(sheetName);
			int rowNum = 0;
			
			for(int r=0;r<downRows.size();r++){
	            String[] dlData = new String[downData.get(r).size()];//获取下拉对象
	            for(int i=0;i<downData.get(r).size();i++){
	            	dlData[i] = downData.get(r).get(i);
	            }
	            int rownum = downRows.get(r);
	            e.addValidationData(setDataValidation(e, dlData, 2, 500, rownum ,rownum)); //超过255个报错 
			}
			
			for (Iterator arg10 = columheader.iterator(); arg10.hasNext(); ++rowNum) {
				List colums = (List) arg10.next();
				HSSFRow row = e.createRow(rowNum);
				HSSFCellStyle style = wb.createCellStyle();
				style.setAlignment((short)2);
				HSSFCell cell = null;

				for (int i = 0; i < colums.size(); ++i) {
					cell = row.createCell(i);
					cell.setCellValue((String) colums.get(i));
					cell.setCellStyle(style);
				}
			}

			file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}

			fout = new FileOutputStream(path);
			wb.write(fout);
			fout.close();
		} catch (Exception arg23) {
			arg23.printStackTrace();
		} finally {
			try {
				if (inStream != null) {
					((FileInputStream) inStream).close();
				}

				if (fout != null) {
					fout.close();
				}
			} catch (IOException arg22) {
				arg22.printStackTrace();
			}

		}

		return path;
	}
	
	public static void createDir(String destDirName) {
		File dir = new File(destDirName);
		if (!dir.exists()) {
			if (!destDirName.endsWith(File.separator)) {
				destDirName = destDirName + File.separator;
			}

			if (dir.mkdirs()) {
				System.out.println("创建目录成功！" + destDirName);
			} else {
				System.out.println("创建目录失败！");
			}
		}

	}
	
	
	private static DataValidation setDataValidation(Sheet sheet, String[] textList, int firstRow, int endRow, int firstCol, int endCol) {

        DataValidationHelper helper = sheet.getDataValidationHelper();
        //加载下拉列表内容
        DataValidationConstraint constraint = helper.createExplicitListConstraint(textList);
        //DVConstraint constraint = new DVConstraint();
        constraint.setExplicitListValues(textList);
        
        //设置数据有效性加载在哪个单元格上。四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList((short) firstRow, (short) endRow, (short) firstCol, (short) endCol);
    
        //数据有效性对象
        DataValidation data_validation = helper.createValidation(constraint, regions);
        //DataValidation data_validation = new DataValidation(regions, constraint);
    
        return data_validation;
    }

	
}