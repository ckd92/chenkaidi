package com.fitech.account.service;

import java.util.List;
import java.util.Map;

import com.fitech.domain.account.Account;
import com.fitech.vo.account.AccountVo;

/**
 * Created by wangjianwei on 2017/8/10.
 */
public interface AccountsService {
	
	/**
	 * 动态查询列数据
	 * @param account
	 * @return
	 */
	public List<Map<String,Object>> findData(Account account);
	
	
	public List<Account> findAllAccount();
	
	
	public Account findAccountById(Long id);
	
	/**
	 * 任务统计
	 * @return
	 */
	public List<AccountVo> getrwtj() throws IllegalArgumentException, IllegalAccessException;
	
	/**
	 * 条件查询任务统计
	 * @param term
	 * @param institutionName
	 * @return
	 */
	public List getrwtjByCondition(Account account);
	
	/**
	 * 任务统计点击百分比显示所含台账信息
	 * @param accountState
	 * @param term
	 * @param institutionName
	 * @return
	 */
	public List<Account> findrwtjAccounts(Account account);
	
}
