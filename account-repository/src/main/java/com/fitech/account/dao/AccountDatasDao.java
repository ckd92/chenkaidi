package com.fitech.account.dao;

import java.util.List;

import org.apache.poi.ss.usermodel.Sheet;

import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountTemplate;

public interface AccountDatasDao {
	
	/**
	 * 
	 * @param accountId
	 * @param accountTemplate
	 * @param sheet
	 * @param account
	 * @return
	 */
	public List<String> loadDataByTemplate(Long accountId, AccountTemplate accountTemplate, Sheet sheet,Account account);
}
