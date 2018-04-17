package com.fitech.account.service.impl;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitech.account.repository.AccountTemplateRepository;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.report.BusSystem;
import com.fitech.system.service.AccountModelService;

/**
 * 补录系统模块外部对接接口实现类
 * @author chengrui
 *
 */
@Service
public class AccountModelServiceImpl implements AccountModelService {

	@Autowired
	private AccountTemplateRepository accountTemplateRepository;
	
	
	@Override
	public Collection<AccountTemplate> findByBusSystem(BusSystem busSystem) {
		return accountTemplateRepository.findByBusSystem(busSystem);
	}

	@Override
	public AccountTemplate findAccountTemplate(Long id) {
		return accountTemplateRepository.findOne(id);
	}

}
