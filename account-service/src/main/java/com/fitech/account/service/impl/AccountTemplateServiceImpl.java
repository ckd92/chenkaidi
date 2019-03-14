package com.fitech.account.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.fitech.account.dao.AccountProcessServiceDao;
import com.fitech.report.service.ReportProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.dao.AccountFieldDAO;
import com.fitech.account.dao.AccountTemplateDAO;
import com.fitech.account.repository.AccountTemplateRepository;
import com.fitech.account.service.AccountFieldService;
import com.fitech.account.service.AccountTemplateService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.report.BusSystem;
import com.fitech.domain.report.RepFreq;
import com.fitech.domain.report.ReportTemplate;
import com.fitech.domain.system.ProcessConfig;
import com.fitech.domain.system.ReportPermission;
import com.fitech.domain.system.SubSystem;
import com.fitech.enums.system.InstituteLevelEnum;
import com.fitech.enums.system.OperationEnum;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.system.repository.ProcessConfigRepository;
import com.fitech.system.repository.ReportPermissionRepository;

/**
 * Created by wangxw on 2017/7/25.
 */
@Service
@ServiceTrace
public class AccountTemplateServiceImpl implements AccountTemplateService {
	public static Long subSystemId = 27l;
	public static Long busSystemId = 192l;
		
    @Autowired
    private AccountTemplateRepository accountTemplateRepository;
    @Autowired
    private ReportPermissionRepository<ReportPermission> reportPermissionRepository;
    @Autowired
    private ProcessConfigRepository<ProcessConfig> processConfigRepository;

    @Autowired
    private AccountTemplateDAO accountTemplateDAO;
    @Autowired
    private AccountFieldDAO accountFieldDAO;
    @Autowired
    private AccountProcessServiceDao accountProcessServiceDao;
    
    @Autowired
    private AccountFieldService accountFieldService;
    @Autowired
    private ReportProcessService reportProcessService;

    @Override
	public List<AccountTemplate> findAll(){
    	return accountTemplateRepository.findAll();
    }
    
    @Override
    public AccountTemplate findById(Long id) {
    	if(null != id){
    		return accountTemplateRepository.findOne(id);
        }
        return null;
    }


    @Override
    @Transactional
    public GenericResult<Boolean> save(AccountTemplate accountTemplate) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            if(null != accountTemplate){
                if(StringUtil.isNotEmpty(accountTemplate.getTemplateCode())){
                	
                    if(valiAccountTemplateNameIsExist(accountTemplate.getTemplateName())){
                        if(valiAccountTemplateCodeIsExist(accountTemplate.getTemplateCode())){
                            accountTemplate.setTableName(accountTemplate.getTemplateCode());

                            Collection<AccountField> acField = accountTemplate.getAccountFields();
                            Iterator<AccountField> itaf = acField.iterator();
                            while (itaf.hasNext()){
                				AccountField af = itaf.next();
                				af.setItemCode(af.getItemCode().toUpperCase());
                            }
                            
                            accountTemplate.setAccountFields(accountFieldService.convertAccountField(accountTemplate.getAccountFields()));

                            BusSystem busSystem = new BusSystem();
                            busSystem.setId(busSystemId);
                            accountTemplate.setBusSystem(busSystem);
                            //添加模板设置模板启用状态
                            accountTemplate.setEnabled(true);
                            accountTemplateRepository.save(accountTemplate);

                            //初始化补录台账权限
                            addReportPermissions(accountTemplate,busSystem);
                            //初始化补录字段权限
                            accountFieldService.addFieldPermessions(accountTemplate);

                            //删除原来的数据表
                            createODSAccount(accountTemplate);
                        }else{
                        	result.setSuccess(false);
                            result.setMessage("台账模板编号【"+accountTemplate.getTemplateCode()+"】已存在!");
                        }
                    }else{
                    	result.setSuccess(false);
                        result.setMessage("台账模板名称【"+accountTemplate.getTemplateName()+"】已存在!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
        return result;
    }

    @Override
    @Transactional
    public GenericResult<Boolean> update(AccountTemplate accountTemplate) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            if(null != accountTemplate && null != accountTemplate.getId()){
                accountTemplate.setTableName(accountTemplate.getTemplateCode());
                BusSystem busSystem = new BusSystem();
                busSystem.setId(busSystemId);
                accountTemplate.setBusSystem(busSystem);
                // 判断是否发生字段变更
                Collection<AccountField> acField = accountTemplate.getAccountFields();
                if(acField == null || acField.size() == 0){
                    if(StringUtil.isNotEmpty(accountTemplate.getTemplateName())){
                    	boolean valiResult = true;
                    	
                        AccountTemplate at = accountTemplateRepository.findOne(accountTemplate.getId());
                        if(!at.getTemplateName().equals(accountTemplate.getTemplateName())){
                            if(!valiAccountTemplateNameIsExist(accountTemplate.getTemplateName())){
                                valiResult = false;
                            }
                        }
                        if(valiResult){
                            accountTemplate.setAccountFields(at.getAccountFields());
                            accountTemplateRepository.save(accountTemplate);
                        }else{
                        	result.setSuccess(false);
                            result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
                            result.setMessage("台账模板名称【"+accountTemplate.getTemplateName()+"】已存在!");
                        }
                    }
                }else{
                    //如果报文模板对应的数据表没有数据,则可以修改此报文下的字段信息
                    Boolean f = accountTemplateDAO.dataIsExist(accountTemplate);
                    if (!f) {
                        accountFieldService.deleteAccountFields(accountTemplate);

                        accountTemplate.setAccountFields(accountFieldService.convertAccountField(accountTemplate.getAccountFields()));
                        accountTemplateRepository.save(accountTemplate);

                        createODSAccount(accountTemplate);
                    }else{
                    	result.setSuccess(false);
                        result.setMessage("台账表【"+accountTemplate.getTemplateName()+"】已存在数据，修改字段失败!");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
        return result;
    }
    
    @Override
    @Transactional
    public GenericResult<Boolean> deleteBatch(List<Long> idList) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            if(null != idList && !idList.isEmpty()){
                for (int i = 0; i < idList.size(); i++) {
                    AccountTemplate accountTemplate = new AccountTemplate();
                    accountTemplate.setId(idList.get(i));
                    AccountTemplate acTemplate = accountTemplateRepository.findById(accountTemplate.getId());
                    
                    //获取台账模板是否可删除标志
                    String flag = valiAccountTemplateIsDelete(acTemplate.getId());
                    
                    //创建条线对象并设置补录id为27
                    SubSystem subSystem = new SubSystem();
                	subSystem.setId(subSystemId);
                	List<ProcessConfig> processConfigs = processConfigRepository.findBySubSystem(subSystem);
                	//循环判断模板是否被流程应用
                	for(ProcessConfig processConfig: processConfigs){
                		if(flag.equals("process")){
                			break;
                		}
                		Collection<ReportTemplate> aTemplates = processConfig.getReportTemplate();
                		 Iterator<ReportTemplate> it = aTemplates.iterator();
                	     while(it.hasNext()){
                	    	 ReportTemplate template = it.next();
                	    	 if(accountTemplate.getId().equals(template.getId())){
                	    		 flag = "process";
                	    	 }
                	     }
                	}	

                	if(flag.equals("true")){
                        // 关联删除该模板对应的待办
                        List<Long> reportIds = accountProcessServiceDao.findReportIdByTemplateId(accountTemplate.getId());
                        reportProcessService.deletePendingWork(reportIds);
                		this.delReportPermissions(accountTemplateRepository.findById(idList.get(i)));
                        accountFieldService.delFieldPermessions(accountTemplateRepository.findById(idList.get(i)));
                        accountTemplateRepository.delete(idList.get(i));
                        accountTemplateDAO.dropAllTemplate(acTemplate);
                    }else if(flag.equals("process")){
                    	result.setSuccess(false);
                    	result.setMessage("模板【" + acTemplate.getTemplateName() + "】被流程引用，不可删除");
                    }else{
                        result.setSuccess(false);
                        result.setMessage("台账模板【"+acTemplate.getTemplateName()+"】被占用，删除失败！");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
        return result;
    }

    @Override
    public boolean valiAccountTemplateNameIsExist(String templateName) {
        if(StringUtil.isNotEmpty(templateName)){
            AccountTemplate accountTemplate = new AccountTemplate();
            accountTemplate.setTemplateName(templateName);
            List<AccountTemplate> accountTemplateList = accountTemplateRepository.findAll(buildServerSpecification(accountTemplate));
            if(null != accountTemplateList && !accountTemplateList.isEmpty()){
                return false;
            }
        }
        return true;
    }
    @Override
    public boolean valiAccountTemplateCodeIsExist(String templateCode) {

        if(StringUtil.isNotEmpty(templateCode)){
            AccountTemplate accountTemplate = new AccountTemplate();
            accountTemplate.setTemplateCode(templateCode);
            List<AccountTemplate> accountTemplateList = accountTemplateRepository.findAll(buildServerSpecification(accountTemplate));
            if(null != accountTemplateList && !accountTemplateList.isEmpty()){
                return false;
            }
        }
        return true;
    }
    
	@Override
	public String valiAccountTemplateIsDelete(Long id) {
    	String flag = "true";
    	AccountTemplate atemplate = new AccountTemplate();
    	atemplate.setId(id);
    	
    	AccountTemplate acTemplate = accountTemplateRepository.findById(atemplate.getId());
        //1.判断报文模板是否被配置权限并且没有被报文使用，如果没有配置，则可以删除，如果被配置则不能删除
    	if(!accountTemplateDAO.isDeleteAble(atemplate.getId())){
    		return "报文权限与角色关联";
    	}
    	Collection<AccountField> acFields = acTemplate.getAccountFields();
    	//2.判断字段是否被配置权限，如果没有配置，则可以删除，如果被配置则不能删除
    	if (acFields != null && acFields.size() > 0){   		
	    	for(AccountField accountField: acFields){    		
	    		if(!accountFieldDAO.isDeleteAble(accountField.getId())){
	    			return "字段权限与角色关联";
	    		}
	    	}
    	}
        return flag;
    }

   
	@Override
	public void addReportPermissions(AccountTemplate accountTemplate, BusSystem busSystem) {
        Collection<ReportPermission> rps=new ArrayList<ReportPermission>();
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                ReportPermission rp=new ReportPermission();
                rp.setBusSystem(busSystem);
                rp.setReportTemplateId(accountTemplate.getId());
                if(i==0){
                    rp.setOperationType(OperationEnum.LOOK);
                }else if(i==1){
                    rp.setOperationType(OperationEnum.OPERATE);
                }else{
                    rp.setOperationType(OperationEnum.CHECK);
                }

                if(j==0){
                    rp.setInstituteLevel(InstituteLevelEnum.CURRENT);
                }else if(j==1){
                    rp.setInstituteLevel(InstituteLevelEnum.NEXT);
                }else{
                    rp.setInstituteLevel(InstituteLevelEnum.NEXTALL);
                }
                rps.add(rp);
            }
        }
        reportPermissionRepository.save(rps);
    }
	
	@Override
	public void delReportPermissions(AccountTemplate accountTemplate) {
		Collection<ReportPermission> rps=reportPermissionRepository.findByReportTemplateId(accountTemplate.getId());
        if (rps!=null && rps.size()>0){
            reportPermissionRepository.delete(rps);
        }
	}

    /**
     * 创建动态查询条件组合.
     */
    private Specification<AccountTemplate> buildSpecification(final AccountTemplate accountTemplate) {
        return new Specification<AccountTemplate> () {
            @Override
            public Predicate toPredicate(Root<AccountTemplate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if(null != accountTemplate){
                    if (StringUtil.isNotEmpty(accountTemplate.getTemplateName())) {
                        list.add(cb.like(root.get("templateName").as(String.class), "%" + accountTemplate.getTemplateName()+ "%"));
                    }
                    if (StringUtil.isNotEmpty(accountTemplate.getTemplateCode())) {
                        list.add(cb.like(root.get("templateCode").as(String.class), "%" + accountTemplate.getTemplateCode()+ "%"));
                    }
                    
                    RepFreq freq = accountTemplate.getRepFreq();
                    if (null != freq) {
                        list.add(cb.equal(root.get("repFreq").as(RepFreq.class), freq));
                    }
                }
                query.orderBy(cb.desc(root.get("id")));
                Predicate[] predicates = new Predicate[list.size()];
                predicates = list.toArray(predicates);
                return cb.and(predicates);
            }
        };
    }

    /**
     * 创建动态查询条件组合.
     */
    private Specification<AccountTemplate> buildServerSpecification(final AccountTemplate accountTemplate) {
        return new Specification<AccountTemplate> () {
            @Override
            public Predicate toPredicate(Root<AccountTemplate> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                List<Predicate> list = new ArrayList<Predicate>();
                if(null != accountTemplate){
                    if (StringUtil.isNotEmpty(accountTemplate.getTemplateName())) {
                        list.add(cb.equal(root.get("templateName").as(String.class), accountTemplate.getTemplateName()));
                    }
                    if (StringUtil.isNotEmpty(accountTemplate.getTemplateCode())) {
                        list.add(cb.equal(root.get("templateCode").as(String.class), accountTemplate.getTemplateCode()));
                    }
                    RepFreq freq = accountTemplate.getRepFreq();
                    if (null != freq) {
                        list.add(cb.equal(root.get("repFreq").as(RepFreq.class), freq));
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
    public void createODSAccount(AccountTemplate accountTemplate) {
        try {
            //建表语句没有问题，再创建台账物理表
            accountTemplate.setTableName(accountTemplate.getTemplateCode());
            //删除原来的数据表
            accountTemplateDAO.dropAllTemplate(accountTemplate);
            //新增添加字段的数据表
            accountTemplateDAO.createTemplate(accountTemplate);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
    }
}
