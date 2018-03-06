package com.fitech.account.repository;

import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.report.BusSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;

/**
 * Created by wangxw on 2017/7/25.
 */
public interface AccountTemplateRepository extends JpaRepository<AccountTemplate,Long>,JpaSpecificationExecutor {

    public Collection<AccountTemplate> findByBusSystem(BusSystem busSystem);
    
    public AccountTemplate findById(Long id);

}
