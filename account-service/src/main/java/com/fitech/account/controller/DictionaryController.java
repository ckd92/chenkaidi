package com.fitech.account.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fitech.constant.LoggerUtill;
import com.fitech.dto.DictionaryDto;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.common.CommonConst;
import com.fitech.framework.lang.util.ExcelUtil;
import com.fitech.framework.lang.util.FileUtil;
import com.fitech.system.annotation.AddOperateLogLast;
import com.fitech.vo.account.AccountDicVo;
import com.fitech.vo.account.AccountFieldVo;
import com.fitech.vo.account.AccountTemplateVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fitech.account.service.DictionaryService;
import com.fitech.domain.account.Dictionary;
import com.fitech.framework.lang.result.GenericResult;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by wangjianwei on 2017/7/28.
 */
@RestController
public class DictionaryController {
	@Autowired
	private DictionaryService dictionaryService;


	/**
	 * 条件分页字典
	 * @param dictionary
	 * @param request
	 * @return
	 */
	@PostMapping("/dictionarys")
	public GenericResult<List<DictionaryDto>> findAll(@RequestBody Dictionary dictionary, HttpServletRequest request) {
		GenericResult<List<DictionaryDto>> result = new GenericResult<>();
		try {
			List<DictionaryDto> collection = dictionaryService.findDictionary(dictionary);
			result.setData(collection);
		} catch (Exception e) {
			result.setSuccess(false);
			e.printStackTrace();
		} finally {
		}
		return result;
	}

	/**
	 * 查询所有字典
	 * @return
	 */
	@GetMapping("/dictionarys")
	public GenericResult<List<Dictionary>> findAllDictionary(){
		GenericResult<List<Dictionary>> result = new GenericResult<>();
		List<Dictionary> list = dictionaryService.findAllDictionary();
		result.setData(list);
		return result;
	}

	/**
	 * 查询所有没有被禁用字典
	 * @return
	 */
	@GetMapping("/dictionarysNoBan")
	public GenericResult<List<Dictionary>> findAllDictionaryNoBan(){
		GenericResult<List<Dictionary>> result = new GenericResult<>();
		List<Dictionary> list = dictionaryService.findDictionaryNoBan("1");
		result.setData(list);
		return result;
	}

	/**
	 * 根据id查询字典项
	 * @param id
	 * @param request
	 * @return
	 */
	@GetMapping("/findDicByid/{id}")
	public GenericResult<Dictionary> findDicByid(@PathVariable("id") Long id ,HttpServletRequest request){
		GenericResult<Dictionary> result = new GenericResult<>();
		Dictionary dic = dictionaryService.findOne(id);
		//String parentId=dic.getParentId();
		result.setData(dic);
		return result;
	}

	/**
	 * 新增字典
	 * @param dictionary
	 * @param request
	 * @return
	 */
	@PostMapping("/dictionary")
	@AddOperateLogLast(targetURI = "/dictionary", baseContent = "科融统计平台-业务设置-数据字典管理-新增字典", logType = LoggerUtill.LogType.OPERATE)
	public GenericResult<Boolean> save(@RequestBody Dictionary dictionary,  HttpServletRequest request){
		GenericResult<Boolean> result = new GenericResult<>();
		try {
			result = dictionaryService.save(dictionary);
		}catch (Exception e) {
			result.setSuccess(false);
			e.printStackTrace();
		} finally {
		}
		return result;
	}

	/**
	 * 全部删除字典和字典项
	 * @param request
	 * @return
	 */
	@GetMapping("/deleteAllDictionary")
	@AddOperateLogLast(targetURI = "/deleteAllDictionary", baseContent = "科融统计平台-业务设置-数据字典管理-刪除字典项", logType = LoggerUtill.LogType.OPERATE)
	public GenericResult<Boolean> deleteAllDictionary(HttpServletRequest request){
		GenericResult<Boolean> result = new GenericResult<>();
		try {
			result = dictionaryService.deleteAll();
		}catch (Exception e) {
			result.setSuccess(false);
			e.printStackTrace();
		} finally {
		}
		return result;
	}


	/**
	 * 修改字典
	 * @param id
	 * @param dictionary
	 * @param request
	 * @return
	 */
	@PutMapping("/dictionary/{id}")
	@AddOperateLogLast(targetURI = "/dictionary/", baseContent = "科融统计平台-业务设置-数据字典管理-修改字典", logType = LoggerUtill.LogType.OPERATE)
	public GenericResult<Boolean> update(@PathVariable("id") Long id,@RequestBody Dictionary dictionary,HttpServletRequest request){
		GenericResult<Boolean> result = new GenericResult<>();
		try {
			result = dictionaryService.update(id,dictionary,"-1");
		}catch (Exception e) {
			result.setSuccess(false);
			e.printStackTrace();
		} finally {
		}
		return result;
	}

	/**
	 * 修改字典
	 * @param id
	 * @param dictionary
	 * @param request
	 * @return
	 */
	@PutMapping("/dictionary/{id}/{flag}")
	@AddOperateLogLast(targetURI = "/dictionary/", baseContent = "科融统计平台-业务设置-数据字典管理-修改字典", logType = LoggerUtill.LogType.OPERATE)
	public GenericResult<Boolean> update(@PathVariable("id") Long id,@PathVariable("flag")String flag,@RequestBody Dictionary dictionary,HttpServletRequest request){
		GenericResult<Boolean> result = new GenericResult<>();
		try {
			result = dictionaryService.update(id,dictionary,flag);
		}catch (Exception e) {
			result.setSuccess(false);
			e.printStackTrace();
		} finally {
		}
		return result;
	}

	
	/**
	 * 删除字典
	 * @param id
	 * @param request
	 * @return
	 */
	@DeleteMapping("/dictionary/{id}")
	@AddOperateLogLast(targetURI = "/dictionary/", baseContent = "科融统计平台-业务设置-数据字典管理-删除字典", logType = LoggerUtill.LogType.OPERATE)
	public GenericResult<Boolean> delete(@PathVariable("id") Long id,HttpServletRequest request){
		GenericResult<Boolean> result = new GenericResult<>();
		try {
			result = dictionaryService.delete(id);
		}catch (Exception e) {
			result.setSuccess(false);
			e.printStackTrace();
		} finally {
		}
		return result;
	}

	@GetMapping("/nextDicId/{id}")
	public GenericResult<Dictionary> nextDicId(@PathVariable("id") Long id){
		GenericResult<Dictionary> result = new GenericResult<>();
		try{
			Dictionary dic = dictionaryService.nextDicId(id);
			result.setData(dic);
		}catch (Exception e){
			result.setSuccess(false);
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * 数据补录批量上传字典项
	 * @param file
	 * @param request
	 * @return
	 */
	@PostMapping("loadDicitemsByExcel")
	public GenericResult<Object> loadDataFromTemplate(@RequestParam(value = "file", required = true) MultipartFile file,
													   HttpServletRequest request){
		GenericResult<Object> result = new GenericResult<>();
		try {
			List<AccountDicVo> list = ExcelUtil.addFormExcel2003And2007(file.getInputStream(), file.getOriginalFilename(), new AccountDicVo());
			result = dictionaryService.batchAdd(list);
		} catch (Exception e){
			e.printStackTrace();
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}
		return  result;
	}

	/**
	 * 数据补录字典项导出
	 * @param request
	 * @return
	 */
	@PostMapping("exportDicByExcel")
	@AddOperateLogLast(targetURI = "/exportDicByExcel", baseContent = "科融统计平台-业务设置-数据字典项管理-字典项导出", logType = LoggerUtill.LogType.OPERATE)
	public GenericResult<Object> exportDicByExcel(HttpServletRequest request, HttpServletResponse response){
		GenericResult<Object> result = new GenericResult<>();
		try {
			List<List<String>> list = dictionaryService.getDicAndDicitemData();
			if(list.size() == 1){
				result.setSuccess(false);
				result.setMessage("字典为空，无法下载！");
				return result;
			}
			String sheetName = "BULUDATA-DicData";
			String filePath = CommonConst.getProperties("basePath") + "sjbl" + File.separator + "data" + File.separator ;
			ExcelUtil.createExcel2007(list,sheetName,filePath,sheetName);
			String fileNameParth = "sjbl|data|" + sheetName;
			result.setSuccess(true);
			result.setMessage(fileNameParth);
		} catch (Exception e){
			e.printStackTrace();
			result.setSuccess(false);
			result.setMessage("载入失败,数据异常！");
		}
		return  result;
	}

	/**
	 * 系统配置 数据补录模板下载
	 * @param request
	 * @param response
	 */
    @GetMapping("downloadSjblTemplate")
    public void downloadTemplate(HttpServletRequest request, HttpServletResponse response){
        try {
            String sheetName = "BULUDATA-DicData-template";
            String filePathstr = CommonConst.getProperties("template_path");
            File filePath = new File(filePathstr);
            if(!filePath.exists()){
            	filePath.mkdirs();
			}
            File file = new File(filePathstr + sheetName + ".xlsx");
            if(!file.exists()){
				List<List<String>> execList = new ArrayList<List<String>>();
				List<String> fieldList = new ArrayList<String>();
				List<String> valueList = new ArrayList<String>();
				fieldList.add("");
				execList.add(fieldList);
				execList.add(valueList);
				ExcelUtil.createExcel2007(execList,sheetName,filePathstr + File.separator,sheetName);
            }
            FileUtil.downLoadFile(file,response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

	/**
	 * 批量载入数据补录 模板和字段
	 * @param templateFile
	 * @param itemFile
	 * @param busSystemId
	 * @param request
	 * @return
	 */
	@PostMapping("/batchSjblInputTemplate/{busSystemId}")
	public GenericResult<Boolean> batchInputTemplate(
			@RequestParam(value = "templateFile", required = true) MultipartFile templateFile,
			@RequestParam(value = "itemFile", required = true) MultipartFile itemFile,
			@PathVariable("busSystemId") String busSystemId,
			HttpServletRequest request) {
		GenericResult<Boolean> result = new GenericResult<Boolean>();
		try{
			//报文模板数据
			List<AccountTemplateVo> templateList = ExcelUtil.addFormExcel2003And2007(templateFile.getInputStream(),templateFile.getOriginalFilename(),new AccountTemplateVo());
			//报文模板字段数据
			List<AccountFieldVo> itemList = ExcelUtil.addFormExcel2003And2007(itemFile.getInputStream(),itemFile.getOriginalFilename(), new AccountFieldVo());
			//判断是否存在字典项，字典项是否已存在
			String checkResult = dictionaryService.validateDataCheck(itemList);
			//checkResult 不为null，表示有字典项不存在，checkResult对应该字典项的codeLibKey
			if( checkResult != null ){
				result.setSuccess(false);
				result.setMessage(checkResult);
			}else{
				//批量导入
				result.setSuccess(true);
				result = dictionaryService.batchAddTempAndField(busSystemId,templateList,itemList);
			}
		}catch (Exception e){
			result.setSuccess(false);
			result.setMessage(e.getMessage());
		}
		return result;
    }

}
