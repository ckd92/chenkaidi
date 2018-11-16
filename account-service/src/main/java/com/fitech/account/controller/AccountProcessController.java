package com.fitech.account.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitech.account.service.AccountProcessService;
import com.fitech.account.service.AccountReportService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Account;
import com.fitech.framework.lang.page.Page;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.security.util.TokenUtils;
import com.fitech.report.service.FreqService;
import com.fitech.system.service.SysLogService;
import com.fitech.system.util.TermUtil;
import com.fitech.vo.account.AccountProcessVo;


/**
 * 补录台账 - 流程有关的业务
 * Created by wupei on 2017/3/1.
 */
@RestController
@RequestMapping("accountTask")
public class AccountProcessController {
    @Autowired
    private AccountProcessService accountProcessService;
    @Autowired
    private SysLogService sysLogService;
    @Autowired
    private AccountReportService accountReportService;
    @Autowired
	private FreqService freqService;
    
    /**
     * 待办任务初始化分页
     * @param accountProcessVo 高级查询
     * @return
     */
    @PostMapping("findPageAccountProcess")
    public GenericResult<List<AccountProcessVo>> findTodoTask(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request){
        GenericResult<List<AccountProcessVo>> result = new GenericResult<>();
        try {
           // 获取token
            Long userId = TokenUtils.getLoginId(request);
            accountProcessVo.setUserId(userId);
            Page page = new Page();
            page.setCurrentPage(accountProcessVo.getPageNum());
            page.setPageSize(accountProcessVo.getPageSize());
            List<AccountProcessVo> pageVo = this.accountProcessService.findTodoTask(accountProcessVo,page);
            result.setData(pageVo);
            result.setPage(page);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
            sysLogService.addOperateLog("待办任务初始化分页",request);
        }
        return result;
    }
    
    /**
     * 已办任务查看
     * @param accountProcessVo
     * @param request
     * @return
     */
    @PostMapping("findAssignedTask")
    public GenericResult<List<AccountProcessVo>> findDoneTask(@RequestBody AccountProcessVo accountProcessVo,HttpServletRequest request){
        GenericResult<List<AccountProcessVo>> result = new GenericResult<>();
        try {
            //获取token
            Long userId = TokenUtils.getLoginId(request);
            accountProcessVo.setUserId(userId);
            Page page = new Page();
            page.setCurrentPage(accountProcessVo.getPageNum());
            page.setPageSize(accountProcessVo.getPageSize());
            List<AccountProcessVo> pageVo = this.accountProcessService.findDoneTask(accountProcessVo,page);
            result.setData(pageVo);
            result.setPage(page);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        }finally{
        	sysLogService.addOperateLog("已办任务查看",request);
        }
        return result;
    }
    
    /**
     * 数据查询数据查询(审核通过的台账)
     * @param accountProcessVo 高级查询
     * @return
     */
    @PostMapping("findPageAccounts")
    public GenericResult<List<AccountProcessVo>> findCurrentAccounts(@RequestBody AccountProcessVo accountProcessVo){
        GenericResult<List<AccountProcessVo>> result = new GenericResult<>();
        try {
            Page page = new Page();
            page.setPageSize(accountProcessVo.getPageSize());
            page.setCurrentPage(accountProcessVo.getPageNum());
            List<AccountProcessVo> pageVo = this.accountProcessService.findPageAccounts(accountProcessVo,page);
            result.setData(pageVo);
            result.setPage(page);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }

    /**
     * 台账补录，数据提交
     * @param accountProcessVoList
     * @param action
     * @param request
     * @return
     */
    @PostMapping("submitProcess/{action}")
    public GenericResult<Boolean> submitProcess(@RequestBody List<AccountProcessVo> accountProcessVoList, @PathVariable String action
            , HttpServletRequest request){
        GenericResult<Boolean> result=new GenericResult<>();
        try {
            //获取token
            Long userId = TokenUtils.getLoginId(request);
            result = accountProcessService.submitProcess(accountProcessVoList,action,userId);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
            result.fail(ExceptionCode.SYSTEM_ERROR, "任务提交异常！");
        }finally {
//            sysLogService.addOperateLog("待办任务提交"+accountProcessVo.getTaskId(),request);
        }
        return result;
    }

//    /**
//     * 转发
//     */
//    @PutMapping("SuperviseTask")
//    public GenericResult transmit(@RequestBody TaskVo taskVo,HttpServletRequest request){
//        GenericResult result = new GenericResult();
//        try {
//        	todoTaskService.setTaskAssignee(taskVo.getTaskId(), taskVo.getAssignee());
//        } catch (Exception e) {
//            e.printStackTrace();
//           result.setSuccess(false);
//        }finally {
//            sysLogService.addOperateLog("转发按钮按下",request);
//        }
//        return result;
//    }
    /**
     * 开启并指派一个新的任务
     *
     * @return
     */
    @PostMapping(value = "startProcess/{term}")
    public GenericResult<Object> startProcess(@PathVariable String term,HttpServletRequest request) {
    	GenericResult<Object> result = new GenericResult<Object>();
        try {
    		Account account = new Account();
            account.setTerm(term);
            accountReportService.startProcess(account);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
            sysLogService.addOperateLog("开启并指派一个新的任务",request);
        }
        return result;
    }
    @PostMapping(value = "statisticsProcess/{term}/{freq}")
    public GenericResult<Object> startProcess(@PathVariable String term,@PathVariable String freq,HttpServletRequest request) {
    	GenericResult<Object> result = new GenericResult<Object>();
        try {
        	//计算当前期数和频度对应的实际日期
        	String freqName = freqService.findRepFreqByEtlFreqId(freq).getRepFreqName();
        	term = TermUtil.getDateByFreqAndTerm(freqName, term, true);
    		Account account = new Account();
            account.setTerm(term);
            accountReportService.startProcess(account);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
            sysLogService.addOperateLog("开启并指派一个新的任务",request);
        }
        return result;
    }
}
