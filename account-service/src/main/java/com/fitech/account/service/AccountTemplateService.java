package com.fitech.account.service;

import java.util.List;

import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.report.BusSystem;
import com.fitech.framework.lang.result.GenericResult;

/**
 * Created by wangxw on 2017/7/25.
 */
public interface AccountTemplateService {

    /**
     * 查询所有台账模板
     * @return
     */
    public List<AccountTemplate> findAll();
    
    /**
     * 根据id查询单个台账
     * @param id
     * @return
     */
    public AccountTemplate findById(Long id);

    /**
     * 保存台账模板
     * @param accountTemplate
     * @return
     */
    public GenericResult<Boolean> save(AccountTemplate accountTemplate);

    /**
     * 更新台账模板
     * @param accountTemplate
     * @return
     */
    public GenericResult<Boolean> update(AccountTemplate accountTemplate);

    /**
     * 批量删除台账模板
     * @param idList
     * @return
     */
    public GenericResult<Boolean> deleteBatch(List<Long> idList);


    /**
     * 验证台账名称是否存在
     * @param templateName
     * @return true:不存在；false:存在
     */
    public boolean valiAccountTemplateNameIsExist(String templateName);

    /**
     * 验证台账编号是否存在
     * @param templateCode
     * @return true:不存在；false:存在
     */
    public boolean valiAccountTemplateCodeIsExist(String templateCode);

    /**
     * 校验当前模板是否可删除
     * @param id
     * @return
     */
    public String valiAccountTemplateIsDelete(Long id);
    
    /**
     * 新增模板报文权限
     * @param accountTemplate
     * @param busSystem
     */
    public void addReportPermissions(AccountTemplate accountTemplate, BusSystem busSystem);
    /**
     * 删除模板报文权限
     * @param accountTemplate
     */
    public void delReportPermissions(AccountTemplate accountTemplate);
    
    /**
     * 清除原有台账，新增新台账
     * @param accountTemplate
     */
    public void createODSAccount(AccountTemplate accountTemplate);
}
