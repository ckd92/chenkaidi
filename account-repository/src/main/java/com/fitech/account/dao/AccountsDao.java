package com.fitech.account.dao;

import com.fitech.vo.account.AccountVo;

import java.util.List;
import java.util.Map;

public interface AccountsDao {
    /**
     * 任务统计
     * @return
     */
    public List<AccountVo> getrwtj() throws IllegalArgumentException, IllegalAccessException;

    /**
     * 根据taskid查找accountid
     * @param taskId
     * @return
     */
    public Long getAccountIdByTaskId(Long taskId);
    
    public List<Map<String,Object>> getrwtjByCondition(Map<String,Object> tempMap);
    
    
    public List<Map<String,Object>> findrwtjAccounts(Map<String,String> tempMap);
}
