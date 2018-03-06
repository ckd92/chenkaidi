package com.fitech.account.dao.impl;

import com.fitech.account.dao.AccountFieldDAO;
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
public class AccountFieldDAOImpl extends NamedParameterJdbcDaoSupport implements AccountFieldDAO {


    @Autowired
    public AccountFieldDAOImpl(DataSource dataSource) {
        setDataSource(dataSource);
        try {
            dataSource.getConnection().setAutoCommit(true);

        } catch (Exception e) {

        }
    }

    @Override
    public Boolean isDeleteAble(Long id) {
        Boolean flag=true;
        StringBuffer sb=new StringBuffer();
        sb.append("select count(*) from FieldPermission f,Role_FieldPermission r ");
        sb.append(" where f.id=r.fieldPermission_id");
        sb.append(" and f.ACCOUNTFIELD_ID="+id);
        List<Map<String, Object>> resultList=this.getNamedParameterJdbcTemplate().queryForList(sb.toString(),new HashMap<String,Object>());
        if(!"0".equals(resultList.get(0).get("count(*)").toString())){
            flag=false;
        }
        return flag;
    }

	@Override
	public Boolean dicIsDeleteAble(Long id) {
		Boolean flag=true;
        StringBuffer sb=new StringBuffer();
        sb.append("select count(*) from ACCOUNTFIELD f");
        sb.append(" where f.DICTIONARY_ID="+id);
        List<Map<String, Object>> resultList=this.getNamedParameterJdbcTemplate().queryForList(sb.toString(),new HashMap<String,Object>());
        if(!"0".equals(resultList.get(0).get("count(*)").toString())){
            flag=false;
        }
        return flag;
	}

}
