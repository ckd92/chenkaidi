package com.fitech.account.repository;

import com.fitech.domain.account.Dictionary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

/**
 * Created by wangxw on 2017/7/25.
 */
public interface DictionaryRepository extends JpaRepository<Dictionary,Long>,JpaSpecificationExecutor {
	
     List<Dictionary> findByIsEnable(String isEnable);

     Dictionary findDictionaryById(Long id);

}
