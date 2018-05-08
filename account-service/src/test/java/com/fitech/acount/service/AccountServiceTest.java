//package com.fitech.acount.service;
//
//import com.fitech.account.service.AccountEditLogService;
//import com.fitech.account.service.AccountService;
//import com.fitech.domain.account.Account;
//import com.fitech.domain.account.AccountEditLog;
//import com.fitech.framework.core.junit.JunitCase;
//import com.fitech.framework.lang.result.GenericResult;
//import com.fitech.vo.account.AccountProcessVo;
//import org.junit.Assert;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//
///**
// * Created by wangxw on 2017/8/18.
// */
//public class AccountServiceTest extends JunitCase {
//
//    @Autowired
//    private AccountService accountService;
//
//    @Autowired
//    private AccountEditLogService accountEditLogService;
//
//    @Test
//    public void testFind(){
//        AccountProcessVo accountProcessVo = new AccountProcessVo();
//        Account account = new Account();
//        account.setId(30212996l);
//        accountProcessVo.setAccount(account);
//        GenericResult<AccountProcessVo> result = accountService.findPageAccounData(accountProcessVo);
//
//        Assert.assertNotNull(result.getData());
//    }
//
//    @Test
//    public void testFindEditlog(){
//
//        AccountEditLog accountEditLog = new AccountEditLog();
//
//        accountEditLog.setPageNum(1);
//        accountEditLog.setPageSize(10);
//        Page<AccountEditLog> accountEditLogs = accountEditLogService.findAccountEditLogByPage(accountEditLog);
//
//        Assert.assertNotNull(accountEditLogs.getContent());
//    }
//}
