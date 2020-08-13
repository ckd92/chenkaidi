package com.fitech.account.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fitech.constant.LoggerUtill;
import com.fitech.domain.system.SubSystem;
import com.fitech.system.annotation.AddOperateLogLast;
import com.fitech.system.service.SubSystemService;
import org.apache.ibatis.annotations.Delete;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fitech.account.service.AccountService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountLine;
import com.fitech.framework.lang.common.CommonConst;
import com.fitech.framework.lang.page.Page;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.FileUtil;
import com.fitech.framework.security.util.TokenUtils;
import com.fitech.vo.account.AccountProcessVo;

/**
 * 
 * 补录台账 - 数据有关的业务
 * Created by wangxw on 2017/8/17.
 */
@RestController
@RequestMapping("accountTask")
public class AccountController {
    @Autowired
    private AccountService accountService;
    @Autowired
    private SubSystemService subSystemService;

    /**
     * 台账代办任务处理  - (数据列权限,列表和新增)
     *
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("Account/accountdatas")
    @AddOperateLogLast(targetURI = "/accountTask/Account/accountdatas", baseContent = "科融统计平台-数据处理-待办任务-查看报表内容",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.SELECT)
    public GenericResult<AccountProcessVo> findAccountDatas(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request) {
        GenericResult<AccountProcessVo> result = new GenericResult<>();
        try {
            Page page = new Page();
            page.setPageSize(accountProcessVo.getPageSize());
            page.setCurrentPage(accountProcessVo.getPageNum());
            accountProcessVo.setUserId(TokenUtils.getLoginId(request));
            result = accountService.findAccounDatas(accountProcessVo, page);
            result.setPage(page);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 台账代办任务处理  - (数据列权限,修改功能)
     *
     * @param accountId 报文ID
     * @param id        台账行ID
     * @param response
     * @param request
     * @return
     */
    @GetMapping("Account/{accountId}/AccountLine/{id}")
    @AddOperateLogLast(targetURI = "/acountTask/Account/accountdatas", baseContent = "科融统计平台-数据处理-待办任务-处理-查询待办处理",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.SELECT)
    public GenericResult<AccountLine> findAccountDatas(@PathVariable Long accountId, @PathVariable Long id, HttpServletResponse response, HttpServletRequest request) {
        GenericResult<AccountLine> result = new GenericResult<>();
        try {
            result.setData(accountService.findAccountDatas(TokenUtils.getLoginId(request), accountId, id));
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 台账数据查询  ,无权限的字段查询(hx)
     *
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("Account/accountdatastwo")
    @AddOperateLogLast(targetURI = "/accountTask/Account/accountdatastwo", baseContent = "科融统计平台-数据明细查询",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.SELECT)
    public GenericResult<AccountProcessVo> findAccountDatatwo(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request) {
        GenericResult<AccountProcessVo> result = new GenericResult<>();
        try {
            Page page = new Page();
            page.setPageSize(accountProcessVo.getPageSize());
            page.setCurrentPage(accountProcessVo.getPageNum());
            result.setData(accountService.findAccountDatatwo(accountProcessVo, page));
            result.setPage(page);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 新增单条明细
     *
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("Account/accountdata")
    @AddOperateLogLast(targetURI = "/accountTask/Account/accountdata", baseContent = "科融统计平台-数据处理-待办任务-处理-新增-保存",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.ADD)
    public GenericResult<Object> addAccountData(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request) {
        GenericResult<Object> result = new GenericResult<>();
        try {
            Long userId = TokenUtils.getLoginId(request);
            accountProcessVo.setUserId(userId);
            List<String> data = accountService.addAccountData(accountProcessVo);//校验结果。如果存在校验不通过的数据，则data不为空，返回校验不通过
            if (data.size() != 0) {
                if ("addAccountData pk is exist!".equals(data.get(0))) {
                    result.setMessage("addAccountData pk is exist!");
                    result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
                }
                result.setSuccess(false);
            }
            result.setData(data);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 修改单条明细
     *
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PutMapping("Account/accountdata")
    @AddOperateLogLast(targetURI = "/accountTask/Account/accountdata", baseContent = "科融统计平台-数据处理-待办任务-处理-修改",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.UPDATE)
    public GenericResult<Object> updateAccountData(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request) {
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
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 批量更新台账数据
     *
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PutMapping("Account/accountdatas")
    @AddOperateLogLast(targetURI = "/accountTask/Account/accountdatas", baseContent = "科融统计平台-数据处理-待办任务-处理-批量数据补录",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.UPDATE)
    public GenericResult<Object> updateBatchAccountData(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request) {
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
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 删除补录台账数据
     *
     * @param accountId
     * @param id
     * @param request
     * @return
     */
    @DeleteMapping("Account/{accountId}/AccountLine/{id}")
    @AddOperateLogLast(targetURI = "accountTask/Account//AccountLine/", baseContent = "科融统计平台-数据处理-待办任务-处理-删除",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.DELETE)
    public GenericResult<Boolean> deleteAccountData(@PathVariable Long accountId, @PathVariable Long id, HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
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
            result = accountService.deleteAccountData(accountProcessVo);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 下载模板到本地，并返回fileName
     *
     * @param accountId 台账ID
     * @param response
     * @param request
     * @return
     */
    @GetMapping("CreateTemplate/{accountId}")
    @AddOperateLogLast(targetURI = "/accountTask/CreateTemplate/", baseContent = "科融统计平台-数据处理-待办任务-处理-模版下载",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.SELECT)
    public GenericResult<Object> createTemplate(@PathVariable Long accountId, HttpServletResponse response, HttpServletRequest request) {
        try {
            GenericResult<Object> obj = new GenericResult<Object>();
            Long userId = TokenUtils.getLoginId(request);
            String fileName = this.accountService.generateAccountTemplate(accountId, userId);
            obj.setData(fileName);
            return obj;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 台账数据装载-全量批量导入
     *
     * @param file
     * @param accountId
     * @param operateFieldStr 待校验的字段
     * @param request
     * @return
     */
    @PostMapping("AccountTemplate/{accountId}/{operateFieldStr}/accountdatas")
    public GenericResult<Boolean> loadDataFromTemplate(@RequestParam(value = "file", required = true) MultipartFile file,
                                                       @PathVariable("accountId") Long accountId, @PathVariable("operateFieldStr") String operateFieldStr,
                                                       HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            Long userId = TokenUtils.getLoginId(request);
            result = accountService.loadDataByTemplate(file.getInputStream(), file.getOriginalFilename(), accountId, userId, operateFieldStr);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 下载台账数据(先生成到服务器) - EXCEL导出
     *
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("Account/downloaddatas")
    @AddOperateLogLast(targetURI = "/accountTask/Account/downloaddatas", baseContent = "科融统计平台-数据明细-下载导出",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.SELECT)
    public GenericResult<AccountProcessVo> createData(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request) {
        GenericResult<AccountProcessVo> result = new GenericResult<>();
        try {
            accountProcessVo.setUserId(TokenUtils.getLoginId(request));
            String filename = accountService.downLoadAccounData(accountProcessVo);
            if (filename.indexOf("/") != -1) {
                String[] name = filename.split("/");
                filename = name[name.length - 1];
                filename = "temp|AccountList|"+filename;
            } else if (filename.indexOf("\\") != -1) {
                String[] name = filename.split("\\\\");
                filename = name[name.length - 1];
                filename = "temp|AccountList|"+filename;
            }
            result.setSuccess(true);
            result.setMessage(filename);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 下载模板到本地
     *
     * @param fileName
     * @param response
     * @param request
     */
    @GetMapping("AccountTemplate/{fileName}")
    public void downloadDatas(@PathVariable String fileName, HttpServletResponse response, HttpServletRequest request) {
        try {
            if (fileName.contains("|")){
                fileName = fileName.replace("|","/");
            }
            fileName = CommonConst.getProperties("basePath") + fileName + ".xlsx";
            File file = new File(fileName);
            FileUtil.downLoadFile(file, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 全表校验
     *
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("Account/validateAll")
    @AddOperateLogLast(targetURI = "/accountTask/Account/validateAll", baseContent = "科融统计平台-数据处理-待办任务-处理-全表校验",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.SELECT)
    public GenericResult<Object> validateAll(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request) {
        GenericResult<Object> result = new GenericResult<>();
        try {
            return accountService.validateAll(accountProcessVo);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 批量校验
     *
     * @param idList  :任务id
     * @param request
     * @return
     */
    @GetMapping("Account/validatePL")
    @AddOperateLogLast(targetURI = "/accountTask/Account/validatePL", baseContent = "科融统计平台-数据处理-待办任务-台账审核-批量校验",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.SELECT)
    public GenericResult<Object> validatePl(@RequestParam("idList") List<Long> idList, HttpServletRequest request) {
        GenericResult<Object> result = new GenericResult<>();
        try {
            Long userId = TokenUtils.getLoginId(request);
            return accountService.validatePL(idList, userId);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    /**
     * 批量删除补录台账数据
     *
     * @param list
     * @param request
     */
    @PostMapping("/BatchDelete")
    @AddOperateLogLast(targetURI = "/accountTask/BatchDelete", baseContent = "科融统计平台-数据处理-待办任务-处理-批量删除",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.DELETE)
    public GenericResult<Object> batchDeleteAccountData(@RequestBody List<AccountProcessVo> list, HttpServletRequest request) {
        GenericResult<Object> genericResult = new GenericResult<>();
        List<Object> arrayList = new ArrayList<>();
        try {
            Long userId = TokenUtils.getLoginId(request);
            for (AccountProcessVo accountProcessVo : list) {
                accountProcessVo.setUserId(userId);
                Account account = new Account();
                account.setId(Long.parseLong(accountProcessVo.getReportId()));
                AccountLine accountLine = new AccountLine();
                accountLine.setId(Long.parseLong(accountProcessVo.getId()));
                List<AccountLine> accountLines = new ArrayList<AccountLine>();
                accountLines.add(accountLine);
                account.setAccountLines(accountLines);
                accountProcessVo.setAccount(account);
                GenericResult<Boolean> result = accountService.deleteAccountData(accountProcessVo);
                arrayList.add(result);
            }
            for (Object o : arrayList) {
                GenericResult<Boolean> result = (GenericResult<Boolean>) o;
                if (result.isSuccess()) {
                    genericResult.setSuccess(true);
                    genericResult.setMessage("删除成功");
                } else {
                    genericResult.setSuccess(false);
                    genericResult.setMessage(result.getMessage());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            genericResult.setSuccess(false);
        } finally {
        }
        return genericResult;
    }

    //删除所有数据
    @DeleteMapping("/deleteAllData/{reportId}")
    public GenericResult deleteAllLedgerLine(@PathVariable Long reportId, HttpServletRequest request) {
        GenericResult result = new GenericResult<>();
        try {
            result = accountService.deleteAllAccountData(reportId);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        } finally {
        }
        return result;
    }

    @PostMapping("/downLoadBusPackage/{busSystemId}")
    public GenericResult<Object> downLoadBusPackage(@PathVariable("busSystemId") String busSystemId) {
        GenericResult<Object> result = new GenericResult<Object>();
        try {
            String msg = accountService.downLoadBusPackage(busSystemId);
            result.setMessage(msg);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        }
        return result;
    }
}


