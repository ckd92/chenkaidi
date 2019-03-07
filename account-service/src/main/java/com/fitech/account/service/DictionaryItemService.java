package com.fitech.account.service;

import java.util.List;
import com.fitech.domain.account.DictionaryItem;
import com.fitech.framework.lang.result.GenericResult;

/**
 * Created by wangjianwei on 2017/7/28.
 */
public interface DictionaryItemService {

	/**
	 * 查询字典项，多条件搜索
	 * @param dictionaryItem
	 * @return
	 */
	public List<DictionaryItem> findDictionaryItem(DictionaryItem dictionaryItem);

	/**
	 * 验证字典项名称是否存在
	 * @param idList
	 * @return
	 */
	public Boolean valiDictionaryItemNameIsExist(Long id,DictionaryItem dictionaryItem);

	/**
	 * 根据字典id条件查询字典项
	 */
	public List<DictionaryItem> getDictionaryItemByDicItemName(Long id,String dicItemName);
	
	/**
	 * 根据字典id查询字典项
	 * @param id
	 * @return
	 */
	public List<DictionaryItem> getDictionaryItemByDictionaryId(Long id);
	
	/**
	 * 根据字典id查询出所有字典项
	 * @param id
	 * @return
	 */
	public List<DictionaryItem> findByDictionaryId(Long id);
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	public DictionaryItem findById(Long id);
	/**
	 * 根据id查询单个字典
	 * @param id
	 * @return
	 */
	public DictionaryItem findOne(Long id);
	/**
	 * 添加字典项
	 * @param dictionaryItem
	 * @return
	 */
	public GenericResult<Boolean> save(DictionaryItem dictionaryItem);
	/**
	 * 修改字典项
	 * @param id
	 * @param dictionaryItem
	 * @return
	 */
	public GenericResult<Boolean> update(Long id,DictionaryItem dictionaryItem);
	/**
	 * 删除字典项
	 * @param idList
	 * @return
	 */
	public GenericResult<Boolean> delete(Long id);
	/**
	 * 根据字典id删除字典项
	 * @param id
	 * @return
	 */
	public void deleteByDictionaryId(Long id);
}
