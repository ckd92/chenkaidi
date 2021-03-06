package com.fitech.account.repository;

import java.util.List;

import com.fitech.domain.account.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.fitech.domain.account.DictionaryItem;

/**
 * Created by wangjianwei on 2017/7/31.
 */
public interface DictionaryItemRepository extends JpaRepository<DictionaryItem, Long>,JpaSpecificationExecutor {

	public List<DictionaryItem> findByDictionaryId(Long dictionaryId);

	public DictionaryItem findById(Long id);

	public List<DictionaryItem> findByParentId(String parentId);

	public DictionaryItem findByDicItemIdAndDictionary(String dicItemId, Dictionary dictionary);

	public DictionaryItem findByDicItemNameAndDictionary(String dicItemId,Dictionary dictionary);


}
