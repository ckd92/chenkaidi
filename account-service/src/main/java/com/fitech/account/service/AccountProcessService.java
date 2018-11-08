package com.fitech.account.service;


import java.util.List;


import com.fitech.domain.account.Account;
import com.fitech.domain.system.ProcessConfig;
import com.fitech.framework.lang.page.Page;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.vo.account.AccountProcessVo;

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
    public List<AccountProcessVo> findTodoTask(AccountProcessVo where, Page page);
    
    /**
     * 台账补录-已办任务查询
     * @param vo
     * @return
     */
    public List<AccountProcessVo> findDoneTask(AccountProcessVo vo, Page page);
    
    /**
     * 数据查询---数据查询的初始化和高级查询
     * @param where
     * @return
     */
    public List<AccountProcessVo> findPageAccounts(AccountProcessVo where,Page page);
    
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


    
    public List<Long> getReceiverIdList(String term,String freq);

}
