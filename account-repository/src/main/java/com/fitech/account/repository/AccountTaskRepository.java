package com.fitech.account.repository;

import com.fitech.domain.account.AccountTask;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by wangxw on 2017/8/16.
 */
public interface AccountTaskRepository extends JpaRepository<AccountTask,Long> {
}
