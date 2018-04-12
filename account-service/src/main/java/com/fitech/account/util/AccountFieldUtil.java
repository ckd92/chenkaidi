package com.fitech.account.util;

import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.CodeField;
import com.fitech.domain.account.DateField;
import com.fitech.domain.account.DecimalField;
import com.fitech.domain.account.Dictionary;
import com.fitech.domain.account.DoubleField;
import com.fitech.domain.account.IntegerField;
import com.fitech.domain.account.StringField;
import com.fitech.enums.SqlTypeEnum;

/**
 * Created by wangxw on 2017/8/10.
 */
public class AccountFieldUtil {
    public static AccountField getLedgerItem(AccountField ledgerItem) {

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
            codeLedgerItem.setDicId(ledgerItem.getDicId());
            codeLedgerItem.setItemDescription(ledgerItem.getItemDescription()==null?"":ledgerItem.getItemDescription());
            if (ledgerItem.getDicId() != null&&!"".equals(ledgerItem.getDicId())) {
                Dictionary codeLib = new Dictionary();
                codeLib.setId(Long.valueOf(ledgerItem.getDicId()));
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
}
