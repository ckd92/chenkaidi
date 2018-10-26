package com.fitech.account.service;

import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountEditLog;
import com.fitech.domain.account.AccountField;


import com.fitech.framework.lang.page.Page;

import java.util.Collection;
import java.util.List;

/**
 * Created by wangxw on 2017/8/24.
 */
public interface AccountEditLogService {

    public AccountEditLog saveAccoutnEditLog(Account account, Long userId, AccountEditLog accountEditLog);

	public void saveAccoutnEditLogItem(AccountEditLog accountEditLog
			, Collection<AccountField> accountFieldList,Account account);

	/**
	 * 修改痕迹查询
	 * @param accountEditLog
	 * @param page
	 * @return
	 */
	public List<AccountEditLog> findAccountEditLogByPage(AccountEditLog accountEditLog,Page page);
	/**
	 * 修改痕迹统计查询
	 * @param accountEditLog
	 * @param page
	 * @return
	 */
	public List<AccountEditLog> findAccountEditLogTJByPage(AccountEditLog accountEditLog,Page page);
	
//	public String downLoadEditLogsTJ(String where);

}
