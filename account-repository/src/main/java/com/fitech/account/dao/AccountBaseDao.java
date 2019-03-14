
package com.fitech.account.dao;

import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 基础dao
 * Created by wupei on 2017/3/2.
 */
public interface AccountBaseDao {
    /**
     * sql分页
     * @param sql sql可以预先拼装
     * @param parMap 参数 可以不填
     * @param pageSize 每页条数
     * @param pageNo 当前页数
     * @return
     */
    public Page<Object[]> findPageBySql(StringBuffer sql, Map<String, String> parMap, int pageSize, int pageNo);

    /**
     * sql
     * @param sql sql可以预先拼装
     * @param parMap  参数 可以不填
     * @return
     */
    public List<Object[]> findBySql(StringBuffer sql, Map<String, String> parMap);


    public Object findObjectBysql(StringBuffer sql, Map<String, String> parMap);
}
