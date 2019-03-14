package com.fitech.account.util;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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

	
	/**
	 * 系统生成EXCEL
	 *
	 * @param columheader
	 *            列集合
	 * @param sheetName
	 *            sheet名称
	 * @param templatePath
	 *            生成地址
	 * @param fileName
	 *            文件名称
	 * @return
	 */
	public static String createExcel2007(List<List<String>> columheader,
										 String sheetName, String templatePath, String fileName,List<Integer> downRows,List<List<String>> downData) {
		String path = templatePath;
		//声明输出流
		FileOutputStream fout = null;
		//声明文件对象
		File file = null;
		//声明Excel文档对象
		XSSFWorkbook xwb = null;
		SXSSFWorkbook swb = null;
		try {
			createDir(path);
			path += fileName + ".xlsx";
			file = new File(path);
			if (!file.exists()) {
				file.createNewFile();
			}
			//初始化输出流
			fout = new FileOutputStream(path);
			//初始化Excel文档对象
			xwb = new XSSFWorkbook();
			//内存中只留1000行数据，多余的暂存在硬盘中，解决XSSFWorkbook一次只能写入64000行问题
			swb = new SXSSFWorkbook(xwb,1000);
			// 第二步，在webbook中添加一个sheet,对应Excel文件中的sheet

			CellStyle textStyle = swb.createCellStyle();
			DataFormat format = swb.createDataFormat();
			textStyle.setDataFormat(format.getFormat("@"));
			//数据超过100万行，分sheet,每个sheet放100万行数据
			int sheetNum = (int) Math.ceil(columheader.size() /1000000.0);
			for(int sheetIndex = 0; sheetIndex < sheetNum; sheetIndex++){
				List<List<String>> partOfColumheader = columheader.subList(0,2);

				int subToNum = (sheetIndex+1)*1000000 +2;
				if(  subToNum >= columheader.size() ){
					subToNum = columheader.size();
				}
				//第i个sheet所需要的数据
				partOfColumheader.addAll( columheader.subList( sheetIndex*1000000+2, subToNum ) );

				// 创建sheet页
				XSSFSheet sheet = xwb.createSheet("part"+sheetIndex);
				
				for(int r=0;r<downRows.size();r++){
		            String[] dlData = new String[downData.get(r).size()];//获取下拉对象
		            for(int i=0;i<downData.get(r).size();i++){
		            	dlData[i] = downData.get(r).get(i);
		            }
		            int rownum = downRows.get(r);
		            sheet.addValidationData(setDataValidation(sheet, dlData, 2, 500, rownum ,rownum)); //超过255个报错 
		            sheet.setDefaultColumnStyle(rownum, textStyle);//设置下拉列表为文本格式
				}
				//设置默认列宽
				for(int j=0;j<columheader.get(0).size();j++){
					sheet.setColumnWidth( j,4000);
				}
				int rowNum = 0;
				for ( ; rowNum<partOfColumheader.size();rowNum++) {
					// 第三步，在sheet中添加表头第N行,注意老版本poi对Excel的行数列数有限制short
					Row row = sheet.createRow(rowNum);
					Cell cell = null;
					for (int i = 0; i < columheader.get(rowNum).size(); i++) {
						cell = row.createCell(i);
						cell.setCellValue(columheader.get(rowNum).get(i));
					}
				}
			}
			swb.write(fout);
			fout.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fout) {
					fout.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return path;
	}
	
	public static String createExcel2007(List<List<String>> columheader,
									 String sheetName, String templatePath,
									 List<Integer> downRows,List<List<String>> downData) {
		String path = templatePath;
		Object inStream = null;
		FileOutputStream fout = null;
		File file = null;
		XSSFWorkbook xwb = null;
		try {
			createDir(path);
			path = path + sheetName + ".xlsx";
			xwb = new XSSFWorkbook();
			XSSFSheet e = xwb.createSheet(sheetName);
			CellStyle textStyle = xwb.createCellStyle();
			DataFormat format = xwb.createDataFormat();
			textStyle.setDataFormat(format.getFormat("@"));

	
			int rowNum = 0;

			for(int r=0;r<downRows.size();r++){
				String[] dlData = new String[downData.get(r).size()];//获取下拉对象
				for(int i=0;i<downData.get(r).size();i++){
					dlData[i] = downData.get(r).get(i);
				}
				int rownum = downRows.get(r);
				e.addValidationData(setDataValidation(e, dlData, 2, 500, rownum ,rownum)); //超过255个报错
				e.setDefaultColumnStyle(rownum, textStyle);//设置下拉列表为文本格式
			}

			for (Iterator arg10 = columheader.iterator(); arg10.hasNext(); ++rowNum) {
				List colums = (List) arg10.next();
				Row row = e.createRow(rowNum);
				CellStyle style = xwb.createCellStyle();
				style.setAlignment((short)2);
				Cell cell = null;

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
			xwb.write(fout);
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