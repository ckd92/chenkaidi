package com.fitech.account.dao;

import java.util.List;


public interface AccountProcessServiceDao {
	public long isMultiInstanceTaskExecOver(String proInstId);
	public List<Long> getReceiverIdList(String term, String freq);
}
