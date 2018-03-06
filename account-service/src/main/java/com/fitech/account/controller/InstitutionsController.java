package com.fitech.account.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fitech.account.service.InstitutionService;
import com.fitech.domain.system.Institution;
import com.fitech.framework.lang.result.GenericResult;

@RestController
public class InstitutionsController {

	@Autowired
	private InstitutionService institutionService;
	
	@GetMapping("/institution")
	public GenericResult<List<Institution>> getAllInstitution(){
		GenericResult<List<Institution>> result = new GenericResult<List<Institution>>();
		try{
			List<Institution> list = institutionService.getAllInstitution();
			result.setData(list);
		}catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
		return result;
	}
	
}
