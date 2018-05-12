//package com.fitech.acount.service;
//
//import com.fitech.account.dao.AccountBaseDao;
//import com.fitech.account.service.AccountProcessService;
//import com.fitech.framework.core.junit.JunitCase;
//import com.fitech.vo.account.AccountProcessVo;
//import org.junit.Assert;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//
//import java.util.List;
//
///**
// * Created by wangxw on 2017/8/11.
// */
//public class AccountProcessServiceTest extends JunitCase {
//
//    @Autowired
//    private AccountProcessService accountProcessService;
//
//    @Autowired
//    private AccountBaseDao accountBaseDao;
//
//    @Test
//    public void findTodoTask(){
//        AccountProcessVo accountProcessVo = new AccountProcessVo();
//        accountProcessVo.setUserId(30211081l);
//        accountProcessVo.setCategory("bulu");
//        Page<AccountProcessVo> accountProcessVos =  accountProcessService.findPageAccountProcessList(accountProcessVo);
//
//        Assert.assertNotNull(accountProcessVos.getContent());
//    }
//
//    @Test
//    public void findDoneTask(){
//        AccountProcessVo accountProcessVo = new AccountProcessVo();
//        accountProcessVo.setUserId(30211081l);
//        accountProcessVo.setCategory("bulu");
//        Page<AccountProcessVo> accountProcessVos =  accountProcessService.findPagefindAssignedTask(accountProcessVo);
//
//        Assert.assertNotNull(accountProcessVos.getContent());
//    }
//
//    @Test
//    public void test(){
////        String sql = "select LONG_ from ACT_RU_VARIABLE where NAME_='nrOfActiveInstances' and PROC_INST_ID_='72501'";
////        Object object = accountBaseDao.findObjectBysql(new StringBuffer(sql),null);
//
//        Boolean result = accountProcessService.isMultiInstanceTaskExecOver("72501");
//
//        System.out.print(result);
//    }
//
//    @Test
//    public void testPermession(){
//
//        List<Long> tps = accountProcessService.queryAccountTaskPermission(148l);
//
//        Assert.assertNotNull(tps);
//    }
//}
