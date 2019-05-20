package com.fitech.account.dao.impl;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fitech.account.dao.DictionaryDao;
import com.fitech.domain.account.*;
import com.fitech.domain.ledger.LedgerItem;
import com.fitech.enums.TableNameEnum;
import com.fitech.framework.lang.common.CommonConst;
import com.fitech.framework.lang.result.GenericResult;
import com.fitech.framework.lang.util.JdbcUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;

import com.fitech.account.dao.AccountDatasDao;
import com.fitech.constant.ExceptionCode;
import com.fitech.enums.SqlTypeEnum;
import com.fitech.framework.core.dao.Dao;
import com.fitech.framework.core.dao.mybatis.DaoMyBatis;
import com.fitech.framework.lang.common.AppException;
import com.fitech.framework.lang.util.ExcelUtil;
import com.fitech.framework.lang.util.StringUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;

@Dao
public class AccountDatasDaoImpl extends DaoMyBatis implements AccountDatasDao {
    @Autowired
    private DictionaryDao dictionaryDao;

    @Override
    public List<String> loadDataByTemplate(Long accountId,
                                           AccountTemplate accountTemplate, Sheet sheet, Account account) {
        List<String> resultList = new ArrayList<>();

        String tableName = accountTemplate.getTableName();
        //获取EXCEL表头
        List<String> columnHeaderlist = ExcelUtil.getColumnHeader(sheet);
        //获取EXCEL数据列
        List<List<String>> datas = ExcelUtil.getDatas(sheet, 2);
        Integer size = datas.size();
        //构建主键MAP
        HashMap<String, Object> pkMap = this.generatePKMap(columnHeaderlist, accountTemplate);

        // 头参数
        Map<String, Object> sqlMap = new HashMap<String, Object>();
        sqlMap.put("tablename", tableName);

        //组装批量导入SQL
        List<String> fields = new ArrayList<String>();
        Collection<AccountField> itemsf = accountTemplate.getAccountFields();
        Collection<AccountField> items = new ArrayList<AccountField>();
        for (int i = 0; i < columnHeaderlist.size(); i++) {
            fields.add(columnHeaderlist.get(i).trim());

            for (AccountField a : itemsf) {
                if (a.getItemCode().equals(columnHeaderlist.get(i).trim())) {
                    items.add(a);
                }
            }
        }
        sqlMap.put("fields", fields);
        sqlMap.put("id", accountId);

        //集合存放复合主键的值，用于对比是否有重复数据
        List<String> pkValueStr = new ArrayList<>();
        //通过主键判断数据是否有重复（联合主键）
        for (int i = 0; i < datas.size(); i++) {
            //存放主键的在columnHeaderlist中的index
            List<Integer> listFlag = new ArrayList<>();
            Iterator pkIterator = pkMap.keySet().iterator();
            if (pkIterator.hasNext()) {
                String pk = (String) pkIterator.next();
                //如果columnHeaderlist中有主键，记录其位置
                Integer index = columnHeaderlist.indexOf(pk);
                if (columnHeaderlist.indexOf(pk) != -1) {
                    listFlag.add(index);
                }
            }
            //取出一行值
            List<String> values = datas.get(i);
            //拼接每条数据的复合主键
            String str = "";
            for (int j = 0; j < listFlag.size(); j++) {
                str += values.get(listFlag.get(j));
            }
            //判断该复合主键是否存在，不存在加入pkValueStr，存在就返回提示
            if (StringUtil.isNotEmpty(str) && pkValueStr.contains(str)) {
                //失败返回重复数据的行号
                resultList.add("false");
                resultList.add(String.valueOf(i + 1 + 2));
                return resultList;
            } else {
                pkValueStr.add(str);
            }
        }


        List<Map<String, Object>> dataMap = new ArrayList<Map<String, Object>>();


        //递归数据行
        for (int i = 0; i < datas.size(); i++) {

            Map<String, Object> fieldMap = new HashMap<String, Object>();
            fieldMap.putAll(sqlMap);

            List<String> valueList = new ArrayList<String>();
            //待录入值
            List<String> values = datas.get(i);
            for (int k = 0; k < values.size(); k++) {
                AccountField field = null;
                try {
                    field = (AccountField) items.toArray()[k];
                } catch (IndexOutOfBoundsException e) {
                    throw new AppException(ExceptionCode.SYSTEM_ERROR, "请确认导入模板是否正确！或字典项字段功能暂未完善，请确认是否导入字典项内容");
                }

                if (pkMap.containsKey(field.getItemCode())) {
                    pkMap.put(field.getItemCode(), values.get(k));
                }
                if (values.get(k) != null) {
                    String tempValue = values.get(k);
                    if (StringUtil.isNotEmpty(field.getDicId()) && tempValue.indexOf("-") > -1) {
                        if (tempValue.indexOf("-") + 1 < tempValue.length()) {
                            tempValue = tempValue.substring(tempValue.indexOf("-") + 1, tempValue.length());
                        } else {
                            tempValue = "";
                        }
                    }

                    if (field.getSqlType().equals(SqlTypeEnum.VARCHAR)) {
                        valueList.add("'" + tempValue + "'");
                    } else if (field.getSqlType().equals(SqlTypeEnum.DATE)) {
                        valueList.add("".equals(tempValue) ? null : "to_date('" + tempValue + "','yyyy-mm-dd')");
                    } else if (field.getSqlType().equals(SqlTypeEnum.INTEGER) ||
                            field.getSqlType().equals(SqlTypeEnum.DECIMAL) ||
                            field.getSqlType().equals(SqlTypeEnum.DOUBLE) ||
                            field.getSqlType().equals(SqlTypeEnum.INT) ||
                            field.getSqlType().equals(SqlTypeEnum.BIGINT)) {
                        //valueList.add("".equals(tempValue)?null:tempValue);
                        //数字类型，若为空值则设置为0
                        if ("".equals(tempValue)) {
                            valueList.add("0");
                        } else {
                            valueList.add(tempValue);
                        }
                    } else {
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

        // 删除所有该台账所有补录数据
        Map<String, String> params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("reportId", String.valueOf(accountId));
        super.delete("accountDatasMapper.delete", params);
        try {
            // 批量新增
            super.batchInsert("accountDatasMapper.insert", dataMap);
        } catch (DataAccessException e) {
            if (e.getMessage().contains("ORA-12899")) {
                throw new AppException(ExceptionCode.SYSTEM_ERROR, "载入数据中存在超出模板定义长度的数据，请核实！");
            }
        }

        //成功返回加载条数
        resultList.add("true");
        resultList.add(String.valueOf(size));
        return resultList;
    }

    @Override
    public Map<String, Object> loadDataByExcel(Long accountId, AccountTemplate accountTemplate, Sheet sheet, Account account) {
        HashMap<String, Object> map = new HashMap<>();
        Connection con = null;
        PreparedStatement ps = null;
        JdbcUtil ju = null;
        CallableStatement call;
        String tableName = accountTemplate.getTableName();
        List<Long> selectList = null;
        try {
            ju = new JdbcUtil();
            con = ju.getConnection();
            con.setAutoCommit(true);

            //获取EXCEL表头
            List<String> columnHeaderlist = ExcelUtil.getColumnHeader(sheet);
            //获取EXCEL数据列
            List<List<String>> datas = ExcelUtil.getDatas(sheet, 2);
            //mapper传值集合
            Map<String, Object> sqlMap = new HashMap<String, Object>();
            //构建主键MAP
            HashMap<String, Object> pkMap = this.generatePKMap(columnHeaderlist, accountTemplate);

            //获取该报文所有字段集合
            List<String> fields = new ArrayList<String>();
            Collection<AccountField> itemsf = accountTemplate.getAccountFields();
            Collection<AccountField> items = new ArrayList<AccountField>();
            for (int i = 0; i < columnHeaderlist.size(); i++) {
                fields.add(columnHeaderlist.get(i).trim());

                for (AccountField a : itemsf) {
                    if (a.getItemCode().equals(columnHeaderlist.get(i).trim())) {
                        items.add(a);
                    }
                }
            }
            sqlMap.put("fields", fields);
            sqlMap.put("id", accountId);
            //判断字段集合是否有业务主键
            boolean havePkable = false;
            Iterator<AccountField> it = items.iterator();
            while (it.hasNext()) {
                AccountField field = it.next();
                if (field.isPkable()) {
                    havePkable = true;
                    break;
                }
            }
            if (datas.size() == 0) {
                map.put("flag", false);
                map.put("message", "载入空数据excel，请检查！");
                return map;
            }
            //清空临时表
//            ps = con.prepareStatement("truncate table " + tableName + "_" + TableNameEnum.STANDARD);
//            ps.executeUpdate();
//            con.commit();

            //判断是否存在业务主键，存在则往临时表中添加数据，不存在则直接往数据表中添加数据
            if (havePkable) {
                sqlMap.put("tablename", tableName + "_" + TableNameEnum.STANDARD);
            } else {
                sqlMap.put("tablename", tableName);
            }

            List<Map<String, Object>> dataMap = new ArrayList<Map<String, Object>>();
            //集合存放复合主键的值，用于对比是否有重复数据
            List<String> pkValueStr = new ArrayList<>();

            //查询原始改期代办中的所有数据的id
            Map<String, Object> hashMap = new HashMap<>();
            hashMap.put("reportId", accountId);
            hashMap.put("tableName", tableName);
            selectList = super.selectList("accountDatasMapper.findDataIdByReportId", hashMap);

            //循环赋值sql
            for (int i = 0; i < datas.size(); i++) {
                int x = 0;
                for (AccountField field : items) {
                    //判断主键字段载入数据是否为空值
                    if (field.isPkable() && StringUtils.isEmpty(datas.get(i).get(x))) {
                        map.put("flag", false);
                        map.put("message", "报表数据主键字段为空值，请检查载入数据");
                        return map;
                    }
                    x++;
                }
                //通过主键判断数据是否有重复（联合主键）
                //存放主键的在columnHeaderlist中的index
                List<Integer> listFlag = new ArrayList<>();
                //适用于联合主键
                for (String s : pkMap.keySet()) {
                    //存在主键，记录位置
                    int index = columnHeaderlist.indexOf(s);
                    if (index != -1) {
                        listFlag.add(index);
                    }
                }
                //取出一行值
                List<String> values = datas.get(i);
                //拼接每条数据的复合主键
                String str = "";
                for (int j = 0; j < listFlag.size(); j++) {
                    str = str + "-" + values.get(listFlag.get(j));
                }
                //判断该复合主键是否存在，不存在加入pkValueStr，存在就返回提示
                if (StringUtil.isNotEmpty(str) && pkValueStr.contains(str)) {
                    //失败返回重复数据的行号
                    map.put("flag", false);
                    map.put("message", "载入的第" + String.valueOf(i + 1 + 2) + "行主键冲突");
                    return map;
                } else {
                    pkValueStr.add(str);
                }
                // 检查字段类型中的日期格式和字典值
                for (AccountField item : items) {
                    if (item instanceof DateField) {
                        try {
                            String value = values.get(((List<AccountField>) items).indexOf(item));
                            if (StringUtils.isNotEmpty(value)) {
                                if (!formulaStringToDate(value)) {
                                    map.put("flag", false);
                                    map.put("message", "日期格式字段" + item.getItemName() + "载入非日期格式数据！");
                                    return map;
                                }
                            } else {
                                values.set(((List<AccountField>) items).indexOf(item), null);
                            }
                        } catch (Exception e) {
                            map.put("flag", false);
                            map.put("message", "日期格式字段" + item.getItemName() + "载入非日期格式数据！");
                            return map;
                        }
                    } else if (item instanceof CodeField) {
                        List<Map<String, Object>> list = dictionaryDao.getDictionaryItemByDictionaryId(Long.parseLong(item.getDicId()));
                        Map<String, String> strings = new HashMap<>();
                        for (Map<String, Object> objectMap : list) {
                            strings.put((String) objectMap.get("DICITEMNAME"),(String) objectMap.get("DICITEMID"));
                        }
                        String dicitemName = values.get(((List<AccountField>) items).indexOf(item));
                        if (StringUtil.isNotEmpty(dicitemName) && strings.size() > 0) {
                            if (strings.get(dicitemName) == null) {
                                map.put("flag", false);
                                map.put("message", "字典类型字段【" + item.getItemName() + "】载入非字典数据！");
                                return map;
                            } 
                            /*else {
                                replaceAll(valueList, "'" + dicitemName + "'", "'" + strings.get(dicitemName) + "'");
                            }*/
                        }
                    }
                }

                Map<String, Object> fieldMap = new HashMap<String, Object>();
                fieldMap.putAll(sqlMap);
                List<String> valueList = new ArrayList<String>();
                //待录入值
                //List<String> values = datas.get(i);
                for (int k = 0; k < values.size(); k++) {
                    AccountField field = null;
                    try {
                        field = (AccountField) items.toArray()[k];
                    } catch (IndexOutOfBoundsException e) {
                        throw new AppException(ExceptionCode.SYSTEM_ERROR, "请确认导入模板是否正确！或字典项字段功能暂未完善，请确认是否导入字典项内容");
                    }

                    if (StringUtil.isNotEmpty(values.get(k))) {
                        String tempValue = values.get(k);
                        if (StringUtil.isNotEmpty(field.getDicId()) && tempValue.indexOf("-") > -1) {
                            if (tempValue.indexOf("-") + 1 < tempValue.length()) {
                                tempValue = tempValue.substring(tempValue.indexOf("-") + 1, tempValue.length());
                            } else {
                                tempValue = "";
                            }
                        }

                        if (field.getSqlType().equals(SqlTypeEnum.VARCHAR)) {
                        	if(field instanceof CodeField){
                        		List<Map<String, Object>> list = dictionaryDao.getDictionaryItemByDictionaryId(Long.parseLong(field.getDicId()));
                                Map<String, String> strings = new HashMap<>();
                                for (Map<String, Object> objectMap : list) {
                                    strings.put((String) objectMap.get("DICITEMNAME"),(String) objectMap.get("DICITEMID"));
                                }
                                valueList.add("'" + strings.get(tempValue) + "'");
                                
                            }else{
                            	valueList.add("'" + tempValue + "'");
                            }
                            
                        } else if (field.getSqlType().equals(SqlTypeEnum.DATE)) {
                            if ("com.mysql.jdbc.Driver".equals(CommonConst.getProperties("jdbc.driverClassName"))){
                                valueList.add("".equals(tempValue) ? null : "str_to_date('" + tempValue + "','%Y-%m-%d')");
                            }else{
                                valueList.add("".equals(tempValue) ? null : "to_date('" + tempValue + "','yyyy-mm-dd')");
                            }
                        } else if (field.getSqlType().equals(SqlTypeEnum.INTEGER) ||
                                field.getSqlType().equals(SqlTypeEnum.DECIMAL) ||
                                field.getSqlType().equals(SqlTypeEnum.DOUBLE) ||
                                field.getSqlType().equals(SqlTypeEnum.INT) ||
                                field.getSqlType().equals(SqlTypeEnum.BIGINT)) {
                            //valueList.add("".equals(tempValue)?null:tempValue);
                            //数字类型，若为空值则设置为0
                            if ("".equals(tempValue)) {
                                valueList.add("0");
                            } else {
                                valueList.add(tempValue);
                            }
                        }else {
                            valueList.add(tempValue);
                        }
                        field.setValue(tempValue);
                    } else {
                        valueList.add("");
                    }
                }
                
                // 赋值
                fieldMap.put("values", valueList);
                dataMap.add(fieldMap);
            }

            // 批量新增
            super.batchInsert("accountDatasMapper.insert", dataMap);
            Boolean testPrint = true;
            if (havePkable) {
                //调用同步修改数据的存储过程
                if ("com.mysql.jdbc.Driver".equals(CommonConst.getProperties("jdbc.driverClassName"))) {
                    Map sqlMapProc = new HashMap();
                    sqlMapProc.put("accountTemplateId", accountTemplate.getId().toString());
                    sqlMapProc.put("tableName", tableName);
                    sqlMapProc.put("result",Types.VARCHAR);
                    try {
                        super.selectList("accountDatasMapper.proc_loadAccountData", sqlMapProc);
                    } catch (Exception e) {
                        e.printStackTrace();
                        testPrint = false;
                    }
                } else {
                    call = con.prepareCall("call PROC_LOADACCOUNTDATA(?,?,?)");
                    call.setString(1, accountTemplate.getId().toString());
                    call.setString(2, tableName);
                    call.registerOutParameter(3, Types.VARCHAR);
                    call.execute();
                    String s = call.getString(3);
                    if (s.equals("false")) {
                        testPrint = false;
                    }
                }
            }
            if (!testPrint) {
                map.put("flag", false);
                map.put("message", "执行存储过程出错！");
            } else {
                map.put("flag", true);
                map.put("message", "载入成功");
                map.put("size", datas.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> hashMap = new HashMap<>();
            hashMap.put("reportId", accountId);
            hashMap.put("tableName", tableName);
            hashMap.put("list", selectList);
            super.delete("accountDatasMapper.deleteErrorData", hashMap);
            hashMap.put("tableName", tableName + "_STANDARD");
            super.delete("accountDatasMapper.deleteErrorData", hashMap);
            map.put("flag", false);
            String str = e.getMessage().toString();//异常信息
            if (str.contains("ORA-12899")) {
                str = str.substring(str.lastIndexOf(".") + 1, str.length() - 1).replace("\"", "");
            } else if (str.contains("ORA-01722")) {
                str = str.substring(str.indexOf(":") + 1);
            } else if (str.contains("ORA-00904")) {
                str = "载入数据存在空值，请核验后重新载入！";
            } else if (str.contains("ORA-06576")) {
                str = "不是有效的函数或过程名,请检查数据同步脚本";
            } else if (str.contains("ORA-01861")) {
                str = "载入非法日期类型数据";
            } else {
                str = "载入数据异常，请核验后重新载入";
            }
            map.put("message", str);
        } finally {
            ju.releaseConn();
            if (ps != null) {
                try {
                    ps.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return map;
    }


    public static <E> void replaceAll(List<E> list, E oldObject, E newObject) {
        for (int i = 0; i < list.size(); i++) {        //遍历
            if (oldObject.equals(list.get(i))) {        //如果list中存在与oldObject相同的值，则用newObject替换
                list.set(i, newObject);                //设置索引为i的值为newObject
            }
        }
    }

    /**
     * 判断字符串是否可以转为标准日期格式
     *
     * @param value
     * @return
     */
    public static Boolean formulaStringToDate(String value) {
        String[] formulas = new String[]{"yyyyMMdd", "yyyy-MM-dd", "yyyy/MM/dd", "yyyy.MM.dd"};
        Boolean flag = false;
        Date date = null;
        if (StringUtils.isEmpty(value)) {
            return false;
        }
        for (String formula : formulas) {
            try {
                date = new SimpleDateFormat(formula).parse(value);
                if (date != null) {
                    flag = true;
                    break;
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }
        }
        return flag;
    }

    /**
     * 构建主键MAP
     *
     * @param columnHeaderlist
     * @return
     */
    private HashMap<String, Object> generatePKMap(List<String> columnHeaderlist, AccountTemplate accountTemplate) {
        HashMap<String, Object> keys = new LinkedHashMap<>();

        for (int i = 0; i < columnHeaderlist.size(); i++) {
            Collection<AccountField> items = accountTemplate.getAccountFields();
            for (AccountField accountField : items) {
                if (accountField.getItemCode().equals(columnHeaderlist.get(i)) && accountField.isPkable()) {
                    keys.put(accountField.getItemCode(), null);
                }
            }
        }
        return keys;
    }

}
