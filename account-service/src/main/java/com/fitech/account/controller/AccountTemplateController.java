package com.fitech.account.controller;

import com.fitech.account.service.AccountTemplateService;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.report.BusSystem;
import com.fitech.domain.system.FieldPermission;
import com.fitech.enums.system.OperationEnum;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.system.service.FieldPermissionService;
import com.fitech.vo.system.FieldPermissionVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

import java.util.Collection;
import java.util.List;

@RestController
public class AccountTemplateController {

    @Autowired
    private AccountTemplateService accountTemplateService;

    @Autowired
    private FieldPermissionService fieldPermissionService;

    /*根据报文查询所有的字段权限*/
    @GetMapping("AccountTemplate/{accountTemplateId}/FieldPermission/read")
    public GenericResult<Collection<FieldPermissionVo>> findFieldPermission(@PathVariable("accountTemplateId") Long accountTemplateId){
        AccountTemplate accountTemplate = new AccountTemplate();
        accountTemplate.setId(accountTemplateId);
        FieldPermission fieldPermission=new FieldPermission();
        fieldPermission.setOperationType(OperationEnum.LOOK);
        //所有的字段权限vo集合
        Collection<FieldPermissionVo> fieldPermissionVos=fieldPermissionService.findFieldAccountPermissionVo(accountTemplate,fieldPermission);
        GenericResult<Collection<FieldPermissionVo>> result=new GenericResult<Collection<FieldPermissionVo>>();
        result.setData(fieldPermissionVos);
        return result;
    }

    /*根据报文查询所有的字段权限*/
    @GetMapping("AccountTemplate/{accountTemplateId}/FieldPermission/edit")
    public GenericResult<Collection<FieldPermissionVo>> findeditFieldPermission(@PathVariable("accountTemplateId") Long accountTemplateId){
        AccountTemplate accountTemplate = new AccountTemplate();
        accountTemplate.setId(accountTemplateId);
        FieldPermission fieldPermission=new FieldPermission();
        fieldPermission.setOperationType(OperationEnum.OPERATE);
        //所有的字段权限vo集合
        Collection<FieldPermissionVo> fieldPermissionVos=fieldPermissionService.findFieldAccountPermissionVo(accountTemplate,fieldPermission);
        GenericResult<Collection<FieldPermissionVo>> result=new GenericResult<Collection<FieldPermissionVo>>();
        result.setData(fieldPermissionVos);
        return result;
    }

    @GetMapping("/ReportSubSystem/BusSystem/{id}/AccountTemplate")
    public GenericResult<Collection<AccountTemplate>> findByBusSystem(@PathVariable Long id, HttpServletRequest request) {
        GenericResult<Collection<AccountTemplate>> result = new GenericResult<>();
        try {
            BusSystem busSystem = new BusSystem();
            busSystem.setId(id);
            Collection<AccountTemplate> collection = accountTemplateService.findByBusSystem(busSystem);
            result.setData(collection);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    @PostMapping("/accountTemplates")
    public GenericResult<Page<AccountTemplate>> findAll(@RequestBody  AccountTemplate accountTemplate,
                                                        HttpServletRequest request) {
        GenericResult<Page<AccountTemplate>> result = new GenericResult<>();
        try {
            Page<AccountTemplate> collection = accountTemplateService.findAccountTemplateByPage(accountTemplate);
            result.setData(collection);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    @PostMapping("/accountTemplate")
    public GenericResult<Boolean> saveAccountTemplate(@RequestBody  AccountTemplate accountTemplate,
                                                        HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountTemplateService.saveAccountTemplate(accountTemplate);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    @PutMapping("/accountTemplate")
    public GenericResult<Boolean> updateAccountTemplate(@RequestBody  AccountTemplate accountTemplate,
                                                      HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountTemplateService.updateAccountTemplate(accountTemplate);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    @DeleteMapping("/accountTemplates")
    public GenericResult<Boolean> deleteAccountTemplate(@RequestParam("idList") List<Long> idList,
                                                        HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountTemplateService.deleteAccountTemplateByList(idList);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    @GetMapping("/accountTemplate/templateName/{templateName}")
    public GenericResult<Boolean> valiAccountTemplateName(@PathVariable("templateName") String templateName,
                                                        HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountTemplateService.valiAccountTemplateNameIsExist(templateName);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    @GetMapping("/accountTemplate/templateCode/{templateCode}")
    public GenericResult<Boolean> valiAccountTemplateCode(@PathVariable("templateCode") String templateCode,
                                                          HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountTemplateService.valiAccountTemplateCodeIsExist(templateCode);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    @GetMapping("/accountTemplate/{id}")
    public GenericResult<AccountTemplate> findAccoutTemplateById(@PathVariable("id") Long id,
                                                          HttpServletRequest request) {
        GenericResult<AccountTemplate> result = new GenericResult<>();
        try {
            AccountTemplate accountTemplate = accountTemplateService.findAccountTemplateById(id);
            result.setData(accountTemplate);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }
    
    /*
     * 获得动态列
     */
    @GetMapping("/accountTemplate/accountField/{id}")
    public GenericResult<Collection<AccountField>> findAccountFieldById(@PathVariable("id") Long id,
                                                          HttpServletRequest request) {
    	GenericResult<Collection<AccountField>> result = new GenericResult<>();
        try {
        	Collection<AccountField> collection = accountTemplateService.findAccountFieldByIdVisable(id);
          
            result.setData(collection);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        }finally {
        }
        return result;
    }
    
    @GetMapping("/accountTemplates")
    public GenericResult<List<AccountTemplate>> findAll(HttpServletRequest request) {
        GenericResult<List<AccountTemplate>> result = new GenericResult<>();
        try {
            List<AccountTemplate> collection = accountTemplateService.findAllAccountTemplate();
            result.setData(collection);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }
    
    /*
     * 模板字段新增
     */
    @PostMapping("/accountTemplateField")
    public GenericResult<Boolean> addAccountTemplateField(@RequestBody  AccountTemplate accountTemplate,
                                                        HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountTemplateService.addAccountTemplateField(accountTemplate);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }
    
    /*
     * 模板字段修改
     */
    @PutMapping("/accountTemplateField")
    public GenericResult<Boolean> modifyAccountTemplateField(@RequestBody  AccountTemplate accountTemplate,
                                                        HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountTemplateService.modifyAccountTemplateField(accountTemplate);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    /*
     * 模板字段删除
     */
    @DeleteMapping("/accountTemplateField/{accountTemplateId}/{accountFieldId}")
    public GenericResult<Boolean> delAccountTemplateField(@PathVariable("accountTemplateId") Long accountTemplateId, 
    		@PathVariable("accountFieldId") Long accountFieldId, HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = accountTemplateService.deleteAccountTemplateField(accountTemplateId,accountFieldId);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

}
