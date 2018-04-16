package com.fitech.account.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.dao.AccountBaseDao;
import com.fitech.account.dao.AccountProcessDao;
import com.fitech.account.repository.AccountProcessRepository;
import com.fitech.account.repository.AccountRepository;
import com.fitech.account.repository.AccountTaskRepository;
import com.fitech.account.repository.DictionaryItemRepository;
import com.fitech.account.service.AccountProcessService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountProcess;
import com.fitech.domain.account.AccountTask;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.ledger.LedgerProcess;
import com.fitech.domain.system.FieldPermission;
import com.fitech.domain.system.Institution;
import com.fitech.domain.system.ProcessConfig;
import com.fitech.domain.system.ReportPermission;
import com.fitech.domain.system.Role;
import com.fitech.domain.system.User;
import com.fitech.enums.ValidateStatusEnum;
import com.fitech.enums.account.AccountStateEnum;
import com.fitech.framework.activiti.service.FFInstance;
import com.fitech.framework.activiti.service.ProcessService;
import com.fitech.framework.activiti.service.TodoTaskService;
import com.fitech.framework.activiti.vo.TaskVo;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.common.CommonConst;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.ExcelUtil;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.system.activiti.ProcessStartListener;
import com.fitech.system.repository.AccountFieldPermissionRepository;
import com.fitech.system.repository.InstitutionRepository;
import com.fitech.system.repository.UserRepository;
import com.fitech.validate.domain.ValidateAnalyzeResult;
import com.fitech.validate.domain.ValidateBatch;
import com.fitech.validate.domain.ValidateResult;
import com.fitech.validate.service.ValidateAnalyzeResultService;
import com.fitech.validate.service.ValidateResultService;
import com.fitech.vo.account.AccountProcessVo;
import com.fitech.vo.ledger.LedgerProcessVo;


/**
 * 补录业务流程
 * Created by wupei on 2017/3/1.
 */
@Service
@ServiceTrace
public class AccountProcessServiceImpl implements AccountProcessService {

    @Autowired
    private UserRepository<User> userRepository;
    @Autowired
    private InstitutionRepository<Institution> institutionInstitutionRepository;

    @Autowired
    private TodoTaskService todoTaskService;

    @Autowired
    private ProcessService processService;

    @Autowired
    private AccountRepository accountRepository;
    
    @Autowired
    private AccountFieldPermissionRepository accountFieldPermissionRepository;

    @Autowired
    private ValidateAnalyzeResultService validateAnalyzeResultService;
    
    @Autowired
    private AccountProcessDao accountProcessDao;

    @Autowired
    private AccountTaskRepository accountTaskRepository;

    @Autowired
    private AccountProcessRepository accountProcessRepository;

    @Autowired
    private ValidateResultService validateResultService;
    
    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private AccountBaseDao accountBaseDao;
    
    @Autowired
    private DictionaryItemRepository dictionaryItemRepository;

    @Override
    public AccountProcess findProcessById(Long id) {
        AccountProcess accountProcess = null;
        if(id != null){
            accountProcess = accountProcessRepository.findOne(id);
        }
        return accountProcess;
    }

    public Page<AccountProcessVo> findPageAccountProcessList(AccountProcessVo vo){
        try {
        	//查询用户
            User user = null;
            if (vo.getUserId() != null) {
                user = this.userRepository.findById(vo.getUserId());
            }
            return accountProcessDao.findTodoTaskBySql(vo, user);
        }catch (Exception e){
            e.printStackTrace();
            throw  new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
    }
    //数据查询---数据查询的初始化和高级查询
    public Page<AccountProcessVo> findPageAccounts(AccountProcessVo vo){
        try {
            return accountProcessDao.findDoneQuerySql(vo);
        }catch (Exception e){
            e.printStackTrace();
            throw  new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
    }
    
  //下载---数据查询的下载
    public String downLoadAccounts(String searchs){
    	AccountProcessVo ap=new AccountProcessVo();
    	if("true".equals(searchs)){
    		ap.setTerm("");
    		ap.setReportTemplateName("");
    		ap.setInstitutionName("");
    	}else{
    		String[] searchlist = searchs.split(",",3);
    		ap.setTerm(searchlist[0]);
        	ap.setReportTemplateName(searchlist[1]);  	
        	ap.setInstitutionName(searchlist[2]);
    	} 	   	
    	List<AccountProcessVo> list =accountProcessDao.findDoneQuerySqltwo(ap);
    	String sheetName="DoneQuery";
    	List<List<String>> hList = new ArrayList<>();
		List<String> lineFirst=new ArrayList<>();
		lineFirst.add("报送机构");
		lineFirst.add("期数");
		lineFirst.add("台账");
		hList.add(lineFirst);
    	for(AccountProcessVo ac:list){
    		List<String> line=new ArrayList<>();
    		line.add(ac.getInstitutionName());
    		line.add(ac.getTerm());
    		line.add(ac.getReportTemplateName());
    		hList.add(line);
    	} 	
    	return ExcelUtil.createExcel(hList, sheetName, CommonConst.getProperties("template_path"),sheetName);
    }

    public Page<AccountProcessVo> findPagefindAssignedTask(AccountProcessVo vo){
        try {
            //查询用户
            User user = null;
            if (vo.getUserId() != null) {
                user = this.userRepository.findById(vo.getUserId());
            }
            return accountProcessDao.findDoneTaskBySql(vo, user);
        }catch (Exception e){
            e.printStackTrace();
            throw  new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
    }


    public Collection<User> findUserByInstitution(Long userId){
        try {
            User user = this.userRepository.findById(userId);
            if (user != null) {

            }
            return null;
        }catch (Exception e){
            e.printStackTrace();
            throw  new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
    }

    /**
     * 对象转化 转化成页面的vo
     * @param lp
     * @return
     */
    private LedgerProcessVo objectChange(LedgerProcess lp){
        try {
            if (lp != null) {
                LedgerProcessVo lpVo = new LedgerProcessVo();
                //设置机构ID
                lpVo.setInstitutionId(lp.getLedgerReport().getInstitution().getInstitutionId());
                //设置机构名称
                lpVo.setInstitutionName(lp.getLedgerReport().getInstitution().getInstitutionName());
                //报文编号
                lpVo.setReportTemplateId(lp.getLedgerReport().getLedgerReportTemplate().getTemplateCode());
                // 报表名称
                lpVo.setReportTemplateName(lp.getLedgerReport().getLedgerReportTemplate().getTemplateName());
                //设置频度
                lpVo.setFreq(lp.getLedgerReport().getFreq());
                //设置期数
                lpVo.setTerm(lp.getLedgerReport().getTerm());
                //校验状态
                lpVo.setValidateStatus(lp.getLedgerReport().getValidateStatus().getValidateStatus());
                //根据流程实例ID 查询流程名称
                List<TaskVo> taskList = todoTaskService.getTasksByProcInstId(lp.getProcInsetId());
                lpVo.setProcessId(lp.getProcInsetId());
                if (taskList != null && taskList.size() > 0) {
                    lpVo.setProcessName(taskList.get(0).getTaskName());
                    lpVo.setTaskId(taskList.get(0).getTaskId());
                }
                return lpVo;
            }
            return null;
        }catch (Exception e){
            e.printStackTrace();
            throw  new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
    }

    
    @Transactional
	public void processStart(ProcessConfig processConfig,Account rpt) {
        try {
            //获取流程定义ID和流程配置ID，方便获取流程开启配置节点参数
            Map<String, Object> variables = new HashMap<String, Object>();
            variables.put(ProcessStartListener.config_key, processConfig.getId());
            variables.put(ProcessStartListener.report_key, rpt.getId());
            //流程开启
            FFInstance processInstance = processService.startInstance(processConfig.getProcessDefId(), variables);
            //保存业务流程关联数据
            AccountProcess ledgerProcess = new AccountProcess();
            ledgerProcess.setAccount(accountRepository.findOne(rpt.getId()));
            ledgerProcess.setProcInsetId(processInstance.getInstanceID());
            ledgerProcess.setStartTime(new Date());
            accountProcessRepository.save(ledgerProcess);
        }catch (Exception e){
            e.printStackTrace();
            throw  new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
	}

    @Override
    @Transactional
    public GenericResult<Boolean> submitProcess(List<AccountProcessVo> accountProcessVoList, String action,Long userId) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            if(accountProcessVoList == null || accountProcessVoList.isEmpty()){
                result.setSuccess(false);
                result.setMessage("commit task parameter require is not null !");
                return result;
            }
            for (int i = 0; i < accountProcessVoList.size(); i++) {
                AccountProcessVo accountProcessVo = accountProcessVoList.get(i);

                accountProcessVo.setUserId(userId);
                if(StringUtil.isEmpty(accountProcessVo.getTaskId())){
                    result.setSuccess(false);
                    result.setMessage("commit TaksId is null !");
                    return result;
                }
                if(accountProcessVo==null ||
                        accountProcessVo.getAccountProcess().getId() == null){
                    result.setSuccess(false);
                    result.setMessage("commit ProcessId is null !");
                    return result;
                }
                if(accountProcessVo.getAccount() == null ||  accountProcessVo.getAccount().getId() ==null){
                    result.setSuccess(false);
                    result.setMessage("commit task account id is null !");
                    return result;
                }
                if(accountProcessVo.getAccount() == null || accountProcessVo.getAccount().getAccountState() ==null){
                    result.setSuccess(false);
                    result.setMessage("commit task account state is null !");
                    return result;
                }
                if(accountProcessVo.getUserId() == null){
                    result.setSuccess(false);
                    result.setMessage("commit task user is null !");
                    return result;
                }
              
                Account account = accountProcessVo.getAccount();

                Account ac = accountRepository.findOne(account.getId());
                //提交的时候进行校验判断
                if("commit".equals(action)){
                	//1查询用户有权限的字段(hx)
                    // 根据userid获取该用户的角色集合
                    Collection<Role> c = userRepository.findById(userId).getRoles();
                    // 该报文已经配置的字段权限
                    Collection<FieldPermission> rrfps = new ArrayList<FieldPermission>();
                    // 迭代该用户的角色集合
                    Iterator<Role> it = c.iterator();
                    while (it.hasNext()) {
                        Role role = it.next();
                        Role r = accountFieldPermissionRepository.findById(role.getId());
                        Collection<FieldPermission> rfps = r.getFieldPermission();
                        // 迭代字段权限集合
                        Iterator<FieldPermission> its = rfps.iterator();
                        while (its.hasNext()) {
                            FieldPermission fp = its.next();
                            // 如果字段权限的模板id是该模板id则将该模板权限添加到已配置的字段权限集合中
                            if (r.getSubSystem().getSubKey().equals("sjbl")) {
                                rrfps.add(fp);
                            }
                        }
                    }           
                    Account accountv = accountRepository.findOne(accountProcessVo.getAccount().getId());
                    Collection<AccountField> accountField = accountv.getAccountTemplate().getAccountFields();
                    Iterator<AccountField> itaf = accountField.iterator();
                    while (itaf.hasNext()) {
                        AccountField af = itaf.next();
                        if(af.getItemType().equals("CODELIB")){
                        	af.setDictionaryItems(dictionaryItemRepository.findByDictionaryId(Long.valueOf(af.getDicId())));
                        }
                        Iterator<FieldPermission> itfp = rrfps.iterator();
                        String allfp = "";
                        while (itfp.hasNext()) {
                            FieldPermission fp = itfp.next();
                            if (af.isPkable() == false && af.getId().equals(fp.getAccountField().getId())) {
                                allfp += fp.getOperationType().toString();
                                allfp += ",";
                            }
                        }
                        if (!allfp.equals("")) {
                            allfp = allfp.substring(0, allfp.lastIndexOf(","));
                        }
                        af.setFieldPermission(allfp);
                    }
                    
                    String[] operateFieldArr=new String[0];
                    AccountTemplate accountTemplate = accountv.getAccountTemplate();
                    for (AccountField accoutnfield : accountTemplate.getAccountFields()) {
                        if (accoutnfield.isPkable()) {
                        	operateFieldArr=Arrays.copyOf(operateFieldArr, operateFieldArr.length+1);
                        	operateFieldArr[operateFieldArr.length-1]=accoutnfield.getItemCode();
                        }else if(accoutnfield.getFieldPermission().indexOf("OPERATE")!=-1){
                        	operateFieldArr=Arrays.copyOf(operateFieldArr, operateFieldArr.length+1);
                        	operateFieldArr[operateFieldArr.length-1]=accoutnfield.getItemCode();
                        }
                    }
                    accountProcessVo.setOperateFieldArr(operateFieldArr);               
                    //2进行校验并修改状态
                    Boolean valValidateRule=true;
                    //业务条线：表名
                    String validateTableName = accountv.getAccountTemplate().getBusSystem().getReportSubSystem().getSubKey() + ":" + accountv.getAccountTemplate().getTableName();
                    Collection<ValidateAnalyzeResult> rules = 
                    		validateAnalyzeResultService.findByObjectID(validateTableName, accountProcessVo.getOperateFieldArr());
                    
                    //首都银行7067缺陷问题修复  2017-11-07 陈超
                    if(!rules.isEmpty()){
                    	ValidateBatch validateBatch = validateAnalyzeResultService.excuteFormu(rules, accountProcessVo.getAccount().getId() + "");
                        //校验结果
                        Collection<ValidateResult> list = validateResultService.findByValidatebatch(validateBatch.getBatchId());
                        if (list.size() > 0) {
                        	valValidateRule=false;  
                        }
                    }
                  //首都银行7067缺陷问题修复结束
                    
                  //3对数据库校验结果进行修改
                    if(false==valValidateRule){
                    	ac.setValidateStatus(ValidateStatusEnum.FAIL);
                    	accountRepository.save(ac);
                    }else{
                    	ac.setValidateStatus(ValidateStatusEnum.SUCCESS);
                    	accountRepository.save(ac);
                    }
                }
                                            
                //对校验结果进行判断
                if(ac.getValidateStatus()!=ValidateStatusEnum.SUCCESS){              	
                	result.setRestCode(ExceptionCode.SYSTEM_ERROR);
                    result.setMessage("commit account ValidateStatus is not sucess !");
                    result.setSuccess(false);
                    return result;
                }
                
                if(ac.getAccountState() == account.getAccountState()){
                    result.setSuccess(false);
                    result.setMessage("commit accountState no change!");
                    throw new AppException(ExceptionCode.SYSTEM_ERROR,"commit accountState error,accountState no change");
                }

              //退回业务流程，保存退回原因
                if("refuse".equals(action)){
                    AccountProcess accountProcess = accountProcessVo.getAccountProcess();

                    AccountProcess ap = accountProcessRepository.findOne(accountProcess.getId());                  

                    ap.setRefuseCause(accountProcess.getRefuseCause());
                                        
                    //在退回中accountstate修改状态
                    ac.setAccountState(account.getAccountState());
                    accountRepository.save(ac);
                    
                    if(accountProcessVo.getAccount().getAccountState() == AccountStateEnum.SHTTG){

                        accountProcessRepository.save(ap);
                    }
                    
                }
                
                //调用流程引擎提交任务
                String[] taskIds = accountProcessVo.getTaskId().split(",");
                todoTaskService.batchComplete(taskIds, action);

                for (String taskId : taskIds) {
                    //记录业务流程轨迹数据
                    AccountTask accountTask = new AccountTask();
                    accountTask.setAccountProcess(accountProcessVo.getAccountProcess());
                    accountTask.setEditUserId(String.valueOf(accountProcessVo.getUserId()));
                    accountTask.setSubmitTime(new Date());
                    accountTask.setTaskId(accountProcessVo.getTaskId());

                    accountTaskRepository.save(accountTask);
                }
                //更新业务状态
                AccountProcess accountProcess = accountProcessVo.getAccountProcess();

                AccountProcess ap = accountProcessRepository.findOne(accountProcess.getId());

                if(isMultiInstanceTaskExecOver(ap.getProcInsetId())){
                    ac.setAccountState(account.getAccountState());

                    accountRepository.save(ac);

                    if(account.getAccountState() == AccountStateEnum.SHTG){

                        ap.setEndTime(new Date());

                        accountProcessRepository.save(ap);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
        return result;
    }

    
    //删除流程下所有实例(hx)
    public void deleteProcessBL(String reportId){
    	StringBuffer sb = new StringBuffer();   	
        sb.append(" select p.id lpid, p.procinsetid lpproc, r.id lrid ");
        sb.append(" from account r ");
        sb.append(" left join accountprocess p ");
        sb.append(" on p.account_id = r.id ");
        sb.append(" where r.processconfig = ");
        sb.append(reportId);
        Map<String,String > map=new HashMap<>();
        List<Object[]> list=accountBaseDao.findBySql(sb, map);
        if (list != null && (!list.isEmpty())) {
            for (Object[] object : list) {
            	if(object[0]!=null){
            		String procid=String.valueOf(object[1]);
                	//捕捉改流程activity已被其他流程删除
                	processService.deleteProcInstance(procid, null);
                	Long lpid=Long.valueOf(String.valueOf(object[0]));
                	accountProcessRepository.delete(lpid);
            	}else{
            		Long lrid=Long.valueOf(String.valueOf(object[2]));
            		accountRepository.delete(lrid);
            		System.out.println("改任务还未进入流程");
            	}
            }
        }
        System.out.println("-------------该方法已执行完成--------------");
    }
    
    public Boolean isMultiInstanceTaskExecOver(String proInstId){
        boolean result = true;
        if(StringUtil.isNotEmpty(proInstId)){

            String sql = "select LONG_ from ACT_RU_VARIABLE where NAME_='nrOfActiveInstances' and PROC_INST_ID_='"+proInstId+"'";

            Object object = accountBaseDao.findObjectBysql(new StringBuffer(sql),null);

            if(null != object){
                BigDecimal decimal = (BigDecimal)object;

                if(decimal.intValue() > 0){
                    return false;
                }
            }
        }
        return result;
    }

    @Override
    public List<Long> queryAccountTaskPermission(Long userId) {
        List<Long> permesionss = new ArrayList<>();
        User user = this.userRepository.findById(userId);

        if(null != user){
            Collection<Role> roleList = user.getRoles();
            if(null != roleList && !roleList.isEmpty()){
                for(Role role : roleList){
                    Collection<ReportPermission> permissions = role.getReportPermission();
                    if(null != permissions && !permissions.isEmpty()){
                        for(ReportPermission reportPermission : permissions){
                            Long opertorType = Long.valueOf(reportPermission.getOperationType().ordinal());
                            if(!permesionss.contains(opertorType)){
                                permesionss.add(opertorType);
                            }
                        }
                    }
                }
            }
        }
        return permesionss;
    }
}
