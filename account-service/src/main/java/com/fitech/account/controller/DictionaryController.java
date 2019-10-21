package com.fitech.account.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.fitech.account.service.DictionaryItemService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.DictionaryItem;
import com.fitech.dto.DictionaryDto;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.common.CommonConst;
import com.fitech.framework.lang.util.ExcelUtil;
import com.fitech.vo.account.AccountDicVo;
import com.fitech.vo.ledger.CodeLibVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
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
	 * 修改字典
	 * @param id
	 * @param dictionary
	 * @param request
	 * @return
	 */
	@PutMapping("/dictionary/{id}")
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
	public GenericResult<Object> exportDicByExcel(HttpServletRequest request){
		GenericResult<Object> result = new GenericResult<>();
		try {
			List<List<String>> list = dictionaryService.searchDicAndDicitem();
			String sheetName = "BULUDATA-DicData";
			String filePath = CommonConst.getProperties("basePath") + "sjbl" + sheetName;
			ExcelUtil.createExcel2007(list,sheetName,filePath,sheetName);
		} catch (Exception e){
			e.printStackTrace();
			result.setSuccess(false);
			result.setMessage("载入失败,数据异常！");
		}
		return  result;
	}

}
