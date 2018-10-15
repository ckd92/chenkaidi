package com.fitech.account.controller;

import com.fitech.account.service.AccountService;
import com.fitech.account.service.DictionaryItemService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountLine;
import com.fitech.domain.system.FieldPermission;
import com.fitech.framework.lang.common.CommonConst;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.FileUtil;
import com.fitech.framework.security.util.TokenUtils;
import com.fitech.system.service.FieldPermissionService;
import com.fitech.vo.account.AccountProcessVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Created by wangxw on 2017/8/17.
 */
@RestController
@RequestMapping("accountTask")
public class AccountController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private DictionaryItemService dictionaryItemService;
    @Autowired
    private FieldPermissionService fieldPermissionService;

    /**
     * 查询台账字段
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("Account/accountFields")
    public GenericResult<AccountProcessVo> findAccountFieldList(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request){
        GenericResult<AccountProcessVo> result=new GenericResult<>();
        try {
            result = accountService.initAccountTable(accountProcessVo);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }

    /**
     * 台账代办任务处理  - (数据列权限,列表和新增)
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("Account/accountdatas")
    public GenericResult<AccountProcessVo> findAccountDatas(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request){
        GenericResult<AccountProcessVo> result=new GenericResult<>();
        try {
        	accountProcessVo.setUserId(TokenUtils.getLoginId(request));
            result = accountService.findAccounDatas(accountProcessVo);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }
    /**
     * 台账代办任务处理  - (数据列权限,修改功能)
     * @param accountId	报文ID
     * @param id	台账行ID
     * @param response
     * @param request
     * @return
     */
    @GetMapping("Account/{accountId}/AccountLine/{id}")
    public GenericResult<AccountLine> findAccountDatas(@PathVariable Long accountId,@PathVariable Long id, HttpServletResponse response, HttpServletRequest request) {
        GenericResult<AccountLine> result=new GenericResult<>();
        try {
            result.setData(accountService.findAccountDatas(TokenUtils.getLoginId(request), accountId, id));
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }
    
    /**
    *
    * 新增单条明细
    * @param accountProcessVo
    * @param request
    * @return
    */
   @PostMapping("Account/accountdata")
   public GenericResult<Object> addAccountData(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request){
       GenericResult<Object> result = new GenericResult<>();
       try {
           List<String> data = new ArrayList<>();
           Long userId = TokenUtils.getLoginId(request);
           accountProcessVo.setUserId(userId);
           data = accountService.addAccountData(accountProcessVo);//校验结果。如果存在校验不通过的数据，则data不为空，返回校验不通过
           if (data.size() != 0) {
           	if("addAccountData pk is exist!".equals(data.get(0))){
           		result.setMessage("addAccountData pk is exist!");
           		result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
           	}
               result.setSuccess(false);
           }
           result.setData(data);
       }catch (Exception e){
           e.printStackTrace();
           result.setSuccess(false);
       }finally {
       }
       return result;
   }

   /**
    * 修改单条明细
    * @param accountProcessVo
    * @param request
    * @return
    */
   @PutMapping("Account/accountdata")
   public GenericResult<Object> updateAccountData(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request){
       GenericResult<Object> result = new GenericResult<>();
       try {
           List<String> data = new ArrayList<>();
           Long userId = TokenUtils.getLoginId(request);
           accountProcessVo.setUserId(userId);
           data = accountService.modifyAccountData(accountProcessVo); //校验结果。如果存在校验不通过的数据，则data不为空，返回校验不通过
           if (data.size() != 0) {
               result.setSuccess(false);
           }
           result.setData(data);
       }catch (Exception e){
           e.printStackTrace();
           result.setSuccess(false);
       }finally {
       }
       return result;
   }
    
    /**
     * 下载台账数据
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("Account/downloaddatas")
    public GenericResult<AccountProcessVo> downLoadPageAccountData(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request){
        GenericResult<AccountProcessVo> result=new GenericResult<>();
        try {
        	accountProcessVo.setUserId(TokenUtils.getLoginId(request));
            String filename = accountService.downLoadPageAccounData(accountProcessVo);
            if(filename.indexOf("/")!=-1){
            	String[] name = filename.split("/");
                filename=name[name.length-1];
            }else if(filename.indexOf("\\")!=-1){
            	String[] name = filename.split("\\\\");
                filename=name[name.length-1];
            }            
            result.setSuccess(true);
            result.setMessage(filename);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }
    /**
     * 高级查询台账数据、数据查询中的查看,不需要权限的字段查询(hx)
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("Account/accountdatastwo")
    public GenericResult<AccountProcessVo> findPageAccountDatatow(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request){
        GenericResult<AccountProcessVo> result=new GenericResult<>();
        try {
            result = accountService.findPageAccounDatatwo(accountProcessVo);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }

    /**
     * 批量更新台账数据
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PutMapping("Account/accountdatas")
    public GenericResult<Object> batchUpdateAccountData(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request){
        GenericResult<Object> result = new GenericResult<>();
        try {
            List<String> data = new ArrayList<>();
            Long userId = TokenUtils.getLoginId(request);
            accountProcessVo.setUserId(userId);
            data = accountService.batchUpdateAccounData(accountProcessVo);
            if (data.size() != 0) {
                result.setSuccess(false);
            }
            result.setData(data);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }

    

    /**
     * 全表校验
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("Account/validateAll")
    public GenericResult<Object> validateAll(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request){
        GenericResult<Object> result = new GenericResult<>();
        try {
            return accountService.validateAll(accountProcessVo);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }

    /**
     * 批量校验
     * @param idList :任务id
     * @param request
     * @return
     */
    @GetMapping("Account/validatePL")
    public GenericResult<Object> validatePl(@RequestParam("idList") List<Long> idList, HttpServletRequest request){
        GenericResult<Object> result = new GenericResult<>();
        try {
            Long userId = TokenUtils.getLoginId(request);
            return accountService.validatePL(idList,userId);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }

    @DeleteMapping("Account/{accountId}/AccountLine/{id}")
    public GenericResult<Boolean> deleteAccountDataById(@PathVariable Long accountId,@PathVariable Long id, HttpServletRequest request){
        GenericResult<Boolean> result=new GenericResult<>();
        try {
            Long userId = TokenUtils.getLoginId(request);
            AccountProcessVo accountProcessVo = new AccountProcessVo();
            accountProcessVo.setUserId(userId);
            Account account = new Account();
            account.setId(accountId);
            accountProcessVo.setAccount(account);
            AccountLine accountLine = new AccountLine();
            accountLine.setId(id);
            List<AccountLine> accountLines = new ArrayList<>();
            accountLines.add(accountLine);
            account.setAccountLines(accountLines);
            result = accountService.deleteAccountDataById(accountProcessVo);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }

    

    /**
     *下载模板到服务器，并返回fileName
     * @param response
     */
    @GetMapping("CreateTemplate/{accountId}")
    public GenericResult<Object> createTemplate(@PathVariable Long accountId, HttpServletResponse response,HttpServletRequest request) {
        try {
            GenericResult<Object> obj = new GenericResult<Object>();
            Long userId = TokenUtils.getLoginId(request);
            String fileName = this.accountService.generateAccountTemplate(accountId,userId);
            obj.setData(fileName);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 下载模板到本地
     * @param fileName
     * @param response
     * @param request
     */
    @GetMapping("AccountTemplate/{fileName}")
    public void downloadTemplate(@PathVariable String fileName, HttpServletResponse response,HttpServletRequest request) {
        try {
            fileName= CommonConst.getProperties("template_path")+fileName+".xlsx";
            File file = new File(fileName);
            FileUtil.downLoadFile(file, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 台账数据批量导入
     * @param file
     * @param accountId
     * @param operateFieldStr   待校验的字段
     * @param request
     * @return
     */
    @PostMapping("AccountTemplate/{accountId}/{operateFieldStr}/accountdatas")
    public GenericResult<Boolean> loadDataFromTemplate (@RequestParam(value = "file", required = true) MultipartFile file,
                                                       @PathVariable("accountId") Long accountId, @PathVariable("operateFieldStr") String  operateFieldStr,
                                                       HttpServletRequest request) {
    	GenericResult<Boolean> result=new GenericResult<>();
        try {
        	Long userId = TokenUtils.getLoginId(request);
            result =  accountService.loadDataByTemplate(file.getInputStream(), file.getOriginalFilename(), accountId,userId,operateFieldStr);
        } catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }
}


