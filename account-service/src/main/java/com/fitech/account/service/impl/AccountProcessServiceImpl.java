package com.fitech.account.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.activiti.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.dao.AccountBaseDao;
import com.fitech.account.dao.AccountProcessDao;
import com.fitech.account.dao.AccountProcessServiceDao;
import com.fitech.account.repository.AccountProcessRepository;
import com.fitech.account.repository.AccountRepository;
import com.fitech.account.repository.AccountTaskRepository;
import com.fitech.account.repository.DictionaryItemRepository;
import com.fitech.account.service.AccountProcessService;
import com.fitech.account.util.AccountConstants;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountProcess;
import com.fitech.domain.account.AccountTask;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.system.FieldPermission;
import com.fitech.domain.system.ProcessConfig;
import com.fitech.domain.system.Role;
import com.fitech.domain.system.SubSystem;
import com.fitech.domain.system.User;
import com.fitech.dto.SubSystemDto;
import com.fitech.enums.ValidateStatusEnum;
import com.fitech.enums.account.AccountStateEnum;
import com.fitech.framework.activiti.lang.ProcessStartConst;
import com.fitech.framework.activiti.service.FFInstance;
import com.fitech.framework.activiti.service.ProcessService;
import com.fitech.framework.activiti.service.TodoTaskService;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.page.Page;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.report.dao.AccountProcDao;
import com.fitech.system.dao.SubSystemDao;
import com.fitech.system.dao.UserDataDao;
import com.fitech.system.repository.RoleRepository;
import com.fitech.system.repository.UserRepository;
import com.fitech.validate.domain.ValidateAnalyzeResult;
import com.fitech.validate.domain.ValidateBatch;
import com.fitech.validate.domain.ValidateResult;
import com.fitech.validate.service.ValidateAnalyzeResultService;
import com.fitech.validate.service.ValidateResultService;
import com.fitech.vo.account.AccountProcessVo;
import com.fitech.enums.SubmitStateEnum;


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
    private AccountRepository accountRepository;
    @Autowired
    private RoleRepository<Role> roleRepository;
    @Autowired
    private AccountTaskRepository accountTaskRepository;
    @Autowired
    private AccountProcessRepository accountProcessRepository;
    @Autowired
    private DictionaryItemRepository dictionaryItemRepository;

    @Autowired
    private TodoTaskService todoTaskService;
    @Autowired
    private ProcessService processService;
    @Autowired
    private ValidateAnalyzeResultService validateAnalyzeResultService;
    @Autowired
    private ValidateResultService validateResultService;
    @Autowired
    private RuntimeService runtimeService;
    
    @Autowired
    private AccountProcessDao accountProcessDao;
    @Autowired
    private AccountBaseDao accountBaseDao;
    @Autowired
    private UserDataDao userDataDao;
    @Autowired
    private AccountProcDao accountProcDao;
    @Autowired
    private AccountProcessServiceDao accountProcessServiceDao;
    @Autowired
    private SubSystemDao subSystemDao;
    public List<AccountProcessVo> findTodoTask(AccountProcessVo vo, Page page){
    	  List<AccountProcessVo> datas=new ArrayList<>();
        try {
        	//查询用户
            User user = null;
            if (vo.getUserId() != null) {
                user = userDataDao.findUserById(vo.getUserId());
            }
            datas=accountProcessDao.findTodoTaskBySql(vo, user,page);
            for(AccountProcessVo accountProcessVo:datas){
            	if(AccountConstants.isRunning(accountProcessVo.getAccount().getId())){
            		accountProcessVo.setReortStatus("数据处理中。。。");
            	}
            }
            return datas;
        }catch (Exception e){
            e.printStackTrace();
            throw  new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
    }
    
    public List<AccountProcessVo> findDoneTask(AccountProcessVo vo, Page page){
        try {
            //查询用户
            User user = null;
            if (vo.getUserId() != null) {
                user = userDataDao.findUserById(vo.getUserId());
            }
            SubSystem subSystem=vo.getSubSystem();
    		if(subSystem!=null){
    			String subKey=subSystem.getSubKey();
    			SubSystemDto subSystemDto=subSystemDao.findSubSystemBySubKey(subKey);
    			vo.setSubSystemId(subSystemDto.getId().intValue());
    		}
            return accountProcessDao.findDoneTaskBySql(vo, user,page);
        }catch (Exception e){
            e.printStackTrace();
            throw  new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
        }
    }
    
    public List<AccountProcessVo> findPageAccounts(AccountProcessVo vo, Page page){
        try {
            return accountProcessDao.findDoneQuerySql(vo,page);
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
            variables.put(ProcessStartConst.config_key, processConfig.getId());
            variables.put(ProcessStartConst.report_key, rpt.getId());
            //流程开启
            FFInstance processInstance = processService.startInstance(processConfig.getProcessDefId(), variables);
            //保存业务流程关联数据
            AccountProcess ledgerProcess = new AccountProcess();
            Account report = accountRepository.findOne(rpt.getId());
            report.setSubmitStateType(SubmitStateEnum.SUBMITING);
            report.setAccountState(AccountStateEnum.DBL);
            ledgerProcess.setAccount(report);
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
            	if(AccountConstants.isRunning(account.getId())){
            		  result.setSuccess(false);
                      result.setMessage("数据处理中，禁止流程操作");
                      throw new AppException(ExceptionCode.SYSTEM_ERROR,"数据处理中，禁止流程操作");
            	}
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
                        Role r = roleRepository.findById(role.getId());
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
                    result.setMessage("commit account ValidateStatus is not success !");
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
                todoTaskService.batchComplete(taskIds, action,userId);

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

    /**
     * 判断多任务是否执行完成（接盘侠的猜测）
     * @param proInstId
     * @return
     */
    private Boolean isMultiInstanceTaskExecOver(String proInstId){
        boolean result = true;
        if(StringUtil.isNotEmpty(proInstId)){
        	long value = accountProcessServiceDao.isMultiInstanceTaskExecOver(proInstId);
            if(value > 0){
                return false;
            }
        }
        return result;
    }
    
    

//	@Override
//	public List<Long> getReceiverIdList(String term, String freq) {
//		return accountProcessServiceDao.getReceiverIdList(term, freq);
//	}

	@Override
	public void createAccountTask(String term) {
		accountProcessDao.createAccountTask(term);
	}

	@Override
	public void createAccountTask(String term, String freq) {
		accountProcessDao.createAccountTask(term,freq);	
	}
}
