package com.fitech.account.dao.impl;

import com.fitech.account.dao.AccountBaseDao;
import com.fitech.account.dao.AccountProcessDao;
import com.fitech.domain.account.*;
import com.fitech.domain.system.User;
import com.fitech.dto.ReasonDto;
import com.fitech.framework.core.dao.mybatis.DaoMyBatis;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.page.Page;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.vo.account.AccountProcessVo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@ServiceTrace
public class AccountProcessDaoImpl extends DaoMyBatis implements AccountProcessDao {
	private static String NOTVALIDATE = "未校验";

    private static String VALIDATING = "校验中";

    private static String SUCCESS = "校验通过";

    private static String FAIL = "校验不通过";
    @Autowired
    private AccountBaseDao accountBaseDao;

    //用于下载数据查询(已完成任务)
    @Override
	public List<AccountProcessVo> findDoneQuerySqltwo(AccountProcessVo vo) {
    	String term = vo.getTerm();
    	String templateName = vo.getReportTemplateName();
    	String institutionname = vo.getInstitutionName();
    	Map<String,String> tempMap = new HashMap<String,String>();
    	tempMap.put("term", term);
    	tempMap.put("templateName", templateName);
    	tempMap.put("institutionname", institutionname);
        List<Map<String,Object>> result = super.selectList("accProServiceMapper.findDoneQuerySqltwo", tempMap);
        List<AccountProcessVo> vos = new ArrayList<>();
        //循环拼凑
        if (result != null && result.size() > 0) {
            for (Map<String,Object> object : result) {
            	
                AccountProcessVo ledgerProcessVo = new AccountProcessVo();
                //机构名称
                //返回也也要改
                ledgerProcessVo.setInstitutionName(object.get("INSTITUTIONNAME").toString());
                ledgerProcessVo.setTerm(object.get("TERM").toString());
                //模版名称
                ledgerProcessVo.setReportTemplateName(object.get("TEMPLATENAME").toString());
                vos.add(ledgerProcessVo);
            }
        }
		return vos;
	}
    
    @Override
	public List<AccountProcessVo> findDoneQuerySql(AccountProcessVo vo, Page page) {
    	String term = vo.getTerm();
    	String templateName = vo.getReportTemplateName();
    	String institutionname = vo.getInstitutionName();
        Long userId = vo.getUserId();
        Map<String,String> tempMap = new HashMap<String,String>();
    	tempMap.put("term", term);
    	tempMap.put("templateName", templateName);
    	tempMap.put("institutionname", institutionname);
    	tempMap.put("userId",String.valueOf(userId));
        List<Map<String,Object>> result = super.selectByPage("accProServiceMapper.findDoneQuerySqlCount", "accProServiceMapper.findDoneQuerySql", tempMap, page);
    	
        List<AccountProcessVo> vos = new ArrayList<>();
        //循环拼凑
        if (result != null && result.size() > 0) {
            for (Map<String,Object> object : result) {
            	
                AccountProcessVo ledgerProcessVo = new AccountProcessVo();
                //机构名称
                //返回也也要改
                ledgerProcessVo.setPageNum(vo.getPageNum());
                ledgerProcessVo.setInstitutionName(object.get("INSTITUTIONNAME").toString());
                ledgerProcessVo.setTerm(object.get("TERM").toString());
                //模版名称
                ledgerProcessVo.setReportTemplateName(object.get("TEMPLATENAME").toString());
                Account account = new Account();
                account.setId(Long.valueOf(object.get("ID").toString()));
                ledgerProcessVo.setAccount(account);
                vos.add(ledgerProcessVo);
            }
        }
		return vos;
	}
    
    
	@Override
	public List<AccountProcessVo> findTodoTaskBySql(AccountProcessVo vo, User user, Page page)  {
		Map<String,String> tempMap = new HashMap<String,String>();
		tempMap.put("templateCode", vo.getReportTemplateId());
		tempMap.put("templateName", vo.getReportTemplateName());
		tempMap.put("freq", vo.getFreq());
		tempMap.put("term", vo.getTerm());
		tempMap.put("institutionName", vo.getInstitutionName());
		tempMap.put("validatestatus", vo.getValidateStatus());
		tempMap.put("proc_inst_id_", vo.getProcessId());
		tempMap.put("accountState", vo.getReortStatus());
        String orgIds=vo.getOrgIds();
        if(StringUtils.isNotEmpty(orgIds)){
            orgIds="'"+orgIds.replaceAll(",","','")+"'";
            tempMap.put("orgIds",orgIds);
        }
		if (!StringUtil.isEmpty(vo.getReortStatus())) {
        	String accountState = "";
        	if(vo.getReortStatus().equals("待补录")){
        		accountState = "0";
        	}else if(vo.getReortStatus().equals("待审核")){
        		accountState = "1";
        	}else if(vo.getReortStatus().equals("审核通过")){
        		accountState = "2";
        	}else if(vo.getReortStatus().equals("审核不通过")){
        		accountState = "3";
        	}
        	tempMap.put("accountState", accountState);
        }
		tempMap.put("sysuser_id", vo.getUserId()==null?"":String.valueOf(vo.getUserId()));
		tempMap.put("institution_id",vo.getInstitutionId());
        tempMap.put("reportSubSystem_id", vo.getSubSystemId()==null?"":String.valueOf(vo.getSubSystemId()));
        tempMap.put("category_", vo.getCategory());
		
        //Page page = new Page(vo.getPageNum(), vo.getPageSize());
        List<Map<String,Object>> result = super.selectByPage("accProServiceMapper.findTodoTaskBySqlCount", 
        		"accProServiceMapper.findTodoTaskBySql", tempMap, page);
        List<AccountProcessVo> vos = new ArrayList<>();
        //循环拼凑
        if (result != null && result.size() > 0) {
            for (Map<String,Object> object : result) {
                AccountProcessVo ledgerProcessVo = new AccountProcessVo();
                //返回也也要改
                ledgerProcessVo.setPageNum(vo.getPageNum());
                //对象ID
                ledgerProcessVo.setId(object.get("ID").toString());
                //机构ID
                ledgerProcessVo.setInstitutionId(object.get("INSTITUTIONID").toString());
                //机构名称
                ledgerProcessVo.setInstitutionName(object.get("INSTITUTIONNAME").toString());
                //模版编号
                ledgerProcessVo.setReportTemplateId(object.get("TEMPLATEID").toString());
                //模版名称
                ledgerProcessVo.setReportTemplateName(object.get("TEMPLATENAME").toString());
                ledgerProcessVo.setTerm(object.get("TERM").toString());
                ledgerProcessVo.setProcessName(object.get("PROCESSNAME").toString());
                //校验状态
                String validateStatus = object.get("VALIDATESTATUS") == null?"": object.get("VALIDATESTATUS").toString();
                if (validateStatus != null) {
                    //校验状态 0：
                    if ("0".equals(validateStatus)) {
                    	ledgerProcessVo.setValidateStatus(NOTVALIDATE);
                    } else if ("1".equals(validateStatus)) {
                    	ledgerProcessVo.setValidateStatus(VALIDATING);
                    } else if ("2".equals(validateStatus)) {
                    	ledgerProcessVo.setValidateStatus(SUCCESS);
                    } else if ("3".equals(validateStatus)) {
                    	ledgerProcessVo.setValidateStatus(FAIL);
                    }
                }
                ledgerProcessVo.setTaskId(object.get("TASKID") == null?"": object.get("TASKID").toString());
                ledgerProcessVo.setReportId(object.get("ACCOUNT_ID") == null?"": object.get("ACCOUNT_ID").toString());
                //描述
                ledgerProcessVo.setDescription(object.get("DESCRIPTION") == null?"": object.get("DESCRIPTION").toString());
                //频度
                ledgerProcessVo.setFreq(object.get("FREQ") == null?"": object.get("FREQ").toString());
              //实例id
                ledgerProcessVo.setProcessId(object.get("PROCINSETID") == null?"": object.get("PROCINSETID").toString());
                 
                ledgerProcessVo.setCategory(String.valueOf(object.get("CATEGORY")));
                ledgerProcessVo.setReortStatus(String.valueOf(object.get("ACCOUNTSTATE")));
                AccountProcess accountProcess = new AccountProcess();
                accountProcess.setId(Long.valueOf(object.get("PID").toString()));
                accountProcess.setRefuseCause(object.get("REFUSECAUSE") == null?"": object.get("REFUSECAUSE").toString());
                ledgerProcessVo.setAccountProcess(accountProcess);

                Account account = new Account();
                account.setId(Long.valueOf(object.get("LID").toString()));
                ledgerProcessVo.setAccount(account);
                List<ReasonDto> reportReasons = (List<ReasonDto>) object.get("reportReasons");
                ledgerProcessVo.setReportReasons(reportReasons);
                vos.add(ledgerProcessVo);
            }
        }
		return vos;
	}

	@Override
	public List<AccountProcessVo> findDoneTaskBySql(AccountProcessVo vo,User user,Page page) {
		Map<String,Object> tempMap = new HashMap<String,Object>();
		tempMap.put("templateCode", vo.getReportTemplateId());
		tempMap.put("templateName", vo.getReportTemplateName());
		tempMap.put("freq", vo.getFreq());
		tempMap.put("term", vo.getTerm());
		tempMap.put("institutionName", vo.getInstitutionName());
		tempMap.put("processId", vo.getProcessId());
		tempMap.put("sysuser_id", vo.getUserId());
		tempMap.put("reportSubSystem_id", vo.getSubSystemId()==null?"":vo.getSubSystemId());
	    tempMap.put("operationType", 0);
		if(null != user.getOrg()){
			tempMap.put("institution_id", user.getOrg().getId());
		}else{
			tempMap.put("institution_id", "");
		}
        List<Map<String,Object>> result = super.selectByPage("accProServiceMapper.findDoneTaskBySqlCount",
        		"accProServiceMapper.findDoneTaskBySql", tempMap, page);
		
        List<AccountProcessVo> vos = new ArrayList<>();
        //循环拼凑
        if (result != null && result.size() > 0) {
            for (Map<String,Object> object : result) {
                AccountProcessVo ledgerProcessVo = new AccountProcessVo();
                //返回也也要改
                ledgerProcessVo.setPageNum(vo.getPageNum());
                //对象ID
                ledgerProcessVo.setId(object.get("ID").toString());
                //机构ID
                ledgerProcessVo.setInstitutionId(object.get("INSTITUTIONID").toString());
                //机构名称
                ledgerProcessVo.setInstitutionName(object.get("INSTITUTIONNAME").toString());
                //模版编号
                ledgerProcessVo.setReportTemplateId(object.get("TEMPLATECODE").toString());
                //模版名称
                ledgerProcessVo.setReportTemplateName(object.get("TEMPLATENAME").toString());
                ledgerProcessVo.setTerm(object.get("TERM").toString());
                ledgerProcessVo.setProcessName(object.get("NAME").toString());
                ledgerProcessVo.setSubmitter(object.get("SUBMITTER")==null?"":object.get("SUBMITTER").toString());
                ledgerProcessVo.setProcessId(object.get("PROCESSID").toString());
                //校验状态
                if (object.get("VALIDATESTATUS") != null) {
                    if ("0".equals(object.get("VALIDATESTATUS").toString())) {
                        vo.setValidateStatus(NOTVALIDATE);
                    } else if ("1".equals(object.get("VALIDATESTATUS").toString())) {
                        vo.setValidateStatus(VALIDATING);
                    } else if ("2".equals(object.get("VALIDATESTATUS").toString())) {
                        vo.setValidateStatus(SUCCESS);
                    } else if ("3".equals(object.get("VALIDATESTATUS").toString())) {
                        vo.setValidateStatus(FAIL);
                    }
                }
                ledgerProcessVo.setTaskId(object.get("TASKID").toString());
                if (object.get("END_TIME_") != null) {
                    ledgerProcessVo.setEndTime(object.get("END_TIME_").toString());
                }
                //频度
                if (object.get("FREQ") != null) {
                    ledgerProcessVo.setFreq(object.get("FREQ").toString());
                }
                vos.add(ledgerProcessVo);
            }
        }
		return vos;
	}

	@Override
	public void createAccountTask(String term) {
		HashMap<String,String> map =new HashMap<String,String>();
		map.put("term", term);
		super.selectList("accProServiceMapper.exculteCall",map);
	}

}
