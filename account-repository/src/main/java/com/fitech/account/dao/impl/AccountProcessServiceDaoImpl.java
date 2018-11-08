package com.fitech.account.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fitech.account.dao.AccountProcessServiceDao;
import com.fitech.framework.core.dao.mybatis.DaoMyBatis;
import com.fitech.framework.core.trace.ServiceTrace;

@Service
@ServiceTrace
public class AccountProcessServiceDaoImpl extends DaoMyBatis implements AccountProcessServiceDao {

	@Override
	public long isMultiInstanceTaskExecOver(String proInstId) {
		Map<String,String> map = new HashMap<String,String>();
		map.put("proInstId", proInstId);
		List<Long> list =  super.selectList("accProServiceMapper.isMultiInstanceTaskExecOver",map);
		long res = 0;
		if(list != null && list.size() > 0){
			res = list.get(0);
		}
		
		return res;
	}

	@Override
	public List<Long> getReceiverIdList(String term, String freq) {
		Map<String,String> map = new HashMap<String,String>();
		map.put("term", term);
		map.put("freq", freq);
		return super.selectList("accProServiceMapper.getReceiverIdList",map);
	}
	
}
