package com.fitech.account.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Sheet;

import com.fitech.account.dao.AccountDatasDao;
import com.fitech.constant.ExceptionCode;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.enums.SqlTypeEnum;
import com.fitech.framework.core.dao.Dao;
import com.fitech.framework.core.dao.mybatis.DaoMyBatis;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.util.ExcelUtil;
import com.fitech.framework.lang.util.StringUtil;

@Dao
public class AccountDatasDaoImpl extends DaoMyBatis implements AccountDatasDao {

	@Override
	public List<String> loadDataByTemplate(Long accountId,
			AccountTemplate accountTemplate, Sheet sheet, Account account) {
		List<String> resultList = new ArrayList<>();

    	String tableName = accountTemplate.getTableName();
        //获取EXCEL表头
        List<String> columnHeaderlist = ExcelUtil.getColumnHeader(sheet);
        //获取EXCEL数据列
        List<List<String>> datas = ExcelUtil.getDatas(sheet,2);
        Integer size = datas.size();
        //构建主键MAP
        HashMap<String, Object> pkMap = this.generatePKMap(columnHeaderlist, accountTemplate);

        // 头参数
        Map<String,Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("tablename", tableName);
        
        //组装批量导入SQL
        List<String> fields = new ArrayList<String>();
        Collection<AccountField> itemsf = accountTemplate.getAccountFields();
        Collection<AccountField> items=new ArrayList<AccountField>();
        for (int i = 0; i < columnHeaderlist.size(); i++) {
        	fields.add(columnHeaderlist.get(i).trim());
        	
            for(AccountField a:itemsf){
            	if(a.getItemCode().equals(columnHeaderlist.get(i).trim())){
            		items.add(a);
            	}
            }
        }
        sqlMap.put("fields", fields);
        sqlMap.put("id", accountId);

        //集合存放复合主键的值，用于对比是否有重复数据
        List<String> pkValueStr = new ArrayList<>();
        //通过主键判断数据是否有重复（联合主键）
        for( int i = 0; i < datas.size(); i++ ){
            //存放主键的在columnHeaderlist中的index
            List<Integer> listFlag = new ArrayList<>();
            Iterator pkIterator = pkMap.keySet().iterator();
            if( pkIterator.hasNext() ){
                String pk = (String)pkIterator.next();
                //如果columnHeaderlist中有主键，记录其位置
                Integer index = columnHeaderlist.indexOf( pk );
                if(columnHeaderlist.indexOf( pk ) != -1){
                    listFlag.add( index );
                }
            }
            //取出一行值
            List<String> values = datas.get(i);
            //拼接每条数据的复合主键
            String str = "";
            for(int j=0;j<listFlag.size();j++){
                str += values.get(listFlag.get(j));
            }
            //判断该复合主键是否存在，不存在加入pkValueStr，存在就返回提示
            if(StringUtil.isNotEmpty(str) && pkValueStr.contains(str)){
                //失败返回重复数据的行号
                resultList.add("false");
                resultList.add(String.valueOf(i+1+2));
                return resultList;
            }else{
                pkValueStr.add(str);
            }
        }
        
        
        List<Map<String,Object>> dataMap = new ArrayList<Map<String,Object>>();
        
        
        //递归数据行
        for (int i = 0; i < datas.size(); i++) {
        	
        	Map<String,Object> fieldMap = new HashMap<String, Object>();
        	fieldMap.putAll(sqlMap);
        	
        	List<String> valueList = new ArrayList<String>(); 
            //待录入值
            List<String> values = datas.get(i);
            for (int k = 0; k < values.size(); k++) {
                AccountField field = null;
                try{
                	field = (AccountField) items.toArray()[k];
            	}catch(IndexOutOfBoundsException e){
            		throw new AppException(ExceptionCode.SYSTEM_ERROR, "请确认导入模板是否正确！或字典项字段功能暂未完善，请确认是否导入字典项内容");
            	}
                
                if (pkMap.containsKey(field.getItemCode())) {
                    pkMap.put(field.getItemCode(), values.get(k));
                }
                if (values.get(k) != null) {
                	String tempValue = values.get(k);
                	if(StringUtil.isNotEmpty(field.getDicId()) && tempValue.indexOf("-") >-1 ){
                		if(tempValue.indexOf("-")+1 < tempValue.length()){
                			tempValue = tempValue.substring(tempValue.indexOf("-")+1, tempValue.length());
                		}else{
                			tempValue = "";
                		}
                	}
                	
                    if (field.getSqlType().equals(SqlTypeEnum.VARCHAR)) {
                        valueList.add("'"+tempValue+"'");
                    } else if(field.getSqlType().equals(SqlTypeEnum.DATE)) {
                    	valueList.add("".equals(tempValue)?null:"to_date('" + tempValue + "','yyyy-mm-dd')");
                    }else if(field.getSqlType().equals(SqlTypeEnum.INTEGER)||
                    		field.getSqlType().equals(SqlTypeEnum.DECIMAL)||
                    		field.getSqlType().equals(SqlTypeEnum.DOUBLE)||
                    		field.getSqlType().equals(SqlTypeEnum.INT)||
                    		field.getSqlType().equals(SqlTypeEnum.BIGINT)) {
                    	valueList.add("".equals(tempValue)?null:tempValue);
                    }else{
                    	valueList.add(tempValue);
                    }
                    field.setValue(tempValue);
                } else {
                    valueList.add(null);
                }
            }
            // 赋值
            fieldMap.put("values", valueList);
            dataMap.add(fieldMap);
        }
        
        // 所有该台账所有补录数据
        super.delete("accountDatasMapper.delete",tableName);
        // 批量新增
        super.batchInsert("accountDatasMapper.insert", dataMap);
        
        //成功返回加载条数
        resultList.add("true");
        resultList.add(String.valueOf(size));
        return resultList;
	}

	
	/**
     * 构建主键MAP
     * @param columnHeaderlist
     * @return
     */
    private HashMap<String, Object> generatePKMap(List<String> columnHeaderlist,AccountTemplate accountTemplate) {
        HashMap<String, Object> keys  = new LinkedHashMap<>();

        for (int i = 0; i < columnHeaderlist.size(); i++) {
            Collection<AccountField> items = accountTemplate.getAccountFields();
            for (AccountField accountField : items) {
				if(accountField.getItemCode().equals(columnHeaderlist.get(i)) && accountField.isPkable()){
					keys.put(accountField.getItemCode(),null);
				}
			}
        }
        return keys;
    }
}
