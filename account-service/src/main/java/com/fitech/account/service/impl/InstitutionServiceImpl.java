package com.fitech.account.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitech.account.service.InstitutionService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.system.Institution;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.system.repository.InstitutionRepository;

@Service
@ServiceTrace
public class InstitutionServiceImpl implements InstitutionService {

	@Autowired
	private InstitutionRepository institutionRepository;
	
	@Override
	public List<Institution> getAllInstitution() {
		try{
			return institutionRepository.findAll();
		}catch(Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
	}
	
	
	

}
