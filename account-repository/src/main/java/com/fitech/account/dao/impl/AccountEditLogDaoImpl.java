package com.fitech.account.dao.impl;

import java.util.List;

import com.fitech.account.dao.AccountEditLogDao;
import com.fitech.domain.account.AccountEditLog;
import com.fitech.framework.core.dao.Dao;
import com.fitech.framework.core.dao.mybatis.DaoMyBatis;
import com.fitech.framework.lang.page.Page;

@Dao
public class AccountEditLogDaoImpl extends DaoMyBatis implements AccountEditLogDao {

	@Override
	public List<AccountEditLog> findAccountEditLogByPage(
			AccountEditLog accountEditLog, Page page) {
		return super.selectByPage("accountEditLogMapper.findAccountEditLogCount", "accountEditLogMapper.findAccountEditLog", accountEditLog, page);
	}

	@Override
	public List<AccountEditLog> findAccountEditLogTJByPage(
			AccountEditLog accountEditLog, Page page) {
		return super.selectByPage("accountEditLogMapper.findAccountEditLogTJCount", "accountEditLogMapper.findAccountEditLogTJ", accountEditLog, page);
	}
}
