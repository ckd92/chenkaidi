package com.fitech.account.service;

import java.util.List;

import com.fitech.domain.system.Institution;

public interface InstitutionService {
	/**
	 * 查询所有报送机构
	 * @return
	 */
	public List<Institution> getAllInstitution();
}
