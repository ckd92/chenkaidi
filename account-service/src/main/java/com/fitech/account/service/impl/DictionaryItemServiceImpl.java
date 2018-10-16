package com.fitech.account.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.dao.AccountFieldDAO;
import com.fitech.account.repository.DictionaryItemRepository;
import com.fitech.account.service.DictionaryItemService;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.DictionaryItem;
import com.fitech.framework.core.trace.ServiceTrace;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.StringUtil;

/**
 * Created by wangjianwei on 2017/7/31.
 */
@Service
@ServiceTrace
@Transactional
public class DictionaryItemServiceImpl implements DictionaryItemService {
	EntityManagerFactory entityManagerFactory;
	/**
	 * 注入entityManagerFactory
	 * @param entityManagerFactory
	 */
	@PersistenceUnit
	public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
		this.entityManagerFactory = entityManagerFactory;
	}

	@Autowired
	private DictionaryItemRepository dictionaryItemRepository;
	@Autowired
	private AccountFieldDAO accountFieldDAO;

	/**
	 * 动态条件查询
	 */
	@Override
	public List<DictionaryItem> findDictionaryItem(DictionaryItem dictionaryItem) {

		try{
			return dictionaryItemRepository.findAll(buildSpecification(dictionaryItem));
		}catch(Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}

	}


	/**
	 * 创建动态查询条件组合
	 */
	private Specification<DictionaryItem> buildSpecification(final DictionaryItem dictionaryItem){
		return new Specification<DictionaryItem>(){
			@Override
			public Predicate toPredicate(Root<DictionaryItem> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				List<Predicate> list = new ArrayList<Predicate>();
				if(null != dictionaryItem){
					if (StringUtil.isNotEmpty(dictionaryItem.getDicItemId())) {
						list.add(cb.like(root.get("dicItemId").as(String.class), "%" + dictionaryItem.getDicItemId()+ "%"));
					}
					if (StringUtil.isNotEmpty(dictionaryItem.getDicItemName())) {
						list.add(cb.like(root.get("dicItemName").as(String.class), "%" + dictionaryItem.getDicItemName()+ "%"));
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
	 * 验证字典项名称是否存在
	 */
	@Override
	public Boolean valiDictionaryItemNameIsExist(Long id,DictionaryItem dictionaryItem) {
		List<DictionaryItem> list = getDictionaryItemByDictionaryId(dictionaryItem.getDictionary().getId());
		Boolean flag=false;
		//若改字典下没有字典项
		if(list.isEmpty()){
			flag=true;
		}
		//若只有一条数据
		else if(list.size()==1){
			//如果字典项名称相同
			if(list.get(0).getDicItemName().equals(dictionaryItem.getDicItemName())){
				//判断id是否相同
				if(list.get(0).getId().equals(id)){
					flag = true;
				}else{
					flag = false;
				}
			}else{
				flag = true;
			}
		}
		//如果字典项名称是空
		else if(dictionaryItem.getDicItemName()==""||dictionaryItem.getDicItemName()==null){
			flag=true;
		}else{
			//若有多条数据，循环遍历
			for(int i=0;i<list.size();i++){
				//如果字典项名称相同
				if(list.get(i).getDicItemName().equals(dictionaryItem.getDicItemName())){
					//判断id是否相同
					if(list.get(i).getId().equals(id)){
						flag=true;
						break;
					}else{
						flag=false;
						break;
					}
				}else{
					flag=true;
				}
			}
		}
		return flag;
	}

	

	/**
	 * 根据id查询单个字典项
	 */
	@Override
	public DictionaryItem findOne(Long id) {
		try{
			return dictionaryItemRepository.findOne(id);
		}catch(Exception e){
			e.printStackTrace();
			throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
		}
	}

	/**
	 * 根据字典id查询字典项
	 */
	public List<DictionaryItem> getDictionaryItemByDictionaryId(Long id){
		EntityManager em = entityManagerFactory.createEntityManager();
		Query query = em.createQuery("from DictionaryItem d where d.dictionary.id=? order by d.id desc");
		query.setParameter(1,id);
		List<DictionaryItem> list = query.getResultList();
		em.close();
		return list;
	}

	/**
	 * 根据字典id条件查询字典项,高级搜索
	 */
	public List<DictionaryItem> getDictionaryItemByDicItemName(Long id,String dicItemName){
		EntityManager em = entityManagerFactory.createEntityManager();
		String hql = "from DictionaryItem d where d.dictionary.id=:id";
		if(null!=dicItemName&&""!=dicItemName){
			hql+=" and d.dicItemName like:dicItemName";
			dicItemName = "%"+dicItemName+"%";
		}
		hql+=" order by d.id desc";
		Query query = em.createQuery(hql);
		query.setParameter("id",id);
		if(null!=dicItemName&&""!=dicItemName){
			query.setParameter("dicItemName",dicItemName);
		}
		List<DictionaryItem> list = query.getResultList();
		em.close();
		return list;
	}


	@Override
	public List<DictionaryItem> findByDictionaryId(Long id) {
		return dictionaryItemRepository.findByDictionaryId(id);
	}

	public DictionaryItem findById(Long id){
		return dictionaryItemRepository.findById(id);
	}

	/**
	 * 添加字典项
	 */
	@Override
	public GenericResult<Boolean> save(DictionaryItem dictionaryItem) {
		GenericResult<Boolean> result = new GenericResult<Boolean>();
		//判断字典项名称是否存在
		if(valiDictionaryItemNameIsExist(null,dictionaryItem)){		
			try{
				dictionaryItemRepository.save(dictionaryItem);
			}catch(Exception e){
				e.printStackTrace();
				throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
			}
		}else{
			result.setSuccess(false);
			result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
		}
		return result;
	}
	/**
	 * 修改字典项
	 */
	@Override
	public GenericResult<Boolean> update(Long id, DictionaryItem dictionaryItem) {
		GenericResult<Boolean> result= new GenericResult<Boolean>();
		DictionaryItem findeddictionaryItem = findOne(id);
		if(findeddictionaryItem!=null&&valiDictionaryItemNameIsExist(id,dictionaryItem)){						
			findeddictionaryItem.setDicItemDesc(dictionaryItem.getDicItemDesc());
			findeddictionaryItem.setDicItemName(dictionaryItem.getDicItemName());
			findeddictionaryItem.setDicItemId(dictionaryItem.getDicItemId());
			dictionaryItemRepository.saveAndFlush(findeddictionaryItem);
		}else{
			result.setSuccess(false);
			result.setRestCode(ExceptionCode.ONLY_VALIDATION_FALSE);
		}
		return result;
	}
	/**
	 * 根据id删除字典项
	 */
	@Override
	public GenericResult<Boolean> delete(Long id) {
		GenericResult<Boolean> result = new GenericResult<Boolean>();
		if(dictionaryItemRepository.exists(id)){
			try{
				DictionaryItem dicItem= dictionaryItemRepository.findOne(id);
				if(accountFieldDAO.dicIsDeleteAble(dicItem.getDictionary().getId())){					
					dictionaryItemRepository.delete(id);
					result.setSuccess(true);
				}else{
					result.setSuccess(false);
					result.setMessage("该字典被使用，字典项不可删除！");
				}
			}catch(Exception e){
				e.printStackTrace();
				throw new AppException(ExceptionCode.SYSTEM_ERROR, e.toString());
			}			
		}else{
			result.setSuccess(false);
			result.setMessage("该字典项不存在");
		}		
		return result;
	}
	/**
	 * 根据字典id删除字典项
	 */
	public void deleteByDictionaryId(Long id){
		List<DictionaryItem> list = getDictionaryItemByDictionaryId(id);
		dictionaryItemRepository.delete(list);
	}
}
