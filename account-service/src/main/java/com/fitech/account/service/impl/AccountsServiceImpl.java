package com.fitech.account.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import com.fitech.account.dao.AccountsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import com.fitech.account.dao.AccountTemplateDAO;
import com.fitech.account.repository.AccountRepository;
import com.fitech.account.service.AccountsService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.system.Institution;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.vo.account.AccountVo;

@Service
@ServiceTrace
public class AccountsServiceImpl implements AccountsService {
	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private AccountTemplateDAO accountTemplateDAO;

	@Autowired
	private EntityManagerFactory entityManagerFactory;

	@Autowired
	private AccountsDao accountsDao;

	/**
	 * 条件查询返回前端动态列的数据
	 */
	@Override
	public List<Map<String,Object>> findData(Account account) {
		List<Account> list =  accountRepository.findAll(buildSpecification(account));		
		List<Map<String,Object>> list1 = new ArrayList<Map<String,Object>>();		
		if(!list.isEmpty()){
			for(int i=0;i<list.size();i++){
				List<Map<String,Object>> list2 = accountTemplateDAO.getAllDate(list.get(i).getAccountTemplate(),list.get(i).getId());
				for(Map<String,Object> map1:list2){
					map1.put("term",list.get(i).getTerm());
					map1.put("institutionName",list.get(i).getInstitution().getInstitutionName());
					map1.put("templateName",list.get(i).getAccountTemplate().getTemplateName());
				}
				list1.addAll(list2);
			}
		}
		return list1;
	}

	/**
	 * 创建动态查询条件组合
	 * @param account
	 * @return
	 */
	private Specification<Account> buildSpecification(final Account account){
		return new Specification<Account>(){

			@Override
			public Predicate toPredicate(Root<Account> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				if(null!=account){
					if(StringUtil.isNotEmpty(account.getAccountTemplate())){
						list.add(cb.equal(root.get("accountTemplate").get("templateName").as(String.class), account.getAccountTemplate().getTemplateName()));
					}
					if(StringUtil.isNotEmpty(account.getTerm())){
						list.add(cb.equal(root.get("term").as(String.class),account.getTerm()));
					}
					if(StringUtil.isNotEmpty(account.getInstitution())){
						if(StringUtil.isNotEmpty(account.getInstitution().getInstitutionName())){	
							list.add(cb.equal(root.get("institution").get("institutionName").as(String.class),account.getInstitution().getInstitutionName()));
						}
					}
				}
				query.orderBy(cb.desc(root.get("id")));
				Predicate[] predicates = new Predicate[list.size()];
				predicates = list.toArray(predicates);
				return cb.and(predicates);
			}
		};
	}

	@Override
	public Account findAccountById(Long id) {

		try{
			return accountRepository.findOne(id);
		}catch(Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}


	}

	@Override
	public List<Account> findAllAccount() {
		try{
			return accountRepository.findAll();
		}catch(Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}

	}

	/**
	 * 任务统计
	 * @throws IllegalAccessException 
	 * @throws IllegalArgumentException 
	 */
	@Override
	public List getrwtj() {
		List<AccountVo> accountVoList = new ArrayList<>();
		try {
			accountVoList = accountsDao.getrwtj();
		}catch (Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
		return accountVoList;
	}

	/**
	 * 条件查询任务统计数据
	 * 
	 */
	@Override
	public List getrwtjByCondition(Account account){
	    return  this.getrwtjByCondition(account,null);
	}

	
	@Override
	public List getrwtjByCondition(Account account, Long userId) {
		Map<String,Object> tempMap = new HashMap<String,Object>();
		List<AccountVo> accountVoList = new ArrayList<>();
		String term = account.getTerm();
		String institutionName=null;
		if(account.getInstitution()!=null){
			institutionName = account.getInstitution().getInstitutionName();
		}
		tempMap.put("term", term);
		tempMap.put("institutionName", institutionName);
		tempMap.put("userId", userId);
		try {
			List<Map<String,Object>> list = accountsDao.getrwtjByCondition(tempMap);
			if(null != list && !list.isEmpty()){
                for (int i = 0; i < list.size(); i++) {
                	Map<String,Object> map = list.get(i);
                    AccountVo accountVo = new AccountVo();
                    accountVo.setDbl(map.get("DBL") == null?"":map.get("DBL").toString());
                    accountVo.setDsh(map.get("DSH") == null?"":map.get("DSH").toString());
                    accountVo.setInstitutionname(map.get("INSTITUTIONNAME") == null?"":map.get("INSTITUTIONNAME").toString());
                    accountVo.setShtg(map.get("SHTG") == null?"":map.get("SHTG").toString());
                    accountVo.setTerm(map.get("TERM") == null?"":map.get("TERM").toString() );
                    accountVo.setTh(map.get("TH") == null?"":map.get("TH").toString());
                    accountVo.setTotalcount( map.get("TOTALCOUNT") == null? 0 :Integer.parseInt(map.get("TOTALCOUNT").toString()));
                    accountVoList.add(accountVo);
                }
            }
		}catch (Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
		return accountVoList;
	}
	
	/**
	 * 任务统计点击百分比显示当前台账信息
	 */
	@Override
	public List<Account> findrwtjAccounts(Account account) {
		Map<String,String> tempMap = new HashMap<String,String>();
		String term = account.getTerm();
		String institutionName=account.getInstitution().getInstitutionName();
		String accountState = String.valueOf(account.getAccountState());
		if("DBL".equals(accountState)){
			accountState="0";
		}else if("DSH".equals(accountState)){
			accountState="1";
		}else if("SHTG".equals(accountState)){
			accountState="2";
		}else{
			accountState="3";
		}
		tempMap.put("accountState", accountState);
		tempMap.put("term", term);
		tempMap.put("institutionName", institutionName);
		List<Account> accounts = new ArrayList<>();
		try{
			List<Map<String,Object>> list = accountsDao.findrwtjAccounts(tempMap);
			if(null != list && !list.isEmpty()){
                for (int i = 0; i < list.size(); i++) {
                	Map<String,Object> map = list.get(i);
                    Account accounto = new Account();
                    Institution institutiono=new Institution();
                    AccountTemplate AccountTemplateo=new AccountTemplate();
                    institutiono.setInstitutionName(map.get("INSTITUTIONNAME") == null?"":map.get("INSTITUTIONNAME").toString());
                    accounto.setInstitution(institutiono);
                    AccountTemplateo.setTemplateName(map.get("TEMPLATENAME") == null?"":map.get("TEMPLATENAME").toString());
                    accounto.setAccountTemplate(AccountTemplateo);
                    accounto.setTerm(map.get("TERM") == null?"":map.get("TERM").toString());
                    accounto.setAccountState(account.getAccountState());           
                    accounts.add(accounto);
                }
            }
			for (int i = 0; i < accounts.size(); i++) {
	            System.out.println(accounts.get(i));
	        }
		}catch (Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
		return accounts;
	}



}
