package com.fitech.account.service.impl;

import com.fitech.account.repository.AccountEditLogItemRepository;
import com.fitech.account.repository.AccountEditLogRepository;
import com.fitech.account.service.AccountEditLogService;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountEditLog;
import com.fitech.domain.account.AccountEditLogItem;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.system.User;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.CommonConst;
import com.fitech.framework.lang.util.DateUtils;
import com.fitech.framework.lang.util.ExcelUtil;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.system.repository.UserRepository;
import com.fitech.vo.account.AccountProcessVo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Created by wangxw on 2017/8/24.
 */
@Service
@ServiceTrace
public class AccountEditLogServiceImpl implements AccountEditLogService {

    @Autowired
    private AccountEditLogRepository accountEditLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountEditLogItemRepository accountEditLogItemRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;


    @Override
    public AccountEditLog saveAccoutnEditLog(Account account, Long userId, AccountEditLog ae) {

        AccountEditLog accountEditLog = new AccountEditLog();
        accountEditLog.setAcccountId(account.getAccountTemplate().getTemplateCode());
        accountEditLog.setAccountEditType(ae.getAccountEditType());
        accountEditLog.setAccountName(account.getAccountTemplate().getTemplateName());
        accountEditLog.setEditDate(DateUtils.date2Str(new Date()));
        accountEditLog.setEditLineNum(ae.getEditLineNum());
        accountEditLog.setEditTime(new Date());

        User user = this.userRepository.findById(userId);

        accountEditLog.setEditUser(user.getLoginId());
        accountEditLog.setInstitutionId(account.getInstitution().getInstitutionId());
        accountEditLog.setInstitutionName(account.getInstitution().getInstitutionName());
        accountEditLog.setLogSource(ae.getLogSource());
        accountEditLog.setTerm(account.getTerm());

        accountEditLogRepository.save(accountEditLog);

        return accountEditLog;
    }

    @Override
    @Transactional
    public void saveAccoutnEditLogItem(AccountEditLog accountEditLog
            , Collection<AccountField> accountFieldList,Account account) {
        if(null != accountFieldList && !accountFieldList.isEmpty()){
            Collection<AccountField> accountFields = account.getAccountTemplate().getAccountFields();

            for(AccountField accountField : accountFieldList){

                AccountEditLogItem accountEditLogItem = new AccountEditLogItem();

                accountEditLogItem.setAccountEditLog(accountEditLog);
                accountEditLogItem.setEditAfterValue(accountField.getValue().toString());
                if(accountField.getEditBeforeValue()!=null &&
                        StringUtil.isNotEmpty(accountField.getEditBeforeValue().toString())){
                    accountEditLogItem.setEditBeforeValue(accountField.getEditBeforeValue().toString());
                }

                for (AccountField accountField1 : accountFields){
                    if(accountField1.getItemCode().equals(accountField.getItemCode())){
                        accountEditLogItem.setFieldId(accountField1.getItemCode());
                        accountEditLogItem.setFieldName(accountField1.getItemName());
                        break;
                    }
                }
                accountEditLogItemRepository.save(accountEditLogItem);
            }
        }
    }
    
    @Override
	public Page<AccountEditLog> findAccountEditLog(AccountEditLog accountEditLog) {
		return accountEditLogRepository.findAll(buildSpecification(accountEditLog),buildPageRequest(accountEditLog));		
	}
	
	
	 private PageRequest buildPageRequest(AccountEditLog accountEditLog){
		  return new PageRequest(accountEditLog.getPageNum()-1,accountEditLog.getPageSize());
	 }
	
	/**
	 * 创建动态查询条件组合.
	 */
	private Specification<AccountEditLog> buildSpecification(final AccountEditLog accountEditLog) {
		return new Specification<AccountEditLog> () {
			@Override
			public Predicate toPredicate(Root<AccountEditLog> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				if(null != accountEditLog){
					if (StringUtil.isNotEmpty(accountEditLog.getInstitutionName())) {
						list.add(cb.like(root.get("institutionName").as(String.class),"%"+ accountEditLog.getInstitutionName()+"%"));
					}
					if (StringUtil.isNotEmpty(accountEditLog.getAccountName())) {
						list.add(cb.equal(root.get("accountName").as(String.class),accountEditLog.getAccountName()));
					}
//					if(StringUtil.isNotEmpty(accountEditLog.getEditTimeBefore())){
//						list.add(cb.greaterThanOrEqualTo(root.get("editTime").as(Date.class),accountEditLog.getEditTimeBefore()));
//					}
//					if(StringUtil.isNotEmpty(accountEditLog.getEditTimeAfter())){
//						list.add(cb.lessThanOrEqualTo(root.get("editTime").as(Date.class),accountEditLog.getEditTimeAfter()));
//					}
                    root.fetch("", JoinType.INNER);
				}
				query.orderBy(cb.desc(root.get("id")));
				Predicate[] predicates = new Predicate[list.size()];
				predicates = list.toArray(predicates);
				return cb.and(predicates);
			}
		};
	}

    @Override
    public Page<AccountEditLog> findAccountEditLogByPage(AccountEditLog accountEditLog) {
        Pageable pageable = new PageRequest(accountEditLog.getPageNum()-1, accountEditLog.getPageSize());
        EntityManager em = null;
        List<AccountEditLog> accountEditLogs = new ArrayList<>();
        Integer count=null;
        String accountName=null;
        String institutionName=null;
        String term=null;
        String editUser=null;
        //获取参数台账名称
        if(!("".equals(accountEditLog.getAccountName()))&&null!=accountEditLog.getAccountName()){
        	accountName=accountEditLog.getAccountName();
        }     
        //获取参数机构名称
        if(!("".equals(accountEditLog.getInstitutionName()))&&null!=accountEditLog.getInstitutionName()){
        	institutionName=accountEditLog.getInstitutionName();
        }  
        //获取期数
        if(!("".equals(accountEditLog.getTerm()))&&null!=accountEditLog.getTerm()){
        	term=accountEditLog.getTerm();
        }
        //获取修改人
        if(!("".equals(accountEditLog.getEditUser()))&&null!=accountEditLog.getEditUser()){
        	editUser=accountEditLog.getEditUser();
        }
        try{
        	em = entityManagerFactory.createEntityManager();
            String hql = "select al.acccountId,al.accountName,al.institutionId,al.institutionName,to_char(to_date(al.term,'yyyy-MM-dd'),'yyyy-MM-dd') as term," +
                    "al.editUser,al.editTime,al.editDate,ai.editBeforeValue,ai.editAfterValue,ai.fieldName" +
                    ",ai.fieldId from ACCOUNT_EDITlOG al,ACCOUNT_EDITlOGITEM ai where al.id = ai.accountEditLog.id and 1=1 ";        
            if (null!=accountName) {
                hql += " and al.accountName='" + accountName + "'";
            }
            if (null!=institutionName) {
                hql += " and al.institutionName like'%" + institutionName + "%'";
            }
            if(null!=term){
            	hql+="and to_char(to_date(al.term,'yyyy-MM-dd'),'yyyy-MM-dd')='"+term+"'";
            }
            if(null!=editUser){
            	hql+=" and al.editUser like '%" + editUser + "%'";
            }
            hql+= "  order by al.editTime DESC";
//            count = findAccountEditLogByPageCount(accountEditLog) ;
            Query query = em.createQuery(hql);
            List<Object[]> totalaccount = query.getResultList();
            count=totalaccount.size();
            query.setFirstResult((accountEditLog.getPageNum() - 1) * accountEditLog.getPageSize());
            query.setMaxResults(accountEditLog.getPageSize());

            List<Object[]> objects = query.getResultList();
     
            if(null != objects && !objects.isEmpty()){
                for(Object[] objects1 : objects){
                    AccountEditLog accountEditLog1 = new AccountEditLog();

                    //设置返回页数hx
                    accountEditLog1.setPageNum(accountEditLog.getPageNum());
                    accountEditLog1.setAcccountId((String) objects1[0]);
                    accountEditLog1.setAccountName((String) objects1[1]);
                    accountEditLog1.setEditTime((java.util.Date) objects1[6]);
                    accountEditLog1.setEditAfterValue((String) objects1[9]);
                    accountEditLog1.setInstitutionId((String) objects1[2]);
                    accountEditLog1.setInstitutionName((String) objects1[3]);
                    accountEditLog1.setEditUser((String) objects1[5]);
                    accountEditLog1.setTerm((String) objects1[4]);
                    accountEditLog1.setEditBeforeValue((String) objects1[8]);
                    accountEditLog1.setFieldName((String) objects1[10]);
                    accountEditLog1.setFieldId((String) objects1[11]);
                    accountEditLogs.add(accountEditLog1);
                }
            }
        }catch(Exception e){
        	e.printStackTrace();
        }finally {
			if(null != em){
				em.close();
			}
		}
        
        Page<AccountEditLog> page = new PageImpl<>(accountEditLogs,pageable,count);
        return page;
    }
    //工作任务统计(hx)
    @Override
    public Page<AccountEditLog> findAccountEditLogTJByPage(AccountEditLog accountEditLog) {
        Pageable pageable = new PageRequest(accountEditLog.getPageNum()-1, accountEditLog.getPageSize());
        EntityManager em = null;
        List<AccountEditLog> accountEditLogs = new ArrayList<>();
        Integer count=null;
        String accountName=null;
        String institutionName=null;
        String term=null;
        String editUser=null;
        String editDate=null;
        Integer editLineNum=null;
        //获取参数台账名称
        if(!("".equals(accountEditLog.getAccountName()))&&null!=accountEditLog.getAccountName()){
        	accountName=accountEditLog.getAccountName();
        }     
        //获取参数机构名称
        if(!("".equals(accountEditLog.getInstitutionName()))&&null!=accountEditLog.getInstitutionName()){
        	institutionName=accountEditLog.getInstitutionName();
        }  
        //获取期数
        if(!("".equals(accountEditLog.getTerm()))&&null!=accountEditLog.getTerm()){
        	term=accountEditLog.getTerm();
        }
        //获取修改人
        if(!("".equals(accountEditLog.getEditUser()))&&null!=accountEditLog.getEditUser()){
        	editUser=accountEditLog.getEditUser();
        }
      //获取修改日期
        if(!("".equals(accountEditLog.getEditDate()))&&null!=accountEditLog.getEditDate()){
        	editDate=accountEditLog.getEditDate();
        }
      //获取修改条数
        if(!("".equals(accountEditLog.getEditLineNum()))&&null!=accountEditLog.getEditLineNum()){
        	editLineNum=accountEditLog.getEditLineNum();
        }
        try{
        	em = entityManagerFactory.createEntityManager();
            String hql = "select t.editUser,to_char(to_date(t.editDate,'yyyy-MM-dd'),'yyyy-MM-dd') as editDate,t.accountName,to_char(to_date(t.term,'yyyy-MM-dd'),'yyyy-MM-dd') as term,t.institutionName,sum(t.editLineNum) from ACCOUNT_EDITlOG t where 1=1 " ; 
            if (null!=accountName) {
                hql += " and t.accountName like'%" + accountName + "%'";
            }
            if (null!=institutionName) {
                hql += " and t.institutionName like'%" + institutionName + "%'";
            }
            if(null!=term){
            	hql+=" and to_char(to_date(t.term,'yyyy-MM-dd'),'yyyy-MM-dd')='"+term+"'";
            }
            if(null!=editUser){
            	hql+=" and t.editUser like '%" + editUser + "%'";
            }
            if(null!=editDate){
            	hql+=" and to_char(to_date(t.editDate,'yyyy-MM-dd'),'yyyy-MM-dd')='"+editDate+"'";
            }
            hql+=" group by t.editUser,t.editDate,t.accountName,t.term,t.institutionName ";
            if(null!=editLineNum){
            	hql+=" having sum(t.editLineNum) = "+editLineNum+" ";
            }
            hql+="   order by  t.editDate desc, t.term desc,t.editUser, t.accountName, t.institutionName ";
//            count = findAccountEditLogByPageCount(accountEditLog) ;
            Query query = em.createQuery(hql);
            List<Object[]> totalaccount = query.getResultList();
            count=totalaccount.size();
            query.setFirstResult((accountEditLog.getPageNum() - 1) * accountEditLog.getPageSize());
            query.setMaxResults(accountEditLog.getPageSize());

            List<Object[]> objects = query.getResultList();
     
            if(null != objects && !objects.isEmpty()){
                for(Object[] objects1 : objects){
                    AccountEditLog accountEditLog1 = new AccountEditLog();

                    //设置返回页数hx
                    accountEditLog1.setPageNum(accountEditLog.getPageNum());
                    accountEditLog1.setEditUser((String) objects1[0]);
                    accountEditLog1.setEditDate((String) objects1[1]);
                    accountEditLog1.setAccountName((String) objects1[2]);
                    accountEditLog1.setTerm((String) objects1[3]);
                    accountEditLog1.setInstitutionName((String) objects1[4]);
                    accountEditLog1.setEditLineNum(Integer.valueOf(objects1[5].toString()));;
                    accountEditLogs.add(accountEditLog1);
                }
            }
        }catch(Exception e){
        	e.printStackTrace();
        }finally {
			if(null != em){
				em.close();
			}
		}        
        Page<AccountEditLog> page = new PageImpl<>(accountEditLogs,pageable,count);
        return page;
    }

  //下载---工作任务统计的下载
    @Override
    public String downLoadEditLogsTJ(String searchs){
    	List<AccountEditLog> list = new ArrayList<>();
    	EntityManager em = null;
    	String[] searchlist=null;
    	List<Object[]> objects=null;
    	String sheetName="GongZuoRenWuTongJi";
    	if("true".equals(searchs)){
    		searchlist=new String[6];
    	}else{
    		searchlist = searchs.split(",",6);
    	}  	
    	try{
        	em = entityManagerFactory.createEntityManager();
            String hql = "select t.editUser,to_char(to_date(t.editDate,'yyyy-MM-dd'),'yyyy-MM-dd') as editDate,t.accountName,to_char(to_date(t.term,'yyyy-MM-dd'),'yyyy-MM-dd') as term,t.institutionName,sum(t.editLineNum) from ACCOUNT_EDITlOG t where 1=1 " ; 
            if (!StringUtil.isEmpty(searchlist[0])) {
            	hql+=" and t.editUser like '%" + searchlist[0] + "%'";
            }
            if (!StringUtil.isEmpty(searchlist[1])) {
            	hql+=" and to_char(to_date(t.editDate,'yyyy-MM-dd'),'yyyy-MM-dd')='"+searchlist[1]+"'";
            }
            if (!StringUtil.isEmpty(searchlist[4])) {
            	hql += " and t.accountName like'%" + searchlist[4] + "%'";
            }
            if (!StringUtil.isEmpty(searchlist[2])) {
            	hql+=" and to_char(to_date(t.term,'yyyy-MM-dd'),'yyyy-MM-dd')='"+searchlist[2]+"'";
            }
            if (!StringUtil.isEmpty(searchlist[5])) {
            	hql += " and t.institutionName like'%" + searchlist[5] + "%'";
            }
            hql+=" group by t.editUser,t.editDate,t.accountName,t.term,t.institutionName ";
            if(!StringUtil.isEmpty(searchlist[3])){
            	hql+=" having sum(t.editLineNum) = "+searchlist[3]+" ";
            }
            hql+="   order by  t.editDate desc, t.term desc,t.editUser, t.accountName, t.institutionName ";
//            count = findAccountEditLogByPageCount(accountEditLog) ;
            Query query = em.createQuery(hql);
            objects = query.getResultList();
            
        }catch(Exception e){
        	e.printStackTrace();
        }finally {
			if(null != em){
				em.close();
			}
		} 
    	//打印数据
    	List<List<String>> hList = new ArrayList<>();
    	List<String> lineFirst=new ArrayList<>();
    	lineFirst.add("修改人");
    	lineFirst.add("修改日期");
    	lineFirst.add("台账名称");
    	lineFirst.add("期数");
    	lineFirst.add("机构名称");
    	lineFirst.add("修改条数");
		hList.add(lineFirst);
    	for(Object[] objects1 : objects){
    		List<String> line=new ArrayList<>();
    		line.add((String) objects1[0]);
    		line.add((String) objects1[1]);
    		line.add((String) objects1[2]);
    		line.add((String) objects1[3]);
    		line.add((String) objects1[4]);
    		line.add(objects1[5].toString());
    		hList.add(line);
        }
    	return ExcelUtil.createExcel(hList, sheetName, CommonConst.getProperties("template_path"),sheetName);
        
    }
    
    @Override
    public Integer findAccountEditLogByPageCount(AccountEditLog accountEditLog) {
        Integer count = 0 ;

        EntityManager em = entityManagerFactory.createEntityManager();
        String hql = "select count(*) from ACCOUNT_EDITlOG al , ACCOUNT_EDITlOGITEM ai where al.id = ai.accountEditLog.id and 1=1 ";
        Query query = em.createQuery(hql);

        if (StringUtil.isNotEmpty(accountEditLog.getInstitutionName())) {
            hql += " and institutionName like '%" + accountEditLog.getInstitutionName() + "%'";
        }
        if (StringUtil.isNotEmpty(accountEditLog.getAccountName())) {
            hql += " and institutionName like '%" + accountEditLog.getInstitutionName() + "%'";
        }

        Long bigDecimal = (Long)query.getSingleResult();

        if(null != bigDecimal && bigDecimal.intValue() >0){
            count = bigDecimal.intValue();
        }
        em.close();

        return count;
    }
}
