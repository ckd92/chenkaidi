package com.fitech.account.dao;

import java.util.List;


public interface AccountProcessServiceDao {
	public long isMultiInstanceTaskExecOver(String proInstId);
//	public List<Long> getReceiverIdList(String term, String freq);

	/**
	 * 根据模板ID获取所有代办
	 * @param templateId
	 * @return
	 */
	public List<Long> findReportIdByTemplateId(Long templateId);
}
