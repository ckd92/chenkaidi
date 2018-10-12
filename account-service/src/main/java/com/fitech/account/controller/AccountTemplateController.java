package com.fitech.account.controller;

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fitech.account.service.AccountTemplateService;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.report.BusSystem;
import com.fitech.framework.lang.result.GenericResult;

@RestController
public class AccountTemplateController {
    @Autowired
    private AccountTemplateService accountTemplateService;

    

    /**
     * 获取制度下所有补录模板
     * @param id
     * @param request
     * @return
     */
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
    /**
     * 高级查询
     * @param accountTemplate
     * @param request
     * @return
     */
    @PostMapping("/accountTemplates")
    public GenericResult<List<AccountTemplate>> findAll(@RequestBody  AccountTemplate accountTemplate,
                                                        HttpServletRequest request) {
        GenericResult<List<AccountTemplate>> result = new GenericResult<>();
        try {
            List<AccountTemplate> list = accountTemplateService.findAllAccountTemplate();
            result.setData(list);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
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
    @GetMapping("/accountTemplate/{id}")
    public GenericResult<AccountTemplate> findById(@PathVariable("id") Long id,
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
    

    /**
     * 新增补录模板
     * @param accountTemplate
     * @param request
     * @return
     */
    @PostMapping("/accountTemplate")
    public GenericResult<Boolean> save(@RequestBody AccountTemplate accountTemplate,HttpServletRequest request) {
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
    /**
     * 修改补录模板
     * @param accountTemplate
     * @param request
     * @return
     */
    @PutMapping("/accountTemplate")
    public GenericResult<Boolean> modity(@RequestBody AccountTemplate accountTemplate,HttpServletRequest request) {
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
    /**
     * 删除补录模板
     * @param idList
     * @param request
     * @return
     */
    @DeleteMapping("/accountTemplates")
    public GenericResult<Boolean> remove(@RequestParam("idList") List<Long> idList,HttpServletRequest request) {
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
    
    /**
     * 校验模板名称是否重复
     * @param templateName
     * @param request
     * @return
     */
    @GetMapping("/accountTemplate/templateName/{templateName}")
    public GenericResult<Boolean> validateTemplateName(@PathVariable("templateName") String templateName,
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
    /**
     * 校验模板编号是否重复
     * @param templateCode
     * @param request
     * @return
     */
    @GetMapping("/accountTemplate/templateCode/{templateCode}")
    public GenericResult<Boolean> validateTemplateCode(@PathVariable("templateCode") String templateCode,
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
    
    
}
