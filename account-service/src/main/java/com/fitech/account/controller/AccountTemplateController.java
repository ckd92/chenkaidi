package com.fitech.account.controller;

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
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.framework.lang.result.GenericResult;

@RestController
public class AccountTemplateController {
    @Autowired
    private AccountTemplateService accountTemplateService;

    /**
     * 补录模板管理 - 模板列表查询
     * @param accountTemplate
     * @param request
     * @return
     */
    @GetMapping("/accountTemplates")
    public GenericResult<List<AccountTemplate>> findAll(HttpServletRequest request) {
        GenericResult<List<AccountTemplate>> result = new GenericResult<>();
        try {
            List<AccountTemplate> collection = accountTemplateService.findAll();
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
            AccountTemplate accountTemplate = accountTemplateService.findById(id);
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
            result = accountTemplateService.save(accountTemplate);
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
            result = accountTemplateService.update(accountTemplate);
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
            result = accountTemplateService.deleteBatch(idList);
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
            if(!accountTemplateService.valiAccountTemplateNameIsExist(templateName)){
            	result.fail(ExceptionCode.ONLY_VALIDATION_FALSE);
            }
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
            if(!accountTemplateService.valiAccountTemplateCodeIsExist(templateCode)){
            	result.fail(ExceptionCode.ONLY_VALIDATION_FALSE);
            }
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }
    
    
}
