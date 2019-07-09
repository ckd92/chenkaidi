package com.fitech.account.service.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Types;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fitech.account.repository.AccountRepository;
import com.fitech.account.util.AccountConstants;
import com.fitech.domain.account.Account;
import com.fitech.enums.SubmitStateEnum;
import com.fitech.framework.activiti.lang.ProcessStartConst;
import com.fitech.framework.lang.util.JdbcUtil;

/**
 * 南洋版本，提交审核后现场需要同步一个存储过程，名称暂固定，后期改成可配置
 * @author chengrui
 *
 */
@Service
public class AccountDataBlpp implements JavaDelegate {
	@Autowired
	private AccountRepository accountRepository;
	
	@Override
	public  void execute(DelegateExecution execution) throws Exception {
		Long accountId=null;
        try{
        JdbcUtil ju = new JdbcUtil();
        CallableStatement call;
        Connection con = ju.getConnection();
        con.setAutoCommit(false);
        
        //获取accountId
        accountId= Long.parseLong(String.valueOf(execution.getVariable(ProcessStartConst.report_key)));
      	AccountConstants.add(accountId);
		call = con.prepareCall("call blpp(?,?)");
        call.setString(1, String.valueOf(accountId));
        call.registerOutParameter(2, Types.VARCHAR);
        call.execute();
        String testPrint = call.getString(2);
        System.out.println("数据补录---存储过程执行" + testPrint);

		Account account = new Account();	
		account = accountRepository.findById(accountId);
		//修改状态为已上报成功
		account.setSubmitStateType(SubmitStateEnum.SUCCESS);
		accountRepository.save(account);
        }catch(Exception e){
        	throw e;
        }finally {
    		AccountConstants.remove(accountId);
		}
	}
}
