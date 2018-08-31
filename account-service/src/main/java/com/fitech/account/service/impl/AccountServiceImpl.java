package com.fitech.account.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.dao.AccountBaseDao;
import com.fitech.account.dao.AccountDataDao;
import com.fitech.account.dao.AccountProcessDao;
import com.fitech.account.repository.AccountEditLogItemRepository;
import com.fitech.account.repository.AccountEditLogRepository;
import com.fitech.account.repository.AccountProcessRepository;
import com.fitech.account.repository.AccountRepository;
import com.fitech.account.repository.AccountTemplateRepository;
import com.fitech.account.repository.DictionaryItemRepository;
import com.fitech.account.service.AccountEditLogService;
import com.fitech.account.service.AccountFieldService;
import com.fitech.account.service.AccountService;
import com.fitech.account.util.ExcelUtils;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountEditLog;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountLine;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.account.DictionaryItem;
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
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.ExcelUtil;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.system.repository.RoleRepository;
import com.fitech.system.repository.UserRepository;
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
public class AccountServiceImpl extends NamedParameterJdbcDaoSupport implements AccountService {

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private AccountProcessDao accountProcessDao;

	@Autowired
	private AccountProcessRepository accountProcessRepository;

	@Autowired
	private AccountBaseDao accountBaseDao;

	@Autowired
	private AccountDataDao accountDataDao;

	@Autowired
	private AccountTemplateRepository accountTemplateRepository;

	@Autowired
	private AccountEditLogRepository accountEditLogRepository;

	@Autowired
	private AccountEditLogItemRepository accountEditLogItemRepository;

	@Autowired
	private AccountEditLogService accountEditLogService;

	@Autowired
	private RoleRepository<Role> roleRepository;

	@Autowired
	private UserRepository<User> userRepository;

	@Autowired
	private AccountFieldService accountFieldService;

	@Autowired
	private ObjectValidateRuleService objectValidateRuleService;

	@Autowired
	private ValidateAnalyzeResultService validateAnalyzeResultService;

	@Autowired
	private ValidateResultService validateResultService;

	@Autowired
	private DictionaryItemRepository dictionaryItemRepository;

	@Override
	public GenericResult<AccountProcessVo> findPageAccounData(AccountProcessVo accountProcessVo) {
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
			Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());

			// 获取模板id
			Account accountt = accountRepository.findById(accountProcessVo.getAccount().getId());
			// 根据userid获取该用户的角色集合
			Collection<Role> c = userRepository.findById(accountProcessVo.getUserId()).getRoles();
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
						if (fp.getReportTemplate().getId().equals(accountt.getAccountTemplate().getId())) {
							rrfps.add(fp);
						}
					}
				}
			}
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
					if (af.isPkable() == false && af.getId().equals(fp.getAccountField().getId()) ) {
						allfp += fp.getOperationType().toString();
						allfp += ",";
					}

				}
				//allfp不为空，去掉末尾的逗号
				if (!allfp.equals("")) {
					allfp = allfp.substring(0, allfp.lastIndexOf(","));
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

			Account ac = accountProcessVo.getAccount();

			ac.setAccountTemplate(account.getAccountTemplate());

			Page<AccountLine> accountLines = accountDataDao.findDataByCondition(accountProcessVo);

			account.setAccountLine(accountLines);

			accountProcessVo.setAccount(account);

			result.setData(accountProcessVo);

			Collection<AccountLine> accountLineCol = accountProcessVo.getAccount().getAccountLine().getContent();
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
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}

		return result;
	}
	
	//下载数据
	@Override
	public String downLoadPageAccounData(AccountProcessVo accountProcessVo) {
		try {
			if (accountProcessVo == null || accountProcessVo.getAccount() == null) {
				return "falseone";
			}
			if (accountProcessVo.getAccount().getId() == null) {
				return "falsetwo";
			}
			Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());

			// 获取模板id
			Account accountt = accountRepository.findById(accountProcessVo.getAccount().getId());
			// 根据userid获取该用户的角色集合
			Collection<Role> c = userRepository.findById(accountProcessVo.getUserId()).getRoles();
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
						if (fp.getReportTemplate().getId().equals(accountt.getAccountTemplate().getId())) {
							rrfps.add(fp);
						}
					}
				}
			}
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
					if (af.isPkable() == false && af.getId().equals(fp.getAccountField().getId()) ) {
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
			//accountFields是用户可以查看的字段
			List<AccountField> accountFields = new ArrayList<>();
            for(AccountField accountFieldnew : account.getAccountTemplate().getAccountFields()){
            	//fieldPermission沒有OPERATE，则有操作权限
                if(accountFieldnew.getFieldPermission().indexOf("OPERATE") == -1){
                    accountFields.add(accountFieldnew);
                }
            }
            String sheetName = "BuLuData-";
//        	UUID uuid = UUID.randomUUID();
            long time = new Date().getTime();
            sheetName += String.valueOf(time);
            List<List<String>> hList = new ArrayList<>();
            List<String> lineFirst = new ArrayList<>();
			for(AccountField accountFieldnew :accountFields){
				lineFirst.add(accountFieldnew.getItemName());
			}
			hList.add(lineFirst);
			for(AccountLine accline:accountLines){
				List<String> lineone = new ArrayList<>();
				for(AccountField accf:accline.getAccountFields()){
					for(AccountField accountFieldnew :accountFields){
						if(accountFieldnew.getItemCode().equals(accf.getItemCode())){
							lineone.add(accf.getValue().toString());
						}
					}
				}
				hList.add(lineone);
			}
			return ExcelUtil.createExcel(hList, sheetName, CommonConst.getProperties("template_path"),sheetName);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
	}

	// 数据查询中查询数据，无权限(hx)
	@Override
	public GenericResult<AccountProcessVo> findPageAccounDatatwo(AccountProcessVo accountProcessVo) {
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
			Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());

			Account ac = accountProcessVo.getAccount();

			ac.setAccountTemplate(account.getAccountTemplate());

			Page<AccountLine> accountLines = accountDataDao.findDataByCondition(accountProcessVo);

			account.setAccountLine(accountLines);

			accountProcessVo.setAccount(account);

			result.setData(accountProcessVo);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}

		return result;
	}

	@Override
	public GenericResult<AccountProcessVo> initAccountTable(AccountProcessVo accountProcessVo) {
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
			Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());

			accountProcessVo.setAccount(account);

			result.setData(accountProcessVo);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
		return result;
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
				//Role r = roleRepository.findById(role.getId());
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
					if (af.isPkable() == false &&  af.getId().equals(fp.getAccountField().getId())  ) {
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
				if (accoutnfield.isPkable()) {
					itemDesc.add(accoutnfield.getItemName());
					itemCode.add(accoutnfield.getItemCode());
					//fieldPermission中没有OPERATE，表示有操作权限
				} else if ( accoutnfield.getFieldPermission().indexOf("OPERATE") == -1 ) {
					itemDesc.add(accoutnfield.getItemName());
					itemCode.add(accoutnfield.getItemCode());
				}
				List<DictionaryItem> dicList = accoutnfield.getDictionaryItems();
				List<String> tempList = new ArrayList<String>();
				if (dicList != null && dicList.size() > 0) {
					downRows.add(i);
					for (DictionaryItem dic : dicList) {
						String str = dic.getDicItemId() + "-" + dic.getDicItemName();
						tempList.add(str);
					}
					downData.add(tempList);
				}
				i++;
			}

			hList.add(itemCode);
			hList.add(itemDesc);

			ExcelUtils.createExcel(hList, sheetName, CommonConst.getProperties("template_path"), downRows, downData);
			return sheetName;
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
					Integer size = accountDataDao.loadDataByTemplate(accountId, accountTemplate, sheet, account);

					if (size == -1) {
						result.setMessage("addAccountData pk is exist!");
						result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
						return result;
					}

					// 业务条线：表名
					String[] operateFieldArr = operateFieldStr.split("\\|"); // 待校验字段
					String validateTableName = account.getAccountTemplate().getBusSystem().getReportSubSystem()
							.getSubKey() + ":" + account.getAccountTemplate().getTableName();
					Collection<ValidateAnalyzeResult> rules = validateAnalyzeResultService
							.findByObjectID(validateTableName, operateFieldArr);
					if (rules != null && rules.size() > 0) {
						ValidateBatch validateBatch = validateAnalyzeResultService.excuteFormu(rules, accountId + "");
						// 校验结果
						Collection<ValidateResult> list = validateResultService
								.findByValidatebatch(validateBatch.getBatchId());

					}
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

	// @Override
	// public GenericResult<Boolean> addAccountData(AccountProcessVo
	// accountProcessVo) {
	// GenericResult<Boolean> result = new GenericResult<>();
	// try {
	// if(accountProcessVo == null || accountProcessVo.getAccount() == null ||
	// accountProcessVo.getAccount().getId() == null){
	// result.setSuccess(false);
	// result.setMessage("addAccountData param is null!");
	// return result;
	// }
	// if(accountProcessVo.getAccount().getAccountLines() == null ||
	// accountProcessVo.getAccount().getAccountLines().isEmpty()){
	// result.setMessage("addAccountData is empty!");
	// return result;
	// }
	// Account account =
	// accountRepository.findOne(accountProcessVo.getAccount().getId());
	//
	// if(account == null){
	// result.setSuccess(false);
	// result.setMessage("addAccountData account ID not exist!");
	// return result;
	// }
	// List<AccountLine> accountLines =
	// accountProcessVo.getAccount().getAccountLines();
	// //判断业务主键数据是否已经存在
	// if(accountDataDao.queryDataisExist(accountLines.get(0),account)){
	// result.setMessage("addAccountData pk is exist!");
	// result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
	// return result;
	// }else{
	// accountDataDao.insertData(accountLines.get(0),account);
	//
	// AccountEditLog accountEditLog = new AccountEditLog();
	// accountEditLog.setAccountEditType(AccountEditType.INSERT);
	// accountEditLog.setLogSource(LogSource.ONLINE);
	// accountEditLog.setEditLineNum(1);
	// accountEditLogService.saveAccoutnEditLog(account,accountProcessVo.getUserId(),accountEditLog);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
	// }
	// return result;
	// }

	@Override
	public List<String> addAccountData(AccountProcessVo accountProcessVo) {
		List<String> data = new ArrayList<>();
		try {
			Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
			List<AccountLine> accountLines = accountProcessVo.getAccount().getAccountLines();
			Collection<AccountField> accountFieldList = accountLines.get(0).getAccountFields();

			// 判断业务主键数据是否已经存在
			if (accountDataDao.queryDataisExist(accountLines.get(0), account)) {
				String result = "addAccountData pk is exist!";
				data.add(result);
				return data;
			} else {
				// 业务条线：表名
				String validateTableName = account.getAccountTemplate().getBusSystem().getReportSubSystem().getSubKey()
						+ ":" + account.getAccountTemplate().getTableName();
				Map<String, Object> map = new HashedMap(); // 待校验字段
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
					// 校验通过，保存明细
					accountDataDao.insertData(accountLines.get(0), account);

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

	// @Override
	// public GenericResult<Boolean> modifyAccountData(AccountProcessVo
	// accountProcessVo) {
	// GenericResult<Boolean> result = new GenericResult<>();
	// try {
	// if (accountProcessVo == null || accountProcessVo.getAccount() == null ||
	// accountProcessVo.getAccount().getId() == null) {
	// result.setSuccess(false);
	// result.setMessage("addAccountData param is null!");
	// return result;
	// }
	// if (accountProcessVo.getAccount().getAccountLines() == null ||
	// accountProcessVo.getAccount().getAccountLines().isEmpty()) {
	// result.setSuccess(false);
	// result.setMessage("addAccountData is empty!");
	// return result;
	// }
	// Account account =
	// accountRepository.findOne(accountProcessVo.getAccount().getId());
	//
	// if (account == null) {
	// result.setSuccess(false);
	// result.setMessage("addAccountData account ID not exist!");
	// return result;
	// }
	// List<AccountLine> accountLines =
	// accountProcessVo.getAccount().getAccountLines();
	//
	// AccountLine accountLine = accountLines.get(0);
	//
	// if (accountLine.getId() == null) {
	// result.setSuccess(false);
	// result.setMessage("addAccountData row ID is null!");
	// return result;
	// }
	// accountDataDao.updateData(accountLines.get(0), account);
	//
	// //记录台账修改痕迹
	// AccountEditLog accountEditLog = new AccountEditLog();
	// accountEditLog.setAccountEditType(AccountEditType.UPDATE);
	// accountEditLog.setLogSource(LogSource.ONLINE);
	// accountEditLog.setEditLineNum(1);
	// accountEditLog = accountEditLogService.saveAccoutnEditLog(account,
	// accountProcessVo.getUserId(), accountEditLog);
	// accountEditLogService.saveAccoutnEditLogItem(accountEditLog,
	// accountLines.get(0).getAccountFields(), account);
	// } catch (Exception e) {
	// e.printStackTrace();
	// throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
	// }
	// return result;
	// }

	@Override
	public List<String> modifyAccountData(AccountProcessVo accountProcessVo) {
		List<String> data = new ArrayList<>();
		try {
			Account account = accountRepository.findOne(accountProcessVo.getAccount().getId());
			List<AccountLine> accountLines = accountProcessVo.getAccount().getAccountLines();
			Collection<AccountField> accountFieldList = accountLines.get(0).getAccountFields();
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
							String after = String.valueOf(af.getValue());
							if (af.getSqlType().equals(SqlTypeEnum.DATE)) {
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
			Collection<ValidateAnalyzeResult> rules = validateAnalyzeResultService.findByObjectID(validateTableName,
					accountProcessVo.getOperateFieldArr());
			if (!rules.isEmpty()) {
				ValidateBatch validateBatch = validateAnalyzeResultService.excuteFormu(rules,
						accountProcessVo.getAccount().getId() + "");
				// 校验结果
				Collection<ValidateResult> list = validateResultService.findByValidatebatch(validateBatch.getBatchId());
				if (list.size() > 0) {
					result.setMessage("校验不通过");
				}
			}
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
			String sql = "SELECT ACC.ID FROM ACCOUNT ACC  inner JOIN AccountProcess ACCPRO ON ACC.id=ACCPRO.ACCOUNT_ID inner JOIN ACT_RU_TASK task on task.proc_inst_id_=ACCPRO.PROCINSETID where  TASK.ID_="
					+ id;
			long accountId = 0;
			List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql,
					new HashMap<String, Object>());
			accountId = Long.parseLong(resultList.get(0).get("ID").toString());
			account = accountRepository.findById(accountId);
			// 模拟AccountProcessVo实体，根据accountid查找用户有操作权限的字段
			accountProcessVo.setAccount(account);
			accountProcessVo.setPageNum(1);
			accountProcessVo.setPageSize(10);
			accountProcessVo.setUserId(userId);
			GenericResult<AccountProcessVo> accountProcessVoGenericResult = findPageAccounData(accountProcessVo);
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
	public GenericResult<AccountLine> findAccountDataById(AccountProcessVo accountProcessVo) {
		GenericResult<AccountLine> result = new GenericResult<>();
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
			AccountLine al = accountDataDao.findDataById(account, accountLine.getId());

			result.setData(al);
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
		return result;
	}

	@Override
	@Transactional
	public GenericResult<Boolean> deleteAccountDataById(AccountProcessVo accountProcessVo) {
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
		} catch (Exception e) {
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
		return result;
	}

	// @Override
	// @Transactional
	// public GenericResult<Boolean> batchUpdateAccounData(AccountProcessVo
	// accountProcessVo) {
	// String taskid = accountProcessVo.getTaskId();
	// GenericResult<Boolean> result = new GenericResult<>();
	// try {
	// if (accountProcessVo == null || accountProcessVo.getAccount() == null) {
	// result.setSuccess(false);
	// result.setMessage("account param is null !");
	// return result;
	// }
	// if (accountProcessVo.getAccount().getId() == null) {
	// result.setSuccess(false);
	// result.setMessage("account id is null !");
	// return result;
	// }
	// //根据id找到数据库中account
	// Account account =
	// accountRepository.findOne(accountProcessVo.getAccount().getId());
	// //传过来的参数account(id,accoutline中的accountfields)
	// Account ac = accountProcessVo.getAccount();
	// //将数据库中account的模板赋予参数
	// ac.setAccountTemplate(account.getAccountTemplate());
	//
	//// Page<AccountLine> accountLines =
	// accountDataDao.findDataByCondition(accountProcessVo);
	////
	//// List<AccountLine> accountLineList = accountLines.getContent();
	//
	// List<Long> lineIds = new ArrayList<>();
	// //分割前台传来数据的id
	// String[] taskids = taskid.split(",");
	// for (int i = 0; i < taskids.length; i++) {
	// lineIds.add(Long.parseLong(taskids[i]));
	// }
	//
	// if (taskid != null && !("".equals(taskid))) {
	// AccountLine accountLine = new AccountLine();
	// accountLine.setAccountFields(accountProcessVo.getAccount().getAccountLines().get(0).getAccountFields());
	// accountDataDao.batchUpdateData(accountLine, account, lineIds);
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
	// }
	//
	// return result;
	//// GenericResult<Boolean> result = new GenericResult<>();
	//// try {
	//// if(accountProcessVo == null || accountProcessVo.getAccount() == null ||
	//// accountProcessVo.getAccount().getId() == null){
	//// result.setSuccess(false);
	//// result.setMessage("addAccountData param is null!");
	//// return result;
	//// }
	//// if(accountProcessVo.getAccount().getAccountLines() == null ||
	//// accountProcessVo.getAccount().getAccountLines().isEmpty()){
	//// result.setMessage("addAccountData is empty!");
	//// return result;
	//// }
	//// Account account =
	// accountRepository.findOne(accountProcessVo.getAccount().getId());
	////
	//// if(account == null){
	//// result.setSuccess(false);
	//// result.setMessage("addAccountData account ID not exist!");
	//// return result;
	//// }
	//// List<AccountLine> accountLines =
	// accountProcessVo.getAccount().getAccountLines();
	//// //判断业务主键数据是否已经存在
	//// if(accountDataDao.queryDataisExist(accountLines.get(0),account)){
	//// result.setMessage("addAccountData pk is exist!");
	//// result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
	//// return result;
	//// }else{
	//// accountDataDao.insertData(accountLines.get(0),account);
	////
	//// AccountEditLog accountEditLog = new AccountEditLog();
	//// accountEditLog.setAccountEditType(AccountEditType.INSERT);
	//// accountEditLog.setLogSource(LogSource.ONLINE);
	//// accountEditLog.setEditLineNum(1);
	////// accountEditLogService.saveAccoutnEditLog(account,accountProcessVo.getUserId(),accountEditLog);
	//// }
	//// } catch (Exception e) {
	//// e.printStackTrace();
	//// throw new AppException(ExceptionCode.SYSTEM_ERROR,e.toString());
	//// }
	//// return result;
	// }

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
	public Collection<FieldPermission> findById(Long userId, Long id) {
		// 根据userid获取该用户的角色集合
		Collection<Role> c = userRepository.findById(userId).getRoles();
		// Collection<Role> c = userRepository.findById(5761L).getRoles();
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
					if (fp.getReportTemplate().getId().equals(id)) {
						rrfps.add(fp);
					}
				}
			}
		}
		return rrfps;
	}

	@Override
	public Long findByAccountTemplateId(Long accountId) {
		return accountRepository.findById(accountId).getAccountTemplate().getId();
	}
}
