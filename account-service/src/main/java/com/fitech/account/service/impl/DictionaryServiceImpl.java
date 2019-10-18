package com.fitech.account.service.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.fitech.account.dao.DictionaryDao;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.DictionaryItem;
import com.fitech.dto.DictionaryDto;
import com.fitech.framework.lang.util.ExcelUtil;
import com.fitech.vo.account.AccountDicVo;
import org.apache.poi.ss.usermodel.Sheet;
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
import com.fitech.domain.account.Dictionary;
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


	/**
	 * 根据id删除字典
	 */
	@Override
	public GenericResult<Boolean> delete(Long id) {
		GenericResult<Boolean> result = new GenericResult<Boolean>();
		//判断该id是否存在字典实体
		if(dictionaryRepository.exists(id)){
			try{
				if( nextDicId(id) == null && accountFieldDAO.dicIsChangeable(id)&&accountFieldDAO.dicIsTemplateUsed(id)){
					dictionaryItemService.deleteByDictionaryId(id);
					dictionaryRepository.delete(id);
					result.setSuccess(true);
				}else{
					result.setSuccess(false);
					result.setMessage("该字典存在下级字典，不可删除！");
				}
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

	@Override
	public GenericResult<Object> batchAdd(Long busSystemId, Collection<AccountDicVo> list)  {
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
				dictionary.setIsEnable("1");
				dictionary.setDicDesc(accountDicVo.getDicDescription());
				dictionaryList2.add(dictionary);
				dictionaryItem = new DictionaryItem();
				dictionaryItem.setDictionary(dictionary);
				dictionaryItem.setId(Long.valueOf(accountDicVo.getItemId()));
				dictionaryItem.setDicItemName(accountDicVo.getDicItemName());
				dictionaryItem.setDicItemId(accountDicVo.getDicItemId());
				dictionaryItem.setDicItemDesc(accountDicVo.getDicItemDescription());
				dictionaryItem.setParentId(accountDicVo.getDicItemParentId());
				ItemsList2.add(dictionaryItem);
			}
			//单属性 去重
			List<String> strs = new ArrayList<>();
			//分别存放半属性的字典和字典项对象，用于后面去重
			List<Dictionary> dictionaryList = new ArrayList<>();
			List<DictionaryItem> ItemList = new ArrayList<>();
			//分别存放去重后的字典集合和字典项集合
			List<Dictionary> lastDictionaryList = new ArrayList<>();
			List<DictionaryItem> lastItemList = new ArrayList<>();
			for(AccountDicVo accountDicVo : dicVoList){
				dictionary = new Dictionary();
				dictionary.setDicName(accountDicVo.getDicName());
				dictionary.setId(Long.valueOf(accountDicVo.getDicId()));
				dictionaryList.add(dictionary);
				dictionaryItem = new DictionaryItem();
				dictionaryItem.setDictionary(dictionary);
				dictionaryItem.setDicItemName(accountDicVo.getDicItemName());
				dictionaryItem.setDicItemId(accountDicVo.getDicItemId());
				ItemList.add(dictionaryItem);
			}
			//对字典去重 字典id和字典名称去重  去除多余属性干扰
			for(Dictionary dic :dictionaryList){
				if(!lastDictionaryList.contains(dic)){
					lastDictionaryList.add(dic);
				}
			}
			//对去过一次重的字典检查是否有字典名 重复
			for(Dictionary dic : lastDictionaryList){
				if(strs.contains(dic.getDicName())){
					result.setSuccess(false);
					result.setMessage("excel中存在字典名称重复，该重复字典名称为" + dic.getDicName());
					return result;
				}else{
					strs.add(dic.getDicName());
				}
			}
			//判断字典是否在数据库中重复
			for(Dictionary dic : lastDictionaryList) {
				if (dictionaryRepository.findAll(buildSpecification1(dic)).size() > 0) {
					result.setSuccess(false);
					result.setMessage("存在字典名称在数据库中重复，该重复字典名称为" + dic.getDicName());
					return result;
				}
			}
			//最终去重之后的字典集合
			List<Dictionary> addDics = new ArrayList<>();
			for(Dictionary dic : lastDictionaryList){
				for(Dictionary dic2 : dictionaryList2){
					if(dic.getDicName().equals(dic2.getDicName())){
						addDics.add(dic2);
						break;
					}
				}
			}
			//判断字典的父级字典是否有重复数据
			strs.clear();
			for(Dictionary dic: addDics){
				if(StringUtil.isNotEmpty(dic.getParentId())){
					if(strs.contains(dic.getParentId())){
						result.setSuccess(false);
						result.setMessage("excel中存在字典的父级字典数据重复，该字典为id为" + dic.getId());
						return  result;
					}else{
						strs.add(dic.getParentId());
					}
				}
			}
			/**字典项**/
			//字典项查重  去除多余属性干扰
			for(DictionaryItem item :ItemList){
				if(lastItemList.contains(item)){
					result.setSuccess(false);
					result.setMessage(" excel中存在某一字典下有多个重复的字典项数据，该字典id为" + item.getDictionary().getId());
					return result;
				}else{
					lastItemList.add(item);
				}
			}
			//字典项去重过后，对excel中同一个字典下，判断是否有重复的字典项名称
			List<DictionaryItem> dicItemIdsAndNames = new ArrayList<>();
			List<DictionaryItem> lastDicItemIdsAndNames = new ArrayList<>();
			for(DictionaryItem item :lastItemList){
				dictionary = new Dictionary();
				dictionary.setId(item.getDictionary().getId());
				dictionary.setDicName(item.getDictionary().getDicName());
				dictionaryItem = new DictionaryItem();
				dictionaryItem.setDictionary(dictionary);
				dictionaryItem.setDicItemId(item.getDicItemId());
				dicItemIdsAndNames.add(dictionaryItem);
			}
			for(DictionaryItem dic : dicItemIdsAndNames){
				if(lastDicItemIdsAndNames.contains(dic)){
					result.setSuccess(false);
					result.setMessage(" excel中存在某一字典下有多个重复的字典项编号，该字典id为" + dic.getDictionary().getId());
					return result;
				}else{
					lastDicItemIdsAndNames.add(dic);
				}
			}
			//excel中同一个字典下，判断是否有重复的字典项名称
			dicItemIdsAndNames.clear();
			lastDicItemIdsAndNames.clear();
			for(DictionaryItem item :lastItemList){
				dictionary = new Dictionary();
				dictionary.setId(item.getDictionary().getId());
				dictionary.setDicName(item.getDictionary().getDicName());
				dictionaryItem = new DictionaryItem();
				dictionaryItem.setDictionary(dictionary);
				dictionaryItem.setDicItemName(item.getDicItemName());
				dicItemIdsAndNames.add(dictionaryItem);
			}
			for(DictionaryItem dic : dicItemIdsAndNames){
				if(lastDicItemIdsAndNames.contains(dic)){
					result.setSuccess(false);
					result.setMessage(" excel中存在某一字典下有多个重复的字典项名称，该字典项id为" + dic.getDictionary().getId());
					return result;
				}else{
					lastDicItemIdsAndNames.add(dic);
				}
			}
			//判断字典项是否在数据库中重复
		/*	for(DictionaryItem dicItem : lastItemList){
				List<DictionaryItem> byDictionaryId = dictionaryItemRepository.findByDictionaryId(Long.valueOf(dicItem.getDictionary().getId()));
				for(DictionaryItem dicItem2 : byDictionaryId){
					if(dicItem.getDicItemId().equals(dicItem2.getDicItemId()) ||
					dicItem.getDicItemName().equals(dicItem2.getDicItemName())) {
						result.setSuccess(false);
						result.setMessage("excel存在字典项编号或者字典项名称在数据库中重复，该对应字典项id为" + dicItem.getId());
						return result;
					}
				}
			}*/
			//最终去重之后的字典项集合
			List<DictionaryItem> addDicitems = new ArrayList<>();
			for(DictionaryItem item : lastItemList){
				for(DictionaryItem item2 : ItemsList2){
					if(item.getDicItemName().equals(item2.getDicItemName()) && item.getDicItemName().equals(item2.getDicItemName()) &&
					item.getDictionary().getId().equals(item2.getDictionary().getId())){
						addDicitems.add(item2);
						break;
					}
				}
			}
				//循环新增字典
				for(Dictionary dic : addDics){
					if(StringUtil.isNotEmpty(dic.getParentId()) &&
							dictionaryDao.getNextDicId(Long.valueOf(dic.getParentId())) != null){
						throw new RuntimeException("excel存在字典的父级字典项在已被使用，该对应字典id为" + dic.getId());
					}else{
						dictionaryDao.addDictionary(dic);
					}
				}
				//循环新增字典项
				for(DictionaryItem item2 : addDicitems){
					//判断字典项是否在数据库中存在
					if(StringUtil.isNotEmpty(item2.getParentId()) &&
							dictionaryItemRepository.findByParentId(item2.getParentId()).size() == 0){
						throw new RuntimeException("excel中存在字典项的父级字典项在数据库中不存在，该对应字典项id为" + item2.getId());
					}else{
						dictionaryDao.addDictionaryItem(item2);
					}
				}
		}catch (RuntimeException e) {
			e.printStackTrace();
			throw new AppException("载入失败，数据异常",e.getMessage());
		}
		return result;
	}

	private GenericResult<Object> valiDictionaryData(Collection<AccountDicVo> list) {
		GenericResult<Object> result = new GenericResult<>();
		List<AccountDicVo> dicVoList = (List<AccountDicVo>)list;
		//存放去重的数据
		List<AccountDicVo> unRepeatList = new ArrayList<>();
		List<String> unRepeatDicId = new ArrayList<>();
		for (AccountDicVo accountDicVo : dicVoList) {
			if(!StringUtil.isNotEmpty(accountDicVo.getDicId())){
				result.setSuccess(false);
				result.setMessage("字典id存在空值");
				return result;
			}else if(accountDicVo.getDicId().equals(accountDicVo.getDicParentId())){
				result.setSuccess(false);
				result.setMessage("excel中存在字典的父级字典等于字典id,该字典id为" + accountDicVo.getDicId());
				return result;
			}
			if(!StringUtil.isNotEmpty(accountDicVo.getDicName() )){
				result.setSuccess(false);
				result.setMessage("字典名称存在空值");
				return result;
			}
			if(!StringUtil.isNotEmpty( accountDicVo.getItemId() )){
				result.setSuccess(false);
				result.setMessage("字典项id称存在空值");
				return result;
			}
			if(!StringUtil.isNotEmpty( accountDicVo.getDicItemId() )){
				result.setSuccess(false);
				result.setMessage("字典项编号称存在空值");
				return result;
			}
			if(!StringUtil.isNotEmpty( accountDicVo.getDicItemName() )){
				result.setSuccess(false);
				result.setMessage("字典项名称存在空值");
				return result;
			}
		}
		for(AccountDicVo accountDicVo : dicVoList){
				if(unRepeatDicId.contains(accountDicVo.getItemId())){
					result.setSuccess(false);
					result.setMessage("excel中存在重复字典项id,重复的字典id为" + accountDicVo.getItemId());
				}else{
					unRepeatDicId.add(accountDicVo.getItemId());
				}
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
