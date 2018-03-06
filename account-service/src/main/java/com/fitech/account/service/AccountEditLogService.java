package com.fitech.account.service;

import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountEditLog;
import com.fitech.domain.account.AccountField;
import org.springframework.data.domain.Page;

import java.util.Collection;

/**
 * Created by wangxw on 2017/8/24.
 */
public interface AccountEditLogService {

    public AccountEditLog saveAccoutnEditLog(Account account, Long userId, AccountEditLog accountEditLog);

	public void saveAccoutnEditLogItem(AccountEditLog accountEditLog
			, Collection<AccountField> accountFieldList,Account account);

	/**
	 * 多条件查询修改痕迹
	 * @param accountEditLog
	 * @return
	 */
	public Page<AccountEditLog> findAccountEditLog(AccountEditLog accountEditLog);

	public Page<AccountEditLog> findAccountEditLogByPage(AccountEditLog accountEditLog);
	
	public Page<AccountEditLog> findAccountEditLogTJByPage(AccountEditLog accountEditLog);
	
	public String downLoadEditLogsTJ(String where);

	public Integer findAccountEditLogByPageCount(AccountEditLog accountEditLog);
}
