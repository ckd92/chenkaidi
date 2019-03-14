package com.fitech.account.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fitech.framework.core.dao.mybatis.DaoMyBatis;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fitech.account.dao.AccountTemplateDAO;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.enums.TableNameEnum;

/**
 * Created by SunBojun on 2017/2/28.
 */
@Service("accountTemplateDAO")
public class AccountTemplateDAOImpl extends DaoMyBatis implements AccountTemplateDAO {

    public void createTemplate(AccountTemplate accountTemplate) {
    	String tableName = accountTemplate.getTableName().toUpperCase();
        String standard = tableName + "_" + TableNameEnum.STANDARD.getTableName().toUpperCase();
        createTemplate(tableName, accountTemplate);
        createTemplate(standard, accountTemplate);
    }

    /**
     * 初始化数据表结构
     *
     * @param
     * @param accountTemplate
     */
   /* private void createSQL(StringBuffer sql, AccountTemplate accountTemplate) {
        sql.append("ID integer NOT NULL PRIMARY KEY,reportId integer NOT NULL,");
        Collection<AccountField> items = new ArrayList<>();
        items = accountTemplate.getAccountFields();
        if (items != null) {
            for (AccountField item : items) {
                //如果是VARCHAR和codeLib类型需要字段添加长度
                sql.append(item.getItemCode() + " ");
                if (item.getSqlType() != null) {
                    if ("VARCHAR".equals(item.getItemType())) {
                        sql.append(item.getSqlType() + "("+item.getLength()+"),");
                    } else if ("CODELIB".equals(item.getItemType())) {
                        sql.append(item.getSqlType() + "(255),");
                    } else if ("DOUBLE".equals(item.getItemType()) || "DECIMAL".equals(item.getItemType())) {
                        sql.append("Number(18,4),");
                    } else {
                        sql.append(item.getSqlType().toString() + ",");
                    }
                } else {
                    if ("VARCHAR".equals(item.getItemType())) {
                        sql.append("VARCHAR(255),");
                    } else if ("CODELIB".equals(item.getItemType())) {
                        sql.append("VARCHAR(255),");
                    } else {
                        sql.append(item.getItemType().toString() + ",");
                    }
                }
            }
        }
    }*/
    
    private void createTemplate(String tableName,AccountTemplate accountTemplate){
//    	StringBuffer sql = new StringBuffer();
//    	sql.append("CREATE TABLE " + tableName + "(");
//    	this.createSQL(sql, accountTemplate);
//    	sql.append(" syncAble VARCHAR(50),check_status VARCHAR(20))");

    	Map<String,Object> parameterMap = new HashMap<>();
    	parameterMap.put("tableName",tableName);
        Collection<AccountField> itemList = accountTemplate.getAccountFields();
    	parameterMap.put("itemList",itemList);
    	super.update("accountTemplateMapper.createTemplate",parameterMap);

    	//this.getNamedParameterJdbcTemplate().update(sql.toString(), new HashMap<String, Object>());
    }

    @Transactional
    public void dropTemplate(AccountTemplate accountTemplate) {
        String tableName = accountTemplate.getTableName();
        int count = super.selectOne("accountTemplateMapper.dropTemplateSql1",tableName);
        if (count > 0) {
            super.update("accountTemplateMapper.dropTemplateSql2",tableName);
        }
    }

    @Override
    public Boolean isDeleteAble(Long id) {
        Boolean flag = true;
        long count = super.selectOne("accountTemplateMapper.isDeleteAble",id);
        if(count != 0){
            flag = false;
        }
        return flag;
    }

    @Override
    public Boolean dataIsExist(AccountTemplate accountTemplate) {
        Boolean flag = false;
        String tableName = accountTemplate.getTableName();
        long count = super.selectOne("accountTemplateMapper.dataIsExist",tableName);
        if(count != 0){
            flag = true;
        }
        return flag;
    }

    @Override
    public void dropAllTemplate(AccountTemplate accountTemplate) {
        String tableName = accountTemplate.getTableName().toUpperCase();
        String standard = tableName + "_" + TableNameEnum.STANDARD.getTableName().toUpperCase();
        dropTemplate(tableName);
        dropTemplate(standard);
    }

    private void dropTemplate(String tableName){
        long count = super.selectOne("accountTemplateMapper.dropTemplateSql1",tableName);
        if(count > 0 ){
            super.update("accountTemplateMapper.dropTemplateSql2",tableName);
        }
    }

    public List<Map<String,Object>> getAllDate(AccountTemplate accountTemplate,Long id){

        Map<String,Object> parameterMap = new HashMap();
        StringBuffer selectColumns = new StringBuffer();
    	for(AccountField accountField:accountTemplate.getAccountFields()){
    		if(accountField.isVisible()){
                selectColumns.append(","+accountField.getItemCode());
    		}
    	}
        parameterMap.put("selectColumns",selectColumns);
    	parameterMap.put("templateCode",accountTemplate.getTemplateCode());
    	parameterMap.put("id",id);
    	List<Map<String,Object>> list = super.selectList("accountTemplateMapper.getAllDate",parameterMap);
    	return list;
    }
}
