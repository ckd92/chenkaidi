package com.fitech.account.dao.impl;

import com.fitech.account.dao.AccountBaseDao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by wupei on 2017/3/2.
 */
@Service
public class AccountBaseDaoImpl implements AccountBaseDao {

    @PersistenceContext
    public EntityManager em;

    public Page<Object[]> findPageBySql(StringBuffer sql, Map<String,String> parMap, int pageSize, int pageNo){
        Pageable pageable = new PageRequest(pageNo-1, pageSize);
        StringBuffer paramSb = this.getQueryParameters(parMap);
        if(paramSb!=null){
            sql=sql.append(paramSb);
        }
        Query query = em.createNativeQuery(sql.toString());
        //获取全部的
        int count = query.getResultList().size();
        em.clear();
        query = em.createNativeQuery(sql.toString());
        //计算当前开始的位置
        query.setFirstResult((pageNo-1)*pageSize);
        query.setMaxResults(pageSize);
        //分页查询的数据
        Page<Object[]> page = new PageImpl<>(query.getResultList(),pageable,count);
        em.close();
        return  page;
    }

    public List<Object[]> findBySql(StringBuffer sql, Map<String,String> parMap){
        StringBuffer paramSb = this.getQueryParameters(parMap);
        if(paramSb!=null){
            sql=sql.append(paramSb);
        }
        Query query = em.createNativeQuery(sql.toString());
        //获取全部的
        int count = query.getResultList().size();
        em.clear();
        query = em.createNativeQuery(sql.toString());
        //分页查询的数据
        List<Object[]>  resultList =  query.getResultList();
        em.close();
        return  resultList;
    }


    /**
     * 拼凑参数
     * @param paraMap
     * @return
     */
    protected StringBuffer getQueryParameters(Map paraMap) {
        StringBuffer stringBuffer  = null;
        if (paraMap != null && (!(paraMap.isEmpty()))) {
            stringBuffer = new StringBuffer();
            Iterator iter = paraMap.keySet().iterator();
            while (iter.hasNext()) {
                String paraName = (String) iter.next();
                Object value = paraMap.get(paraName);
                stringBuffer.append(" AND "+paraName+"='"+ value.toString()+"'");
            }
        }
        return stringBuffer;
    }


    @Override
    public Object findObjectBysql(StringBuffer sql, Map<String, String> parMap) {
        StringBuffer paramSb = this.getQueryParameters(parMap);
        if(paramSb!=null){
            sql=sql.append(paramSb);
        }
        Query query = em.createNativeQuery(sql.toString());
        //获取全部的
        List list = query.getResultList();
        return  list == null || list.isEmpty()?null :list.get(0);
    }
}
