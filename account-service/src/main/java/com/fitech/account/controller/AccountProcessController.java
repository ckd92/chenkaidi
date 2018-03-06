package com.fitech.account.controller;

import com.fitech.account.service.AccountProcessService;
import com.fitech.account.service.AccountReportService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountProcess;
import com.fitech.framework.activiti.service.ProcessRegistryService;
import com.fitech.framework.activiti.service.TodoTaskService;
import com.fitech.framework.activiti.vo.TaskVo;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.FileUtil;
import com.fitech.framework.security.util.TokenUtils;
import com.fitech.system.service.ProcessConfigService;
import com.fitech.system.service.SysLogService;
import com.fitech.vo.account.AccountProcessVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


/**
 * 任务管理Controller
 * Created by wupei on 2017/3/1.
 */
@RestController
@RequestMapping("accountTask")
public class AccountProcessController {
    //task Service
    @Autowired
    private TodoTaskService todoTaskService;

    //明细service
    @Autowired
    private AccountProcessService accountProcessService;

    @Autowired
    private ProcessRegistryService fProcessRegistry;

    //流程配置service
    @Autowired
    private ProcessConfigService processConfigService;

    @Autowired
    private SysLogService sysLogService;

//    @Autowired
//    private SuperviseTaskService superviseTaskService;
    
    @Autowired
    private AccountReportService accountReportService;
    /**
     * 待办任务初始化分页
     * @param accountProcessVo 高级查询
     * @return
     */
    @PostMapping("findPageAccountProcess")
    public GenericResult<Page<AccountProcessVo>> findPageAccountProcessList(@RequestBody AccountProcessVo accountProcessVo, HttpServletRequest request){
        GenericResult<Page<AccountProcessVo>> result = new GenericResult<>();
        try {
           // 获取token
            Long userId = TokenUtils.getLoginId(request);
            accountProcessVo.setUserId(userId);
            Page<AccountProcessVo> pageVo = this.accountProcessService.findPageAccountProcessList(accountProcessVo);
            result.setData(pageVo);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
            sysLogService.addOperateLog("待办任务初始化分页",request);
        }
        return result;
    }
    
    /**
     * 数据查询数据查询(审核通过的台账)
     * @param accountProcessVo 高级查询
     * @return
     */
    @PostMapping("findPageAccounts")
    public GenericResult<Page<AccountProcessVo>> findPageAccounts(@RequestBody AccountProcessVo accountProcessVo){
        GenericResult<Page<AccountProcessVo>> result = new GenericResult<>();
        try {
            Page<AccountProcessVo> pageVo = this.accountProcessService.findPageAccounts(accountProcessVo);
            result.setData(pageVo);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }
    /**
     * 下载审核通过的台账
     * @param response
     */
    @GetMapping("DownLoadAccount/{searchs}")
    public void downloadTemplate(@PathVariable String searchs, HttpServletResponse response,HttpServletRequest request) {
        try {
            String fileName = this.accountProcessService.downLoadAccounts(searchs);
            File file = new File(fileName);
            FileUtil.downLoadFile(file, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping("findPageAccountProcess/test/{userId}")
    public GenericResult<Page<AccountProcessVo>> findPageAccountProcessList(@RequestBody AccountProcessVo accountProcessVo
                                                                            ,@PathVariable String userId,HttpServletRequest request){
        GenericResult<Page<AccountProcessVo>> result = new GenericResult<>();
        try {
            // 获取token
//            Long userId = TokenUtils.getLoginId(request);
            accountProcessVo.setUserId(Long.valueOf(userId));
            Page<AccountProcessVo> pageVo = this.accountProcessService.findPageAccountProcessList(accountProcessVo);
            result.setData(pageVo);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
            sysLogService.addOperateLog("待办任务初始化分页",request);
        }
        return result;
    }


    /**
     * 处理查看
     */
    @GetMapping("findNextNode/{taskId}")
    public GenericResult<TaskVo> findNextNode(@PathVariable String taskId,HttpServletRequest request){
    	GenericResult<TaskVo> result = new GenericResult<>();
        sysLogService.addOperateLog("处理按钮按下",request);
    	TaskVo task = todoTaskService.getTaskByTaskId(taskId);
        result.setData(task);
        return result;
    }

    /**
     * 流程图片查看
     */
    @GetMapping("findProcessImg/{procInstId}")
    public void findProcessImg(@PathVariable String procInstId,HttpServletResponse response,HttpServletRequest request){
        try {
            InputStream imageStream = fProcessRegistry.createProcImage(procInstId);
            OutputStream out = response.getOutputStream();
            byte[] b = new byte[1024];
            while (imageStream.read(b) != -1) {
                out.write(b);
            }
            imageStream.close();
            out.flush();
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            sysLogService.addOperateLog("流程图片查看",request);
        }
    }

    /**
     * 已办任务查看
     */
    @PostMapping("findAssignedTask")
    public GenericResult<Page<AccountProcessVo>> findPagefindAssignedTask(@RequestBody AccountProcessVo accountProcessVo,HttpServletRequest request){
        GenericResult<Page<AccountProcessVo>> result = new GenericResult<>();
        try {
            sysLogService.addOperateLog("已办任务查看",request);

            //获取token
            Long userId = TokenUtils.getLoginId(request);
            accountProcessVo.setUserId(userId);
            Page<AccountProcessVo> pageVo = this.accountProcessService.findPagefindAssignedTask(accountProcessVo);
            result.setData(pageVo);
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 转发
     */
    @PutMapping("SuperviseTask")
    public GenericResult transmit(@RequestBody TaskVo taskVo,HttpServletRequest request){
        GenericResult result = new GenericResult();
        try {
        	todoTaskService.setTaskAssignee(taskVo.getTaskId(), taskVo.getAssignee());
        } catch (Exception e) {
            e.printStackTrace();
           result.setSuccess(false);
        }finally {
            sysLogService.addOperateLog("转发按钮按下",request);
        }
        return result;
    }
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

    @GetMapping("accountProcess/{id}")
    public GenericResult<AccountProcess> findAccountProcessById(@PathVariable Long id, HttpServletRequest request){
        GenericResult<AccountProcess> result = new GenericResult<>();
        try {
            AccountProcess accountProcess = accountProcessService.findProcessById(id);
            result.setData(accountProcess);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }

    @GetMapping("accountProcess/accountTaskPermessions")
    public GenericResult<List<Long>> queryAccountTaskPermessions( HttpServletRequest request){
        GenericResult<List<Long>> result = new GenericResult<>();
        try {
            Long userId = TokenUtils.getLoginId(request);

            List<Long> permissions = accountProcessService.queryAccountTaskPermission(userId);
            result.setData(permissions);
        }catch (Exception e){
            e.printStackTrace();
            result.setSuccess(false);
        }finally {
        }
        return result;
    }
    
}
