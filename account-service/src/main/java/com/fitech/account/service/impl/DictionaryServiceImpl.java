package com.fitech.account.service.impl;

import com.fitech.account.dao.AccountFieldDAO;
import com.fitech.account.repository.DictionaryRepository;
import com.fitech.account.service.DictionaryItemService;
import com.fitech.account.service.DictionaryService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Dictionary;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

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

	/**
	 * 动态条件查询
	 */
	@Override
	public Page<Dictionary> findDictionary(Dictionary dictionary) {
		try{
			return dictionaryRepository.findAll(buildSpecification(dictionary),buildPageRequest(dictionary));
		}catch(Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}

	}


	/**
	 * 分页请求
	 */
	private PageRequest buildPageRequest(Dictionary dictionary){
		return new PageRequest(dictionary.getPageNum()-1,dictionary.getPageSize());
	}
	
	/**
	 * 创建动态查询条件组合.
	 */
	private Specification<Dictionary> buildSpecification(final Dictionary dictionary) {
		return new Specification<Dictionary> () {
			@Override
			public Predicate toPredicate(Root<Dictionary> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				if(null != dictionary){
					if (StringUtil.isNotEmpty(dictionary.getDicName())) {
						list.add(cb.like(root.get("dicName").as(String.class), "%" + dictionary.getDicName()+ "%"));
					}
				}
				query.orderBy(cb.desc(root.get("id")));
				Predicate[] predicates = new Predicate[list.size()];
				predicates = list.toArray(predicates);
				return cb.and(predicates);
			}
		};
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
	public GenericResult<Boolean> saveDictionary(Dictionary dictionary) {
		GenericResult<Boolean> result = new GenericResult<Boolean>();
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
			result.setSuccess(false);
			result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
		}
		return result;
	}


	/**
	 * 根据id删除字典
	 */
	@Override
	public GenericResult<Boolean> deleteDictionary(Long id) {
		GenericResult<Boolean> result = new GenericResult<Boolean>();
		//判断该id是否存在字典实体
		if(dictionaryRepository.exists(id)){
			try{
				if(accountFieldDAO.dicIsDeleteAble(id)){
					dictionaryItemService.deleteDictionaryItemByDictionaryId(id);
					dictionaryRepository.delete(id);
					result.setSuccess(true);
				}else{
					result.setSuccess(false);
					result.setMessage("该字典被使用，不可删除！");
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
	public GenericResult<Boolean> updateDictionary(Long id,Dictionary dictionary) {
		GenericResult<Boolean> result= new GenericResult<Boolean>();
		Dictionary findeddictionary = findOne(id);
		//若此id存在对应字典并且字典名称不重复
		if(findeddictionary!=null&&valiDictionaryNameIsExist(id, dictionary).getRestCode().equals("")){
			if(dictionary.getIsEnable().equals("0") && !accountFieldDAO.dicIsDeleteAble(id)){
				result.setSuccess(false);
				result.setMessage("该字典被使用，不可禁用！");
			}else{
				findeddictionary.setDicDesc(dictionary.getDicDesc());
				findeddictionary.setDicName(dictionary.getDicName());
				findeddictionary.setIsEnable(dictionary.getIsEnable());
				dictionaryRepository.saveAndFlush(findeddictionary);
				result.setSuccess(true);
			}
		}else{
			result.setSuccess(false);
			result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
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

	/**
	 * 验证字典是否可以删除
	 */
	@Override
	public Boolean valiDictionaryIsDelete(Dictionary dictionary) {


		return null;
	}

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
}
