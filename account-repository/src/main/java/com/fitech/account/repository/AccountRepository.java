package com.fitech.account.repository;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.fitech.domain.account.Account;
import com.fitech.enums.SubmitStateEnum;

/**
 * Created by wangxw on 2017/8/10.
 */
public interface AccountRepository extends JpaRepository<Account,Long>,JpaSpecificationExecutor {

    /**
     * 根据期数获取待开启的报文实例
     * @param term
     * @return
     */
    public Collection<Account> findByTermAndSubmitStateType(String term,SubmitStateEnum submitStateType);
    
    /**
     * 根据期数,频度获取待开启的报文实例
     * @param term
     * @return
     */
    public Collection<Account> findByTermAndFreqAndSubmitStateType(String term,String freq,SubmitStateEnum submitStateType);

    
    public Account findById(Long Id);

}
