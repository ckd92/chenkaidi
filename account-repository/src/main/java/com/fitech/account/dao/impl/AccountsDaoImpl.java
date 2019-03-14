package com.fitech.account.dao.impl;

import com.fitech.account.dao.AccountsDao;
import com.fitech.framework.core.dao.Dao;
import com.fitech.framework.core.dao.mybatis.DaoMyBatis;
import com.fitech.vo.account.AccountVo;

import java.util.List;
import java.util.Map;

@Dao("accountsDao")
public class AccountsDaoImpl extends DaoMyBatis implements AccountsDao {
    @Override
    public List<AccountVo> getrwtj() throws IllegalArgumentException, IllegalAccessException {
        return super.selectList("accountsMapper.getrwtj");
    }

    @Override
    public Long getAccountIdByTaskId(Long taskId) {
        return super.selectOne("accountsMapper.getAccountIdByTaskId",taskId);
    }

	@Override
	public List<Map<String, Object>> getrwtjByCondition(
			Map<String, String> tempMap) {
		return super.selectList("accountsMapper.getrwtjByCondition", tempMap);
	}

	@Override
	public List<Map<String, Object>> findrwtjAccounts(
			Map<String, String> tempMap) {
		return super.selectList("accountsMapper.findrwtjAccounts", tempMap);
	}
}
