package com.fitech.account.repository;

import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountProcess;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by wangxw on 2017/8/10.
 */
public interface AccountProcessRepository extends JpaRepository<AccountProcess,Long> {

    public AccountProcess findByAccount(Account account);

}
