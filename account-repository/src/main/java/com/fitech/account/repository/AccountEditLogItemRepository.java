package com.fitech.account.repository;

import com.fitech.domain.account.AccountEditLogItem;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by wangxw on 2017/8/10.
 */
public interface AccountEditLogItemRepository extends JpaRepository<AccountEditLogItem,Long> {

}
