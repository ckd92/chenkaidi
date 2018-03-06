//package com.fitech.account.dao.impl.mysql;
//
//import com.fitech.domain.ledger.LedgerReportTemplate;
//import com.fitech.domain.system.User;
//import com.fitech.framework.lang.util.StringUtil;
//import com.fitech.account.dao.AccountBaseDao;
//import com.fitech.account.dao.AccountProcessDao;
//import com.fitech.account.repository.LedgerReportTemplateRepository;
//import com.fitech.vo.ledger.DataMonitoringVo;
//import com.fitech.vo.ledger.LedgerProcessVo;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
//
//import javax.sql.DataSource;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//public class AccountProcessDaoImpl extends NamedParameterJdbcDaoSupport implements AccountProcessDao {
//	@Autowired
//    public AccountProcessDaoImpl(DataSource dataSource) {
//        setDataSource(dataSource);
//        try {
//            dataSource.getConnection().setAutoCommit(true);
//
//        } catch (Exception e) {
//
//        }
//    }
//	private static String NOTVALIDATE = "未校验";
//
//    private static String VALIDATING = "校验中";
//
//    private static String SUCCESS = "校验通过";
//
//    private static String FAIL = "校验失败";
//    @Autowired
//    private AccountBaseDao baseDao;
//    @Autowired
//    private LedgerReportTemplateRepository ledgerReportTemplateRepository;
//
//	@Override
//	public Page<LedgerProcessVo> findTodoTaskBySql(LedgerProcessVo vo,User user) {
//        StringBuffer sql = new StringBuffer();
//        //拼凑sql
//        //id,机构ID,机构名称,报表编号,频度,期数,提交人,校验状态，完成时间,节点名称
//        sql.append("select task.id_ as id,it.institutionId,it.institutionName,lrt.id as templateId,lrt.templateName,lr.term,task.name_,lr.validateStatus,task.id_,o.ledgerReport_id,task.description_,lr.freq,o.procInsetId  ");
//        sql.append(" from LedgerProcess o inner join LedgerReport lr on lr.id=o.ledgerReport_id");
//        sql.append(" inner join Institution it on it.id=lr.institution_id");
//        sql.append(" inner join ledgerrpttemplate lrt on lrt.id=lr.ledgerReportTemplate_id");
//        sql.append(" inner join BusSystem bs on bs.id = lrt.busSystem_id");
//        sql.append(" inner join ACT_RU_TASK task on task.proc_inst_id_=o.procInsetId");
//        sql.append(" inner join sysuser_role ur on ur.roles_id=task.ASSIGNEE_");
//        sql.append(" where 1=1 ");
//        //报表编号
//        if (!StringUtil.isEmpty(vo.getReportTemplateName())) {
//            sql.append("and lrt.templateName like '%" + vo.getReportTemplateName() + "%'");
//        }
//        //报表频度
//        if (!StringUtil.isEmpty(vo.getFreq())) {
//            sql.append("and lr.freq='" + vo.getFreq() + "'");
//        }
//        //期数
//        if (!StringUtil.isEmpty(vo.getTerm())) {
//            sql.append("and lr.term='" + vo.getTerm() + "'");
//        }
//        //机构名称
//        if (!StringUtil.isEmpty(vo.getInstitutionName())) {
//            sql.append("and it.institutionName like '%" + vo.getInstitutionName() + "%'");
//        }
//        //校验状态
//        if (!StringUtil.isEmpty(vo.getValidateStatus())) {
//            sql.append(" and lr.checkStatus='" + vo.getValidateStatus() + "'");
//        }
//        //报送状态
//        if (!StringUtil.isEmpty(vo.getProcessId())) {
//            sql.append("and task.proc_inst_id_='" + vo.getProcessId() + "'");
//        }
//        //用户ID
//        if (vo.getUserId() != null) {
//            sql.append(" and ur.sysuser_id='" + vo.getUserId() + "'");
//        }
//        //条线ID
//        if (vo.getSubSystemId() != null) {
//            sql.append(" and bs.reportSubSystem_id='" + vo.getSubSystemId() + "'");
//        }
//        //只查询此用户下的机构
//        sql.append(" and lr.institution_id='" + user.getOrg().getId() + "'");
//        sql.append(" order by o.id");
//        Page<Object[]> page = baseDao.findPageBySql(sql, null, vo.getPageSize(), vo.getPageNum());
//        List<LedgerProcessVo> vos = new ArrayList<>();
//        //循环拼凑
//        if (page != null && page.getSize() > 0) {
//            for (Object[] object : page.getContent()) {
//                LedgerProcessVo ledgerProcessVo = new LedgerProcessVo();
//                //对象ID
//                ledgerProcessVo.setId(object[0].toString());
//                //机构ID
//                ledgerProcessVo.setInstitutionId(object[1].toString());
//                //机构名称
//                ledgerProcessVo.setInstitutionName(object[2].toString());
//                //模版编号
//                ledgerProcessVo.setReportTemplateId(object[3].toString());
//                //模版名称
//                ledgerProcessVo.setReportTemplateName(object[4].toString());
//                ledgerProcessVo.setTerm(object[5].toString());
//                ledgerProcessVo.setProcessName(object[6].toString());
//                //校验状态
//                if (object[7] != null) {
//                    //校验状态 0：
//                    if ("0".equals(object[7].toString())) {
//                    	ledgerProcessVo.setValidateStatus(NOTVALIDATE);
//                    } else if ("1".equals(object[7].toString())) {
//                    	ledgerProcessVo.setValidateStatus(VALIDATING);
//                    } else if ("2".equals(object[7].toString())) {
//                    	ledgerProcessVo.setValidateStatus(SUCCESS);
//                    } else if ("3".equals(object[7].toString())) {
//                    	ledgerProcessVo.setValidateStatus(FAIL);
//                    }
//                }
//                ledgerProcessVo.setTaskId(object[8].toString());
//                ledgerProcessVo.setReportId(object[9].toString());
//                //描述
//                if (object[10] != null) {
//                	ledgerProcessVo.setDescription(object[10].toString());
//                }
//                //频度
//                if (object[11] != null) {
//                	ledgerProcessVo.setFreq(object[11].toString());
//                }
//                //实例id
//                if (object[12] != null) {
//                	ledgerProcessVo.setProcessId(object[12].toString());
//                }
//                vos.add(ledgerProcessVo);
//            }
//        }
//        Pageable pageable = new PageRequest(vo.getPageNum() - 1, vo.getPageSize());
//        Page<LedgerProcessVo> voPage = new PageImpl<>(vos, pageable, page.getTotalElements());
//		return voPage;
//	}
//
//	@Override
//	public Page<LedgerProcessVo> findDoneTaskBySql(LedgerProcessVo vo,User user) {
//		StringBuffer sql = new StringBuffer();
//        //id,机构ID,机构名称,报表编号,频度,期数,提交人,校验状态，完成时间,节点名称
//        sql.append("select o.id,it.institutionId,it.institutionName,lrt.templateCode,lrt.templateName,lr.term,task.name_,lr.validateStatus,task.id_,task.end_time_,lr.freq  ");
//        sql.append(" from LedgerProcess o inner join LedgerReport lr on lr.id=o.ledgerReport_id");
//        sql.append(" inner join Institution it on it.id=lr.institution_id");
//        sql.append(" inner join ledgerrpttemplate lrt on lrt.id=lr.ledgerReportTemplate_id");
//        sql.append(" inner join BusSystem bs on bs.id = lrt.busSystem_id");
//        sql.append(" inner join ACT_HI_TASKINST task on task.proc_inst_id_=o.procInsetId");
//        sql.append(" inner join sysuser_role ur on ur.roles_id=task.ASSIGNEE_");
//        sql.append(" where 1=1 ");
//        //报表编号
//        if (!StringUtil.isEmpty(vo.getReportTemplateName())) {
//            sql.append("and lrt.templateName like '%" + vo.getReportTemplateName() + "%'");
//        }
//        //报表频度
//        if (!StringUtil.isEmpty(vo.getFreq())) {
//            sql.append("and lr.freq='" + vo.getFreq() + "'");
//        }
//        //期数
//        if (!StringUtil.isEmpty(vo.getTerm())) {
//            sql.append("and lr.term='" + vo.getTerm() + "'");
//        }
//        //报表名称
//        if (!StringUtil.isEmpty(vo.getReportTemplateName())) {
//            sql.append("and lrt.templateName like '%" + vo.getReportTemplateName() + "%'");
//        }
//        //机构名称
//        if (!StringUtil.isEmpty(vo.getInstitutionName())) {
//            sql.append("and it.institutionName like '%" + vo.getInstitutionName() + "%'");
//        }
//        //校验状态
//        if (!StringUtil.isEmpty(vo.getValidateStatus())) {
//            sql.append("and lr.checkStatus='" + vo.getValidateStatus() + "'");
//        }
//        //报送状态
//        if (!StringUtil.isEmpty(vo.getProcessId())) {
//            sql.append(" and task.proc_inst_id_='" + vo.getProcessId() + "'");
//        }
//        //用户ID
//        if (vo.getUserId() != null) {
//            sql.append(" and ur.sysuser_id='" + vo.getUserId() + "'");
//        }
//        //条线ID
//        if (vo.getSubSystemId() != null) {
//            sql.append(" and bs.reportSubSystem_id='" + vo.getSubSystemId() + "'");
//        }
//        //只查询此用户下的机构
//        sql.append(" and lr.institution_id='" + user.getOrg().getId() + "'");
//        sql.append(" and task.end_time_ is not null  ");
//        sql.append(" order by o.id");
//        Page<Object[]> page = baseDao.findPageBySql(sql, null, vo.getPageSize(), vo.getPageNum());
//        List<LedgerProcessVo> vos = new ArrayList<>();
//        //循环拼凑
//        if (page != null && page.getSize() > 0) {
//            for (Object[] object : page.getContent()) {
//                LedgerProcessVo ledgerProcessVo = new LedgerProcessVo();
//                //对象ID
//                ledgerProcessVo.setId(object[0].toString());
//                //机构ID
//                ledgerProcessVo.setInstitutionId(object[1].toString());
//                //机构名称
//                ledgerProcessVo.setInstitutionName(object[2].toString());
//                //模版编号
//                ledgerProcessVo.setReportTemplateId(object[3].toString());
//                //模版名称
//                ledgerProcessVo.setReportTemplateName(object[4].toString());
//                ledgerProcessVo.setTerm(object[5].toString());
//                ledgerProcessVo.setProcessName(object[6].toString());
//                //校验状态
//                if (object[7] != null) {
//                    if ("0".equals(object[7].toString())) {
//                        vo.setValidateStatus(NOTVALIDATE);
//                    } else if ("1".equals(object[7].toString())) {
//                        vo.setValidateStatus(VALIDATING);
//                    } else if ("2".equals(object[7].toString())) {
//                        vo.setValidateStatus(SUCCESS);
//                    } else if ("3".equals(object[7].toString())) {
//                        vo.setValidateStatus(FAIL);
//                    }
//                }
//                ledgerProcessVo.setTaskId(object[8].toString());
//                if (object[9] != null) {
//                    ledgerProcessVo.setEndTime(object[9].toString());
//                }
//                //频度
//                if (object[10] != null) {
//                    ledgerProcessVo.setFreq(object[10].toString());
//                }
//                vos.add(ledgerProcessVo);
//            }
//        }
//        Pageable pageable = new PageRequest(vo.getPageNum() - 1, vo.getPageSize());
//        Page<LedgerProcessVo> voPage = new PageImpl<>(vos, pageable, page.getTotalElements());
//		return voPage;
//	}
//
//	@Override
//	public Page<LedgerProcessVo> findSuperviseTaskBySql(LedgerProcessVo vo,User user) {
//		StringBuffer sql = new StringBuffer();
//        //id,机构ID,机构名称,报表编号,频度,期数,提交人,校验状态，完成时间,节点名称
//        sql.append("select o.id,it.institutionId,it.institutionName,lrt.templateCode,lrt.templateName,lr.term,task.name_,lr.validateStatus,task.id_,lr.freq,task.description_,o.procInsetId ");
//        sql.append(" from LedgerProcess o left join LedgerReport lr on lr.id=o.ledgerReport_id");
//        sql.append(" left join Institution it on it.id=lr.institution_id");
//        sql.append(" left join ledgerrpttemplate lrt on lrt.id=lr.ledgerReportTemplate_id");
//        sql.append(" inner join BusSystem bs on bs.id = lrt.busSystem_id");
//        // sql.append(" left join BusSystem bs on bs.id=lrt.busSystem_id");
//        //  sql.append(" left join SubSystem_BusSystem ssbs on ssbs.busSystems_id=bs.id");
//        sql.append(" left join ACT_RU_TASK task on task.proc_inst_id_=o.procInsetId");
//        sql.append(" left join ACT_HI_IDENTITYLINK ahitask on ahitask.task_id_=task.id_");
//        // sql.append(" left join Code c on c.id=lr.freq");
//        sql.append(" left join sysuser_role ur on ur.roles_id=ahitask.user_id_");
//        sql.append(" where ahitask.type_='candidate' ");
//        if(null!=user.getOrg()){
//            sql.append(" and lr.institution_id='"+user.getOrg().getId()+"' ");
//        }
//        //报表名称
//        if(!StringUtil.isEmpty(vo.getReportTemplateName())){
//            sql.append(" and lrt.templateName like '%"+vo.getReportTemplateName()+"%'");
//        }
//        //报表频度
//        if(!StringUtil.isEmpty(vo.getFreq())){
//            sql.append(" and lr.freq='"+vo.getFreq()+"'");
//        }
//        //期数
//        if(!StringUtil.isEmpty(vo.getTerm())){
//            sql.append(" and lr.term='"+vo.getTerm()+"'");
//        }
//        //机构名称
//        if(!StringUtil.isEmpty(vo.getInstitutionName())){
//            sql.append(" and it.institutionName like '%"+vo.getInstitutionName()+"%'");
//        }
//        //校验状态
////    if(!StringUtil.isEmpty(vo.getValidateStatus())){
////        sql.append(" and o.checkStatus='"+vo.getValidateStatus()+"'");
////    }
//        //报送状态
////    if(!StringUtil.isEmpty(vo.getProcessId())){
////        sql.append(" and task.proc_inst_id_='"+vo.getProcessId()+"'");
////    }
//
//        if(vo.getUserId()!=null){
//            sql.append(" and ur.sysuser_id='"+vo.getUserId()+"'");
//        }
//        //条线ID
//        if (vo.getSubSystemId() != null) {
//            sql.append(" and bs.reportSubSystem_id='" + vo.getSubSystemId() + "'");
//        }
//
//        sql.append(" order by o.id");
//        Page<Object[]> page = baseDao.findPageBySql(sql,null,vo.getPageSize(),vo.getPageNum());
//        List<LedgerProcessVo> vos = new ArrayList<>();
//        //循环拼凑
//        if(page!=null && page.getSize()>0){
//            for (Object[] object: page.getContent()){
//                LedgerProcessVo ledgerProcessVo = new LedgerProcessVo();
//                //对象ID
//                ledgerProcessVo.setId(String.valueOf(object[0]));
//                //机构ID
//                ledgerProcessVo.setInstitutionId(String.valueOf(object[1]));
//                //机构名称
//                ledgerProcessVo.setInstitutionName(String.valueOf(object[2]));
//                //模版编号
//                ledgerProcessVo.setReportTemplateId(String.valueOf(object[3]));
//                //模版名称
//                ledgerProcessVo.setReportTemplateName(String.valueOf(object[4]));
//                ledgerProcessVo.setTerm(String.valueOf(object[5]));
//                ledgerProcessVo.setProcessName(String.valueOf(object[6]));
//                //校验状态
//                if(object[7]!=null){
//                    if("0".equals(object[7].toString())){
//                        ledgerProcessVo.setValidateStatus(NOTVALIDATE);
//                    }else if("1".equals(object[7].toString())){
//                        ledgerProcessVo.setValidateStatus(VALIDATING);
//                    }else if("2".equals(object[7].toString())){
//                        ledgerProcessVo.setValidateStatus(SUCCESS);
//                    }else if("3".equals(object[7].toString())){
//                        ledgerProcessVo.setValidateStatus(FAIL);
//                    }
//                }
//                ledgerProcessVo.setTaskId(String.valueOf(object[8]));
//                ledgerProcessVo.setFreq(String.valueOf(object[9]));
//                ledgerProcessVo.setDescription(String.valueOf(object[10]));
//                ledgerProcessVo.setProcessId(String.valueOf(object[11]));
//                if(object[3]!=null){
//                    List<LedgerReportTemplate> lrt=(ArrayList)ledgerReportTemplateRepository.findByTemplateCode(String.valueOf(object[3]));
//                    ledgerProcessVo.setSubSystem(lrt.get(0).getBusSystem().getReportSubSystem());
//                }
//                vos.add(ledgerProcessVo);
//            }
//        }
//        Pageable pageable = new PageRequest(vo.getPageNum()-1, vo.getPageSize());
//        Page<LedgerProcessVo> voPage = new PageImpl<>(vos,pageable,page.getTotalElements());
//		return voPage;
//	}
//
//	@Override
//	public Collection<DataMonitoringVo> findDataEnterMonitoring() {
//		StringBuffer sql = new StringBuffer();
//		sql.append("select i.institutionname,count(r.id) as total,count(t.id_) as unenter ");
//		sql.append("from institution i ");
//		sql.append("left join ledgerreport r on i.id = r.institution_id ");
//		sql.append("left join ledgerprocess p on r.id = p.ledgerreport_id ");
//		sql.append("left join act_ru_task t on p.procinsetid = t.proc_inst_id_ and t.category_ ='entering' ");
//		sql.append("group by i.institutionid, i.institutionname ");
//		//处理数据集
//		Collection<DataMonitoringVo> monitorList = new ArrayList<>();
//		List<Object[]> datas = baseDao.findBySql(sql, null);
//		for (Object[] data : datas) {
//			DataMonitoringVo monitor = new DataMonitoringVo();
//			monitor.setInstitutionname(String.valueOf(data[0]));
//			monitor.setTotal(String.valueOf(data[1]));
//			monitor.setUnenter(String.valueOf(data[2]));
//			monitorList.add(monitor);
//		}
//		return monitorList;
//	}
//}
