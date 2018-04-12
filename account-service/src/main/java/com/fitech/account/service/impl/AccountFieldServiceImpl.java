package com.fitech.account.service.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.repository.AccountFieldRepository;
import com.fitech.account.repository.AccountTemplateRepository;
import com.fitech.account.repository.DictionaryRepository;
import com.fitech.account.service.AccountFieldService;
import com.fitech.account.util.AccountFieldUtil;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.account.CodeField;
import com.fitech.domain.account.DateField;
import com.fitech.domain.account.DecimalField;
import com.fitech.domain.account.Dictionary;
import com.fitech.domain.account.DoubleField;
import com.fitech.domain.account.IntegerField;
import com.fitech.domain.account.StringField;
import com.fitech.enums.SqlTypeEnum;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;

/**
 * Created by wangxw on 2017/8/10.
 */
@Service
@ServiceTrace
public class AccountFieldServiceImpl implements AccountFieldService {

    @Autowired
    private AccountFieldRepository accountFieldRepository;

    @Autowired
    private AccountTemplateRepository accountTemplateRepository;
    
    @Autowired
	private DictionaryRepository dictionaryRepository;

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

    @Override
    @Transactional
    public void deleteAccountFieldByAccountTemplate(AccountTemplate accountTemplate) {
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
	public AccountField getLedgerItem(AccountField ledgerItem) {
		if ("CODELIB".equals(ledgerItem.getItemType())) {
            CodeField codeLedgerItem = new CodeField();
            codeLedgerItem.setId(null);
            codeLedgerItem.setItemCode(ledgerItem.getItemCode() == null ? "" : ledgerItem.getItemCode());
            codeLedgerItem.setItemName(ledgerItem.getItemName() == null ? "" : ledgerItem.getItemName());
            codeLedgerItem.setSearchable(ledgerItem.isSearchable());
            codeLedgerItem.setSqlType(SqlTypeEnum.VARCHAR);
            codeLedgerItem.setPkable(ledgerItem.isPkable());
            codeLedgerItem.setEditable(ledgerItem.isEditable());
            codeLedgerItem.setItemType(ledgerItem.getItemType() == null ? "" : ledgerItem.getItemType());
            codeLedgerItem.setVisible(ledgerItem.isVisible());
            codeLedgerItem.setRequire(ledgerItem.isRequire());
            codeLedgerItem.setCover(ledgerItem.isCover());
            codeLedgerItem.setOrderNumber(ledgerItem.getOrderNumber());
            codeLedgerItem.setTemplateId(ledgerItem.getTemplateId());
            codeLedgerItem.setItemDescription(ledgerItem.getItemDescription()==null?"":ledgerItem.getItemDescription());
            codeLedgerItem.setDicId(ledgerItem.getDicId());
            if (ledgerItem.getDicId() != null&&!"".equals(ledgerItem.getDicId())) {
            	Long id = Long.valueOf(ledgerItem.getDicId());
                Dictionary codeLib = dictionaryRepository.findOne(id);
                codeLedgerItem.setDictionary(codeLib);
            }
            return codeLedgerItem;

        } else if ("DOUBLE".equals(ledgerItem.getItemType())) {
            DoubleField doubleLedgerItem = new DoubleField();
            doubleLedgerItem.setId(null);
            doubleLedgerItem.setItemCode(ledgerItem.getItemCode() == null ? "" : ledgerItem.getItemCode());
            doubleLedgerItem.setItemName(ledgerItem.getItemName() == null ? "" : ledgerItem.getItemName());
            doubleLedgerItem.setSearchable(ledgerItem.isSearchable());
            doubleLedgerItem.setSqlType(SqlTypeEnum.DOUBLE);
            doubleLedgerItem.setPkable(ledgerItem.isPkable());
            doubleLedgerItem.setEditable(ledgerItem.isEditable());
            doubleLedgerItem.setItemType(ledgerItem.getItemType() == null ? "" : ledgerItem.getItemType());
            doubleLedgerItem.setVisible(ledgerItem.isVisible());
            doubleLedgerItem.setRequire(ledgerItem.isRequire());
            doubleLedgerItem.setCover(ledgerItem.isCover());
            doubleLedgerItem.setOrderNumber(ledgerItem.getOrderNumber());
            doubleLedgerItem.setTemplateId(ledgerItem.getTemplateId());
            doubleLedgerItem.setItemDescription(ledgerItem.getItemDescription()==null?"":ledgerItem.getItemDescription());
            return doubleLedgerItem;

        } else if ("INTEGER".equals(ledgerItem.getItemType())) {
            IntegerField integerItem = new IntegerField();
            integerItem.setId(null);
            integerItem.setItemCode(ledgerItem.getItemCode() == null ? "" : ledgerItem.getItemCode());
            integerItem.setItemName(ledgerItem.getItemName() == null ? "" : ledgerItem.getItemName());
            integerItem.setSearchable(ledgerItem.isSearchable());
            integerItem.setSqlType(SqlTypeEnum.INTEGER);
            integerItem.setPkable(ledgerItem.isPkable());
            integerItem.setEditable(ledgerItem.isEditable());
            integerItem.setItemType(ledgerItem.getItemType() == null ? "" : ledgerItem.getItemType());
            integerItem.setVisible(ledgerItem.isVisible());
            integerItem.setRequire(ledgerItem.isRequire());
            integerItem.setCover(ledgerItem.isCover());
            integerItem.setOrderNumber(ledgerItem.getOrderNumber());
            integerItem.setTemplateId(ledgerItem.getTemplateId());
            integerItem.setItemDescription(ledgerItem.getItemDescription()==null?"":ledgerItem.getItemDescription());
            return integerItem;

        } else if ("DATE".equals(ledgerItem.getItemType())) {
            DateField dateLedgerItem = new DateField();
            dateLedgerItem.setId(null);
            dateLedgerItem.setItemCode(ledgerItem.getItemCode() == null ? "" : ledgerItem.getItemCode());
            dateLedgerItem.setItemName(ledgerItem.getItemName() == null ? "" : ledgerItem.getItemName());
            dateLedgerItem.setSearchable(ledgerItem.isSearchable());
            dateLedgerItem.setSqlType(SqlTypeEnum.DATE);
            dateLedgerItem.setPkable(ledgerItem.isPkable());
            dateLedgerItem.setEditable(ledgerItem.isEditable());
            dateLedgerItem.setItemType(ledgerItem.getItemType() == null ? "" : ledgerItem.getItemType());
            dateLedgerItem.setVisible(ledgerItem.isVisible());
            dateLedgerItem.setRequire(ledgerItem.isRequire());
            dateLedgerItem.setCover(ledgerItem.isCover());
            dateLedgerItem.setOrderNumber(ledgerItem.getOrderNumber());
            dateLedgerItem.setTemplateId(ledgerItem.getTemplateId());
            dateLedgerItem.setItemDescription(ledgerItem.getItemDescription()==null?"":ledgerItem.getItemDescription());
            return dateLedgerItem;

        } else if ("VARCHAR".equals(ledgerItem.getItemType())) {
            StringField stringLedgerItem = new StringField();
            stringLedgerItem.setId(null);
            stringLedgerItem.setItemCode(ledgerItem.getItemCode() == null ? "" : ledgerItem.getItemCode());
            stringLedgerItem.setItemName(ledgerItem.getItemName() == null ? "" : ledgerItem.getItemName());
            stringLedgerItem.setSearchable(ledgerItem.isSearchable());
            stringLedgerItem.setSqlType(SqlTypeEnum.VARCHAR);
            stringLedgerItem.setPkable(ledgerItem.isPkable());
            stringLedgerItem.setEditable(ledgerItem.isEditable());
            stringLedgerItem.setItemType(ledgerItem.getItemType() == null ? "" : ledgerItem.getItemType());
            stringLedgerItem.setVisible(ledgerItem.isVisible());
            stringLedgerItem.setRequire(ledgerItem.isRequire());
            stringLedgerItem.setCover(ledgerItem.isCover());
            stringLedgerItem.setOrderNumber(ledgerItem.getOrderNumber());
            stringLedgerItem.setTemplateId(ledgerItem.getTemplateId());
            stringLedgerItem.setItemDescription(ledgerItem.getItemDescription()==null?"":ledgerItem.getItemDescription());
            stringLedgerItem.setLength(ledgerItem.getLength());
            return stringLedgerItem;
        } else if("DECIMAL".equals(ledgerItem.getItemType())){
			DecimalField decimalLedgerItem = new DecimalField();
            decimalLedgerItem.setId(null);
            decimalLedgerItem.setItemCode(ledgerItem.getItemCode() == null ? "" : ledgerItem.getItemCode());
            decimalLedgerItem.setItemName(ledgerItem.getItemName() == null ? "" : ledgerItem.getItemName());
            decimalLedgerItem.setSearchable(ledgerItem.isSearchable());
            decimalLedgerItem.setSqlType(SqlTypeEnum.DECIMAL);
            decimalLedgerItem.setPkable(ledgerItem.isPkable());
            decimalLedgerItem.setEditable(ledgerItem.isEditable());
            decimalLedgerItem.setItemType(ledgerItem.getItemType() == null ? "" : ledgerItem.getItemType());
            decimalLedgerItem.setVisible(ledgerItem.isVisible());
            decimalLedgerItem.setRequire(ledgerItem.isRequire());
            decimalLedgerItem.setCover(ledgerItem.isCover());
            decimalLedgerItem.setOrderNumber(ledgerItem.getOrderNumber());
            decimalLedgerItem.setTemplateId(ledgerItem.getTemplateId());
            decimalLedgerItem.setItemDescription(ledgerItem.getItemDescription()==null?"":ledgerItem.getItemDescription());
            return decimalLedgerItem;

        }else {
            return null;
        }
	}

	@Override
	public Collection<AccountField> modifyAccountField(Collection<AccountField> ledgerItems) {
		Collection<AccountField> newItems = null;
        try {
            newItems = new ArrayList<>();
            for (AccountField ledgerItem : ledgerItems) {
                if (ledgerItem instanceof CodeField || ledgerItem instanceof DateField || ledgerItem instanceof DoubleField
                        || ledgerItem instanceof StringField || ledgerItem instanceof IntegerField || ledgerItem instanceof DecimalField) {
                    newItems.add(ledgerItem);
                } else {
                    newItems.add(getLedgerItem(ledgerItem));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }
        return newItems;
	}
}
