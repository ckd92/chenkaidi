package com.fitech.account.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.dao.AccountFieldDAO;
import com.fitech.account.dao.AccountTemplateDAO;
import com.fitech.account.repository.AccountFieldRepository;
import com.fitech.account.repository.AccountTemplateRepository;
import com.fitech.account.repository.DictionaryRepository;
import com.fitech.account.service.AccountFieldService;
import com.fitech.account.service.AccountTemplateService;
import com.fitech.account.util.AccountFieldUtil;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.account.CodeField;
import com.fitech.domain.account.DateField;
import com.fitech.domain.account.DecimalField;
import com.fitech.domain.account.DoubleField;
import com.fitech.domain.account.IntegerField;
import com.fitech.domain.account.StringField;
import com.fitech.domain.system.FieldPermission;
import com.fitech.enums.system.OperationEnum;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.system.repository.FieldPermissionRepository;

/**
 * Created by wangxw on 2017/8/10.
 */
@Service
@ServiceTrace
public class AccountFieldServiceImpl implements AccountFieldService {
    @Autowired
    private AccountTemplateRepository accountTemplateRepository;
    @Autowired
    private AccountFieldRepository accountFieldRepository;
    @Autowired
    private FieldPermissionRepository<FieldPermission> fieldPermissionRepository;
    @Autowired
	private DictionaryRepository dictionaryRepository;
    
    @Autowired
    private AccountTemplateDAO accountTemplateDAO;
    @Autowired
    private AccountTemplateService accountTemplateService;

    @Override
    @Transactional
    public GenericResult<Boolean> save(AccountTemplate accountTemplate) {
    	GenericResult<Boolean> result = new GenericResult<>();
        try {
        	String flag = accountTemplateService.valiAccountTemplateIsDelete(accountTemplate.getId());
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
                acTemplate.setAccountFields(this.convertAccountField(oldAcField));
                accountTemplateRepository.save(acTemplate);                
                //初始化补录字段权限
                addAccountFieldPermessionsBatch(acTemplate);
                accountTemplateService.createODSAccount(acTemplate);
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
    public GenericResult<Boolean> modify(AccountTemplate accountTemplate) {
    	GenericResult<Boolean> result = new GenericResult<>();
		try {
            //获取原本该模板
            AccountTemplate acTemplate = accountTemplateRepository.findById(accountTemplate.getId());
            
            //获取待修改的字段集合
			Collection<AccountField> acField = accountTemplate.getAccountFields();
            //原本的字段集合
            Collection<AccountField> oldAcField = acTemplate.getAccountFields();

            Map<Boolean,AccountField> map = this.valiAccountTemplateIsUpdate(acField,oldAcField);
            //key为true的值不为空，说明可以直接修改
            if(map.get(true) != null){
                this.accountFieldRepository.save(map.get(true));
                return result;
            }

			String flag = accountTemplateService.valiAccountTemplateIsDelete(accountTemplate.getId());
        	if(!flag.equals("true")){
				result.setSuccess(false);
				result.setMessage(flag + "，不可修改");;
				return result;
			}

			//如果报文模板对应的数据表没有数据,则可以修改此报文下的字段信息
            Boolean f = accountTemplateDAO.dataIsExist(acTemplate);
            if (!f) {
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
        						delFieldPermission(a.getId());
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
    			acTemplate.setAccountFields(this.convertAccountField(oldAcField));
    			accountTemplateRepository.save(acTemplate);
    			//初始化补录字段权限
    	        addAccountFieldPermessionsBatch(acTemplate);
    	        accountTemplateService.createODSAccount(acTemplate);
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
    public GenericResult<Boolean> delete(Long accountTemplateId, Long accountFieldId) {
    	GenericResult<Boolean> result = new GenericResult<>();
		try {
			String flag = accountTemplateService.valiAccountTemplateIsDelete(accountTemplateId);
        	if(!flag.equals("true")){
				result.setSuccess(false);
				result.setMessage(flag + "，不可删除");;
				return result;
			}
			AccountTemplate accountTemplate = accountTemplateRepository.findById(accountTemplateId);
			//如果报文模板对应的数据表没有数据,则可以修改此报文下的字段信息
            Boolean f = accountTemplateDAO.dataIsExist(accountTemplate);
            if (!f) {
            	delFieldPermission(accountFieldId);
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

    			accountTemplateService.createODSAccount(acTemplate);
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
    @Transactional
    public void deleteAccountFields(AccountTemplate accountTemplate) {
        if(null != accountTemplate){
            AccountTemplate fullAccountTemplate = accountTemplateRepository.findOne(accountTemplate.getId());
            try {
                if(null != fullAccountTemplate.getAccountFields() && !fullAccountTemplate.getAccountFields().isEmpty()){
                    for (AccountField accountField : fullAccountTemplate.getAccountFields()){
                        accountFieldRepository.delete(accountField);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
            }
        }
    }
    
    @Override
	public Collection<AccountField> findVisableField(Long id) {
        AccountTemplate accountTemplate = null;
        Collection<AccountField> newCollection = new ArrayList<AccountField>();
        if(null != id){
            try{
            	accountTemplate = accountTemplateRepository.findOne(id);
            	Collection<AccountField> collection = accountTemplate.getAccountFields();
                for(AccountField accountField:collection){
                	if(accountField.isVisible()){
                		newCollection.add(accountField);
                	}
                }
            }catch (Exception e) {
                e.printStackTrace();
                throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
            }
        }
        return newCollection;
    }
    
    @Override
    public Collection<FieldPermission> addFieldPermessions(AccountTemplate accountTemplate) {
        //最新的报文字段
        Collection<AccountField> ledgerItems=accountTemplate.getAccountFields();
        //数据库中已有的报文权限数据
        Collection<FieldPermission> hasPermission = fieldPermissionRepository.findByReportTemplate(accountTemplate);
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
                    fieldPermission.setReportTemplate(accountTemplate);
                    fieldPermission.setOperationType(OperationEnum.LOOK);
                    FieldPermission operateFieldPermission=new FieldPermission();
                    operateFieldPermission.setOperationType(OperationEnum.OPERATE);
                    operateFieldPermission.setAccountField(ledgerItem);
                    operateFieldPermission.setReportTemplate(accountTemplate);
                    fieldPermissions.add(operateFieldPermission);
                    fieldPermissions.add(fieldPermission);
                    break;
                }
            }else{
                FieldPermission fieldPermission=new FieldPermission();
                fieldPermission.setAccountField(ledgerItem);
                fieldPermission.setReportTemplate(accountTemplate);
                fieldPermission.setOperationType(OperationEnum.LOOK);
                FieldPermission operateFieldPermission=new FieldPermission();
                operateFieldPermission.setOperationType(OperationEnum.OPERATE);
                operateFieldPermission.setAccountField(ledgerItem);
                operateFieldPermission.setReportTemplate(accountTemplate);
                fieldPermissions.add(operateFieldPermission);
                fieldPermissions.add(fieldPermission);
            }

        }
        return fieldPermissionRepository.save(fieldPermissions);
    }
    
    @Override
	public void delFieldPermessions(AccountTemplate accountTemplate) {
		 Collection<FieldPermission> rfps=fieldPermissionRepository.findByReportTemplate(accountTemplate);
		 if (rfps!=null && rfps.size()>0){
			 fieldPermissionRepository.delete(rfps);
		 }
	}
    
    @Override
    public Collection<AccountField> convertAccountField(Collection<AccountField> ledgerItems) {
        Collection<AccountField> newItems = null;
        try {
            newItems = new ArrayList<>();
            for (AccountField ledgerItem : ledgerItems) {
                if (ledgerItem instanceof CodeField || ledgerItem instanceof DateField || ledgerItem instanceof DoubleField
                        || ledgerItem instanceof StringField || ledgerItem instanceof IntegerField || ledgerItem instanceof DecimalField) {
                    newItems.add(ledgerItem);
                } else {
                    newItems.add(AccountFieldUtil.getLedgerItem(ledgerItem));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }
        return newItems;
    }

	/**
     * 判断是否可以直接修改，只修改  （查询，修改，只读 ，字段描述）  部分时，可以直接修改
     * @param acField 修改的值
     * @param oldAcField 原有值
     * @return  key值表示是否可以修改，value表示修改的AccountField
     */
    private Map<Boolean,AccountField> valiAccountTemplateIsUpdate(Collection<AccountField> acField, Collection<AccountField> oldAcField ){
        Map<Boolean,AccountField> result = new HashMap<>();
        Boolean flag = false;
        // 待修改的字段实例
        AccountField resultAccountField = null;
        
        for(AccountField accountField:acField){
            for(AccountField accountField1:oldAcField){
                if(accountField.getId().equals(accountField1.getId())){
                	// 如果列名，字段类型和字段长度发生变更，则不可直接修改字段实例
                	if(accountField.getItemCode() != accountField1.getItemCode() ||
                			accountField.getItemType() != accountField1.getItemType() ||
                			accountField.getLength() != accountField1.getLength()){
                		flag = false;
                	}else{
                		if("CODELIB".equals(accountField.getItemType())){
                			CodeField codeField = (CodeField)accountField1;
                			codeField.getDictionary().setId(Long.parseLong(accountField.getDicId()));
                			codeField.setVisible(accountField.isVisible());
                			codeField.setEditable(accountField.isEditable());
                			codeField.setSearchable(accountField.isSearchable());
                			codeField.setOrderNumber(accountField.getOrderNumber());
                			codeField.setRequire(accountField.isRequire());
                			codeField.setPkable(accountField.isPkable());
                			codeField.setItemName(accountField.getItemName());
                			codeField.setItemDescription(accountField.getItemDescription());
                			codeField.setDicId(accountField.getDicId());
                			resultAccountField = codeField;
                		}else{
                			flag = true;
    						accountField1.setVisible(accountField.isVisible());
    						accountField1.setEditable(accountField.isEditable());
    						accountField1.setSearchable(accountField.isSearchable());
    						accountField1.setOrderNumber(accountField.getOrderNumber());
    						accountField1.setRequire(accountField.isRequire());
    						accountField1.setPkable(accountField.isPkable());
    						accountField1.setItemName(accountField.getItemName());
    						accountField1.setItemDescription(accountField.getItemDescription());
    						accountField1.setDicId(accountField.getDicId());
    						resultAccountField = accountField1;
                		}
                	}
                }
            }
        }
        result.put(flag,resultAccountField);
        return result;
    }
	
	private Collection<FieldPermission> addAccountFieldPermessionsBatch(AccountTemplate accountTemplate) throws Exception {
        //最新的报文字段
        Collection<AccountField> ledgerItems=accountTemplate.getAccountFields();
        //数据库中已有的报文权限数据
        Collection<FieldPermission> hasPermission = fieldPermissionRepository.findByReportTemplate(accountTemplate);
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
                    fieldPermission.setReportTemplate(accountTemplate);
                    fieldPermission.setOperationType(OperationEnum.LOOK);
                    FieldPermission operateFieldPermission=new FieldPermission();
                    operateFieldPermission.setOperationType(OperationEnum.OPERATE);
                    operateFieldPermission.setAccountField(ledgerItem);
                    operateFieldPermission.setReportTemplate(accountTemplate);
                    fieldPermissions.add(operateFieldPermission);
                    fieldPermissions.add(fieldPermission);
                    break;
                }
            }else{
                FieldPermission fieldPermission=new FieldPermission();
                fieldPermission.setAccountField(ledgerItem);
                fieldPermission.setReportTemplate(accountTemplate);
                fieldPermission.setOperationType(OperationEnum.LOOK);
                FieldPermission operateFieldPermission=new FieldPermission();
                operateFieldPermission.setOperationType(OperationEnum.OPERATE);
                operateFieldPermission.setAccountField(ledgerItem);
                operateFieldPermission.setReportTemplate(accountTemplate);
                fieldPermissions.add(operateFieldPermission);
                fieldPermissions.add(fieldPermission);
            }

        }
        return fieldPermissionRepository.save(fieldPermissions);
    }
	
	private void delFieldPermission(Long accountFieldId) {
		Collection<FieldPermission> fieldPermissions = fieldPermissionRepository.findByAccountFieldId(accountFieldId);
		fieldPermissionRepository.delete(fieldPermissions);
	}
}
