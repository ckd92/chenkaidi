package com.fitech.account.dao.impl;

import com.fitech.account.dao.AccountFieldDAO;
import com.fitech.framework.core.dao.mybatis.DaoMyBatis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SunBojun on 2017/4/5.
 */
@Repository
public class AccountFieldDAOImpl extends DaoMyBatis implements AccountFieldDAO {

    @Override
    public Boolean isDeleteAble(Long id) {
        Boolean flag=true;
        Long count = super.selectOne("accountFieldMapper.isDeleteAble",id);
        if(!count.equals(0L)){
            flag = false;
        }
        return flag;
    }

	@Override
	public Boolean dicIsChangeable(Long id) {
		Boolean flag=true;
        Long count = super.selectOne("accountFieldMapper.dicIsChangeable",id);
        if(!count.equals(0L)){
            flag = false;
        }
        return flag;
	}

}
