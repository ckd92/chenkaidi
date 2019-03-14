package com.fitech.account.dao;

import java.util.List;

import com.fitech.domain.account.AccountEditLog;
import com.fitech.framework.lang.page.Page;

public interface AccountEditLogDao {
	
	/**
	 * 分页查询修改痕迹数据
	 * @param accountEditLog
	 * @param page
	 * @return
	 */
	public List<AccountEditLog> findAccountEditLogByPage(AccountEditLog accountEditLog,Page page);
	
	/**
	 * 分页查询痕迹数据修改条数
	 * @param accountEditLog
	 * @param page
	 * @return
	 */
	public List<AccountEditLog> findAccountEditLogTJByPage(AccountEditLog accountEditLog,Page page);
}
