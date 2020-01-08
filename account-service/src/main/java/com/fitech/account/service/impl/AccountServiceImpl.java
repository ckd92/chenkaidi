package com.fitech.account.service.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fitech.account.dao.DictionaryDao;
import com.fitech.account.repository.AccountTemplateRepository;
import com.fitech.account.repository.DictionaryRepository;
import com.fitech.domain.account.*;
import com.fitech.framework.lang.annotation.Description;
import com.fitech.vo.account.AccountDicVo;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.dao.AccountDataDao;
import com.fitech.account.dao.AccountDatasDao;
import com.fitech.account.dao.AccountsDao;
import com.fitech.account.repository.AccountRepository;
import com.fitech.account.repository.DictionaryItemRepository;
import com.fitech.account.service.AccountEditLogService;
import com.fitech.account.service.AccountService;
import com.fitech.account.util.ExcelUtils;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.system.FieldPermission;
import com.fitech.domain.system.Role;
import com.fitech.domain.system.User;
import com.fitech.enums.SqlTypeEnum;
import com.fitech.enums.ValidateStatusEnum;
import com.fitech.enums.account.AccountEditEnum;
import com.fitech.enums.account.LogSourceEnum;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.common.CommonConst;
import com.fitech.framework.lang.page.Page;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.ExcelUtil;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.system.repository.UserRepository;
import com.fitech.system.service.FieldPermissionService;
import com.fitech.validate.domain.ObjectValidateRule;
import com.fitech.validate.domain.ValidateAnalyzeResult;
import com.fitech.validate.domain.ValidateBatch;
import com.fitech.validate.domain.ValidateResult;
import com.fitech.validate.service.ObjectValidateRuleService;
import com.fitech.validate.service.ValidateAnalyzeResultService;
import com.fitech.validate.service.ValidateResultService;
import com.fitech.vo.account.AccountProcessVo;

/**
 * Created by wangxw on 2017/8/17.
 */
@Service
@ServiceTrace
public class AccountServiceImpl implements AccountService {
    @Autowired
    private AccountTemplateRepository accountTemplateRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private DictionaryDao dictionaryDao;
    @Autowired
    private UserRepository<User> userRepository;
    
    @Autowired
    private AccountDataDao accountDataDao;
    @Autowired
    private AccountDatasDao accountDatasDao;

    @Autowired
    private DictionaryRepository dictionaryRepository;
    @Autowired
    private AccountEditLogService accountEditLogService;
    @Autowired
    private ObjectValidateRuleService objectValidateRuleService;
    @Autowired
    private ValidateAnalyzeResultService validateAnalyzeResultService;
    @Autowired
    private ValidateResultService validateResultService;
    @Autowired
    private DictionaryItemRepository dictionaryItemRepository;
    @Autowired
    private FieldPermissionService fieldPermissionService;
    @Autowired
    private AccountsDao accountsDao;

    @Override
    public GenericResult<AccountProcessVo> findAccounDatas(AccountProcessVo accountProcessVo, Page page) {
        GenericResult<AccountProcessVo> result = new GenericResult<>();
        try {
            if (accountProcessVo == null || accountProcessVo.getAccount() == null) {
                result.setSuccess(false);
                result.setMessage("account param is null !");
                return result;
            }
            if (accountProcessVo.getAccount().getId() == null) {
                result.setSuccess(false);
                result.setMessage("account id is null !");
                return result;
            }
            
            // 补录台账信息
            Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
            // 该报文已经配置的字段权限
            Collection<FieldPermission> rrfps = fieldPermissionService.findByUserAndTemplate(accountProcessVo.getUserId(), account.getAccountTemplate());
            // 补录台账字段信息
            Collection<AccountField> accountField = account.getAccountTemplate().getAccountFields();
            Iterator<AccountField> itaf = accountField.iterator();
            //放置前端查询条件
            List<AccountField> fieldList = new ArrayList<>();
            for (AccountField field : accountField) {
                List<AccountField> accountSearchs = accountProcessVo.getAccount().getAccountSearchs();
                if (accountSearchs != null){
                    for (AccountField accountSearch : accountSearchs) {
                        if (field.getItemCode().equals(accountSearch.getItemCode())){
                            field.setValue(accountSearch.getValue());
                        }
                    }
                }
                fieldList.add(field);
            }
            account.setAccountSearchs(fieldList);
            while (itaf.hasNext()) {
                AccountField af = itaf.next();
                if (af.getItemType().equals("CODELIB")) {
                    af.setDictionaryItems(dictionaryItemRepository.findByDictionaryId(Long.valueOf(af.getDicId())));
                }
                
                String allfp = "";
                
                Iterator<FieldPermission> itfp = rrfps.iterator();
                while (itfp.hasNext()) {
                    FieldPermission fp = itfp.next();
                    if (af.isPkable() == false && af.getId().equals(fp.getAccountField().getId())) {
                        allfp += String.valueOf(fp.getOperationType()).concat(",");
                    }
                }
                //allfp不为空，去掉末尾的逗号
                if (StringUtil.isNotEmpty(allfp)) {
                    allfp = allfp.substring(0, allfp.lastIndexOf(","));
                }
                //权限转换，实际有的操作权限没有存放数据库，数据库存放的权限实际没有
                switch (allfp) {
                    case "LOOK":
                        allfp = "OPERATE";
                        break;
                    case "OPERATE":
                        allfp = "LOOK";
                        break;
                    case "":
                        allfp = "LOOK,OPERATE";
                        break;
                    default:
                        allfp = "";
                        break;
                }
                af.setFieldPermission(allfp);
            }
            
            accountProcessVo.setAccount(account);

            List<AccountLine> accountLines = accountDataDao.findDataByCondition(accountProcessVo,page);
            account.setAccountLines(accountLines);
            

            Collection<AccountLine> accountLineCol = accountProcessVo.getAccount().getAccountLines();
            Iterator accountIt = accountLineCol.iterator();
            while (accountIt.hasNext()) {
                AccountLine accountLine = (AccountLine) accountIt.next();
                Iterator accountLineIt = accountLine.getAccountFields().iterator();
                while (accountLineIt.hasNext()) {
                    AccountField af = (AccountField) accountLineIt.next();
                    Object obj = af.getValue();
                    if (obj != null) {
                        String value = obj.toString();
                        af.setValue(value);
                    }
                }
            }
//            //为accountSearchs赋值
//            List<AccountField> accountFields = new ArrayList<AccountField>();
//            for (AccountField field : accountField) {
//                for (AccountLine accountLine : accountLines) {
//                    Collection<AccountField> fields = accountLine.getAccountFields();
//                    for (AccountField field1 : fields) {
//                        if (field.getItemCode().equals(field1.getItemCode())){
//                            field.setValue(field1.getValue());
//                        }
//                    }
//                }
//                accountFields.add(field);
//            }
//            account.setAccountSearchs(accountFields);
            result.setData(accountProcessVo);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }
        return result;
    }
    @Override
	public AccountLine findAccountDatas(Long userId,Long accountId, Long lineId) {
    	AccountProcessVo accountProcessVo = new AccountProcessVo();
    	
        List<AccountLine> accountLines = new ArrayList<>();
        AccountLine accountLine = new AccountLine();
        accountLine.setId(lineId);
        accountLines.add(accountLine);
        
        Account account = accountRepository.findOne(accountId);
        account.setAccountLines(accountLines);
        
        accountProcessVo.setAccount(account);
        AccountLine result = this.findAccountDataById(accountProcessVo);
        
        Collection<FieldPermission> rrfps = fieldPermissionService.findByUserAndTemplate(userId, account.getAccountTemplate());
        
        Collection<AccountField> accountField = result.getAccountFields();
		Iterator<AccountField> itaf = accountField.iterator();
		while (itaf.hasNext()){
			AccountField af = itaf.next();
			if(af.getItemType().equals("CODELIB")){
            	af.setDictionaryItems(dictionaryItemRepository.findByDictionaryId(Long.valueOf(af.getDicId())));
            }
			Iterator<FieldPermission> itfp = rrfps.iterator();
			String allfp = "";
			while (itfp.hasNext()){
				FieldPermission fp = itfp.next();
				if(af.isPkable() == false && af.getId().equals(fp.getAccountField().getId())){
					allfp += fp.getOperationType().toString();
					allfp += ",";
				}
			}
			if(!allfp.equals("")){
				allfp = allfp.substring(0,allfp.lastIndexOf(","));
			}
            //权限转换，实际有的操作权限没有存放数据库，数据库存放的权限实际没有
            switch (allfp){
                case "LOOK":
                    allfp = "OPERATE";
                    break;
                case "OPERATE":
                    allfp = "LOOK";
                    break;
                case "":
                    allfp = "LOOK,OPERATE";
                    break;
                default:
                    allfp = "";
                    break;
            }
			af.setFieldPermission(allfp);
		}
		return result;
	}
    
    @Override
    public AccountProcessVo findAccountDatatwo(AccountProcessVo accountProcessVo, Page page) {
        try {
            Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
            Collection<AccountField> accountField = account.getAccountTemplate().getAccountFields();

            Account ac = accountProcessVo.getAccount();
            ac.setAccountTemplate(account.getAccountTemplate());
            // 补录台账字段信息
            Collection<AccountField> accountFields = account.getAccountTemplate().getAccountFields();
            Iterator<AccountField> itaf = accountFields.iterator();
            while (itaf.hasNext()) {
                AccountField af = itaf.next();
                if (af.getItemType().equals("CODELIB")) {
                    af.setDictionaryItems(dictionaryItemRepository.findByDictionaryId(Long.valueOf(af.getDicId())));
                }
            }
            
            //放置前端查询条件
            List<AccountField> fieldList = new ArrayList<>();
            for (AccountField field : accountField) {
                List<AccountField> accountSearchs = accountProcessVo.getAccount().getAccountSearchs();
                
                if (accountSearchs != null){
                    for (AccountField accountSearch : accountSearchs) {
                        if (field.getItemCode().equals(accountSearch.getItemCode())){
                            field.setValue(accountSearch.getValue());
                        }
                    }
                }
                fieldList.add(field);
            }
            account.setAccountSearchs(fieldList);
            accountProcessVo.setAccount(account);
            List<AccountLine> accountLines = accountDataDao.findDataByCondition(accountProcessVo,page);
            account.setAccountLines(accountLines);
//            //为accountSearchs赋值
//            List<AccountField> accountFields = new ArrayList<AccountField>();
//            for (AccountField field : accountField) {
//                for (AccountLine accountLine : accountLines) {
//                    Collection<AccountField> fields = accountLine.getAccountFields();
//                    for (AccountField field1 : fields) {
//                        if (field.getItemCode().equals(field1.getItemCode())){
//                            field.setValue(field1.getValue());
//                        }
//                    }
//                }
//                accountFields.add(field);
//            }
//            account.setAccountSearchs(accountFields);

            accountProcessVo.setAccount(account);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }

        return accountProcessVo;
    }
    
    @Override
    public List<String> addAccountData(AccountProcessVo accountProcessVo) {
        List<String> data = new ArrayList<>();
        try {
            Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
            List<AccountLine> accountLines = accountProcessVo.getAccount().getAccountLines();
            Collection<AccountField> accountFieldList = accountLines.get(0).getAccountFields();
          /*  Collection<AccountField> accountFieldList1= new ArrayList<>();
            accountFieldList1=accountLines.get(0).getAccountFields();*/
            AccountLine accountLine=accountLines.get(0);
            // 判断业务主键数据是否已经存在
            if (accountDataDao.queryDataisExist(accountLine, account)) {
                String result = "addAccountData pk is exist!";
                data.add(result);
                return data;
            } else {
                // 业务条线：表名
                String validateTableName = account.getAccountTemplate().getBusSystem().getReportSubSystem().getSubKey()
                        + ":" + account.getAccountTemplate().getTableName();
                Map<String, Object> map = new HashedMap(); // 待校验字段
                for (AccountField accountField : accountFieldList) {
                	if(accountField.isPkable()&&accountField.getValue()==null){
                		  String result = accountField.getItemName()+"是主键，不能为空";
                          data.add(result);
                          return data;            		
                	}              	
                    map.put(accountField.getItemCode(), accountField.getValue());
                }
                Collection<ObjectValidateRule> rules = objectValidateRuleService.findPageRule("SJBLSYS:".concat(validateTableName).toUpperCase());
                // 校验结果
                List<String> list = validateAnalyzeResultService.excuteFormuForOne(rules, map);
                for (String s : list) {
                    String[] str = s.split(":");
                    if ("true".equals(str[2])) {
                        data.add(s);// 字段存在校验问题
                    }
                }
                if (data.size() == 0) {
                    // 校验通过，保存明细
                    accountDataDao.insertData(accountLines.get(0), account);
                    account.setValidateStatus(ValidateStatusEnum.NOTVALIDATE);
                    accountRepository.save(account);
                    AccountEditLog accountEditLog = new AccountEditLog();
                    accountEditLog.setAccountEditType(AccountEditEnum.INSERT);
                    accountEditLog.setLogSource(LogSourceEnum.ONLINE);
                    accountEditLog.setEditLineNum(1);
                    accountEditLogService.saveAccoutnEditLog(account, accountProcessVo.getUserId(), accountEditLog);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }
        return data;
    }

    @Override
    public List<String> modifyAccountData(AccountProcessVo accountProcessVo) {
        List<String> data = new ArrayList<>();
        try {
            Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
            List<AccountLine> accountLines = accountProcessVo.getAccount().getAccountLines();
            Collection<AccountField> accountFieldList = accountLines.get(0).getAccountFields();
            for (AccountLine accountLine : accountLines) {
                Collection<AccountField> fields = accountLine.getAccountFields();
                for (AccountField field : fields) {
                    if ("1970-01-01".equals(field.getValue())){
                        field.setValue("");
                    }
                }
            }
            // 查询数据库中原来数据的值
            AccountLine albefore = accountDataDao.findDataById(account, accountLines.get(0).getId());
            // 业务条线：表名
            String validateTableName = account.getAccountTemplate().getBusSystem().getReportSubSystem().getSubKey()
                    + ":" + account.getAccountTemplate().getTableName();
            Map<String, Object> map = new HashedMap();// 待校验的字段
            for (AccountField accountField : accountFieldList) {
                map.put(accountField.getItemCode(), accountField.getValue());
            }
            Collection<ObjectValidateRule> rules = objectValidateRuleService.findByObjectID(validateTableName);
            // 校验结果
            List<String> list = validateAnalyzeResultService.excuteFormuForOne(rules, map);
            for (String s : list) {
                String[] str = s.split(":");
                if ("true".equals(str[2])) {
                    data.add(s);// 字段存在校验问题
                }
            }
            if (data.size() == 0) {
                // 将模板主键赋予数据并将主键剔除
                AccountField pk = null;
                Collection<AccountField> acfs = account.getAccountTemplate().getAccountFields();
                for (AccountField af : accountLines.get(0).getAccountFields()) {
                    for (AccountField as : acfs) {
                        if (af.getItemCode().equals(as.getItemCode()) && as.isPkable()) {
                            pk = af;
                        }
                    }
                }
                accountLines.get(0).getAccountFields().remove(pk);
                // 校验通过，更新明细
                accountDataDao.updateData(accountLines.get(0), account);
                account.setValidateStatus(ValidateStatusEnum.NOTVALIDATE);
                accountRepository.save(account);
                // 记录台账修改痕迹
                AccountEditLog accountEditLog = new AccountEditLog();
                accountEditLog.setAccountEditType(AccountEditEnum.UPDATE);
                accountEditLog.setLogSource(LogSourceEnum.ONLINE);
                accountEditLog.setEditLineNum(1);
                accountEditLog = accountEditLogService.saveAccoutnEditLog(account, accountProcessVo.getUserId(),
                        accountEditLog);
                // 保证修改的记录进去，没修改的不记录
                Collection<AccountField> afbefore = albefore.getAccountFields();
                Collection<AccountField> afafter = accountLines.get(0).getAccountFields();
                Collection<AccountField> afdelete = new ArrayList<AccountField>();
                for (AccountField af : afafter) {
                    for (AccountField ab : afbefore) {
                        if (ab.getItemCode().equals(af.getItemCode())) {
                            String before = String.valueOf(ab.getValue());
//                            String after = String.valueOf(af.getValue());
//                            if (af.getSqlType().equals(SqlTypeEnum.DATE)) {
                            String after = "null";
                            if (StringUtil.isNotEmpty(af.getValue()) && !String.valueOf(af.getValue()).contains("-") && af.getSqlType().equals(SqlTypeEnum.DATE)){
                                after = (new SimpleDateFormat("yyyy-MM-dd")).format(Long.parseLong(String.valueOf(af.getValue())));
                            }else if (StringUtil.isEmpty(String.valueOf(af.getValue()))){
                            }else {
                                after = String.valueOf(af.getValue());
                            }
                            if (before != "null" && af.getSqlType().equals(SqlTypeEnum.DATE)) {
                                before = before.substring(0, 10);
                            }
                            if (before.equals(after)) {
                                afdelete.add(af);
                            }
                        }
                    }
                }
                afafter.removeAll(afdelete);
                accountLines.get(0).setAccountFields(afafter);
                accountEditLogService.saveAccoutnEditLogItem(accountEditLog, accountLines.get(0).getAccountFields(),
                        account);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }
        return data;
    }
    
    @Override
    @Transactional
    public List<String> batchUpdateAccounData(AccountProcessVo accountProcessVo) {
        String taskid = accountProcessVo.getTaskId();
        List<String> data = new ArrayList<>();
        try {
            // 根据id找到数据库中account
            Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
            Collection<AccountField> accountFieldList = accountProcessVo.getAccount().getAccountLines().get(0)
                    .getAccountFields();
            // 业务条线：表名
            String validateTableName = account.getAccountTemplate().getBusSystem().getReportSubSystem().getSubKey()
                    + ":" + account.getAccountTemplate().getTableName();
            Map<String, Object> map = new HashedMap();
            for (AccountField accountField : accountFieldList) {
                map.put(accountField.getItemCode(), accountField.getValue());
            }
            Collection<ObjectValidateRule> rules = objectValidateRuleService.findByObjectID(validateTableName);
            List<String> list = validateAnalyzeResultService.excuteFormuForOne(rules, map);
            for (String s : list) {
                String[] str = s.split(":");
                if ("true".equals(str[2])) {
                    data.add(s);// 字段存在校验问题
                }
            }
            if (data.size() == 0) {
                List<Long> lineIds = new ArrayList<>();
                // 分割前台传来数据的id
                String[] taskids = taskid.split(",");
                for (int i = 0; i < taskids.length; i++) {
                    lineIds.add(Long.parseLong(taskids[i]));
                }

                if (taskid != null && !("".equals(taskid))) {
                    AccountLine accountLine = new AccountLine();
                    accountLine.setAccountFields(accountFieldList);
                    accountDataDao.batchUpdateData(accountLine, account, lineIds);
                    account.setValidateStatus(ValidateStatusEnum.NOTVALIDATE);
                    accountRepository.save(account);
                    for (Long id : lineIds) {
                        AccountEditLog accountEditLog = new AccountEditLog();
                        accountEditLog.setAccountEditType(AccountEditEnum.UPDATE);
                        accountEditLog.setLogSource(LogSourceEnum.ONLINE);
                        accountEditLog.setEditLineNum(1);
                        accountEditLog = accountEditLogService.saveAccoutnEditLog(account, accountProcessVo.getUserId(),
                                accountEditLog);
                        accountEditLogService.saveAccoutnEditLogItem(accountEditLog, accountLine.getAccountFields(),
                                account);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }

        return data;
    }
    
    @Override
    @Transactional
    public GenericResult<Boolean> deleteAccountData(AccountProcessVo accountProcessVo) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            if (accountProcessVo == null || accountProcessVo.getAccount() == null
                    || accountProcessVo.getAccount().getId() == null) {
                result.setSuccess(false);
                result.setMessage("queryAccountData param is null!");
                return result;
            }

            Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());

            if (account == null) {
                result.setSuccess(false);
                result.setMessage("queryAccountData account ID not exist!");
                return result;
            }
            List<AccountLine> accountLines = accountProcessVo.getAccount().getAccountLines();

            AccountLine accountLine = accountLines.get(0);

            if (accountLine.getId() == null) {
                result.setSuccess(false);
                result.setMessage("queryAccountData line ID is null!");
                return result;
            }
            accountDataDao.deleteData(accountLine, account);
            account.setValidateStatus(ValidateStatusEnum.NOTVALIDATE);
            accountRepository.save(account);
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }
        return result;
    }

    @Override
    public GenericResult<Boolean> deleteAllAccountData(Long reportId) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            if (reportId == null) {
                result.setSuccess(false);
                result.setMessage("queryAccountData param is null!");
                return result;
            }
            Account account = accountRepository.findById(reportId);
            accountDataDao.deleteAllData(account);
            account.setValidateStatus(ValidateStatusEnum.NOTVALIDATE);
            accountRepository.save(account);
        }catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }
        return result;
    }

    @Override
    public String downLoadBusPackage(String busSystemId) {
        String basePath = CommonConst.getProperties("basePath") + File.separator + "tempFile" + File.separator;
        File basepathFile = new File(basePath);
        //生成目录
        if (!basepathFile.exists()) {
            basepathFile.mkdirs();
        }
        //生成字典项表
        List<AccountDicVo> dicVoList = dictionaryDao.searchDictionary();
        this.createDicExcel(basePath,dicVoList);
        //生成模板表
        List<AccountTemplate> accountTemplates=accountTemplateRepository.findAll();
        this.createTemplateExcel(basePath,accountTemplates);
        //生成字段表
        this.createFieldExcel(basePath,accountTemplates);
        return null;
    }

    public void createTemplateExcel(String templateFile, List<AccountTemplate> accountTemplates){
        try {
            OutputStream os = new FileOutputStream(templateFile + File.separator + "Template.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("字典项");
            Row row = null;
            Cell cell = null;
            //设置表头
            row = sheet.createRow(0);
            cell = row.createCell(0);
            cell.setCellValue("模板编号");
            cell = row.createCell(1);
            cell.setCellValue("模板名称");
            cell = row.createCell(2);
            cell.setCellValue("开始日期");
            cell = row.createCell(3);
            cell.setCellValue("结束日期");
            cell = row.createCell(4);
            cell.setCellValue("模板频度");
            for(int index=1;index<=accountTemplates.size();index++){
                AccountTemplate accountTemplate=accountTemplates.get(index-1);
                row = sheet.createRow(index);
                for (int j = 0; j < 10; j++) {
                    cell = row.createCell(j);
                    switch (j) {
                        case 0:
                            if (null != accountTemplate.getTemplateCode()) {
                                cell.setCellValue(accountTemplate.getTemplateCode());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 1:
                            if (null != accountTemplate.getTemplateName()) {
                                cell.setCellValue(accountTemplate.getTemplateName());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 2:
                            if (null != accountTemplate.getStartDate()) {
                                cell.setCellValue(accountTemplate.getStartDate());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 3:
                            if (null != accountTemplate.getEndDate()) {
                                cell.setCellValue(accountTemplate.getEndDate());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 4:
                            if (null != accountTemplate.getRepFreq().getRepFreqName()) {
                                cell.setCellValue(accountTemplate.getRepFreq().getRepFreqName());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            workbook.write(os);
            os.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void createFieldExcel(String templateFile, List<AccountTemplate> accountTemplates){
        try {
            OutputStream os = new FileOutputStream(templateFile + File.separator + "Field.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("字段");
            Row row = null;
            Cell cell = null;
            //设置表头
            row = sheet.createRow(0);
            cell = row.createCell(0);
            cell.setCellValue("表名");
            cell = row.createCell(1);
            cell.setCellValue("字段码");
            cell = row.createCell(2);
            cell.setCellValue("字段名称");
            cell = row.createCell(3);
            cell.setCellValue("字段类型");
            cell = row.createCell(4);
            cell.setCellValue("字段长度");
            cell = row.createCell(5);
            cell.setCellValue("主键");
            cell = row.createCell(6);
            cell.setCellValue("字段描述");
            cell = row.createCell(7);
            cell.setCellValue("数据字典");
            cell = row.createCell(8);
            cell.setCellValue("显示序号");
            int indexROW=1;
            for(int index=1;index<=accountTemplates.size();index++){
                AccountTemplate accountTemplate=accountTemplates.get(index-1);
                Collection<AccountField> fields= accountTemplate.getAccountFields();

                List<AccountField> fieldList = new ArrayList<>();
                for (AccountField code : fields) {
                    fieldList.add(code);
                }

                for(int i=0;i<fieldList.size();i++){
                    AccountField field=fieldList.get(i);
                    row = sheet.createRow(indexROW);
                    for (int j = 0; j < 9; j++) {
                        cell = row.createCell(j);
                        switch (j) {
                            case 0:
                                if (null != accountTemplate.getTemplateName()) {
                                    cell.setCellValue(accountTemplate.getTemplateName());
                                } else {
                                    cell.setCellValue("");
                                }
                                break;
                            case 1:
                                if (null != field.getItemCode()) {
                                    cell.setCellValue(field.getItemCode());
                                } else {
                                    cell.setCellValue("");
                                }
                                break;
                            case 2:
                                if (null != field.getItemName()) {
                                    cell.setCellValue(field.getItemName());
                                } else {
                                    cell.setCellValue("");
                                }
                                break;
                            case 3:
                                if (null != field.getSqlType()) {
                                    cell.setCellValue(field.getItemType());
                                } else {
                                    cell.setCellValue("");
                                }
                                break;
                            case 4:
                                if (null != field.getLength()) {
                                    cell.setCellValue(field.getLength());
                                } else {
                                    cell.setCellValue("");
                                }
                                break;
                            case 5:
                                cell.setCellValue(field.isPkable());
                                break;
                            case 6:
                                //字段描述
                                cell.setCellValue(field.getItemDescription());
                                break;
                            case 7:
                                if (null != field.getItemType()){
                                    if("CODELIB".equals(field.getItemType())){
                                        Dictionary dictionary= dictionaryRepository.findDictionaryById(Long.parseLong(field.getDicId()));
                                        cell.setCellValue(dictionary.getDicName());
                                    }else {
                                        cell.setCellValue("");
                                    }
                                } else {
                                    cell.setCellValue("");
                                }
                                break;
                            case 8:
                                if (null != String.valueOf(field.getOrderNumber())) {
                                    cell.setCellValue(String.valueOf(field.getOrderNumber()));
                                } else {
                                    cell.setCellValue("");
                                }
                                break;
                            default:
                                break;
                        }
                     }
                indexROW++;
                }
            }
            workbook.write(os);
            os.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void createDicExcel(String templateFile, List<AccountDicVo> dicList) {
        try {
            OutputStream os = new FileOutputStream(templateFile + File.separator + "DicData.xlsx");
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("字典字典项");
            Row row = null;
            Cell cell = null;
            //设置表头
            row = sheet.createRow(0);
            cell = row.createCell(0);
            cell.setCellValue("字典id");
            cell = row.createCell(1);
            cell.setCellValue("字典名称");
            cell = row.createCell(2);
            cell.setCellValue("字典父级字典");
            cell = row.createCell(3);
            cell.setCellValue("字典状态");
            cell = row.createCell(4);
            cell.setCellValue("字典描述");
            cell = row.createCell(5);
            cell.setCellValue("字典项id");
            cell = row.createCell(6);
            cell.setCellValue("字典项编号");
            cell = row.createCell(7);
            cell.setCellValue("字典项名称");
            cell = row.createCell(8);
            cell.setCellValue("字典项的父级字典项");
            cell = row.createCell(9);
            cell.setCellValue("字典项描述");

            //添加数据
            for (int index = 1; index <= dicList.size(); index++) {
                AccountDicVo dicVo = dicList.get(index - 1);
                row = sheet.createRow(index);
                for (int j = 0; j < 10; j++) {
                    cell = row.createCell(j);
                    switch (j) {
                        case 0:
                            if (null != dicVo.getDicId()) {
                                cell.setCellValue(dicVo.getDicId());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 1:
                            if (null != dicVo.getDicName()) {
                                cell.setCellValue(dicVo.getDicName());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 2:
                            if (null != dicVo.getDicParentId()) {
                                cell.setCellValue( dicVo.getDicParentId());

                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 3:
                            if (null != dicVo.getDicIsEnabel()) {
                                cell.setCellValue(dicVo.getDicIsEnabel());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 4:
                            if (null != dicVo.getDicDescription()) {
                                cell.setCellValue(dicVo.getDicDescription());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 5:
                            if (null != dicVo.getItemId()) {
                                cell.setCellValue(dicVo.getItemId());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 6:
                            if (null != dicVo.getDicItemId()) {
                                cell.setCellValue(dicVo.getDicItemId());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 7:
                            if (null != dicVo.getDicItemName()) {
                                cell.setCellValue(dicVo.getDicItemName());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 8:
                            if (null != dicVo.getDicItemParentId()) {
                                cell.setCellValue(dicVo.getDicItemParentId());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        case 9:
                            if (null != dicVo.getDicItemDescription()) {
                                cell.setCellValue(dicVo.getDicItemDescription());
                            } else {
                                cell.setCellValue("");
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            workbook.write(os);
            os.close();
            } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public String generateAccountTemplate(Long accountId, Long userId) {
        if (null != accountId) {
            // 根据userid获取该用户的角色集合
            Collection<Role> c = userRepository.findById(userId).getRoles();
            // 该报文已经配置的字段权限
            Collection<FieldPermission> rrfps = new ArrayList<FieldPermission>();
            // 迭代该用户的角色集合
            Iterator<Role> it = c.iterator();
            while (it.hasNext()) {
                Role role = it.next();
                Collection<FieldPermission> rfps = role.getFieldPermission();
                // 迭代字段权限集合
                Iterator<FieldPermission> its = rfps.iterator();
                while (its.hasNext()) {
                    FieldPermission fp = its.next();
                    // 如果字段权限的模板id是该模板id则将该模板权限添加到已配置的字段权限集合中
                    if (role.getSubSystem().getSubKey().equals("sjbl")) {
                        rrfps.add(fp);
                    }
                }
            }

            Account account = accountRepository.findOne(accountId);

            Collection<AccountField> accountField = account.getAccountTemplate().getAccountFields();
            Iterator<AccountField> itaf = accountField.iterator();
            while (itaf.hasNext()) {
                AccountField af = itaf.next();
                if (af.getItemType().equals("CODELIB")) {
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

            AccountTemplate accountTemplate = account.getAccountTemplate();

            String sheetName = accountTemplate.getTableName();

            List<List<String>> hList = new ArrayList<>();

            List<String> itemDesc = new ArrayList<>();
            List<String> itemCode = new ArrayList<>();

            List<AccountField> a = (List<AccountField>) accountTemplate.getAccountFields();
            Collections.sort(a, new Comparator<AccountField>() {

                @Override
                public int compare(AccountField o1, AccountField o2) {
                    if (o1.getOrderNumber() < o2.getOrderNumber())
                        return -1;
                    else if (o1.getOrderNumber() > o2.getOrderNumber())
                        return 1;
                    else
                        return o1.getId().compareTo(o2.getId());
                }
            });
            // 模板下载设置下拉框
            List<Integer> downRows = new ArrayList<Integer>();
            List<List<String>> downData = new ArrayList<List<String>>();
            int i = 0;
            for (AccountField accoutnfield : a) {
                List<DictionaryItem> dicList = accoutnfield.getDictionaryItems();
                List<String> tempList = new ArrayList<String>();
                if (dicList != null && dicList.size() > 0) {
                    downRows.add(i);           
                    for (DictionaryItem dic : dicList) {
                        tempList.add(dic.getDicItemName());
                    }
                    downData.add(tempList);
                }
                if (accoutnfield.isPkable()) {
                    itemDesc.add(accoutnfield.getItemName());
                    itemCode.add(accoutnfield.getItemCode());
                    i++;
                    //fieldPermission中没有OPERATE，表示有操作权限
                } else if (accoutnfield.getFieldPermission().indexOf("OPERATE") == -1) {
                    itemDesc.add(accoutnfield.getItemName());
                    itemCode.add(accoutnfield.getItemCode());
                    i++;
                }

            }

            hList.add(itemCode);
            hList.add(itemDesc);

            ExcelUtils.createExcel2007(hList, sheetName, CommonConst.getProperties("basePath")+File.separator+"temp"+File.separator+"AccountTemplate"+File.separator, downRows, downData);
            return "temp|AccountTemplate|"+sheetName;
        }
        return null;
    }
    
    @Override
    public GenericResult<Boolean> loadDataByTemplate(InputStream inputStream, String fileName, Long accountId,
                                                     Long userId, String operateFieldStr) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            if (inputStream == null) {
                result.setSuccess(false);
                result.setMessage("inputStream file is null!");
                return result;
            }
            if (StringUtil.isEmpty(fileName)) {
                result.setSuccess(false);
                result.setMessage("fileName is null!");
                return result;
            }
            if (accountId == null) {
                result.setSuccess(false);
                result.setMessage("accountId is null!");
                return result;
            }
            boolean isExcel = false;
            if (ExcelUtil.isExcel2003(fileName) || ExcelUtil.isExcel2007(fileName)) {
                isExcel = true;
            }
            if (isExcel) {
                Account account = accountRepository.findOne(accountId);
                if (account != null) {
                    AccountTemplate accountTemplate = account.getAccountTemplate();

                    Sheet sheet = ExcelUtil.getExcelSheet(inputStream, fileName);

                    Map<String, Object> map = accountDatasDao.loadDataByExcel(accountId, accountTemplate, sheet, account);
                    //返回false,载入失败，返回重复行号
                    if (!(Boolean) map.get("flag")) {
                        result.setSuccess(false);
                        result.setMessage((String) map.get("message"));
                        result.setErrorCode(ExceptionCode.ONLY_VALIDATION_FALSE);
                        return result;
                    }
                    //否则返回成功，size变量存储成功条数
                    Integer size = (Integer) map.get("size");
                    // 业务条线：表名
                    String[] operateFieldArr = operateFieldStr.split("\\|"); // 待校验字段
                    String validateTableName = account.getAccountTemplate().getBusSystem().getReportSubSystem()
                            .getSubKey() + ":" + account.getAccountTemplate().getTableName();
                    Collection<ValidateAnalyzeResult> rules = validateAnalyzeResultService
                            .findByObjectID(validateTableName, operateFieldArr);
                    if (rules != null && rules.size() > 0) {
                        ValidateBatch validateBatch = validateAnalyzeResultService.excuteFormu(rules, accountId + "");
                         //校验结果
                        Collection<ValidateResult> list = validateResultService
                                .findByValidatebatch(validateBatch.getBatchId());

                    }
                    account.setValidateStatus(ValidateStatusEnum.NOTVALIDATE);
                    accountRepository.save(account);
                    AccountEditLog accountEditLog = new AccountEditLog();
                    accountEditLog.setAccountEditType(AccountEditEnum.INSERT);
                    accountEditLog.setLogSource(LogSourceEnum.OFFLINE);
                    accountEditLog.setEditLineNum(size);
                    accountEditLogService.saveAccoutnEditLog(account, userId, accountEditLog);
                } else {
                    result.setSuccess(false);
                    result.setMessage("error:input accountId not exists!");
                    return result;
                }
            } else {
                result.setSuccess(false);
                result.setMessage("error:load data file is not excel!");
                return result;
            }
        } catch (AppException e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMessage("载入失败，数据类型错误或者模板不正确！");
            return result;
        }
        return result;
    }

    @Override
    public String downLoadAccounData(AccountProcessVo accountProcessVo) {
        try {
            if (accountProcessVo == null || accountProcessVo.getAccount() == null) {
                return "falseone";
            }
            if (accountProcessVo.getAccount().getId() == null) {
                return "falsetwo";
            }
            // 补录台账信息
            Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
            // 该报文已经配置的字段权限
            Collection<FieldPermission> rrfps = fieldPermissionService.findByUserAndTemplate(accountProcessVo.getUserId(), account.getAccountTemplate());
            
            Collection<AccountField> accountField = account.getAccountTemplate().getAccountFields();

            //放置accountSearchs
            List<AccountField> fieldList = new ArrayList<>();
            for (AccountField field : accountField) {
                List<AccountField> accountSearchs = accountProcessVo.getAccount().getAccountSearchs();
                if (accountSearchs != null){
                    for (AccountField accountSearch : accountSearchs) {
                        if (field.getItemCode().equals(accountSearch.getItemCode())){
                            field.setValue(accountSearch.getValue());
                        }
                    }
                }
                fieldList.add(field);
            }
            account.setAccountSearchs(fieldList);
            accountProcessVo.setAccount(account);

//            Collection<AccountField> accountField = new ArrayList<>();
//            for(AccountField af:accountFieldTemp){
//                if(af.isSearchable()){
//                    accountField.add(af);
//                }
//            }
            //将collection中的数据放置到新建的list中
            List<AccountField> list = new ArrayList<>();
            for (AccountField field : accountField) {
                list.add(field);
            }
            //根据accountFiled的orderNumber字段进行冒泡排序
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < list.size() - 1 - i; j++) {
                    int orderNumber1 = list.get(j).getOrderNumber();
                    int orderNumber2 = list.get(j + 1).getOrderNumber();
                    if (orderNumber1 > orderNumber2) {
                        //互换位置
                        AccountField accountF = list.get(j);
                        list.set(j, list.get(j + 1));
                        list.set(j + 1, accountF);
                    }
                }
            }
            accountField.clear();
            for (AccountField field : list) {
                accountField.add(field);
            }
            Iterator<AccountField> itaf = accountField.iterator();
            while (itaf.hasNext()) {
                AccountField af = itaf.next();
                if (af.getItemType().equals("CODELIB")) {
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

            Account ac = accountProcessVo.getAccount();

            ac.setAccountTemplate(account.getAccountTemplate());
            //accountLines是查询出来的数据
            List<AccountLine> accountLines = accountDataDao.downLoadDataByCondition(accountProcessVo);
            for (AccountLine accountLine : accountLines) {
                Collection<AccountField> accountFields = accountLine.getAccountFields();
                for (AccountField field : accountFields) {
                    if (field.getSqlType() != null && field.getSqlType().equals(SqlTypeEnum.DATE)){
                        if(StringUtil.isNotEmpty(field.getValue())){
                            field.setValue(new SimpleDateFormat("yyyy-MM-dd").format(field.getValue()));
                        }
                    }else  if (field.getItemType()!=null&&field.getItemType().equals("CODELIB")){
                        if(StringUtil.isNotEmpty(field.getValue())){
                            field.setValue(getDictionaryNameByKey(field, accountField));
                        }
                    }
                }
            }

            //accountFields是用户可以查看的字段
            List<AccountField> accountFields = new ArrayList<>();
            for (AccountField accountFieldnew : accountField) {
                //fieldPermission沒有OPERATE，则有操作权限
                if (accountFieldnew.getFieldPermission().indexOf("OPERATE") == -1) {
                    accountFields.add(accountFieldnew);
                }
            }
            String sheetName = "BuLuData-";
//        	UUID uuid = UUID.randomUUID();
            long time = new Date().getTime();
            sheetName += String.valueOf(time);
            List<List<String>> hList = new ArrayList<>();
            List<String> lineFirst = new ArrayList<>();
            List<String> lineSecond = new ArrayList<>();
            List<Integer> downRows=new ArrayList<>();
            List<List<String>> downData=new ArrayList<>();
            int i=0;                                                    
            for (AccountField accountFieldnew : accountFields) {            
                List<DictionaryItem> dicList = accountFieldnew.getDictionaryItems();
                List<String> tempList = new ArrayList<String>();
                if (dicList != null && dicList.size() > 0) {
                       downRows.add(i);
                    for (DictionaryItem dic : dicList) {
                        tempList.add(dic.getDicItemName());
                    }
                    downData.add(tempList);
                }
                lineFirst.add(accountFieldnew.getItemCode());
                lineSecond.add(accountFieldnew.getItemName());
                i++;
            }
            hList.add(lineFirst);
            hList.add(lineSecond);
            //循环查询出得数据accountLines
            for (AccountLine accline : accountLines) {
                List<String> lineone = new ArrayList<>();
                //循环排序好的字段，控制输出顺序
                for (AccountField accountFieldnew : accountFields) {
                    //循环数据行仲的字段取值
                    for (AccountField accf : accline.getAccountFields()) {
                        if (accountFieldnew.getItemCode().equals(accf.getItemCode())) {
                            if (accf.getValue() == null) {
                                lineone.add("");
                            } else {                       
                                lineone.add(String.valueOf(accf.getValue()));                   
                            }                
                        }
                    }
                }
                hList.add(lineone);
            }
           // String result = ExcelUtil.createExcel2007(hList, sheetName, CommonConst.getProperties("basePath")+"temp/AccountList/", sheetName);
            
           String result = ExcelUtils.createExcel2007(hList, sheetName, CommonConst.getProperties("basePath")+"temp"+File.separator+"AccountList"+File.separator, sheetName,downRows,downData);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }
    }

    public String getDictionaryNameByKey(AccountField accountField,Collection<AccountField> accountFields){
    	String dictionaryName="";
    	
    	if(accountField==null||accountFields==null){
    		 return dictionaryName;
    	}
    	    for(AccountField field:accountFields){
    	    	if(field.getItemCode().equals(accountField.getItemCode())){
    	    		List<DictionaryItem> dictionaryItems=field.getDictionaryItems();
    	    		for(DictionaryItem dictionaryItem:dictionaryItems){
    	    			  if(dictionaryItem.getDicItemId().equals(accountField.getValue())){
    	    				  dictionaryName=dictionaryItem.getDicItemName();
    	    				  break;
    	    			  }
    	    		}
    	    	}
    	    }
    	    return dictionaryName;
    }

//    @Override
//    public GenericResult<AccountProcessVo> initAccountTable(AccountProcessVo accountProcessVo) {
//        GenericResult<AccountProcessVo> result = new GenericResult<>();
//        try {
//            if (accountProcessVo == null || accountProcessVo.getAccount() == null) {
//                result.setSuccess(false);
//                result.setMessage("account param is null !");
//                return result;
//            }
//            if (accountProcessVo.getAccount().getId() == null) {
//                result.setSuccess(false);
//                result.setMessage("account id is null !");
//                return result;
//            }
//            Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
//
//            accountProcessVo.setAccount(account);
//
//            result.setData(accountProcessVo);
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
//        }
//        return result;
//    }

   
	
    @Override
    public GenericResult<Object> validateAll(AccountProcessVo accountProcessVo) {
        GenericResult<Object> result = new GenericResult<>();
        Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
        // 业务条线：表名
        String validateTableName = account.getAccountTemplate().getBusSystem().getReportSubSystem().getSubKey() + ":"
                + account.getAccountTemplate().getTableName();
        if (objectValidateRuleService == null) {
            result.setSuccess(false);
        } else {
            result.setSuccess(true);
            result.setMessage("校验通过");
            account.setValidateStatus(ValidateStatusEnum.SUCCESS);
            Collection<ValidateAnalyzeResult> rules = validateAnalyzeResultService.findByObjectID(validateTableName,
                    accountProcessVo.getOperateFieldArr());
            if (!rules.isEmpty()) {
                ValidateBatch validateBatch = validateAnalyzeResultService.excuteFormu(rules,
                        accountProcessVo.getAccount().getId() + "");
                // 校验结果
                Collection<ValidateResult> list = validateResultService.findByValidatebatch(validateBatch.getBatchId());
                if (list.size() > 0) {
                    result.setMessage("校验不通过");
                    account.setValidateStatus(ValidateStatusEnum.FAIL);
                }
            }
            accountRepository.save(account);
        }
        return result;
    }

    @Override
    public GenericResult<Object> validatePL(List<Long> idList, Long userId) {
        GenericResult<Object> result = new GenericResult<>();
        result.setSuccess(true);
        result.setMessage("校验通过");

        Account account = new Account();
        AccountProcessVo accountProcessVo = new AccountProcessVo();
        for (Long id : idList) {
            // 根据taskid查找accountid
            long accountId = accountsDao.getAccountIdByTaskId(id);
            account = accountRepository.findById(accountId);
            // 模拟AccountProcessVo实体，根据accountid查找用户有操作权限的字段
            accountProcessVo.setAccount(account);
//            accountProcessVo.setPageNum(1);
//            accountProcessVo.setPageSize(10);
            Page page = new Page();
            page.setCurrentPage(1);
            page.setPageSize(10);
            accountProcessVo.setUserId(userId);
            GenericResult<AccountProcessVo> accountProcessVoGenericResult = findAccounDatas(accountProcessVo,page);
            Collection<AccountField> accountFieldCollection = accountProcessVoGenericResult.getData().getAccount()
                    .getAccountTemplate().getAccountFields();
            String[] operateFieldStrAll = new String[accountFieldCollection.size()]; // 以accountFieldCollection的长度建一个string
            // []
            int index = 0;
            for (AccountField accountField : accountFieldCollection) {
                // 如果是主键，或者是操作权限的字段
                if ((accountField.isPkable()) || (accountField.getFieldPermission().indexOf("OPERATE") != -1)) {
                    operateFieldStrAll[index++] = accountField.getItemCode();
                }
            }
            String[] operateFieldStr = new String[index]; // 字段是主键，或者是有操作权限的字段
            operateFieldStr = Arrays.copyOf(operateFieldStrAll, index);

            // 业务条线：表名
            String validateTableName = account.getAccountTemplate().getBusSystem().getReportSubSystem().getSubKey()
                    + ":" + account.getAccountTemplate().getTableName();
            if (objectValidateRuleService == null) {
                result.setSuccess(false);
            } else {
                Collection<ValidateAnalyzeResult> rules = validateAnalyzeResultService.findByObjectID(validateTableName,
                        operateFieldStr); // 校验公式
                if (!rules.isEmpty()) {
                    ValidateBatch validateBatch = validateAnalyzeResultService.excuteFormu(rules, accountId + "");
                    Collection<ValidateResult> list = validateResultService
                            .findByValidatebatch(validateBatch.getBatchId()); // 校验结果
                    if (list.size() > 0) {
                        result.setSuccess(false);
                        result.setMessage("校验不通过");
                        account.setValidateStatus(ValidateStatusEnum.FAIL);
                        accountRepository.save(account);
                    } else {
                        account.setValidateStatus(ValidateStatusEnum.SUCCESS);
                        accountRepository.save(account);
                    }
                } else {
                    account.setValidateStatus(ValidateStatusEnum.SUCCESS);
                    accountRepository.save(account);
                }
            }
        }
        return result;
    }
    
    @Override
    public AccountLine findAccountDataById(AccountProcessVo accountProcessVo) {
        try {
        	// 报文信息
            Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
            // 报文行数据
            List<AccountLine> accountLines = accountProcessVo.getAccount().getAccountLines();
            AccountLine accountLine = accountLines.get(0);

            return accountDataDao.findDataById(account, accountLine.getId());
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
        }
    }
}
