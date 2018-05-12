//package com.fitech.acount.service;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Assert;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import com.fitech.account.service.AccountTemplateService;
//import com.fitech.domain.account.AccountField;
//import com.fitech.domain.account.AccountTemplate;
//import com.fitech.domain.report.BusSystem;
//import com.fitech.enums.SqlTypeEnum;
//import com.fitech.framework.core.junit.JunitCase;
//
///**
// * Created by wangxw on 2017/7/25.
// */
//public class AccountTemplateServiceTest extends JunitCase {
//
//    @Autowired
//    private AccountTemplateService accountTemplateService;
//
//    @Test
//    public void testFindAll(){
//        BusSystem busSystem = new BusSystem();
//        busSystem.setId(192l);
//        List<AccountTemplate> accountTemplateList = accountTemplateService.findByBusSystem(busSystem);
//
//        Assert.assertNotNull(accountTemplateList);
//    }
//
//    @Test
//    public void testAddAccountTemplate(){
//        AccountTemplate accountTemplate = new AccountTemplate();
//        List<AccountField> accountFields = new ArrayList<>();
//        AccountField accountField = new AccountField();
//        accountField.setItemType("VARCHAR");
//        accountField.setItemCode("NBJGH");
//        accountField.setSqlType(SqlTypeEnum.VARCHAR);
//        accountField.setLength("20");
//        accountFields.add(accountField);
//        accountTemplate.setAccountFields(accountFields);
//
//        accountTemplate.setTemplateCode("DKFHZ001");
//        accountTemplate.setTemplateName("贷款分户账");
//
////        accountTemplateService.saveAccountTemplate(accountTemplate);
//    }
//}