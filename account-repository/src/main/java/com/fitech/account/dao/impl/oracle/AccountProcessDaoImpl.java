package com.fitech.account.dao.impl.oracle;

import com.fitech.account.dao.AccountBaseDao;
import com.fitech.account.dao.AccountProcessDao;
import com.fitech.domain.account.*;
import com.fitech.domain.system.User;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.vo.account.AccountProcessVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

import javax.sql.DataSource;
import java.util.*;

public class AccountProcessDaoImpl extends NamedParameterJdbcDaoSupport implements AccountProcessDao {
	@Autowired
    public AccountProcessDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
        try {
            dataSource.getConnection().setAutoCommit(true);

        } catch (Exception e) {

        }
    }
	private static String NOTVALIDATE = "未校验";

    private static String VALIDATING = "校验中";

    private static String SUCCESS = "校验通过";

    private static String FAIL = "校验不通过";
    @Autowired
    private AccountBaseDao accountBaseDao;

    //用于下载数据查询(已完成任务)
    @Override
	public List<AccountProcessVo> findDoneQuerySqltwo(AccountProcessVo vo) {
        StringBuffer sql = new StringBuffer();
        //拼凑sql
        //id,机构ID,机构名称,报表编号,频度,期数,提交人,校验状态，完成时间,节点名称
        //sql.append("select o.id,it.institutionId,it.institutionName,lrt.id as templateId,lrt.templateName,lr.term,Translate(task.name_ USING CHAR_CS),lr.validateStatus,Translate(task.id_ USING CHAR_CS),o.ledgerReport_id,Translate(task.description_ USING CHAR_CS),lr.freq,o.procInsetId  ");
        sql.append(" select ac.id,it.institutionname,to_char(to_date(ac.term,'yyyy-MM-dd'),'yyyy-MM-dd') as term,ap.templatename from account ac INNER JOIN INSTITUTION it on AC.INSTITUTION_ID=it.id INNER JOIN ACCOUNTTEMPLATE ap ON  ");
        sql.append(" ac.ACCOUNTTEMPLATE_ID=ap.id where ac.accountstate=2");
        //期数
        if (!StringUtil.isEmpty(vo.getTerm())) {
            sql.append(" and to_char(to_date(ac.term,'yyyy-MM-dd'),'yyyy-MM-dd')='" + vo.getTerm() + "'");
        }
        //报表编号
        if (!StringUtil.isEmpty(vo.getReportTemplateName())) {
            sql.append(" and ap.templatename like '%" + vo.getReportTemplateName() + "%'");
        }
        //机构名称
        if (!StringUtil.isEmpty(vo.getInstitutionName())) {
            sql.append(" and it.institutionname like '%" + vo.getInstitutionName() + "%'");
        }
        sql.append(" order by ac.id");
        List<Object[]> result = accountBaseDao.findBySql(sql,null);
        List<AccountProcessVo> vos = new ArrayList<>();
        //循环拼凑
        if (result != null && result.size() > 0) {
            for (Object[] object : result) {
            	
                AccountProcessVo ledgerProcessVo = new AccountProcessVo();
                //机构名称
                //返回也也要改
                ledgerProcessVo.setInstitutionName(object[1].toString());
                ledgerProcessVo.setTerm(object[2].toString());
                //模版名称
                ledgerProcessVo.setReportTemplateName(object[3].toString());
                vos.add(ledgerProcessVo);
            }
        }
		return vos;
	}
    
    @Override
	public Page<AccountProcessVo> findDoneQuerySql(AccountProcessVo vo) {
        StringBuffer sql = new StringBuffer();
        //拼凑sql
        //id,机构ID,机构名称,报表编号,频度,期数,提交人,校验状态，完成时间,节点名称
        //sql.append("select o.id,it.institutionId,it.institutionName,lrt.id as templateId,lrt.templateName,lr.term,Translate(task.name_ USING CHAR_CS),lr.validateStatus,Translate(task.id_ USING CHAR_CS),o.ledgerReport_id,Translate(task.description_ USING CHAR_CS),lr.freq,o.procInsetId  ");
        sql.append(" select ac.id,it.institutionname,to_char(to_date(ac.term,'yyyy-MM-dd'),'yyyy-MM-dd') as term,ap.templatename from account ac INNER JOIN INSTITUTION it on AC.INSTITUTION_ID=it.id INNER JOIN ACCOUNTTEMPLATE ap ON  ");
        sql.append(" ac.ACCOUNTTEMPLATE_ID=ap.id where ac.accountstate=2");
        //期数
        if (!StringUtil.isEmpty(vo.getTerm())) {
            sql.append(" and to_char(to_date(ac.term,'yyyy-MM-dd'),'yyyy-MM-dd')='" + vo.getTerm() + "'");
        }
        //报表编号
        if (!StringUtil.isEmpty(vo.getReportTemplateName())) {
            sql.append(" and ap.templatename like '%" + vo.getReportTemplateName() + "%'");
        }
        //机构名称
        if (!StringUtil.isEmpty(vo.getInstitutionName())) {
            sql.append(" and it.institutionname like '%" + vo.getInstitutionName() + "%'");
        }
        sql.append(" order by ac.id");
        Page<Object[]> page = accountBaseDao.findPageBySql(sql, null, vo.getPageSize(), vo.getPageNum());
        List<AccountProcessVo> vos = new ArrayList<>();
        //循环拼凑
        if (page != null && page.getSize() > 0) {
            for (Object[] object : page.getContent()) {
            	
                AccountProcessVo ledgerProcessVo = new AccountProcessVo();
                //机构名称
                //返回也也要改
                ledgerProcessVo.setPageNum(vo.getPageNum());
                ledgerProcessVo.setInstitutionName(object[1].toString());
                ledgerProcessVo.setTerm(object[2].toString());
                //模版名称
                ledgerProcessVo.setReportTemplateName(object[3].toString());
                Account account = new Account();
                account.setId(Long.valueOf(object[0].toString()));
                ledgerProcessVo.setAccount(account);
                vos.add(ledgerProcessVo);
            }
        }
        Pageable pageable = new PageRequest(vo.getPageNum() - 1, vo.getPageSize());
        Page<AccountProcessVo> voPage = new PageImpl<>(vos, pageable, page.getTotalElements());
		return voPage;
	}
    
    
	@Override
	public Page<AccountProcessVo> findTodoTaskBySql(AccountProcessVo vo, User user) {
        StringBuffer sql = new StringBuffer();
        //拼凑sql
        //id,机构ID,机构名称,报表编号,频度,期数,提交人,校验状态，完成时间,节点名称
        //sql.append("select o.id,it.institutionId,it.institutionName,lrt.id as templateId,lrt.templateName,lr.term,Translate(task.name_ USING CHAR_CS),lr.validateStatus,Translate(task.id_ USING CHAR_CS),o.ledgerReport_id,Translate(task.description_ USING CHAR_CS),lr.freq,o.procInsetId  ");
        sql.append(" select Translate(task.id_ USING CHAR_CS) as id,it.institutionId,it.institutionName,lrt.templateCode as " +
                "templateId,lrt.templateName,to_char(to_date(lr.term,'yyyy-MM-dd'),'yyyy-MM-dd') as term,Translate(task.name_ " +
                "USING CHAR_CS),lr.validateStatus,Translate(task.id_ USING CHAR_CS),o.ACCOUNT_ID,Translate(task.description_ USING CHAR_CS)," +
                "lr.freq,o.procInsetId,Translate(task.category_ USING CHAR_CS),lr.accountState,o.id pid,lr.id lid,o.refuseCause" +
                " ");
        sql.append(" from AccountProcess o inner join Account lr on lr.id=o.ACCOUNT_ID");
        sql.append(" inner join Institution it on it.id=lr.institution_id");
        sql.append(" inner join ACCOUNTTEMPLATE lrt on lrt.id=lr.ACCOUNTTEMPLATE_ID");
        sql.append(" inner join BusSystem bs on bs.id = lrt.busSystem_id");
        sql.append(" inner join ACT_RU_TASK task on task.proc_inst_id_=o.procInsetId");
        sql.append(" inner join sysuser_role ur on ur.roles_id=task.ASSIGNEE_");
        sql.append(" where 1=1 ");
        //台账编号
        if (!StringUtil.isEmpty(vo.getReportTemplateId())) {
            sql.append("and lrt.templateCode like '%" + vo.getReportTemplateId() + "%'");
        }
        //报表编号
        if (!StringUtil.isEmpty(vo.getReportTemplateName())) {
            sql.append("and lrt.templateName like '%" + vo.getReportTemplateName() + "%'");
        }
        //报表频度
        if (!StringUtil.isEmpty(vo.getFreq())) {
            sql.append("and lr.freq='" + vo.getFreq() + "'");
        }
        //期数
        if (!StringUtil.isEmpty(vo.getTerm())) {
            sql.append("and to_char(to_date(lr.term,'yyyy-MM-dd'),'yyyy-MM-dd')='" + vo.getTerm() + "'");
        }
        //机构名称
        if (!StringUtil.isEmpty(vo.getInstitutionName())) {
            sql.append("and it.institutionName like '%" + vo.getInstitutionName() + "%'");
        }
        //校验状态
        if (!StringUtil.isEmpty(vo.getValidateStatus())) {
            sql.append(" and lr.validatestatus='" + vo.getValidateStatus() + "'");
        }
        //任务状态
        if (!StringUtil.isEmpty(vo.getReortStatus())) {
        	String s = "";
        	if(vo.getReortStatus().equals("待补录")){
        		s = "0";
        	}else if(vo.getReortStatus().equals("待审核")){
        		s = "1";
        	}else if(vo.getReortStatus().equals("审核通过")){
        		s = "2";
        	}else if(vo.getReortStatus().equals("审核不通过")){
        		s = "3";
        	}
            sql.append(" and lr.accountState='" + s + "'");
        }
        //报送状态
        if (!StringUtil.isEmpty(vo.getProcessId())) {
            sql.append("and task.proc_inst_id_='" + vo.getProcessId() + "'");
        }
        //用户ID
        if (vo.getUserId() != null) {
            sql.append(" and ur.sysuser_id='" + vo.getUserId() + "'");
        }
        //条线ID
        if (vo.getSubSystemId() != null) {
            sql.append(" and bs.reportSubSystem_id='" + vo.getSubSystemId() + "'");
        }
        //只查询此用户下的机构
        if(null != user && null != user.getOrg()){
        	sql.append(" and lr.institution_id='" + user.getOrg().getId() + "'");
        }
        if(null != vo.getCategory()){
        	sql.append(" and task.category_='" + vo.getCategory() + "'");
        }
        sql.append(" order by term desc,templateid,institutionid");
        Page<Object[]> page = accountBaseDao.findPageBySql(sql, null, vo.getPageSize(), vo.getPageNum());
        List<AccountProcessVo> vos = new ArrayList<>();
        //循环拼凑
        if (page != null && page.getSize() > 0) {
            for (Object[] object : page.getContent()) {
                AccountProcessVo ledgerProcessVo = new AccountProcessVo();
                //返回也也要改
                ledgerProcessVo.setPageNum(vo.getPageNum());
                //对象ID
                ledgerProcessVo.setId(object[0].toString());
                //机构ID
                ledgerProcessVo.setInstitutionId(object[1].toString());
                //机构名称
                ledgerProcessVo.setInstitutionName(object[2].toString());
                //模版编号
                ledgerProcessVo.setReportTemplateId(object[3].toString());
                //模版名称
                ledgerProcessVo.setReportTemplateName(object[4].toString());
                ledgerProcessVo.setTerm(object[5].toString());
                ledgerProcessVo.setProcessName(object[6].toString());
                //校验状态
                if (object[7] != null) {
                    //校验状态 0：
                    if ("0".equals(object[7].toString())) {
                    	ledgerProcessVo.setValidateStatus(NOTVALIDATE);
                    } else if ("1".equals(object[7].toString())) {
                    	ledgerProcessVo.setValidateStatus(VALIDATING);
                    } else if ("2".equals(object[7].toString())) {
                    	ledgerProcessVo.setValidateStatus(SUCCESS);
                    } else if ("3".equals(object[7].toString())) {
                    	ledgerProcessVo.setValidateStatus(FAIL);
                    }
                }
                ledgerProcessVo.setTaskId(object[8].toString());
                ledgerProcessVo.setReportId(object[9].toString());
                //描述
                if (object[10] != null) {
                	ledgerProcessVo.setDescription(object[10].toString());
                }
                //频度
                if (object[11] != null) {
                	ledgerProcessVo.setFreq(object[11].toString());
                }
                //实例id
                if (object[12] != null) {
                	ledgerProcessVo.setProcessId(object[12].toString());
                }
                ledgerProcessVo.setCategory(String.valueOf(object[13]));
                ledgerProcessVo.setReortStatus(String.valueOf(object[14]));
                AccountProcess accountProcess = new AccountProcess();
                accountProcess.setId(Long.valueOf(object[15].toString()));
                if(object[17] != null){
                    accountProcess.setRefuseCause(object[17].toString());
                }
                ledgerProcessVo.setAccountProcess(accountProcess);

                Account account = new Account();
                account.setId(Long.valueOf(object[16].toString()));
                ledgerProcessVo.setAccount(account);
                vos.add(ledgerProcessVo);
            }
        }
        Pageable pageable = new PageRequest(vo.getPageNum() - 1, vo.getPageSize());
        Page<AccountProcessVo> voPage = new PageImpl<>(vos, pageable, page.getTotalElements());
		return voPage;
	}

	@Override
	public Page<AccountProcessVo> findDoneTaskBySql(AccountProcessVo vo,User user) {
		StringBuffer sql = new StringBuffer();
        //id,机构ID,机构名称,报表编号,频度,期数,提交人,校验状态，完成时间,节点名称
        sql.append("select o.id,it.institutionId,it.institutionName,lrt.templateCode,lrt.templateName,to_char(to_date(lr.term,'yyyy-MM-dd'),'yyyy-MM-dd') as term,Translate(task.name_ USING CHAR_CS),lr.validateStatus,Translate(task.id_ USING CHAR_CS),task.end_time_,lr.freq  ");
        sql.append(" from AccountProcess o inner join Account lr on lr.id=o.ACCOUNT_ID");
        sql.append(" inner join Institution it on it.id=lr.institution_id");
        sql.append(" inner join ACCOUNTTEMPLATE lrt on lrt.id=lr.ACCOUNTTEMPLATE_ID");
        sql.append(" inner join BusSystem bs on bs.id = lrt.busSystem_id");
        sql.append(" inner join ACT_HI_TASKINST task on task.proc_inst_id_=o.procInsetId");
        sql.append(" inner join sysuser_role ur on ur.roles_id=task.ASSIGNEE_");
        sql.append(" where 1=1 ");
        //台账编号
        if (!StringUtil.isEmpty(vo.getReportTemplateId())) {
            sql.append("and lrt.templateCode like '%" + vo.getReportTemplateId() + "%'");
        }
        //报表编号
        if (!StringUtil.isEmpty(vo.getReportTemplateName())) {
            sql.append("and lrt.templateName like '%" + vo.getReportTemplateName() + "%'");
        }
        //报表频度
        if (!StringUtil.isEmpty(vo.getFreq())) {
            sql.append("and lr.freq='" + vo.getFreq() + "'");
        }
        //期数
        if (!StringUtil.isEmpty(vo.getTerm())) {
            sql.append("and to_char(to_date(lr.term,'yyyy-MM-dd'),'yyyy-MM-dd')='" + vo.getTerm() + "'");
        }
        //报表名称
        if (!StringUtil.isEmpty(vo.getReportTemplateName())) {
            sql.append("and lrt.templateName like '%" + vo.getReportTemplateName() + "%'");
        }
        //机构名称
        if (!StringUtil.isEmpty(vo.getInstitutionName())) {
            sql.append("and it.institutionName like '%" + vo.getInstitutionName() + "%'");
        }
        //报送状态
        if (!StringUtil.isEmpty(vo.getProcessId())) {
            sql.append(" and task.proc_inst_id_='" + vo.getProcessId() + "'");
        }
        //用户ID
        if (vo.getUserId() != null) {
            sql.append(" and ur.sysuser_id='" + vo.getUserId() + "'");
        }
        //条线ID
        if (vo.getSubSystemId() != null) {
            sql.append(" and bs.reportSubSystem_id='" + vo.getSubSystemId() + "'");
        }
        //只查询此用户下的机构
        if(null != user.getOrg()){
        	sql.append(" and lr.institution_id='" + user.getOrg().getId() + "'");
        }
        sql.append(" and task.end_time_ is not null  ");
        sql.append(" order by o.id");
        Page<Object[]> page = accountBaseDao.findPageBySql(sql, null, vo.getPageSize(), vo.getPageNum());
        List<AccountProcessVo> vos = new ArrayList<>();
        //循环拼凑
        if (page != null && page.getSize() > 0) {
            for (Object[] object : page.getContent()) {
                AccountProcessVo ledgerProcessVo = new AccountProcessVo();
                //返回也也要改
                ledgerProcessVo.setPageNum(vo.getPageNum());
                //对象ID
                ledgerProcessVo.setId(object[0].toString());
                //机构ID
                ledgerProcessVo.setInstitutionId(object[1].toString());
                //机构名称
                ledgerProcessVo.setInstitutionName(object[2].toString());
                //模版编号
                ledgerProcessVo.setReportTemplateId(object[3].toString());
                //模版名称
                ledgerProcessVo.setReportTemplateName(object[4].toString());
                ledgerProcessVo.setTerm(object[5].toString());
                ledgerProcessVo.setProcessName(object[6].toString());
                //校验状态
                if (object[7] != null) {
                    if ("0".equals(object[7].toString())) {
                        vo.setValidateStatus(NOTVALIDATE);
                    } else if ("1".equals(object[7].toString())) {
                        vo.setValidateStatus(VALIDATING);
                    } else if ("2".equals(object[7].toString())) {
                        vo.setValidateStatus(SUCCESS);
                    } else if ("3".equals(object[7].toString())) {
                        vo.setValidateStatus(FAIL);
                    }
                }
                ledgerProcessVo.setTaskId(object[8].toString());
                if (object[9] != null) {
                    ledgerProcessVo.setEndTime(object[9].toString());
                }
                //频度
                if (object[10] != null) {
                    ledgerProcessVo.setFreq(object[10].toString());
                }
                vos.add(ledgerProcessVo);
            }
        }
        Pageable pageable = new PageRequest(vo.getPageNum() - 1, vo.getPageSize());
        Page<AccountProcessVo> voPage = new PageImpl<>(vos, pageable, page.getTotalElements());
		return voPage;
	}

}
