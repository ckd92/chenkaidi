package com.fitech.account.service;

import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;

import java.util.Collection;

/**
 * Created by wangxw on 2017/8/10.
 */
public interface AccountFieldService {

    public Collection<AccountField> convertAccountField(Collection<AccountField> ledgerItems);

    public void deleteAccountFieldByAccountTemplate(AccountTemplate accountTemplate);
    
    public Collection<AccountField> modifyAccountField(Collection<AccountField> ledgerItems);
    
    public AccountField getLedgerItem(AccountField ledgerItem);
}
