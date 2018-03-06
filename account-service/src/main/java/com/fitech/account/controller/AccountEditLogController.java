package com.fitech.account.controller;



import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fitech.account.service.AccountEditLogService;
import com.fitech.domain.account.AccountEditLog;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.FileUtil;


@RestController
public class AccountEditLogController {
	
	@Autowired
	private AccountEditLogService accountEditLogService;
	
	@PostMapping("/accountEditLogs")
	public GenericResult<Page<AccountEditLog>> getAll(@RequestBody AccountEditLog accountEditLog){
		
		GenericResult<Page<AccountEditLog>> result = new GenericResult<Page<AccountEditLog>>();
		try{
			Page<AccountEditLog> collection = accountEditLogService.findAccountEditLogByPage(accountEditLog);
			result.setData(collection);
		}catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
		
		return result;
	}
	//工作任务统计(hx)
	@PostMapping("/accountEditLogsTJ")
	public GenericResult<Page<AccountEditLog>> getTJ(@RequestBody AccountEditLog accountEditLog){
		
		GenericResult<Page<AccountEditLog>> result = new GenericResult<Page<AccountEditLog>>();
		try{
			Page<AccountEditLog> collection = accountEditLogService.findAccountEditLogTJByPage(accountEditLog);
			result.setData(collection);
		}catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
		
		return result;
	}
	
	/**
     * 下载工作任务统计
     * @param response
     */
    @GetMapping("/DownLoadEditLogsTJ/{searchs}")
    public void downloadTemplate(@PathVariable String searchs, HttpServletResponse response,HttpServletRequest request) {
        try {
            String fileName = accountEditLogService.downLoadEditLogsTJ(searchs);
            File file = new File(fileName);
            FileUtil.downLoadFile(file, response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
