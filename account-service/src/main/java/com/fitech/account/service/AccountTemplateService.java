package com.fitech.account.service;

import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.report.BusSystem;
import com.fitech.framework.lang.result.GenericResult;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;

/**
 * Created by wangxw on 2017/7/25.
 */
public interface AccountTemplateService {

    /**
     * 查询台账模板，包括分页，多条件搜索
     * @param accountTemplate
     * @return
     */
    public Page<AccountTemplate> findAccountTemplateByPage(AccountTemplate accountTemplate);
    
    /**
     * 查询所有台账
     * @return
     */
    public List<AccountTemplate> findAllAccountTemplate();

    /**
     * 保存台账模板
     * @param accountTemplate
     * @return
     */
    public GenericResult<Boolean> saveAccountTemplate(AccountTemplate accountTemplate);

    /**
     * 更新台账模板
     * @param accountTemplate
     * @return
     */
    public GenericResult<Boolean> updateAccountTemplate(AccountTemplate accountTemplate);

    /**
     * 删除台账模板
     * @param idList
     * @return
     */
    public GenericResult<Boolean> deleteAccountTemplateByList(List<Long> idList);

    /**
     * 验证台账模板是否可以删除
     * @param accountTemplate
     * @return
     */
    public String valiAccountTemplateIsDelete(Long id);

    /**
     * 验证台账名称是否存在
     * @param templateName
     * @return
     */
    public GenericResult<Boolean> valiAccountTemplateNameIsExist(String templateName);

    /**
     * 验证台账编号是否存在
     * @param templateCode
     * @return
     */
    public GenericResult<Boolean> valiAccountTemplateCodeIsExist(String templateCode);


    /**
     * 根据id查询单个台账
     * @param id
     * @return
     */
    public AccountTemplate findAccountTemplateById(Long id);

    /**
     * 查询制度下的台账模板
     * @param busSystem
     * @return
     */
    public List<AccountTemplate> findByBusSystem(BusSystem busSystem);

    /**
     * 增加字段
     * @param accountTemplate
     * @return
     */
    public GenericResult<Boolean> addAccountTemplateField(AccountTemplate accountTemplate);

    /**
     * 修改字段
     * @param accountTemplate
     * @return
     */
    public GenericResult<Boolean> modifyAccountTemplateField(AccountTemplate accountTemplate);

    /**
     * 删除字段
     * @param accountTemplate
     * @return
     */
    public GenericResult<Boolean> deleteAccountTemplateField(Long accountTemplateId, Long accountFieldId);

    /**
     * 查询单条字段
     * @param accountTemplate
     * @return
     */
    public GenericResult<AccountField> findOneAccountTemplateField(AccountTemplate accountTemplate);

    /**
     * 初始化补录权限数据
     * @param accountTemplate
     * @param busSystem
     * @return
     * @throws Exception
     */
    public void addBatch(AccountTemplate accountTemplate, BusSystem busSystem) throws Exception ;
    
    /**
     * 查询动态列
     * @param id
     * @return
     */
    public Collection<AccountField> findAccountFieldByIdVisable(Long id);

    /**
     * 删除模板报文权限
     * @param accountTemplate
     */
    public void delBatch(AccountTemplate accountTemplate);
    
    /**
     * 删除模板字段权限
     * @param accountTemplate
     */
    public void delFieldBatch(AccountTemplate accountTemplate);
    
    /**
     * 删除模板字段权限
     * @param accountFieldId
     */
    public void delOneFieldPermission(Long accountFieldId);
}
