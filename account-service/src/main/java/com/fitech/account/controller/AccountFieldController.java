package com.fitech.account.controller;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import com.fitech.constant.LoggerUtill;
import com.fitech.system.annotation.AddOperateLogLast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fitech.account.service.AccountFieldService;
import com.fitech.account.service.AccountTemplateService;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.system.FieldPermission;
import com.fitech.enums.system.OperationEnum;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.system.service.FieldPermissionService;
import com.fitech.vo.system.FieldPermissionVo;

@RestController
public class AccountFieldController {
	@Autowired
    private AccountTemplateService accountTemplateService;
	@Autowired
    private FieldPermissionService fieldPermissionService;
	@Autowired
	private AccountFieldService accountFieldService;
	
	
	/**
     * 获得可查看的字段
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/accountTemplate/accountField/{id}")
    public GenericResult<Collection<AccountField>> findAccountFieldById(@PathVariable("id") Long id,
                                                          HttpServletRequest request) {
    	GenericResult<Collection<AccountField>> result = new GenericResult<>();
        try {
        	Collection<AccountField> collection = accountFieldService.findVisableField(id);
            result.setData(collection);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        }finally {
        }
        return result;
    }
    
    /**
     * 模板字段新增
     * @param accountTemplate
     * @param request
     * @return
     */
    @PostMapping("/accountTemplateField")
    @AddOperateLogLast(targetURI = "/accountTemplateField", baseContent = "科融统计平台-业务设置-补录模版管理-字段配置-新增字段", logType = LoggerUtill.LogType.OPERATE)
    public GenericResult<Boolean> save(@RequestBody AccountTemplate accountTemplate,
                                                        HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountFieldService.save(accountTemplate);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }
    
    /**
     * 模板字段修改
     * @param accountTemplate
     * @param request
     * @return
     */
    @PutMapping("/accountTemplateField")
    @AddOperateLogLast(targetURI = "/accountTemplateField", baseContent = "科融统计平台-业务设置-补录模版管理-字段配置-模版字段修改", logType = LoggerUtill.LogType.OPERATE)
    public GenericResult<Boolean> modify(@RequestBody  AccountTemplate accountTemplate,
                                                        HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountFieldService.modify(accountTemplate);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    /**
     * 模板字段删除
     * @param accountTemplateId
     * @param accountFieldId
     * @param request
     * @return
     */
    @DeleteMapping("/accountTemplateField/{accountTemplateId}/{accountFieldId}")
    @AddOperateLogLast(targetURI = "/accountTemplateField/", baseContent = "科融统计平台-业务设置-补录模版管理-字段配置-模版字段删除", logType = LoggerUtill.LogType.OPERATE)
    public GenericResult<Boolean> remove(@PathVariable("accountTemplateId") Long accountTemplateId, 
    		@PathVariable("accountFieldId") Long accountFieldId, HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountFieldService.delete(accountTemplateId,accountFieldId);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    /**
     * 获取指定模板所有的-查询权限
     * @param accountTemplateId
     * @return
     */
    @GetMapping("AccountTemplate/{accountTemplateId}/FieldPermission/read")
    public GenericResult<Collection<FieldPermissionVo>> fieldReadPermission(@PathVariable("accountTemplateId") Long accountTemplateId){
    	GenericResult<Collection<FieldPermissionVo>> result=new GenericResult<Collection<FieldPermissionVo>>();
    	
    	AccountTemplate accountTemplate = new AccountTemplate();
        accountTemplate.setId(accountTemplateId);
        FieldPermission fieldPermission=new FieldPermission();
        fieldPermission.setOperationType(OperationEnum.LOOK);
        //所有的字段权限vo集合
        Collection<FieldPermissionVo> fieldPermissionVos=fieldPermissionService.findFieldAccountPermissionVo(accountTemplate,fieldPermission);
        result.setData(fieldPermissionVos);
        return result;
    }

    /**
     * 获取指定模板所有的-操作权限
     * @param accountTemplateId
     * @return
     */
    @GetMapping("AccountTemplate/{accountTemplateId}/FieldPermission/edit")
    public GenericResult<Collection<FieldPermissionVo>> fieldEditPermission(@PathVariable("accountTemplateId") Long accountTemplateId){
    	GenericResult<Collection<FieldPermissionVo>> result=new GenericResult<Collection<FieldPermissionVo>>();
    	
    	AccountTemplate accountTemplate = new AccountTemplate();
        accountTemplate.setId(accountTemplateId);
        FieldPermission fieldPermission=new FieldPermission();
        fieldPermission.setOperationType(OperationEnum.OPERATE);
        //所有的字段权限vo集合
        Collection<FieldPermissionVo> fieldPermissionVos=fieldPermissionService.findFieldAccountPermissionVo(accountTemplate,fieldPermission);
        result.setData(fieldPermissionVos);
        return result;
    }
}
