package com.fitech.account.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

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
		EntityManager em = null;
		try {
			em = entityManagerFactory.createEntityManager();
			String sql = "select k.term,k.institutionname,k.totalcount,to_char(k.dbl/k.totalcount*100,'9990') dbl," +
					"to_char(k.dsh/k.totalcount*100,'9990') dsh,to_char(k.shtg/k.totalcount*100,'9990') shtg," +
					"to_char(k.th/k.totalcount*100,'9990') th from (select a.term,(select institutionname " +
					"from INSTITUTION where id=a.institution_id) institutionname,a.totalcount,nvl(b.dbl, 0) dbl,nvl(c.dsh, 0) dsh," +
					"nvl(d.shtg, 0) shtg,nvl(e.th, 0) th from (select count(1) totalcount,t.term,t.institution_id from ACCOUNT t " +
					"group by t.term, t.institution_id) a left join (select count(1) dbl,term,institution_id " +
					"from account where accountstate = 0 group by term, institution_id) b on a.term = b.term and " +
					"a.institution_id = b.institution_id left join (select count(1) dsh, term, institution_id " +
					"from account where accountstate = 1 group by term, institution_id) c on a.term = c.term and" +
					" a.institution_id = c.institution_id left join (select count(1) shtg, term, institution_id " +
					"from account where accountstate = 2 group by term, institution_id) d on a.term = d.term and " +
					"a.institution_id = d.institution_id left join (select count(1) th, term, institution_id " +
					"from account where accountstate = 3 group by term, institution_id) e on a.term = e.term and " +
					"a.institution_id = e.institution_id)k";
			Query query = em.createNativeQuery(sql);
			List list = query.getResultList();

			if(null != list && !list.isEmpty()){
                for (int i = 0; i < list.size(); i++) {
                    Object[] objs = (Object[]) list.get(i);

                    AccountVo accountVo = new AccountVo();
                    accountVo.setDbl((String) objs[3]);
                    accountVo.setDsh((String) objs[4]);
                    accountVo.setInstitutionname((String) objs[1]);
                    accountVo.setShtg((String) objs[5]);
                    accountVo.setTerm((String) objs[0]);
                    accountVo.setTh((String) objs[6]);
                    accountVo.setTotalcount(((BigDecimal) objs[2]).intValue());

                    accountVoList.add(accountVo);
                }
            }
		}catch (Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}finally {
			if(null != em){
				em.close();
			}
		}
		return accountVoList;
	}

	/**
	 * 条件查询任务统计数据
	 * 
	 */
	@Override
	public List getrwtjByCondition(Account account){
		List<AccountVo> accountVoList = new ArrayList<>();
		EntityManager em = null;
		String term = account.getTerm();
		String institutionName=null;
		if(account.getInstitution()!=null){
			institutionName = account.getInstitution().getInstitutionName();
		}
		try {
			em = entityManagerFactory.createEntityManager();
			String sql = "select * from(select to_char(to_date(k.term,'yyyy-MM-dd'),'yyyy-MM-dd') as term,k.institutionname,k.totalcount,to_char(k.dbl/k.totalcount*100,'9990') dbl," +
					"to_char(k.dsh/k.totalcount*100,'9990') dsh,to_char(k.shtg/k.totalcount*100,'9990') shtg," +
					"to_char(k.th/k.totalcount*100,'9990') th from (select a.term,(select institutionname " +
					"from INSTITUTION where id=a.institution_id) institutionname,a.totalcount,nvl(b.dbl, 0) dbl," +
					"nvl(c.dsh, 0) dsh,nvl(d.shtg, 0) shtg,nvl(e.th, 0) th from (select count(1) totalcount,t.term," +
					"t.institution_id from ACCOUNT t group by t.term, t.institution_id) a left join (select count(1) dbl," +
					"term,institution_id from account where accountstate = 0 group by term, institution_id) b on a.term = b.term " +
					"and a.institution_id = b.institution_id left join (select count(1) dsh, term, institution_id from account " +
					"where accountstate = 1 group by term, institution_id) c on a.term = c.term and a.institution_id = c.institution_id " +
					"left join (select count(1) shtg, term, institution_id from account where accountstate = 2 group by term," +
					" institution_id) d on a.term = d.term and a.institution_id = d.institution_id left join (select count(1) th," +
					" term, institution_id from account where accountstate = 3 group by term, institution_id) e on a.term = e.term " +
					"and a.institution_id = e.institution_id)k)j where ";
			
			if(institutionName==null||"".equals(institutionName)){
				sql = sql + " to_char(to_date(j.term,'yyyy-MM-dd'),'yyyy-MM-dd')=:term";
			}else{
				institutionName="%"+institutionName+"%";
				sql = sql + " to_char(to_date(j.term,'yyyy-MM-dd'),'yyyy-MM-dd')=:term and j.institutionname like:institutionName";
			}
			Query query = em.createNativeQuery(sql);
			if(institutionName==null||"".equals(institutionName)){
				query.setParameter("term",term);
			}else{
				query.setParameter("term",term);
				query.setParameter("institutionName",institutionName);
			}
			List list = query.getResultList();

			if(null != list && !list.isEmpty()){
                for (int i = 0; i < list.size(); i++) {
                    Object[] objs = (Object[]) list.get(i);

                    AccountVo accountVo = new AccountVo();
                    accountVo.setDbl((String) objs[3]);
                    accountVo.setDsh((String) objs[4]);
                    accountVo.setInstitutionname((String) objs[1]);
                    accountVo.setShtg((String) objs[5]);
                    accountVo.setTerm((String) objs[0]);
                    accountVo.setTh((String) objs[6]);
                    accountVo.setTotalcount(((BigDecimal) objs[2]).intValue());

                    accountVoList.add(accountVo);
                }
            }
		}catch (Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}finally {
			if(null != em){
				em.close();
			}
		}
		return accountVoList;
	}

	/**
	 * 任务统计点击百分比显示当前台账信息
	 */
	@Override
	public List<Account> findrwtjAccounts(Account account) {
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
		System.out.println(accountState);
		EntityManager em=null;
		List<Account> accounts = new ArrayList<>();
		try{
			em = entityManagerFactory.createEntityManager();
			String hql = "select i.institutionname,t.templatename,to_char(to_date(a.term,'yyyy-MM-dd'),'yyyy-MM-dd') as term,a.accountstate from ACCOUNT a left join INSTITUTION i on"+
						" a.institution_id=i.id left join reporttemplate t on a.accounttemplate_id=t.id "+
						" where a.accountstate=:accountState and to_char(to_date(a.term,'yyyy-MM-dd'),'yyyy-MM-dd')=:term and i.institutionname=:institutionName";
			Query query = em.createNativeQuery(hql);
			query.setParameter("accountState",accountState);
			query.setParameter("term", term);
			query.setParameter("institutionName", institutionName);
			List list = query.getResultList();
			if(null != list && !list.isEmpty()){
                for (int i = 0; i < list.size(); i++) {
                    Object[] objs = (Object[]) list.get(i);

                    Account accounto = new Account();
                    Institution institutiono=new Institution();
                    AccountTemplate AccountTemplateo=new AccountTemplate();
                  
                    institutiono.setInstitutionName((String) objs[0]);
                    accounto.setInstitution(institutiono);
                    AccountTemplateo.setTemplateName((String) objs[1]);
                    accounto.setAccountTemplate(AccountTemplateo);
                    accounto.setTerm((String) objs[2]);
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
		}finally {
			if(null != em){
				em.close();
			}
		}
		return accounts;
	}

}
