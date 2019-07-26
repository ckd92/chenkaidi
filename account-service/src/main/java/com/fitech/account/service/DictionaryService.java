package com.fitech.account.service;



import java.util.List;

import com.fitech.domain.account.Dictionary;
import com.fitech.dto.DictionaryDto;
import com.fitech.framework.lang.result.GenericResult;



/**
 * Created by wangxw on 2017/7/1.
 */
public interface DictionaryService {
	/**
     * 查询所有字典
     * @return
     */
    public List<Dictionary> findAllDictionary();
	/**
     * 查询字典，多条件搜索
     * @param dictionary
     * @return
     */
    public List<DictionaryDto> findDictionary(Dictionary dictionary);

    /**
     * 查询没有被禁用的字典
     * @param dictionary
     * @return
     */
    public List<Dictionary> findDictionaryNoBan(String isEnable);

    /**
     * 根据id查询单个字典
     * @param id
     * @return
     */
    public Dictionary findOne(Long id);

    /**
     * 保存字典
     * @param dictionary
     * @return
     */
    public GenericResult<Boolean> save(Dictionary dictionary);
   
    /**
     * 删除字典
     * @param
     * @return
     */
    public GenericResult<Boolean> delete(Long id);
       
    /**
     * 更新字典
     * @param dictionary
     * @return
     */
    public GenericResult<Boolean> update(Long id,Dictionary dictionary);
    
    /**
     * 验证字典名称是否存在
     * @param
     * @return
     */
    public GenericResult<Boolean> valiDictionaryNameIsExist(Long id,Dictionary dictionary);

    /**
     * 递归搜索
     * @param id
     * @return
     */
    public Dictionary nextDicId(Long id);

}
