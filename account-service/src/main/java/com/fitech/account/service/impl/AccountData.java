package com.fitech.account.service.impl;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitech.account.repository.AccountRepository;
import com.fitech.domain.account.Account;
import com.fitech.enums.SubmitStateEnum;
import com.fitech.framework.activiti.lang.ProcessStartConst;

@Service
public class AccountData implements JavaDelegate{

	@Autowired
	private AccountRepository accountRepository;
	
	@Override
	public void execute(DelegateExecution execution) throws Exception {
		Account account = new Account();
		//获取accountId
		Long accountId = Long.parseLong(String.valueOf(execution.getVariable(ProcessStartConst.report_key)));
		account = accountRepository.findById(accountId);
		//修改状态为已上报成功
		account.setSubmitStateType(SubmitStateEnum.SUCCESS);
		accountRepository.save(account);
	}

}
