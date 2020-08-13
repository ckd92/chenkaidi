package com.fitech.account.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fitech.constant.LoggerUtill;
import com.fitech.system.annotation.AddCustomLog;
import com.fitech.system.annotation.AddOperateLogLast;
import com.fitech.system.annotation.MarkLog;
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
import com.fitech.framework.lang.common.AppException;
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
    @AddCustomLog(targetURI = "/accountTask/findPageAccountProcess", logType = LoggerUtill.LogType.OPERATE, logCategory = LoggerUtill.LogCategory.SELECT,
            includeCustomItems = {
            @AddCustomLog.CustomItem(category = "bulu", baseContent = "科融统计平台-数据处理-待办任务-台账补录-待办查询"),
            @AddCustomLog.CustomItem(category = "shenhe", baseContent = "科融统计平台-数据处理-待办任务-台账审核-待办查询")
    })
    @MarkLog
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
//            sysLogService.addOperateLogLast("科融统计平台-数据补录-数据处理-待办任务-查询",request,LoggerUtill.LogType.OPERATE);
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
    @AddOperateLogLast(targetURI = "/accountTask/findAssignedTask", baseContent = "科融统计平台-数据处理-已办任务-查询",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.SELECT)
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
//        	sysLogService.addOperateLogLast("科融统计平台-数据补录-数据处理-已办任务-查询",request,LoggerUtill.LogType.OPERATE);
        }
        return result;
    }
    
    /**
     * 数据查询数据查询(审核通过的台账)
     * @param accountProcessVo 高级查询
     * @return
     */
    @PostMapping("findPageAccounts")
    @AddOperateLogLast(targetURI = "/accountTask/findPageAccounts", baseContent = "科融统计平台-数据查询-数据查询-查询",
            logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.SELECT)
    public GenericResult<List<AccountProcessVo>> findCurrentAccounts(@RequestBody AccountProcessVo accountProcessVo,HttpServletRequest request){
        GenericResult<List<AccountProcessVo>> result = new GenericResult<>();
        try {
            Page page = new Page();
            page.setPageSize(accountProcessVo.getPageSize());
            page.setCurrentPage(accountProcessVo.getPageNum());
            TokenUtils.getLoginId(request);
            accountProcessVo.setUserId(TokenUtils.getLoginId(request));
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
    @AddCustomLog(targetURI = "/accountTask/submitProcess/", logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.UPDATE,
            includeCustomItems = {
            @AddCustomLog.CustomItem(category = "bulu", action = "commit", baseContent = "科融统计平台-数据处理-待办任务-台账补录-提交审核"),
            @AddCustomLog.CustomItem(category = "shenhe", action = "refuse", baseContent = "科融统计平台-数据处理-待办任务-台账审核-退回"),
            @AddCustomLog.CustomItem(category = "shenhe", action = "commit", baseContent = "科融统计平台-数据处理-待办任务-台账审核-提交审核"),
            @AddCustomLog.CustomItem(category = "shenhe", action = "approval", baseContent = "科融统计平台-数据处理-待办任务-台账审核-审核通过")
    })
    @MarkLog
    public GenericResult<Boolean> submitProcess(@RequestBody List<AccountProcessVo> accountProcessVoList, @PathVariable String action
            , HttpServletRequest request){
        GenericResult<Boolean> result=new GenericResult<>();
        try {
            //获取token
            Long userId = TokenUtils.getLoginId(request);
            result = accountProcessService.submitProcess(accountProcessVoList,action,userId);
        }catch (AppException e){
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage(e.getMessage());
            result.fail(ExceptionCode.SYSTEM_ERROR, "任务提交异常！");
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
            int count = accountReportService.startProcess(account);
            if(count>0){
            	result.setMessage("补录流程执行成功，并生成了【"+count+"】条代办事项");
            }else{
            	result.setMessage("没有可生成的补录代办任务！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
            sysLogService.addOperateLog("开启并指派一个新的任务",request);
        }
        return result;
    }
    /**
     * 开启本期
     * @param term
     * @param freq
     * @param request
     * @return
     */
    @PostMapping(value = "statisticsProcess/{term}/{freq}")
    public GenericResult<Object> startProcess(@PathVariable String term,@PathVariable String freq,HttpServletRequest request) {
    	GenericResult<Object> result = new GenericResult<Object>();
        try {
        	//计算当前期数和频度对应的实际日期
        	String freqName = freqService.findRepFreqByEtlFreqId(freq).getRepFreqName();
        	term = TermUtil.getDateByFreqAndTerm(freqName, term, true);
    		Account account = new Account();
            account.setTerm(term);
            account.setFreq(freqName);
            int count = accountReportService.startProcess(account);
            if(count>0){
            	result.setMessage("补录流程执行成功，并生成了【"+count+"】条代办事项");
            }else{
            	result.setMessage("没有可生成的补录代办任务！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
            sysLogService.addOperateLog("开启并指派一个新的任务",request);
        }
        return result;
    }
    
    /**
     * 开启上期
     * @param term
     * @param freq
     * @param request
     * @return
     */
    @PostMapping(value = "statisticsLastProcess/{term}/{freq}")
    public GenericResult<Object> statisticsLastProcess(@PathVariable String term,@PathVariable String freq,HttpServletRequest request) {
    	GenericResult<Object> result = new GenericResult<Object>();
        try {
        	//计算当前期数和频度对应的实际日期
        	String freqName = freqService.findRepFreqByEtlFreqId(freq).getRepFreqName();
        	term = TermUtil.getDateByFreqAndTerm(freqName, term, false);
    		Account account = new Account();
            account.setTerm(term);
            account.setFreq(freqName);
            int count = accountReportService.startProcess(account);
            if(count>0){
            	result.setMessage("补录流程执行成功，并生成了【"+count+"】条代办事项");
            }else{
            	result.setMessage("没有可生成的补录代办任务！");
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
            sysLogService.addOperateLog("开启并指派一个新的任务",request);
        }
        return result;
    }
    
    
    /**
     * ETL批量重报接口
     * @param term
     * @param type true表示当期，false表示上期
     * @param response
     * @param request
     */
    @SuppressWarnings("finally")
	@PostMapping("batchRepeatReport/{term}/{freq}/{type}")
    public GenericResult<Object> batchRepeatReport(@PathVariable String term,@PathVariable String freq,@PathVariable String type,HttpServletResponse response,HttpServletRequest request){
    	GenericResult<Object> result = new GenericResult<Object>();
        try {
         	String freqName = freqService.findRepFreqByEtlFreqId(freq).getRepFreqName();
        	term = TermUtil.getDateByFreqAndTerm(freqName, term, type.toLowerCase().equals("true")?true:false); 
        	Account account = new Account();
            account.setTerm(term);
            account.setFreq(freqName);
            accountReportService.batchRepeatReport(account);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
            sysLogService.addOperateLog("数据补录批量重报",request);
            return result;          
        }
    }
}
