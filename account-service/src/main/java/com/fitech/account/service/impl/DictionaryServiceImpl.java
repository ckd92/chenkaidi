package com.fitech.account.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import com.fitech.account.dao.DictionaryDao;
import com.fitech.domain.account.DictionaryItem;
import com.fitech.dto.DictionaryDto;
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

		if(dictionary.getIsEnable().equals("0") && dictionaryDao.getDicByParentOrId(null,id,"1") != null
				|| dictionary.getIsEnable().equals("0") && !accountFieldDAO.dicIsTemplateUsed(id)){
			result.setSuccess(false);
			result.setMessage("该字典被使用，不可禁用！");
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

}
