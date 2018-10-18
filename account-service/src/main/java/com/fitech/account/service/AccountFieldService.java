package com.fitech.account.service;

import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.system.FieldPermission;
import com.fitech.framework.lang.result.GenericResult;

import java.util.Collection;

/**
 * Created by wangxw on 2017/8/10.
 */
public interface AccountFieldService {
	/**
     * 增加字段
     * @param accountTemplate
     * @return
     */
    public GenericResult<Boolean> save(AccountTemplate accountTemplate);
    
    /**
     * 修改字段
     * @param accountTemplate
     * @return
     */
    public GenericResult<Boolean> modify(AccountTemplate accountTemplate);
    
    /**
     * 删除字段
     * @param accountTemplate
     * @return
     */
    public GenericResult<Boolean> delete(Long accountTemplateId, Long accountFieldId);
    
    /**
     * 删除台账下的所有字段
     * @param accountTemplate
     */
    public void deleteAccountFields(AccountTemplate accountTemplate);
    
    /**
     * 查询可查看的台账字段-动态列
     * @param id 模板ID
     * @return
     */
    public Collection<AccountField> findVisableField(Long id);
    
    /**
     * 创建台账模板字段实例（字段类型）
     * @param ledgerItems
     * @return
     */
    public Collection<AccountField> convertAccountField(Collection<AccountField> ledgerItems);
    
    /**
     * 
     * @param accountTemplate
     * @return
     */
    public Collection<FieldPermission> addFieldPermessions(AccountTemplate accountTemplate);
    
    /**
     * 删除模板字段权限
     * @param accountTemplate
     */
    public void delFieldPermessions(AccountTemplate accountTemplate);
    
}
