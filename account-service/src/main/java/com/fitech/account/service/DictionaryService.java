package com.fitech.account.service;



import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import com.fitech.domain.account.Dictionary;
import com.fitech.dto.DictionaryDto;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.vo.account.AccountDicVo;
import com.fitech.vo.account.AccountFieldVo;
import com.fitech.vo.account.AccountTemplateVo;
import org.apache.poi.ss.usermodel.Sheet;


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
     * 删除全部字典
     * @param
     * @return
     */
    public GenericResult<Boolean> deleteAll();

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
    public GenericResult<Boolean> update(Long id,Dictionary dictionary,String flag);
    
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

    /**
     * 批量载入字典项
     * @param list
     * @return
     */
    public GenericResult<Object> batchAdd(Collection<AccountDicVo> list);

    /**
     * 获取字典和字典项全部数据
     * @return
     */
    public List<List<String>> getDicAndDicitemData();

    /**
     * 字典和字典项查询是否存在
     * @return
     */
    public String validateDataCheck(List<AccountFieldVo> itemList);

    /**
     * 批量载入数据补录 表和字段
     * @return
     */
    public GenericResult<Boolean> batchAddTempAndField(String busSystemId,List<AccountTemplateVo> templateList,List<AccountFieldVo> itemList);

}
