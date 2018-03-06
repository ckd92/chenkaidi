package com.fitech.account.service;

import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountLine;
import com.fitech.domain.system.FieldPermission;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.vo.account.AccountProcessVo;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

/**
 * Created by wangxw on 2017/8/17.
 */
public interface AccountService {

    /**
     * 支持高级搜索分页查询台账数据
     * @param accountProcessVo
     * @return
     */
    public GenericResult<AccountProcessVo> findPageAccounData(AccountProcessVo accountProcessVo);

    /**
     * 支持高级搜索分页查询台账数据,无权限(hx)
     * @param accountProcessVo
     * @return
     */
    public GenericResult<AccountProcessVo> findPageAccounDatatwo(AccountProcessVo accountProcessVo);
    
    /**
     * 批量补录台账数据
     * @param accountProcessVo
     * @return
     */
    public List<String> batchUpdateAccounData(AccountProcessVo accountProcessVo);

    /**
     * 初始化单张台账表格列
     * @param accountProcessVo
     * @return
     */
    public GenericResult<AccountProcessVo> initAccountTable(AccountProcessVo accountProcessVo);

    /**
     *
     * 生成台账模板
     * @param accountId
     * @return
     */
    public String generateAccountTemplate(Long accountId,Long userId);


    /**
     * 新增台账数据
     * @param accountProcessVo
     * @return
     */
    public List<String > addAccountData(AccountProcessVo accountProcessVo);

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
    public GenericResult<Boolean> deleteAccountDataById(AccountProcessVo accountProcessVo);

    /**
     * 查询单条台账数据
     * @param accountProcessVo
     * @return
     */
    public GenericResult<AccountLine> findAccountDataById(AccountProcessVo accountProcessVo);

    /**
     * 批量导入台账数据
     * @param inputStream
     * @param fileName
     * @param accountId
     * @return
     */
    public GenericResult<Boolean> loadDataByTemplate(InputStream inputStream, String fileName, Long accountId,Long userId,String operateFieldStr);
    
    /**
     * 根据角色ID获取角色信息
     * @param role
     * @return Role
     */
    public Collection<FieldPermission> findById(Long userId, Long id);
    
    /**
     * 获取模板id
     * @param Account
     * @return
     */
    public Long findByAccountTemplateId(Long accountId);

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
