package com.fitech.account.dao;

import java.util.List;
import java.util.Map;

import com.fitech.domain.ledger.LedgerReportTemplate;
import com.fitech.framework.lang.result.GenericResult;
import org.apache.poi.ss.usermodel.Sheet;

import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountTemplate;

public interface AccountDatasDao {
	
	/**
	 * 载入数据，删除原有数据，新增添加的数据。
	 * @param accountId
	 * @param accountTemplate
	 * @param sheet
	 * @param account
	 * @return
	 */
	public List<String> loadDataByTemplate(Long accountId, AccountTemplate accountTemplate, Sheet sheet,Account account);

	/**
	 * 载入数据，覆盖主键重复，新增不重复数据
	 * @param accountId
	 * @param accountTemplate
	 * @param sheet
	 * @param account
	 * @return
	 */
	public Map<String,Object> loadDataByExcel(Long accountId, AccountTemplate accountTemplate, Sheet sheet, Account account);

}
