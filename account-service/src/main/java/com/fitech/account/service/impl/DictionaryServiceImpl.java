package com.fitech.account.service.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.fitech.account.dao.DictionaryDao;
import com.fitech.account.service.AccountTemplateService;
import com.fitech.domain.account.*;
import com.fitech.domain.account.Dictionary;
import com.fitech.dto.DictionaryDto;
import com.fitech.framework.lang.annotation.Description;
import com.fitech.report.repository.RepFreqRepository;
import com.fitech.report.service.RLedgerModelService;
import com.fitech.system.dao.FieldPermissionDao;
import com.fitech.vo.account.AccountDicVo;
import com.fitech.vo.account.AccountFieldVo;
import com.fitech.vo.account.AccountTemplateVo;
//import com.sun.org.apache.xpath.internal.operations.Bool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.dao.AccountFieldDAO;
import com.fitech.account.repository.DictionaryItemRepository;
import com.fitech.account.repository.DictionaryRepository;
import com.fitech.account.service.DictionaryItemService;
import com.fitech.account.service.DictionaryService;
import com.fitech.constant.ExceptionCode;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.StringUtil;

/**
 * Created by wangxw on 2017/7/1.
 */
@Service
@ServiceTrace
@Transactional
public class DictionaryServiceImpl implements DictionaryService {

	@Autowired
	private DictionaryRepository dictionaryRepository;
	@Autowired
	private DictionaryItemService dictionaryItemService;
	@Autowired
	private AccountFieldDAO accountFieldDAO;
	@Autowired
	private DictionaryItemRepository dictionaryItemRepository;

	@Autowired
	private DictionaryDao dictionaryDao;
	@Autowired(required=false)
	private RLedgerModelService rLedgerModelService;

	@Autowired
	private AccountTemplateService accountTemplateService;
	@Autowired
	private FieldPermissionDao fieldPermissionDao;
	@Autowired
	private RepFreqRepository repFreqRepository;

	/**
	 * 查所有字典
	 */
	@Override
	public List<Dictionary> findAllDictionary() {
		try{
			return dictionaryRepository.findAll();
		}catch(Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
	}
	/**
	 * 动态条件查询
	 */
	@Override
	public List<DictionaryDto> findDictionary(Dictionary dictionary) {
		try{
			return dictionaryDao.getAllDic();
		}catch(Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
	}

	@Override
	public List<Dictionary> findDictionaryNoBan(String isEnable) {
		return dictionaryRepository.findByIsEnable(isEnable);
	}

	/**
	 * 根据id查询单个字典
	 */
	@Override
	public Dictionary findOne(Long id) {
		try{
			return dictionaryRepository.findOne(id);
		}catch(Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
	}
	/**
	 * 添加字典
	 */
	@Override
	public GenericResult<Boolean> save(Dictionary dictionary) {
		GenericResult<Boolean> result = new GenericResult<Boolean>();
		Dictionary dic =new Dictionary();
		if(null!= dictionary.getParentId()&&!"".equals(dictionary.getParentId())){
			dic= dictionaryDao.getNextDicId(Long.valueOf(dictionary.getParentId()));
		}
		if( null!=dic){
			if(null!=dic.getId()){
				result.setMessage("该父级字典已是["+dic.getDicName()+"]的父级字典，请重新选择父级字典");
				result.fail(ExceptionCode.ONLY_VALIDATION_FALSE);
				return result;
			}
		}
		//判断字典名称、字典编码是否存在
		if(valiDictionaryNameIsExist(null,dictionary).getRestCode().equals("")){
			try{
				dictionaryRepository.save(dictionary);
			}catch(Exception e){
				e.printStackTrace();
				throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
			}
		}else{
			result.setMessage("字典名称存在重复");
			result.fail(ExceptionCode.ONLY_VALIDATION_FALSE);
		}
		return result;
	}

	@Transactional
	@Override
	public GenericResult<Boolean> deleteAll() {
		GenericResult<Boolean> result = new GenericResult<>();
		List<Dictionary> all = dictionaryRepository.findAll();
		try {
				for (Dictionary dictionary : all) {
					Long id = dictionary.getId();
					if (!accountFieldDAO.dicIsChangeable(id) || !accountFieldDAO.dicIsTemplateUsed(id)) {
						result.setMessage("存在字典已生成任务或者被字典引用的不可删除！");
					}else{
						dictionaryItemService.deleteByDictionaryId(id);
						dictionaryRepository.delete(id);
					}
				}
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
		result.setSuccess(true);
		return result;
	}

	@Transactional
	@Override
	public GenericResult<Boolean> Pldelete(Long[] ids) {
		GenericResult<Boolean> result = new GenericResult<>();
		try {
			for (Long id : ids) {
				if (!accountFieldDAO.dicIsChangeable(id) || !accountFieldDAO.dicIsTemplateUsed(id)) {
					result.setMessage("存在字典已生成任务或者被字典引用的不可删除！");
				} else {
					dictionaryItemService.deleteByDictionaryId(id);
					dictionaryRepository.delete(id);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			result.setSuccess(false);
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
		result.setSuccess(true);
		return result;
	}

	/**
	 * 根据id删除字典
	 */
	@Override
	public GenericResult<Boolean> delete(Long id) {
		GenericResult<Boolean> result = new GenericResult<Boolean>();
		//判断该id是否存在字典实体
		if(dictionaryRepository.exists(id)){
			try{
				if(nextDicId(id) != null){
					result.setSuccess(false);
					result.setMessage("该字典存在下级字典，不可删除！");
					return result;
				}
				if(!accountFieldDAO.dicIsChangeable(id) || !accountFieldDAO.dicIsTemplateUsed(id)){
					result.setSuccess(false);
					result.setMessage("该字典存在已生成任务或者被字典引用，不可删除！");
					return result;
				}
				dictionaryItemService.deleteByDictionaryId(id);
				dictionaryRepository.delete(id);
				result.setSuccess(true);
			}catch(Exception e){
				e.printStackTrace();
				throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
			}
		}else{
			result.setSuccess(false);
			result.setMessage("该字典不存在");
		}
		return result;
	}


	/**
	 * 更新字典
	 */
	@Override
	public GenericResult<Boolean> update(Long id,Dictionary dictionary,String flag) {
		GenericResult<Boolean> result= new GenericResult<Boolean>();
		Dictionary findeddictionary = findOne(id);
		Dictionary dic =new Dictionary();

		if(null!= dictionary.getParentId()&&!"".equals(dictionary.getParentId())){
			if(String.valueOf(dictionary.getId()).equals(dictionary.getParentId())){
				result.setMessage("自己不能作为自己的父级字典");
				result.fail(ExceptionCode.ONLY_VALIDATION_FALSE);
				return result;
			}
			dic= dictionaryDao.getNextDicId(Long.valueOf(dictionary.getParentId()));
		}
		if( null!=dic && null!=dic.getId()){
			if(!id.equals(dic.getId())){
				result.setMessage("该父级字典已是["+dic.getDicName()+"]的父级字典，请重新选择父级字典");
				result.fail(ExceptionCode.ONLY_VALIDATION_FALSE);
				return result;
			}
		}

		if(dictionary.getParentId() != null && !dictionary.getParentId().equals("")){
			Dictionary findParentId = nextDicId(dictionary.getId());
			Long parentId = Long.valueOf(dictionary.getParentId());
			if(findParentId != null && findParentId.getId().equals(parentId)){
				result.setMessage("该字典为当前字典项的子字典项，无法设置该字典项为父类");
				result.fail(ExceptionCode.ONLY_VALIDATION_FALSE);
				return result;
			}
		}

		if( dictionaryDao.getDicByParentOrId(null,id,"1") != null ||  !accountFieldDAO.dicIsTemplateUsed(id)){
			result.setSuccess(false);
			result.setMessage("该字典被使用，不可禁用或修改！");
			return result;
		}
		if(dictionary.getIsEnable().equals("1") && dictionary.getParentId() !=null && !dictionary.getParentId().equals("")
				&& dictionaryDao.getDicByParentOrId(Long.valueOf(dictionary.getParentId()),null,null).getIsEnable().equals("0")){
			result.setSuccess(false);
			result.setMessage("该父类字典被禁用，不可启用！");
			return result;
		}

		//若此id存在对应字典并且字典名称不重复
		if(findeddictionary!=null&&valiDictionaryNameIsExist(id, dictionary).getRestCode().equals("")){
			findeddictionary.setDicDesc(dictionary.getDicDesc());
			findeddictionary.setDicName(dictionary.getDicName());
			findeddictionary.setIsEnable(dictionary.getIsEnable());
			findeddictionary.setParentId(dictionary.getParentId());
			dictionaryRepository.saveAndFlush(findeddictionary);
			//1 代表父级字典改变了，清除父级字典项的引用
			if(flag.equals("1")){
				List<DictionaryItem> byDictionaryId = dictionaryItemRepository.findByDictionaryId(id);
				for(DictionaryItem d : byDictionaryId){
					d.setParentId("");
				}
				dictionaryItemRepository.save(byDictionaryId);
			}
			result.setSuccess(true);
		}else{
			result.fail(ExceptionCode.ONLY_VALIDATION_FALSE);
		}

		return result;
	}

	/**
	 * 判断字典名称是否存在
	 */
	@Override
	public GenericResult<Boolean> valiDictionaryNameIsExist(Long id,Dictionary dictionary) {
		GenericResult<Boolean> result = new GenericResult<Boolean>();
		if(StringUtil.isNotEmpty(dictionary.getDicName())){
			Dictionary newDictionary = new Dictionary();
			newDictionary.setDicName(dictionary.getDicName());
			List<Dictionary> list = dictionaryRepository.findAll(buildSpecification1(dictionary));
			if(!list.isEmpty()&&null!=list){
				if(list.size()>=2){
					result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
				}else if(list.get(0).getId()!=id){
					result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
				}
			}
		}
		return result;

	}

	private Specification<Dictionary> buildSpecification1(final Dictionary dictionary) {
		return new Specification<Dictionary> () {
			@Override
			public Predicate toPredicate(Root<Dictionary> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				if(null != dictionary){
					if (StringUtil.isNotEmpty(dictionary.getDicName())) {
						list.add(cb.like(root.get("dicName").as(String.class),dictionary.getDicName()));
					}
				}
				query.orderBy(cb.desc(root.get("id")));
				Predicate[] predicates = new Predicate[list.size()];
				predicates = list.toArray(predicates);
				return cb.and(predicates);
			}
		};
	}

	@Override
	public Dictionary nextDicId(Long id) {
		Dictionary dic=dictionaryDao.getNextDicId(id);
		return dic;
	}

	@Transactional
	@Override
	public GenericResult<Object> batchAdd( Collection<AccountDicVo> list)  {
		GenericResult<Object> result = new GenericResult<>();
		try {
			DictionaryItem dictionaryItem = null;
			Dictionary dictionary = null;
			//导入的数据
			List<AccountDicVo> dicVoList = (List<AccountDicVo>)list;
			//判断数据是否通过验证
			result = this.valiDictionaryData(list);
			if(!result.isSuccess()){
				return  result;
			}
			//初始数据 存放字典和字典项对象
			List<Dictionary> dictionaryList2 = new ArrayList<>();
			List<DictionaryItem> ItemsList2 = new ArrayList<>();
			for(AccountDicVo accountDicVo : dicVoList){
				dictionary = new Dictionary();
				dictionary.setDicName(accountDicVo.getDicName());
				dictionary.setId(Long.valueOf(accountDicVo.getDicId()));
				dictionary.setParentId(accountDicVo.getDicParentId());
				dictionary.setIsEnable(accountDicVo.getDicIsEnabel());
				dictionary.setDicDesc(accountDicVo.getDicDescription());
				dictionaryList2.add(dictionary);
				if(StringUtil.isNotEmpty(accountDicVo.getItemId())) {
					dictionaryItem = new DictionaryItem();
					dictionaryItem.setDictionary(dictionary);
					dictionaryItem.setId(Long.valueOf(accountDicVo.getItemId()));
					dictionaryItem.setDicItemName(accountDicVo.getDicItemName());
					dictionaryItem.setDicItemId(accountDicVo.getDicItemId());
					dictionaryItem.setDicItemDesc(accountDicVo.getDicItemDescription());
					dictionaryItem.setParentId(accountDicVo.getDicItemParentId());
					ItemsList2.add(dictionaryItem);
				}
			}

			//去重之后的字典集合
			Set<Dictionary> dictinarySet = new HashSet();
			for(Dictionary dic : dictionaryList2){
				dictinarySet.add(dic);
			}
			//对去过一次重的字典检查是否有字典名 重复
			for(Dictionary dic : dictinarySet){
				//表内去重
				for(Dictionary dic2 : dictionaryList2) {
					if(!dic.equals(dic2)){
						if(dic2.getDicName().equals(dic.getDicName())){
							result.setSuccess(false);
							result.setMessage("excel中存在字典名称重复，该重复字典名称为" + dic.getDicName());
							return result;
						}
						if(!dic2.getDicName().equals(dic.getDicName()) && dic.getId().equals(dic2.getId())){
							result.setSuccess(false);
							result.setMessage("excel中存在字典Id重复，该重复字典Id为" + dic.getId());
							return result;
						}
					}
				}
				//数据库查重
				if (dictionaryRepository.findAll(buildSpecification1(dic)).size() > 0) {
					result.setSuccess(false);
					result.setMessage("excel中存在字典名称在数据库中重复，该重复字典名称为" + dic.getDicName());
					return result;
				}
				if(dictionaryRepository.findDictionaryById(dic.getId()) != null){
					result.setSuccess(false);
					result.setMessage("excel中存在字典Id在数据库中重复，该重复字典Id为" + dic.getId());
					return result;
				}

			}

			/**字典项**/
			//字典项查重
			Set<DictionaryItem> itemSet = new HashSet<>();
			int OrignSize = 0;
			int nowSize = 0;
			for(DictionaryItem item : ItemsList2) {
				OrignSize = itemSet.size();
				itemSet.add(item);
				nowSize = itemSet.size() > OrignSize ? nowSize++ : OrignSize;
				if(OrignSize != 0 && nowSize != 0 && OrignSize == nowSize){
					result.setSuccess(false);
					result.setMessage(" excel中存在某一字典下有多个重复的字典项数据，该字典id为" + item.getDictionary().getId());
					return result;
				}
			}

			for (DictionaryItem item : ItemsList2) {
				for (DictionaryItem item2 : ItemsList2) {
					if (!item.equals(item2)){
						//excel中同一个字典下 查重是否有相同的字典项编号和名称
						if(item.getDictionary().getId().equals(item2.getDictionary().getId())){
							if (item.getDicItemId().equals(item2.getDicItemId())) {
								result.setSuccess(false);
								result.setMessage(" excel中存在某一字典下有多个重复的字典项编号，该字典id为" + item.getDictionary().getId());
								return result;
							}
							if (item.getDicItemName().equals(item2.getDicItemName())) {
								result.setSuccess(false);
								result.setMessage(" excel中存在某一字典下有多个重复的字典项名称，该字典id为" + item.getDictionary().getId());
								return result;
							}
						}
						//excel中的字典项id是否有重复
						if (item.getId().equals(item2.getId())) {
							result.setSuccess(false);
							result.setMessage(" excel中存在某一字典下有多个重复的字典项Id，该字典项id为" + item.getDictionary().getId());
							return result;
						}
					}
				}
				//数据库查重id是否重复
				if(dictionaryItemRepository.findById(item.getId()) != null){
					result.setSuccess(false);
					result.setMessage("excel中存在字典项Id在数据库中重复，该重复字典项对应的字典Id为" + item.getDictionary().getId());
					return result;
				}
				//数据库查重字典下的字典项的编号和名称是否在数据库中重复
				if(dictionaryItemRepository.findByDicItemIdAndDictionary(item.getDicItemId(),item.getDictionary()) != null){
					result.setSuccess(false);
					result.setMessage("excel中存在某一字典的字典项编号在数据库中重复，该对应的字典Id和字典项编号为" + item.getDictionary().getId() + "," + item.getDicItemId());
					return result;
				}
				if(dictionaryItemRepository.findByDicItemNameAndDictionary(item.getDicItemName(),item.getDictionary()) != null){
					result.setSuccess(false);
					result.setMessage("excel中存在某一字典的字典项名称在数据库中重复，该对应的字典Id和字典项名称为"  + item.getDictionary().getId() + "," + item.getDicItemName());
					return result;
				}
			}

			//循环新增字典
			for(Dictionary dic : dictinarySet){
				dictionaryDao.addDictionary(dic);
			}

			//循环新增字典项
			for(DictionaryItem item2 : ItemsList2){
				dictionaryDao.addDictionaryItem(item2);
			}
				result.setSuccess(true);
		}catch (Exception e) {
			e.printStackTrace();
			throw new AppException("载入失败，数据异常");
		}
		return result;
	}

	@Override
	public List<List<String>> getDicAndDicitemData() {
		List<List<String>> list = new ArrayList<>();
		List<String> fieldNames = new ArrayList<>();
		List<String> values = null;
		List<AccountDicVo> dicVoList = dictionaryDao.searchDictionary();
		try {
			Class clazz = Class.forName("com.fitech.vo.account.AccountDicVo");
			Field[] fields = clazz.getDeclaredFields();
			for(Field field : fields){
				Description f = field.getAnnotation(Description.class);
				String annotation = f.value()[0];
				fieldNames.add(annotation);
			}
			list.add(fieldNames);
			for(AccountDicVo dic : dicVoList){
				values = new ArrayList<>();
				clazz = dic.getClass();
				for(Field field :clazz.getDeclaredFields()){
						String methodName = "get" + field.getName().substring(0,1).toUpperCase() + field.getName().substring(1);
						Method method =  clazz.getDeclaredMethod(methodName);
						Object invoke = method.invoke(dic);
						values.add(String.valueOf(invoke).equals("null") ? "" : String.valueOf(invoke));
				}
				list.add(values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public String validateDataCheck(List<AccountFieldVo> itemList) {
		GenericResult<Boolean> result = new GenericResult<>();
		for(AccountFieldVo accountFieldVo : itemList){
			String itemType = accountFieldVo.getFieldType();
			if("CODELIB".equals(itemType) || "SINGLECODELIB".equals(itemType)){
				Dictionary dic = new Dictionary();
				dic.setDicName(accountFieldVo.getDictionaryName());
				List<Dictionary> all = dictionaryRepository.findAll(buildSpecification1(dic));
				if(null == all || all.isEmpty()){
					return "数据字典" + accountFieldVo.getDictionaryName()+"不存在";
				}
			}
		}
		return null;
	}
	@Transactional
	@Override
	public GenericResult<Boolean> batchAddTempAndField(String busSystemId, List<AccountTemplateVo> templateList, List<AccountFieldVo> itemList) {
		GenericResult<Boolean> result = new GenericResult<>();
		//建表初始化报文权限
		try{
			for (AccountTemplateVo vo : templateList) {
				AccountTemplate accountTemplate = new AccountTemplate();
				accountTemplate.setTemplateCode(vo.getTemplateCode());
				accountTemplate.setTableName(vo.getTemplateCode());
				accountTemplate.setTemplateName(vo.getTemplateName());
				accountTemplate.setStartDate(vo.getStartDate());
				accountTemplate.setEndDate(vo.getEndDate());
				accountTemplate.setRepFreq(repFreqRepository.findByRepFreqName(vo.getTemplatefreq()));
				AccountField accountField = null;
				List<AccountField> accountFieldList = new ArrayList<>();
				for(AccountFieldVo fieldvo : itemList){
					if(accountTemplate.getTableName().equals(fieldvo.getTablename())){
						accountField = new AccountField();
						accountField.setItemCode(fieldvo.getFieldCode());
						accountField.setItemName(fieldvo.getFieldName());
						accountField.setItemType(fieldvo.getFieldType());
						accountField.setLength(fieldvo.getFieldLength());
						accountField.setPkable(Boolean.valueOf(fieldvo.getPrimaryKey()));
						accountField.setItemDescription(fieldvo.getDescription());
						if (fieldvo.getFieldType().equals("CODELIB")) {
							Dictionary dic = new Dictionary();
							dic.setDicName(fieldvo.getDictionaryName());
							List<Dictionary> all = dictionaryRepository.findAll(buildSpecification1(dic));
							accountField.setDicId(String.valueOf(all.get(0).getId()));
						}
						accountField.setOrderNumber(Integer.valueOf(fieldvo.getOrdernumber()));
						accountFieldList.add(accountField);
					}
				}
				accountTemplate.setAccountFields(accountFieldList);
				result = accountTemplateService.save(accountTemplate);
				if(!result.isSuccess()){
					return result;
				}
			}
		}catch (Exception e){
			e.printStackTrace();
			throw new AppException("批量载入模板失败，数据异常！");
		}
		return result;
	}


	/**
	 * 校验导入字典项数据
	 * @param list
	 * @return
	 */
	private GenericResult<Object> valiDictionaryData(Collection<AccountDicVo> list) {
		GenericResult<Object> result = new GenericResult<>();
		List<AccountDicVo> dicVoList = (List<AccountDicVo>)list;
		//存放去重的数据
		List<AccountDicVo> unRepeatList = new ArrayList<>();
		for (AccountDicVo accountDicVo : dicVoList) {
			//查询字典id是否为空和字典项父级id是否等于其自身
			if(!StringUtil.isNotEmpty(accountDicVo.getDicId())){
				result.setSuccess(false);
				result.setMessage("excel中字典id存在空值");
				return result;
			}else if(accountDicVo.getDicId().equals(accountDicVo.getDicParentId())){
				result.setSuccess(false);
				result.setMessage("excel中存在字典的父级字典等于字典id,该字典id为" + accountDicVo.getDicId());
				return result;
			}
			//查询字典名称是否为空
			if(!StringUtil.isNotEmpty(accountDicVo.getDicName() )){
				result.setSuccess(false);
				result.setMessage("excel中字典名称存在空值");
				return result;
			}
			//如果字典项id不为空，则字典编号和字典名称不能为空
			if(StringUtil.isNotEmpty(accountDicVo.getItemId())){
				if(!StringUtil.isNotEmpty(accountDicVo.getDicItemId())){
					result.setSuccess(false);
					result.setMessage("excel中存在字典项id不为空，字典项编号为空");
					return result;
				}
				if(!StringUtil.isNotEmpty(accountDicVo.getDicItemName())){
					result.setSuccess(false);
					result.setMessage("excel中存在字典项id不为空，字典项名称为空");
					return result;
				}
			}
		}
		for(AccountDicVo accountDicVo : dicVoList){
				//判断是否有重复的数据
				if(unRepeatList.contains(accountDicVo)){
					result.setSuccess(false);
					result.setMessage("excel中存在重复数据源,重复数据id为" + accountDicVo.getDicId());
				}else{
					unRepeatList.add(accountDicVo);
				}
		}

		return result;
	}

}
