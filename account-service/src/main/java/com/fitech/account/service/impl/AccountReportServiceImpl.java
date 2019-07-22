package com.fitech.account.service.impl;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.repository.AccountRepository;
import com.fitech.account.service.AccountProcessService;
import com.fitech.account.service.AccountReportService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Account;
import com.fitech.domain.report.ReportTemplate;
import com.fitech.domain.system.Institution;
import com.fitech.domain.system.NoticeScene;
import com.fitech.domain.system.ProcessConfig;
import com.fitech.enums.SubmitStateEnum;
import com.fitech.enums.system.NoticeSceneEnum;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.report.dao.ReportProcessDao;
import com.fitech.system.repository.ProcessConfigRepository;
import com.fitech.system.service.NoticeWaysService;
import com.fitech.vo.system.NoticeSceneVo;


/**
 * Created by SunBojun on 2017/3/2.
 */
@Service
@ServiceTrace
public class AccountReportServiceImpl implements AccountReportService {
    @Autowired
    private ProcessConfigRepository<ProcessConfig> processConfigRepository;
    @Autowired
    private AccountProcessService accountProcessService;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired(required=false)
	private NoticeWaysService noticeWaysService;
    @Autowired
    private ReportProcessDao reportProcessDao;
    
    @Override
    public int startProcess(Account account){
    	int num = this.createPorcess(account);
    	if(num>0){
    		//通过流程id和期数查询出生成待办任务第一节点的用户
            List<Long> receiverIdList = new ArrayList<>();
            List<String> receiverPhones = new ArrayList<>();
            List<Map<String,Object>> receivers = reportProcessDao.getReceiverIdList_bl(account.getTerm(), "");
            for (Map<String, Object> map : receivers) {
            	receiverIdList.add(Long.parseLong(String.valueOf(map.get("USERID"))));
            	receiverPhones.add(String.valueOf(map.get("MOBILE")));
    		}
    		sendNotice(receiverIdList,receiverPhones);
    	}
    	return num;
    }
    @Transactional
    private int createPorcess(Account account){
    	accountProcessService.createAccountTask(account.getTerm());
        //根据报文期数获取待开启的报文实例
        Collection<Account> ledgerReportList = accountRepository.findByTermAndSubmitStateType(account.getTerm(), SubmitStateEnum.NOTSUBMIT);
        for (Account report : ledgerReportList) {
            try {
                //获取报文对应的流程配置信息
                ProcessConfig processConfig = this.findByAccountReport(report);
                if (null != processConfig) {
                    //开启流程
                    accountProcessService.processStart(processConfig, report);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
            }
        }
        return ledgerReportList.size();
    }

    private ProcessConfig findByAccountReport(Account account) throws Exception {
        //报文模板
        List<ReportTemplate> accountTemplateList = new ArrayList<>();
        accountTemplateList.add(account.getAccountTemplate());
        //报文所属机构
        List<Institution> institutionList = new ArrayList<Institution>();
        institutionList.add(account.getInstitution());
        List<ProcessConfig> processConfigList = processConfigRepository.findByOrgsAndReportTemplateAndEnabledOrderByIdDesc(institutionList, accountTemplateList,true);
        if(null!=processConfigList && processConfigList.size()>0){
            return processConfigList.get(0);
        }
        return null;
    }


    private void sendNotice(List<Long> receiverIdList,List<String> receiverPhones){
		NoticeSceneVo noticeSceneVo = new NoticeSceneVo();
		noticeSceneVo.setReceiverIdList(receiverIdList);
		noticeSceneVo.setReceiverPhones(receiverPhones);
    	NoticeScene noticeScene = new NoticeScene();
    	noticeScene.setUnum(NoticeSceneEnum.ActivitiStart002);
    	noticeSceneVo.setNoticeScene(noticeScene);
    	noticeWaysService.noticeSend(noticeSceneVo);
	}
}
