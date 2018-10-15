package com.fitech.account.service;

import java.io.InputStream;
import java.util.List;

import com.fitech.domain.account.AccountLine;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.vo.account.AccountProcessVo;

/**
 * Created by wangxw on 2017/8/17.
 */
public interface AccountService {

    /**
     * 支持高级搜索分页查询台账数据
     * @param accountProcessVo
     * @return
     */
    public GenericResult<AccountProcessVo> findAccounDatas(AccountProcessVo accountProcessVo);
    
    /**
     * 获取台账行数据
     * @param accountId 报文ID
     * @param lineId	台账行ID
     * @return
     */
    public AccountLine findAccountDatas(Long userId, Long accountId,Long lineId);
    
    /**
     * 台账数据查询  ,无权限的字段查询(hx)
     * @param accountProcessVo
     * @return
     */
    public AccountProcessVo findAccountDatatwo(AccountProcessVo accountProcessVo);
    
    /**
     * 新增台账数据
     * @param accountProcessVo
     * @return
     */
    public List<String> addAccountData(AccountProcessVo accountProcessVo);

    /**
     * 修改台账数据
     * @param accountProcessVo
     * @return
     */
    public List<String> modifyAccountData(AccountProcessVo accountProcessVo);
    
    /**
     * 删除单条台账数据
     * @param accountProcessVo
     * @return
     */
    public GenericResult<Boolean> deleteAccountData(AccountProcessVo accountProcessVo);

    /**
     * 生成台账模板
     * @param accountId
     * @param userId
     * @return
     */
   public String generateAccountTemplate(Long accountId,Long userId);
   
   /**
    * 批量导入台账数据
    * @param inputStream
    * @param fileName
    * @param accountId
    * @return
    */
   public GenericResult<Boolean> loadDataByTemplate(InputStream inputStream, String fileName, Long accountId,Long userId,String operateFieldStr);
   
    
    /**
     * 下载台账数据 
     * @param accountProcessVo
     * @return
     */
    public String downLoadAccounData(AccountProcessVo accountProcessVo);

    
    
    
    /**
     * 批量补录台账数据
     * @param accountProcessVo
     * @return
     */
    public List<String> batchUpdateAccounData(AccountProcessVo accountProcessVo);

//    /**
//     * 初始化单张台账表格列
//     * @param accountProcessVo
//     * @return
//     */
//    public GenericResult<AccountProcessVo> initAccountTable(AccountProcessVo accountProcessVo);

    /**
     * 查询单条台账数据
     * @param accountProcessVo
     * @return
     */
    public AccountLine findAccountDataById(AccountProcessVo accountProcessVo);

    /**
     * 全表校验
     * @param accountProcessVo
     * @return
     */
    public GenericResult<Object> validateAll(AccountProcessVo accountProcessVo);

    /**
     * 批量校验
     * @param idList
     * @param userId
     * @return
     */
    public GenericResult<Object> validatePL(List<Long> idList,Long userId);
}
