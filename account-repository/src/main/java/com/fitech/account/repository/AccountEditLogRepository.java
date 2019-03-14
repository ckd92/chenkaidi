package com.fitech.account.repository;

import com.fitech.domain.account.AccountEditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by wangxw on 2017/8/10.
 */
public interface AccountEditLogRepository extends JpaRepository<AccountEditLog,Long>,JpaSpecificationExecutor {


}
