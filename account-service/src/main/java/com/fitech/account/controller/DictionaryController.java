package com.fitech.account.controller;

import com.fitech.account.service.DictionaryService;
import com.fitech.domain.account.Dictionary;
import com.fitech.framework.lang.result.GenericResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by wangjianwei on 2017/7/28.
 */
@RestController
public class DictionaryController {

	@Autowired
	private DictionaryService dictionaryService;

	//条件分页字典
	@PostMapping("/dictionarys")
	public GenericResult<List<Dictionary>> findAll(@RequestBody Dictionary dictionary,HttpServletRequest request) {
		GenericResult<List<Dictionary>> result = new GenericResult<>();
		try {
			List<Dictionary> collection = dictionaryService.findDictionary(dictionary);
			result.setData(collection);
		} catch (Exception e) {
			result.setSuccess(false);
			e.printStackTrace();
		} finally {
		}
		return result;
	}
	

	//新增字典
	@PostMapping("/dictionary")
	public GenericResult<Boolean> saveDictionary(@RequestBody Dictionary dictionary,HttpServletRequest request){
		GenericResult<Boolean> result = new GenericResult<>();
		try {
			result = dictionaryService.saveDictionary(dictionary);					 
		}catch (Exception e) {
			result.setSuccess(false);
			e.printStackTrace();
		} finally {
		}
		return result;
	}
	
	//修改字典
	@PutMapping("/dictionary/{id}")
	public GenericResult<Boolean> updateDictionary(@PathVariable("id") Long id,@RequestBody Dictionary dictionary,HttpServletRequest request){
		GenericResult<Boolean> result = new GenericResult<>();
		try {
			result = dictionaryService.updateDictionary(id,dictionary);						 
		}catch (Exception e) {
			result.setSuccess(false);
			e.printStackTrace();
		} finally {
		}
		return result;
	}
	
	

	
	//删除字典
	@DeleteMapping("/dictionary/{id}")
	public GenericResult<Boolean> deleteDictionary(@PathVariable("id") Long id,HttpServletRequest request){
		GenericResult<Boolean> result = new GenericResult<>();
		try {
			result = dictionaryService.deleteDictionary(id);		 
		}catch (Exception e) {
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


}
