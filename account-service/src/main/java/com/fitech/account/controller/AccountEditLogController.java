package com.fitech.account.controller;

import java.io.File;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fitech.account.service.AccountEditLogService;
import com.fitech.domain.account.AccountEditLog;
import com.fitech.framework.lang.page.Page;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.FileUtil;


@RestController
public class AccountEditLogController {
	@Autowired
	private AccountEditLogService accountEditLogService;
	
	/**
	 * 修改痕迹查询
	 * @param accountEditLog
	 * @return
	 */
	@PostMapping("/accountEditLogs")
	public GenericResult<List<AccountEditLog>> getEditLogs(@RequestBody AccountEditLog accountEditLog){
		GenericResult<List<AccountEditLog>> result = new GenericResult<List<AccountEditLog>>();
		try{
			Page page = new Page();
			page.setCurrentPage(accountEditLog.getPageNum());
			page.setPageSize(accountEditLog.getPageSize());
			List<AccountEditLog> collection = accountEditLogService.findAccountEditLogByPage(accountEditLog,page);
			result.setData(collection);
			result.setPage(page);
		}catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
		return result;
	}
	/**
	 * 工作任务统计(hx)
	 * @param accountEditLog
	 * @return
	 */
	@PostMapping("/accountEditLogsTJ")
	public GenericResult<List<AccountEditLog>> getEditLogsTJ(@RequestBody AccountEditLog accountEditLog){
		GenericResult<List<AccountEditLog>> result = new GenericResult<List<AccountEditLog>>();
		try{
			Page page = new Page();
			page.setCurrentPage(accountEditLog.getPageNum());
			page.setPageSize(accountEditLog.getPageSize());
			List<AccountEditLog> collection = accountEditLogService.findAccountEditLogTJByPage(accountEditLog,page);
			result.setData(collection);
			result.setPage(page);
		}catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
		
		return result;
	}
	
//	/**
//     * 下载工作任务统计
//     * @param response
//     */
//    @GetMapping("/DownLoadEditLogsTJ/{searchs}")
//    public void downloadTemplate(@PathVariable String searchs, HttpServletResponse response,HttpServletRequest request) {
//        try {
//            String fileName = accountEditLogService.downLoadEditLogsTJ(searchs);
//            File file = new File(fileName);
//            FileUtil.downLoadFile(file, response);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
