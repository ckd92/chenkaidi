package com.fitech.account.service;

import com.fitech.domain.account.Account;

/**
 * Created by SunBojun on 2017/3/2.
 */
public interface AccountReportService {

    /**
     * 流程开启服务
     * @param account
     */
    public void startProcess(Account account);

    public void modify(Account account);

}
