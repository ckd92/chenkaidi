package com.fitech.account.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import com.fitech.constant.ExceptionCode;
import com.fitech.dto.DictionaryDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.fitech.account.service.DictionaryService;
import com.fitech.domain.account.Dictionary;
import com.fitech.framework.lang.result.GenericResult;

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
			result = dictionaryService.update(id,dictionary);					 
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

}
