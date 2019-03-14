package com.fitech.account.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitech.account.repository.AccountProcessRepository;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountProcess;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.report.external.RAccountModelService;

@Service
@ServiceTrace
public class RAccountModelServiceImpl implements RAccountModelService {
	@Autowired
	private AccountProcessRepository accountProcessRepository;

	@Override
	public void saveAccountProcess(AccountProcess accountProcess) {
		accountProcessRepository.save(accountProcess);
	}

	@Override
	public AccountProcess findAccountProcess(Long processId) {
		return accountProcessRepository.findOne(processId);
	}

	@Override
	public AccountProcess findAccountProcess(Account account) {
		return accountProcessRepository.findByAccount(account);
	}

}
