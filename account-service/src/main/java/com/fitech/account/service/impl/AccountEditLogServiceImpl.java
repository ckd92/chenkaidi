package com.fitech.account.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.dao.AccountEditLogDao;
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
import com.fitech.framework.lang.page.Page;
import com.fitech.framework.lang.util.DateUtils;
import com.fitech.framework.lang.util.ExcelUtil;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.system.dao.UserDataDao;

/**
 * Created by wangxw on 2017/8/24.
 */
@Service
@ServiceTrace
public class AccountEditLogServiceImpl implements AccountEditLogService {
	@Autowired
	private EntityManagerFactory entityManagerFactory;
	
    @Autowired
    private AccountEditLogRepository accountEditLogRepository;
    @Autowired
    private AccountEditLogItemRepository accountEditLogItemRepository;
    @Autowired
    private UserDataDao userDataDao;
    @Autowired
    private AccountEditLogDao accountEditLogDao;

    @Override
    public AccountEditLog saveAccoutnEditLog(Account account, Long userId, AccountEditLog ae) {

        AccountEditLog accountEditLog = new AccountEditLog();
        accountEditLog.setAcccountId(account.getAccountTemplate().getTemplateCode());
        accountEditLog.setAccountEditType(ae.getAccountEditType());
        accountEditLog.setAccountName(account.getAccountTemplate().getTemplateName());
        accountEditLog.setEditDate(DateUtils.date2Str(new Date()));
        accountEditLog.setEditLineNum(ae.getEditLineNum());
        accountEditLog.setEditTime(new Date());

        User user = userDataDao.findUserById(userId);

        accountEditLog.setEditUser(user.getUsername());
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
                accountEditLogItem.setEditAfterValue(String.valueOf(accountField.getValue()==null?"":accountField.getValue()));
                if(accountField.getEditBeforeValue()!=null &&
                        StringUtil.isNotEmpty(String.valueOf(accountField.getEditBeforeValue()))){
                    accountEditLogItem.setEditBeforeValue(String.valueOf(accountField.getEditBeforeValue()));
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
    public List<AccountEditLog> findAccountEditLogByPage(AccountEditLog accountEditLog,Page page) {
    	return accountEditLogDao.findAccountEditLogByPage(accountEditLog, page);
    }
    @Override
    public List<AccountEditLog> findAccountEditLogTJByPage(AccountEditLog accountEditLog,Page page) {
    	return accountEditLogDao.findAccountEditLogTJByPage(accountEditLog, page);
    }

//  //下载---工作任务统计的下载
//    @Override
//    public String downLoadEditLogsTJ(String searchs){
//    	List<AccountEditLog> list = new ArrayList<>();
//    	EntityManager em = null;
//    	String[] searchlist=null;
//    	List<Object[]> objects=null;
//    	String sheetName="GongZuoRenWuTongJi";
//    	if("true".equals(searchs)){
//    		searchlist=new String[6];
//    	}else{
//    		searchlist = searchs.split(",",6);
//    	}  	
//    	try{
//        	em = entityManagerFactory.createEntityManager();
//            String hql = "select t.editUser,to_char(to_date(t.editDate,'yyyy-MM-dd'),'yyyy-MM-dd') as editDate,t.accountName,to_char(to_date(t.term,'yyyy-MM-dd'),'yyyy-MM-dd') as term,t.institutionName,sum(t.editLineNum) from ACCOUNT_EDITlOG t where 1=1 " ; 
//            if (!StringUtil.isEmpty(searchlist[0])) {
//            	hql+=" and t.editUser like '%" + searchlist[0] + "%'";
//            }
//            if (!StringUtil.isEmpty(searchlist[1])) {
//            	hql+=" and to_char(to_date(t.editDate,'yyyy-MM-dd'),'yyyy-MM-dd')='"+searchlist[1]+"'";
//            }
//            if (!StringUtil.isEmpty(searchlist[4])) {
//            	hql += " and t.accountName like'%" + searchlist[4] + "%'";
//            }
//            if (!StringUtil.isEmpty(searchlist[2])) {
//            	hql+=" and to_char(to_date(t.term,'yyyy-MM-dd'),'yyyy-MM-dd')='"+searchlist[2]+"'";
//            }
//            if (!StringUtil.isEmpty(searchlist[5])) {
//            	hql += " and t.institutionName like'%" + searchlist[5] + "%'";
//            }
//            hql+=" group by t.editUser,t.editDate,t.accountName,t.term,t.institutionName ";
//            if(!StringUtil.isEmpty(searchlist[3])){
//            	hql+=" having sum(t.editLineNum) = "+searchlist[3]+" ";
//            }
//            hql+="   order by  t.editDate desc, t.term desc,t.editUser, t.accountName, t.institutionName ";
////            count = findAccountEditLogByPageCount(accountEditLog) ;
//            Query query = em.createQuery(hql);
//            objects = query.getResultList();
//            
//        }catch(Exception e){
//        	e.printStackTrace();
//        }finally {
//			if(null != em){
//				em.close();
//			}
//		} 
//    	//打印数据
//    	List<List<String>> hList = new ArrayList<>();
//    	List<String> lineFirst=new ArrayList<>();
//    	lineFirst.add("修改人");
//    	lineFirst.add("修改日期");
//    	lineFirst.add("台账名称");
//    	lineFirst.add("期数");
//    	lineFirst.add("机构名称");
//    	lineFirst.add("修改条数");
//		hList.add(lineFirst);
//    	for(Object[] objects1 : objects){
//    		List<String> line=new ArrayList<>();
//    		line.add((String) objects1[0]);
//    		line.add((String) objects1[1]);
//    		line.add((String) objects1[2]);
//    		line.add((String) objects1[3]);
//    		line.add((String) objects1[4]);
//    		line.add(objects1[5].toString());
//    		hList.add(line);
//        }
//    	return ExcelUtil.createExcel(hList, sheetName, CommonConst.getProperties("template_path"),sheetName);
//        
//    }
    
}
