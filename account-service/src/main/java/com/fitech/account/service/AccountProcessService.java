package com.fitech.account.service;


import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountProcess;
import com.fitech.domain.system.ProcessConfig;
import com.fitech.domain.system.User;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.vo.account.AccountProcessVo;
import org.springframework.data.domain.Page;

import java.util.Collection;
import java.util.List;

/**
 * 任务管理service
 * Created by wupei on 2017/3/1.
 */
public interface AccountProcessService {

    /**
     * 高级查询 分页
     *
     * @param where 查询条件
     * @return
     */
    public Page<AccountProcessVo> findPageAccountProcessList(AccountProcessVo where);
    
    public Page<AccountProcessVo> findPageAccounts(AccountProcessVo where);
    
    public String downLoadAccounts(String where);

    public Page<AccountProcessVo> findPagefindAssignedTask(AccountProcessVo vo);

    public AccountProcess findProcessById(Long id);

    /**
     * 根据用户查询此用户下的机构
     * @param userId 用户Id
     * @return
     */
    public Collection<User> findUserByInstitution(Long userId);

    /**
     * 流程开启
     * @param processConfig 流程配置对象
     * @param rpt 报文对象
     */
    public void processStart(ProcessConfig processConfig, Account rpt);

    /**
     * 提交任务
     * @param accountProcessVoList
     * @param action
     */
    public GenericResult<Boolean> submitProcess(List<AccountProcessVo> accountProcessVoList
            , String action,Long userId);

    public Boolean isMultiInstanceTaskExecOver(String proInstId);

    /**
     * 查询用户待办任务权限
     * @param userId
     * @return
     */
    public List<Long> queryAccountTaskPermission(Long userId);

}
