package com.fitech.account.service;



import com.fitech.domain.account.Dictionary;
import com.fitech.framework.lang.result.GenericResult;


import java.util.List;

import org.springframework.data.domain.Page;



/**
 * Created by wangxw on 2017/7/1.
 */
public interface DictionaryService {
	/**
     * 查询字典，多条件搜索
     * @param dictionary
     * @return
     */
    public Page<Dictionary> findDictionary(Dictionary dictionary);
	
    
    /**
     * 根据id查询单个字典
     * @param id
     * @return
     */
    public Dictionary findOne (Long id);
    
    /**
     * 保存字典
     * @param dictionary
     * @return
     */
    public GenericResult<Boolean> saveDictionary(Dictionary dictionary);
   
    /**
     * 删除字典
     * @param idList
     * @return
     */
    public GenericResult<Boolean> deleteDictionary(Long id);
       
    /**
     * 更新字典
     * @param dictionary
     * @return
     */
    public GenericResult<Boolean> updateDictionary(Long id,Dictionary dictionary);
    
    /**
     * 验证字典名称是否存在
     * @param idList
     * @return
     */
    public GenericResult<Boolean> valiDictionaryNameIsExist(Long id,Dictionary dictionary);
    
    /**
     * 验证字典是否可以删除
     * @param accountTemplate
     * @return
     */
    public Boolean valiDictionaryIsDelete(Dictionary dictionary);
    
    /**
     * 查询所有字典
     * @return
     */
    public List<Dictionary> findAllDictionary();


}
