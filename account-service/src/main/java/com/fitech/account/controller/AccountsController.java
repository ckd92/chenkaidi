package com.fitech.account.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fitech.constant.LoggerUtill;
import com.fitech.system.annotation.AddOperateLogLast;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fitech.account.service.AccountsService;
import com.fitech.domain.account.Account;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.security.util.TokenUtils;
import com.fitech.vo.account.AccountVo;

@RestController
public class AccountsController {
	@Autowired
	private AccountsService accountsService;
		
	@PostMapping("/data")
	public GenericResult<List<Map<String,Object>>> getData(@RequestBody Account account){
		GenericResult<List<Map<String,Object>>> result = new GenericResult<List<Map<String,Object>>>();
		try{
			List<Map<String,Object>> collection = accountsService.findData(account);
			result.setData(collection);
		}catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
		return result;
	}
	
	
	@GetMapping("/account/{id}")
	public GenericResult<Account> getAccountById(@PathVariable("id")Long id){
		GenericResult<Account> result = new GenericResult<Account>();
		Account account = accountsService.findAccountById(id);
		result.setData(account);
		return result;
	}
	
	@GetMapping("/account")
	public GenericResult<List<Account>> getAllAccount(){
		GenericResult<List<Account>> result = new GenericResult<List<Account>>();
		try{
			List<Account> list = accountsService.findAllAccount();
			result.setData(list);
		}catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }		
		return result;
	}
	
	/**
	 * 任务统计
	 * @return
	 */
	@GetMapping("/getrwtjs")
	public GenericResult<List<AccountVo>> getrwtj(){
		GenericResult<List<AccountVo>> result = new GenericResult<List<AccountVo>>();
		try{
			List<AccountVo> list = accountsService.getrwtj();
			result.setData(list);
		}catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }				
		return result;
	}
	
	/**
	 * 条件查询任务统计
	 * @param account
	 * @return
	 */
	@PostMapping("/getrwtjbycondition")
	@AddOperateLogLast(targetURI = "/getrwtjbycondition", baseContent = "科融统计平台-数据查询-任务统计-查询",
			logType = LoggerUtill.LogType.OPERATE,logCategory = LoggerUtill.LogCategory.SELECT)
	public GenericResult<List> getrwtjByCondition(@RequestBody Account account,  HttpServletRequest request){
		GenericResult<List> result = new GenericResult<List>();
		Long userId = TokenUtils.getLoginId(request);
		try{
			List list = accountsService.getrwtjByCondition(account,userId);
			result.setData(list);
		}catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }		
		
		return result;
	}
	
	/**
	 * 任务统计点击百分比显示所含台账信息
	 * @param account
	 * @return
	 */
	@PostMapping("/getrwtjAccounts")
	public GenericResult<List<Account>> getrwtjAccounts(@RequestBody Account account){
		GenericResult<List<Account>> result = new GenericResult<List<Account>>();
		try{
			List<Account> list = accountsService.findrwtjAccounts(account);
			result.setData(list);
		}catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }			
		return result;
	}
	
}
