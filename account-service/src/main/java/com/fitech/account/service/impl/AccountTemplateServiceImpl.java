package com.fitech.account.service.impl;

import com.fitech.account.dao.AccountFieldDAO;
import com.fitech.account.dao.AccountTemplateDAO;
import com.fitech.account.repository.AccountFieldRepository;
import com.fitech.account.repository.AccountTemplateRepository;
import com.fitech.account.service.AccountFieldService;
import com.fitech.account.service.AccountTemplateService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.ledger.TableNameType;
import com.fitech.domain.report.BusSystem;
import com.fitech.domain.system.FieldPermission;
import com.fitech.domain.system.InstituteLevelType;
import com.fitech.domain.system.OperationType;
import com.fitech.domain.system.ProcessConfig;
import com.fitech.domain.system.ReportPermission;
import com.fitech.domain.system.SubSystem;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.DateUtils;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.system.repository.FieldPermissionRepository;
import com.fitech.system.repository.ProcessConfigRepository;
import com.fitech.system.repository.ReportPermissionRepository;
import com.fitech.system.service.ReportPermissionService;

import org.apache.poi.ss.formula.functions.Odd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.*;

/**
 * Created by wangxw on 2017/7/25.
 */
@Service
@ServiceTrace
public class AccountTemplateServiceImpl implements AccountTemplateService {

    @Autowired
    private AccountTemplateRepository accountTemplateRepository;

    @Autowired
    private AccountTemplateDAO accountTemplateDAO;
    
    @Autowired
    private AccountFieldDAO accountFieldDAO;

    @Autowired
    private AccountFieldService accountFieldService;
    
    @Autowired
    private ReportPermissionRepository reportPermissionRepository;

    @Autowired
    private FieldPermissionRepository fieldPermissionRepository;
    
    @Autowired
    private AccountFieldRepository accountFieldRepository;
    
    @Autowired
    private ProcessConfigRepository processConfigRepository;

    @Override
    public Page<AccountTemplate> findAccountTemplateByPage(AccountTemplate accountTemplate) {
        return accountTemplateRepository.findAll(buildSpecification(accountTemplate),buildPageRequest(accountTemplate));
    }

    @Override
    public List<AccountTemplate> findByBusSystem(final BusSystem busSystem) {
        if(null != busSystem && busSystem.getId() != null){
            return accountTemplateRepository.findAll(new Specification() {
                @Override
                public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder cb) {
                    List<Predicate> list = new ArrayList<Predicate>();

                    String todayDate = DateUtils.date2Str(new Date());

                    list.add(cb.equal(root.get("busSystem").as(BusSystem.class), busSystem));

                    list.add(cb.greaterThanOrEqualTo(root.get("endDate").as(String.class),todayDate));

                    list.add(cb.lessThanOrEqualTo(root.get("startDate").as(String.class),todayDate));

                    Predicate[] predicates = new Predicate[list.size()];
                    predicates = list.toArray(predicates);
                    return cb.and(predicates);
                }
            });
        }
        return null;
    }

    /**
     * 创建分页请求.
     */
    private PageRequest buildPageRequest(AccountTemplate accountTemplate) {
        // 页数、每页条数
        return new PageRequest(accountTemplate.getPageNum()- 1, accountTemplate.getPageSize());
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
                    if (StringUtil.isNotEmpty(accountTemplate.getFreq())) {
                        list.add(cb.like(root.get("freq").as(String.class), accountTemplate.getFreq()));
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
                    if (StringUtil.isNotEmpty(accountTemplate.getFreq())) {
                        list.add(cb.equal(root.get("freq").as(String.class), accountTemplate.getFreq()));
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
    @Transactional
    public GenericResult<Boolean> saveAccountTemplate(AccountTemplate accountTemplate) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            if(null != accountTemplate){
                if(StringUtil.isNotEmpty(accountTemplate.getTemplateCode()) &&
                        StringUtil.isNotEmpty(accountTemplate.getTemplateCode())){
                    if(valiAccountTemplateNameIsExist(accountTemplate.getTemplateName()).getRestCode().equals("")){
                        if(valiAccountTemplateCodeIsExist(accountTemplate.getTemplateCode()).getRestCode().equals("")){
                            accountTemplate.setTableName(accountTemplate.getTemplateCode());

                            Collection<AccountField> acField = accountTemplate.getAccountFields();
                            Iterator<AccountField> itaf = acField.iterator();
                            while (itaf.hasNext()){
                				AccountField af = itaf.next();
                				af.setItemCode(af.getItemCode().toUpperCase());
                            }
                            
                            accountTemplate.setAccountFields(accountFieldService.convertAccountField(accountTemplate.getAccountFields()));

                            BusSystem busSystem = new BusSystem();
                            busSystem.setId(192l);
                            accountTemplate.setBusSystem(busSystem);
                            accountTemplateRepository.save(accountTemplate);

                            //初始化补录权限
                            addBatch(accountTemplate,busSystem);

                            //初始化补录字段权限
                            addAccountFieldPermessionsBatch(accountTemplate);

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
    public GenericResult<Boolean> updateAccountTemplate(AccountTemplate accountTemplate) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            if(null != accountTemplate && null != accountTemplate.getId()){
                accountTemplate.setTableName(accountTemplate.getTemplateCode());
                BusSystem busSystem = new BusSystem();
                busSystem.setId(192l);
                accountTemplate.setBusSystem(busSystem);
                Collection<AccountField> acField = accountTemplate.getAccountFields();
                if(acField == null || acField.size() == 0){
                    if(StringUtil.isNotEmpty(accountTemplate.getTemplateName())){
                        AccountTemplate at = accountTemplateRepository.findOne(accountTemplate.getId());

                        boolean valiResult = true;
                        if(!at.getTemplateName().equals(accountTemplate.getTemplateName())){
                            if(valiAccountTemplateNameIsExist(accountTemplate.getTemplateName()).
                                    getRestCode().equals(ExceptionCode.ONLY_VALIDATION_FALSE)){
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
                        accountFieldService.deleteAccountFieldByAccountTemplate(accountTemplate);

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

    @Transactional("transactionManagerSjbl")
    private void createODSAccount(AccountTemplate accountTemplate) {
        try {
            //创建模拟测试表名
            Random random = new Random();
            String mockTableName = accountTemplate.getTableName()+ random.nextInt(200);
            accountTemplate.setTableName(mockTableName);
            //测试创建表语句是否有问题
//            accountTemplateDAO.createTemplate(accountTemplate);
//            accountTemplateDAO.dropAllTemplate(accountTemplate);

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

    @Override
    @Transactional
    public GenericResult<Boolean> deleteAccountTemplateByList(List<Long> idList) {
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
                	subSystem.setId(27l);
                	List<ProcessConfig> processConfigs = processConfigRepository.findBySubSystem(subSystem);
                	//循环判断模板是否被流程应用
                	for(ProcessConfig processConfig: processConfigs){
                		if(flag.equals("process")){
                			break;
                		}
                		Collection<AccountTemplate> aTemplates = processConfig.getSjblRpts();
                		 Iterator<AccountTemplate> it = aTemplates.iterator();
                	     while(it.hasNext()){
                	    	 AccountTemplate template = it.next();
                	    	 if(accountTemplate.getId().equals(template.getId())){
                	    		 flag = "process";
                	    	 }
                	     }
                	}	

                	if(flag.equals("true")){
                    	delBatch(accountTemplateRepository.findById(idList.get(i)));
                    	delFieldBatch(accountTemplateRepository.findById(idList.get(i)));
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
    public AccountTemplate findAccountTemplateById(Long id) {
        AccountTemplate accountTemplate = null;
        if(null != id){
            accountTemplate = accountTemplateRepository.findOne(id);
            List<AccountField> a = (List<AccountField>) accountTemplate.getAccountFields();
            Collections.sort(a, new Comparator<AccountField>() {

				@Override
				public int compare(AccountField o1, AccountField o2) {
					if (o1.getOrderNumber() < o2.getOrderNumber())
	                    return -1;
	                else if (o1.getOrderNumber() > o2.getOrderNumber())
	                    return 1;
	                else
	                    return o1.getId().compareTo(o2.getId());
				}
			});
            accountTemplate.setAccountFields(a);
        }
        return accountTemplate;
    }

    @Override
    public GenericResult<Boolean> valiAccountTemplateNameIsExist(String templateName) {
        GenericResult<Boolean> result = new GenericResult<>();

        if(StringUtil.isNotEmpty(templateName)){
            AccountTemplate accountTemplate = new AccountTemplate();
            accountTemplate.setTemplateName(templateName);
            List<AccountTemplate> accountTemplateList = accountTemplateRepository.findAll(buildServerSpecification(accountTemplate));
            if(null != accountTemplateList && !accountTemplateList.isEmpty()){
                result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
            }
        }
        return result;
    }

    @Override
    public GenericResult<Boolean> valiAccountTemplateCodeIsExist(String templateCode) {
        GenericResult<Boolean> result = new GenericResult<>();

        if(StringUtil.isNotEmpty(templateCode)){
            AccountTemplate accountTemplate = new AccountTemplate();
            accountTemplate.setTemplateCode(templateCode);
            List<AccountTemplate> accountTemplateList = accountTemplateRepository.findAll(buildServerSpecification(accountTemplate));
            if(null != accountTemplateList && !accountTemplateList.isEmpty()){
                result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
            }
        }
        return result;
    }

    public Collection<FieldPermission> addAccountFieldPermessionsBatch(AccountTemplate accountTemplate) throws Exception {
        //最新的报文字段
        Collection<AccountField> ledgerItems=accountTemplate.getAccountFields();
        //数据库中已有的报文权限数据
        Collection<FieldPermission> hasPermission = fieldPermissionRepository.findByAccountTemplate(accountTemplate);
        List<Long> hasPermissionId = new ArrayList<Long>();
        for (FieldPermission permission : hasPermission) {
            //对 没有权限的字段 进行权限初始化分配
        	hasPermissionId.add(permission.getAccountField().getId());
        }
        //待生成的报文权限
        Collection<FieldPermission> fieldPermissions=new ArrayList<FieldPermission>();
        Iterator<AccountField> it=ledgerItems.iterator();
        while(it.hasNext()){
            AccountField ledgerItem=it.next();
            if(ledgerItem.isPkable()){
                continue;
            }
            if(null != hasPermission && hasPermission.size()>0){
                if(!hasPermissionId.contains(ledgerItem.getId())){
                    FieldPermission fieldPermission=new FieldPermission();
                    fieldPermission.setAccountField(ledgerItem);
                    fieldPermission.setAccountTemplate(accountTemplate);
                    fieldPermission.setOperationType(OperationType.LOOK);
                    FieldPermission operateFieldPermission=new FieldPermission();
                    operateFieldPermission.setOperationType(OperationType.OPERATE);
                    operateFieldPermission.setAccountField(ledgerItem);
                    operateFieldPermission.setAccountTemplate(accountTemplate);
                    fieldPermissions.add(operateFieldPermission);
                    fieldPermissions.add(fieldPermission);
                    break;
                }
            }else{
                FieldPermission fieldPermission=new FieldPermission();
                fieldPermission.setAccountField(ledgerItem);
                fieldPermission.setAccountTemplate(accountTemplate);
                fieldPermission.setOperationType(OperationType.LOOK);
                FieldPermission operateFieldPermission=new FieldPermission();
                operateFieldPermission.setOperationType(OperationType.OPERATE);
                operateFieldPermission.setAccountField(ledgerItem);
                operateFieldPermission.setAccountTemplate(accountTemplate);
                fieldPermissions.add(operateFieldPermission);
                fieldPermissions.add(fieldPermission);
            }

        }
        return fieldPermissionRepository.save(fieldPermissions);
    }

    @Override
    @Transactional
    public void addBatch(AccountTemplate accountTemplate, BusSystem busSystem) throws Exception {
        Collection<ReportPermission> rps=new ArrayList<ReportPermission>();
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                ReportPermission rp=new ReportPermission();
                rp.setBusSystem(busSystem);
                rp.setAccountTemplate(accountTemplate);
                if(i==0){
                    rp.setOperationType(OperationType.LOOK);
                }else if(i==1){
                    rp.setOperationType(OperationType.OPERATE);
                }else{
                    rp.setOperationType(OperationType.CHECK);
                }

                if(j==0){
                    rp.setInstituteLevel(InstituteLevelType.CURRENT);
                }else if(j==1){
                    rp.setInstituteLevel(InstituteLevelType.NEXT);
                }else{
                    rp.setInstituteLevel(InstituteLevelType.NEXTALL);
                }
                rps.add(rp);
            }
        }
        reportPermissionRepository.save(rps);
    }

    @Override
    @Transactional
    public GenericResult<Boolean> addAccountTemplateField(AccountTemplate accountTemplate) {
    	GenericResult<Boolean> result = new GenericResult<>();
        try {
        	String flag = valiAccountTemplateIsDelete(accountTemplate.getId());
        	if(!flag.equals("true")){
				result.setSuccess(false);
				result.setMessage(flag + "，不可新增");;
				return result;
			}
        	//获取原本该模板
			AccountTemplate acTemplate = accountTemplateRepository.findById(accountTemplate.getId());
        	//如果报文模板对应的数据表没有数据,则可以修改此报文下的字段信息
            Boolean f = accountTemplateDAO.dataIsExist(acTemplate);
            if (!f) {
                //原有字段集合
                Collection<AccountField> oldAcField = acTemplate.getAccountFields();      
                //新增字段集合
                Collection<AccountField> acField = accountTemplate.getAccountFields();
                Iterator<AccountField> itaf = acField.iterator();
                //循环将字段编码转为大写
                while (itaf.hasNext()){
     				AccountField af = itaf.next();
     				Iterator<AccountField> it = oldAcField.iterator();
     				while (it.hasNext()){
     					AccountField a = it.next();
     					if(a.getItemCode().toUpperCase().equals(af.getItemCode().toUpperCase())){
     						result.setSuccess(false);
     		                result.setMessage("列名重复!");
     		                return result;
     					}
     				}
     				af.setItemCode(af.getItemCode().toUpperCase());
     				oldAcField.add(af);
                }
                acTemplate.setAccountFields(accountFieldService.modifyAccountField(oldAcField));
                accountTemplateRepository.save(acTemplate);                
                //初始化补录字段权限
                addAccountFieldPermessionsBatch(acTemplate);
                createODSAccount(acTemplate);
            }else{
            	result.setSuccess(false);
                result.setMessage("台账表已存在数据，新增字段失败!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
        return result;
    }

    @Override
    @Transactional
    public GenericResult<Boolean> modifyAccountTemplateField(AccountTemplate accountTemplate) {
    	GenericResult<Boolean> result = new GenericResult<>();
		try {
			//获取修改的字段集合
			Collection<AccountField> acField = accountTemplate.getAccountFields();
			String flag = valiAccountTemplateIsDelete(accountTemplate.getId());
        	if(!flag.equals("true")){
				result.setSuccess(false);
				result.setMessage(flag + "，不可修改");;
				return result;
			}
			//获取原本该模板
			AccountTemplate acTemplate = accountTemplateRepository.findById(accountTemplate.getId());
			//如果报文模板对应的数据表没有数据,则可以修改此报文下的字段信息
            Boolean f = accountTemplateDAO.dataIsExist(acTemplate);
            if (!f) {
    			//原本的字段集合
    			Collection<AccountField> oldAcField = acTemplate.getAccountFields();
    			//迭代修改字段集合
    			Iterator<AccountField> itaf = acField.iterator();
    			Long oid = null;
    			AccountField newAcField = new AccountField();
    			while (itaf.hasNext()) {
    				AccountField af = itaf.next();
    				af.setItemCode(af.getItemCode().toUpperCase());
    				Iterator<AccountField> it = oldAcField.iterator();
    				//迭代原本的字段集合
    				while (it.hasNext()) {
    					AccountField a = it.next();
    					//判断列名是否重复(除了本身)
    					if(af.getItemCode().toUpperCase().equals(a.getItemCode().toUpperCase()) && !a.getId().equals(af.getId())){
    						result.setSuccess(false);
    		                result.setMessage("列名重复!");
    		                return result;
    					}else{
    						//找到修改字段id在原本字段集合中的位置
        					if (a.getId().equals(af.getId())) {
        						//从集合中删除
        						oid = a.getId();
        						newAcField = af;
        						oldAcField.remove(a);
        						delOneFieldPermission(a.getId());
        						break;
        					}
    					}
    				}
    			}
    			//清空关联表，并删除字段
    			accountTemplateRepository.save(acTemplate);
    			accountFieldRepository.delete(oid);
    			//保存新字段
    			oldAcField.add(newAcField);
    			acTemplate.setAccountFields(accountFieldService.modifyAccountField(oldAcField));
    			accountTemplateRepository.save(acTemplate);
    			//初始化补录字段权限
    	        addAccountFieldPermessionsBatch(acTemplate);
                createODSAccount(acTemplate);
            }else{
            	result.setSuccess(false);
                result.setMessage("台账表已存在数据，修改字段失败!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
        return result;
    }

    @Override
    public GenericResult<Boolean> deleteAccountTemplateField(Long accountTemplateId, Long accountFieldId) {
    	GenericResult<Boolean> result = new GenericResult<>();
		try {
			String flag = valiAccountTemplateIsDelete(accountTemplateId);
        	if(!flag.equals("true")){
				result.setSuccess(false);
				result.setMessage(flag + "，不可删除");;
				return result;
			}
			AccountTemplate accountTemplate = accountTemplateRepository.findById(accountTemplateId);
			//如果报文模板对应的数据表没有数据,则可以修改此报文下的字段信息
            Boolean f = accountTemplateDAO.dataIsExist(accountTemplate);
            if (!f) {
            	delOneFieldPermission(accountFieldId);
    			//获取原本该模板
    			AccountTemplate acTemplate = accountTemplateRepository.findById(accountTemplateId);
    			//原本的字段集合
    			Collection<AccountField> oldAcField = acTemplate.getAccountFields();
    			Iterator<AccountField> it = oldAcField.iterator();
    			//迭代原本的字段集合
    			while (it.hasNext()) {
    				AccountField a = it.next();
    				//找到修改字段id在原本字段集合中的位置
    				if (a.getId().equals(accountFieldId)) {
    					//从集合中删除
    					oldAcField.remove(a);
    					break;
    				}
    			}
    			accountTemplateRepository.save(acTemplate);
    			accountFieldRepository.delete(accountFieldId);

                createODSAccount(acTemplate);
            }else{
            	result.setSuccess(false);
                result.setMessage("台账表已存在数据，删除字段失败!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
        return result;
    }

    @Override
    public GenericResult<AccountField> findOneAccountTemplateField(AccountTemplate accountTemplate) {
        return null;
    }

	@Override
	public List<AccountTemplate> findAllAccountTemplate(){
    	return accountTemplateRepository.findAll();
    }

	@Override
	public Collection<AccountField> findAccountFieldByIdVisable(Long id) {
        AccountTemplate accountTemplate = null;
        Collection<AccountField> newCollection=null;
        if(null != id){
            try{
            	accountTemplate = accountTemplateRepository.findOne(id);
            }catch (Exception e) {
                e.printStackTrace();
                throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
            }
          Collection<AccountField> collection = accountTemplate.getAccountFields();
          newCollection = new ArrayList<AccountField>();
          for(AccountField accountField:collection){
        	  if(accountField.isVisible()){
        		  newCollection.add(accountField);
        	  }
          }
        }
        return newCollection;
    }

	@Override
	public void delBatch(AccountTemplate accountTemplate) {
		Collection<FieldPermission> rps=reportPermissionRepository.findByAccountTemplate(accountTemplate);
        if (rps!=null && rps.size()>0){
            reportPermissionRepository.delete(rps);
        }
	}

	@Override
	public void delFieldBatch(AccountTemplate accountTemplate) {
		 Collection<FieldPermission> rfps=fieldPermissionRepository.findByAccountTemplate(accountTemplate);
		 if (rfps!=null && rfps.size()>0){
			 fieldPermissionRepository.delete(rfps);
		 }
	}

	@Override
	public void delOneFieldPermission(Long accountFieldId) {
		Collection<FieldPermission> fieldPermissions = fieldPermissionRepository.findByAccountFieldId(accountFieldId);
		fieldPermissionRepository.delete(fieldPermissions);
	}
}
