package com.fitech.account.repository;

import com.fitech.domain.account.AccountField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

/**
 * Created by wangxw on 2017/7/25.
 */
public interface AccountFieldRepository extends JpaRepository<AccountField,Long> {

    public List<AccountField> findByTemplateIdOrderByOrderNumberAsc(Long templateId);

    public Collection<AccountField> findByTemplateIdAndVisible(Long templateId, Boolean visible);

}
