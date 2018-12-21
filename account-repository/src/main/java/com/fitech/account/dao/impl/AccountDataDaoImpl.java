package com.fitech.account.dao.impl;

import java.sql.SQLType;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.fitech.framework.core.dao.mybatis.DaoMyBatis;
import com.fitech.framework.lang.page.Page;
import org.omg.CORBA.Object;
import com.fitech.account.dao.AccountDataDao;
import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountLine;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.account.CodeField;
import com.fitech.domain.account.DateField;
import com.fitech.domain.account.DoubleField;
import com.fitech.domain.account.IntegerField;
import com.fitech.enums.SqlTypeEnum;
import com.fitech.framework.lang.util.StringUtil;
import com.fitech.vo.account.AccountProcessVo;
import org.springframework.stereotype.Service;

@Service
public class AccountDataDaoImpl extends DaoMyBatis implements AccountDataDao {

    @Override
    public List<AccountLine> findDataByCondition(AccountProcessVo accountProcessVo, Page page) {
        Account account = accountProcessVo.getAccount();

        AccountTemplate accountTemplate = account.getAccountTemplate();

        Collection<AccountField> collection = accountTemplate.getAccountFields();

        //sql参数列表
        Map sqlParameterMap = new HashMap();
        sqlParameterMap.put("collection",collection);
        sqlParameterMap.put("tableName",accountTemplate.getTableName());
        sqlParameterMap.put("accountId",account.getId());
        sqlParameterMap.put("serachFileds",account.getAccountSearchs());


        List<String> list = new ArrayList<>();
//        StringBuffer sql = new StringBuffer();
//        sql.append("SELECT * FROM " +
//                "     (SELECT A.*, rownum r " +
//                "       FROM " +
//                "          (");
//        sql.append("select id,reportId,");
        list.add("id");
        list.add("reportId");
//        for (AccountField item : collection) {
//            sql.append(item.getItemCode() + ",");
//            list.add(item.getItemCode());
//        }
//        sql.deleteCharAt(sql.length() - 1);
//        sql.append(" from " + accountTemplate.getTableName() + " where reportId=" + account.getId() + "  ");
        //没有查询条件 查询所有

        accountTemplate.setAccountFields(null);
        Collection<AccountField> serachFileds = account.getAccountSearchs();
        List<AccountField> fileds = (List<AccountField>)sqlParameterMap.get("serachFileds");
        if (serachFileds != null){
            for (AccountField filed : fileds) {
                for (AccountField serachFiled : serachFileds) {
                    if (filed.getItemCode().equals(serachFiled.getItemCode())){
                        filed.setValue(serachFiled.getValue());
                    }
                }
            }
        }

        //字段类型
        Map<String,List<String>> itemInstanceMap = new HashMap<>();
        List<String> integerFieldAndDoubleFieldList = new ArrayList<>();
        List<String> codeFieldList = new ArrayList<>();
        if (null != serachFileds && !serachFileds.isEmpty()) {
            //将itemtype赋值
            for (AccountField afl : serachFileds) {
                for (AccountField afd : collection) {
                    if (afl.getItemCode().equals(afd.getItemCode()) && "DATE".equals(afd.getItemType()) && (!"".equals(afl.getValue()))) {
                        afl.setItemType(afd.getItemType());
                    }
                }
            }
            //进行循环

            for (AccountField item : serachFileds) {
                String code = item.getItemCode();
                if (item.getValue() != null) {
                    //sql.append("and " + code);
                    //IntegerField和DoubleField类型itemCode集合
                    if (item instanceof IntegerField || item instanceof DoubleField) {
                        integerFieldAndDoubleFieldList.add(code);
                        //sql.append(" = " + item.getValue() + " ");
                    } else if (item instanceof CodeField) {
                        codeFieldList.add(code);
                        //sql.append(" = '" + item.getValue() + "' ");
                    } else if ("DATE".equals(item.getItemType())) {
                        //sql.append(" = to_date('" + item.getValue() + "','yyyy-mm-dd') ");
                    } else {
                        //sql.append(" like '%" + item.getValue() + "%' ");
                    }
                }
            }
            itemInstanceMap.put("integerFieldAndDoubleFieldList",integerFieldAndDoubleFieldList);
            itemInstanceMap.put("codeFieldList",codeFieldList);
        }


        sqlParameterMap.put("itemInstanceMap",itemInstanceMap);

        accountTemplate.setAccountFields(collection);
//        StringBuffer totalsum = new StringBuffer();
//        totalsum.append("with a as ( \n");
//        totalsum.append(sql);
//        totalsum.append(" ) A) \n");
//        totalsum.append(")select count(*) from a ");
//
//        sql.append(") A" +
//                "       WHERE rownum <= " + (accountProcessVo.getPageNum() * accountProcessVo.getPageSize()) +
//                "     ) B " +
//                "         WHERE r >= " + ((accountProcessVo.getPageNum() - 1) * accountProcessVo.getPageSize() + 1));
        Map<String, Object> map1 = new HashMap<>();
        List<Map<String, Object>> resultList = super.selectByPage("accountDataMapper.findDataByConditionCount","accountDataMapper.findDataByCondition",sqlParameterMap,page);
//        System.out.println(sql.toString());
//        int totalList = this.getNamedParameterJdbcTemplate().queryForObject(
//                totalsum.toString(), map1, Integer.class);
        List<AccountLine> lineList = new ArrayList<>();
        for (Map<String, Object> ledgerLineMap : resultList) {
            AccountLine ledgerLine = new AccountLine();
            Collection<AccountField> set = new ArrayList<>();
            for (String s : ledgerLineMap.keySet()) {
                AccountField item = new AccountField();
                if ("id".equals(s)) {
                    ledgerLine.setId(Long.parseLong(String.valueOf(ledgerLineMap.get(s))));
                    continue;
                }
                item.setItemCode(s);
                String value = String.valueOf(ledgerLineMap.get(s));
                if (value.indexOf("\"") != -1) {
                    value = value.replaceAll("\"", "&#34;");
                    item.setValue(value);
                } else if (value.indexOf("\t") != -1) {
                    value = value.replaceAll("\t", "");
                    item.setValue(value);
                } else {
                    item.setValue(ledgerLineMap.get(s));
                }
                set.add(item);
            }
            ledgerLine.setAccountFields(set);
            lineList.add(ledgerLine);
        }
        //将sqltype从template复制到accountline
        for (AccountLine a : lineList) {
            for (AccountField af : a.getAccountFields()) {
                for (AccountField ac : collection) {
                    if (af.getItemCode().equals(ac.getItemCode())) {
                        af.setSqlType(ac.getSqlType());
                        af.setItemType(ac.getItemType());
                    }
                }
            }
        }
//        long totalNum = findMaxNumDataByCondition(accountProcessVo);
//        Pageable pageable = new PageRequest(accountProcessVo.getPageNum() - 1, accountProcessVo.getPageSize());
//        List<AccountLine> ledgerLinePage = new PageImpl<>(lineList, pageable, totalList);
//        account.setAccountLine(ledgerLinePage);

        return lineList;
    }

    //用于下载
    @Override
    public List<AccountLine> downLoadDataByCondition(AccountProcessVo accountProcessVo) {
        Account account = accountProcessVo.getAccount();

        AccountTemplate accountTemplate = account.getAccountTemplate();

        Collection<AccountField> collection = accountTemplate.getAccountFields();

        //sql参数列表
        Map sqlParameterMap = new HashMap();
        sqlParameterMap.put("collection",collection);
        sqlParameterMap.put("tableName",accountTemplate.getTableName());
        sqlParameterMap.put("accountId",account.getId());
        sqlParameterMap.put("serachFileds",account.getAccountSearchs());

        List<String> list = new ArrayList<>();
        //StringBuffer sql = new StringBuffer();
        //sql.append("select id,reportId,");
        list.add("id");
        list.add("reportId");
        for (AccountField item : collection) {
            if (item.getItemType().equals("DATE")) {
                //sql.append("to_date(to_char("+item.getItemCode()+", 'yyyy-MM-dd')"+",'yyyy-mm-dd') as "+item.getItemCode()+""+ ",");
                list.add(item.getItemCode());
            } else {
                //sql.append(item.getItemCode() + ",");
                list.add(item.getItemCode());
            }
        }
        //sql.deleteCharAt(sql.length() - 1);
        //sql.append(" from " + accountTemplate.getTableName() + " where reportId=" + account.getId() + "  ");
        //没有查询条件 查询所有

        accountTemplate.setAccountFields(null);
        Collection<AccountField> serachFileds = account.getAccountSearchs();

        //字段类型
        Map<String,List<String>> itemInstanceMap = new HashMap<>();
        List<String> integerFieldAndDoubleFieldList = new ArrayList<>();
        List<String> codeFieldList = new ArrayList<>();

        if (null != serachFileds && !serachFileds.isEmpty()) {
            //将itemtype赋值
            for (AccountField afl : serachFileds) {
                for (AccountField afd : collection) {
                    if (afl.getItemCode().equals(afd.getItemCode()) && "DATE".equals(afd.getItemType()) && (!"".equals(afl.getValue()))) {
                        afl.setItemType(afd.getItemType());
                    }
                }
            }
            //进行循环
            for (AccountField item : serachFileds) {
                String code = item.getItemCode();
                if (item.getValue() != null) {
                    //sql.append("and " + code);
                    if (item instanceof IntegerField || item instanceof DoubleField) {
                        integerFieldAndDoubleFieldList.add(code);
                        //sql.append(" = " + item.getValue() + " ");
                    } else if (item instanceof CodeField) {
                        codeFieldList.add(code);
                        //sql.append(" = '" + item.getValue() + "' ");
                    } /*else if ("DATE".equals(item.getItemType())) {
                        //sql.append(" = to_date('" + item.getValue() + "','yyyy-mm-dd') ");
                    } else {
                        //sql.append(" like '%" + item.getValue() + "%' ");
                    }*/
                }
            }
            itemInstanceMap.put("integerFieldAndDoubleFieldList",integerFieldAndDoubleFieldList);
            itemInstanceMap.put("codeFieldList",codeFieldList);
        }
        sqlParameterMap.put("itemInstanceMap",itemInstanceMap);

        accountTemplate.setAccountFields(collection);

        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map1 = new HashMap<>();
        List<Map<String, Object>> resultList = super.selectList("accountDataMapper.downLoadDataByCondition",sqlParameterMap);
        List<AccountLine> lineList = new ArrayList<>();
        for (Map<String, Object> ledgerLineMap : resultList) {
            AccountLine ledgerLine = new AccountLine();
            Collection<AccountField> set = new ArrayList<>();
            for (String s : ledgerLineMap.keySet()) {
                AccountField item = new AccountField();
                if ("id".equals(s)) {
                    ledgerLine.setId(Long.parseLong(String.valueOf(ledgerLineMap.get(s))));
                    continue;
                }
                item.setItemCode(s);
                String value = String.valueOf(ledgerLineMap.get(s));
                if (value.indexOf("\"") != -1) {
                    value = value.replaceAll("\"", "&#34;");
                    item.setValue(value);
                } else if (value.indexOf("\t") != -1) {
                    value = value.replaceAll("\t", "");
                    item.setValue(value);
                } else {
                    item.setValue(ledgerLineMap.get(s));
                }
                set.add(item);
            }
            ledgerLine.setAccountFields(set);
            lineList.add(ledgerLine);
        }
        //将sqltype从template复制到accountline
        for (AccountLine a : lineList) {
            for (AccountField af : a.getAccountFields()) {
                for (AccountField ac : collection) {
                    if (af.getItemCode().equals(ac.getItemCode())) {
                        af.setSqlType(ac.getSqlType());
                        af.setItemType(ac.getItemType());
                    }
                }
            }
        }
//        long totalNum = findMaxNumDataByCondition(accountProcessVo);

        return lineList;
    }

    @Override
    public Long findMaxNumDataByCondition(AccountProcessVo accountProcessVo) {
        Account account = accountProcessVo.getAccount();

        AccountTemplate accountTemplate = account.getAccountTemplate();

        Collection<AccountField> collection = accountTemplate.getAccountFields();

        Collection<AccountField> serachFileds = account.getAccountSearchs();



        //将搜索值赋给模板
        if (!serachFileds.isEmpty()) {
            for (AccountField aff : collection) {
                for (AccountField afs : serachFileds) {
                    if (aff.getItemCode().equals(afs.getItemCode()) && (!"".equals(afs.getValue())) && null != afs.getValue()) {
                        aff.setValue(afs.getValue());
                    }
                }
            }
        }

        //字段类型
        Map<String,List<String>> itemInstanceMap = new HashMap<>();
        List<String> integerFieldAndDoubleFieldList = new ArrayList<>();
        List<String> codeFieldList = new ArrayList<>();

        //没有查询条件 查询所有
        for (AccountField item : collection) {
            String code = item.getItemCode();
            if (item.getValue() != null) {
                //sql.append("and " + code);
                if (item instanceof IntegerField || item instanceof DoubleField) {
                    integerFieldAndDoubleFieldList.add(code);
                    //sql.append(" = " + item.getValue() + " ");
                } else if (item instanceof CodeField) {
                    codeFieldList.add(code);
                    //sql.append(" = '" + item.getValue() + "' ");
                } else if (item instanceof DateField) {
                    //sql.append("to_date('" + item.getValue() + "','yyyy-mm-dd')");
                } else {
                    //sql.append(" like '%" + item.getValue() + "%' ");
                }
            }
        }
        //sql参数列表
        Map sqlParameterMap = new HashMap();
        sqlParameterMap.put("collection",collection);
        sqlParameterMap.put("tableName",accountTemplate.getTableName());
        sqlParameterMap.put("accountId",account.getId());


        Long totalNum = super.selectOne("accountDataMapper.findMaxNumDataByCondition",sqlParameterMap);

        return totalNum;
    }

    @Override
    public void insertData(AccountLine accountLine, Account account) {
        Map sqlParameterMap = new HashMap();
        sqlParameterMap.put("tableName", account.getAccountTemplate().getTableName() );
        sqlParameterMap.put("accountId",account.getId());

//        String sql = "insert into " + account.getAccountTemplate().getTableName() + "(";
        Collection<AccountField> items = accountLine.getAccountFields();
        sqlParameterMap.put("items",items);
        Collection<AccountField> itemegs = account.getAccountTemplate().getAccountFields();
        for (AccountField af : items) {
            for (AccountField ag : itemegs) {
                if (af.getItemCode().equals(ag.getItemCode())) {
                    af.setSqlType(ag.getSqlType());
                }
            }
        }
        super.insert("accountDataMapper.insertData",sqlParameterMap);

    }

    @Override
    public void updateData(AccountLine accountLine, Account account) {
        Map sqlParameterMap = new HashMap();
        sqlParameterMap.put("tableName", account.getAccountTemplate().getTableName() );
        sqlParameterMap.put("accountLineId",accountLine.getId());

//        String sql = "update " + account.getAccountTemplate().getTableName() + " set ";
        Collection<AccountField> items = accountLine.getAccountFields();
        Collection<AccountField> itemegs = account.getAccountTemplate().getAccountFields();
        for (AccountField af : items) {
            for (AccountField ag : itemegs) {
                if (af.getItemCode().equals(ag.getItemCode())) {
                    af.setSqlType(ag.getSqlType());
                    if(af.getSqlType().equals(SqlTypeEnum.DATE)){
                         if(!af.getValue().toString().contains("-")){
                             af.setValue((new SimpleDateFormat("yyyy-MM-dd")).format(Long.parseLong(String.valueOf(af.getValue()))));
                         }else if(af.getValue().toString().equals("NaN-aN-aN")){
                        	 af.setValue(null);
                         }
                    }
                }
            }
        }
        sqlParameterMap.put("items",items);
//        if (items.size() > 0) {
//            for (AccountField item : items) {
//                if (StringUtil.isEmpty(String.valueOf(item.getValue()))) {
//                    sql = sql + item.getItemCode() + "=null,";
//                } else {
//                    if (SqlTypeEnum.DATE.equals(item.getSqlType()) && !StringUtil.isEmpty(String.valueOf(item.getValue()))) {
////                        String date = (new SimpleDateFormat("yyyy-MM-dd")).format(Long.parseLong(item.getValue().toString()));
//                        sql = sql + item.getItemCode() + "=to_date('" + item.getValue() + "','yyyy-mm-dd'),";
//                    } else if (SqlTypeEnum.INTEGER.equals(item.getSqlType())) {
//                        sql = sql + item.getItemCode() + "=" + "'" + Integer.parseInt(item.getValue() + "") + "',";
//                    } else if (SqlTypeEnum.DOUBLE.equals(item.getSqlType())) {
//                        sql = sql + item.getItemCode() + "=" + "'" + Double.parseDouble(item.getValue() + "") + "',";
//                    } else {
//                        sql = sql + item.getItemCode() + "=" + "'" + item.getValue() + "',";
//                    }
//                }
//            }
//            sql = sql.substring(0, sql.length() - 1);
//            sql = sql + " where id=" + accountLine.getId();
//            this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
//        }
        super.update("accountDataMapper.updateData",sqlParameterMap);

    }

    @Override
    public void batchUpdateData(AccountLine accountLine, Account account, List<Long> lineId) {
        Map sqlParameterMap = new HashMap();
        sqlParameterMap.put("tableName",account.getAccountTemplate().getTableName());
//        String sql = "update " + account.getAccountTemplate().getTableName() + " set ";
        Collection<AccountField> items = accountLine.getAccountFields();
        sqlParameterMap.put("items",items);
        if (items.size() > 0) {
            for (AccountField item : items) {
                if (StringUtil.isEmpty(String.valueOf(item.getValue()))) {
//                    sql = sql + item.getItemCode() + "=null,";
                } else {
                    if (SqlTypeEnum.DATE.equals(item.getSqlType()) && !StringUtil.isEmpty(String.valueOf(item.getValue()))) {
//                        sql = sql + item.getItemCode() + "=to_date('" + item.getValue() + "','yyyy-mm-dd'),";
                    } else {
//                        sql = sql + item.getItemCode() + "='" + item.getValue() + "',";
                    }
                }
            }
//            sql = sql.substring(0, sql.length() - 1);
            String idList = "";
            for (int i = 0; i < lineId.size(); i++) {
                idList += lineId.get(i) + ",";
            }
            idList = idList.substring(0, idList.length() - 1);
            sqlParameterMap.put("idList",idList);
//            sql = sql + " where id in(" + idList + ")";
//            this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
            super.update("accountDataMapper.batchUpdateData",sqlParameterMap);
        }
    }

    @Override
    public AccountLine findDataById(Account account, Long id) {
        Map sqlParameterMap = new HashMap();
        sqlParameterMap.put("tableName",account.getAccountTemplate().getTableName());
        sqlParameterMap.put("id",id);
        //String sql = "select * from  " + account.getAccountTemplate().getTableName() + " where id=" + id;
        List<Map<String, Object>> resultList = super.selectList("accountDataMapper.findDataById",sqlParameterMap);
        Map<String, Object> map = resultList.get(0);
        AccountLine ledgerLine = new AccountLine();
        Set<AccountField> set = new LinkedHashSet<>();
        Collection<AccountField> accountFields = account.getAccountTemplate().getAccountFields();
        if (null != accountFields) {
            for (AccountField accountField : accountFields) {
                if (map.containsKey(accountField.getItemCode())) {
                    accountField.setValue(map.get(accountField.getItemCode()));
                    accountField.setEditBeforeValue(map.get(accountField.getItemCode()));
                    set.add(accountField);
                }
            }
        }
        ledgerLine.setId(id);
        ledgerLine.setAccountFields(set);
        return ledgerLine;
    }

    @Override
    public Boolean queryDataisExist(AccountLine accountLine, Account account) {
        Boolean result = false;
        Boolean point = false;
        Collection<AccountField> items = accountLine.getAccountFields();
        Map sqlParameterMap = new HashMap();
        sqlParameterMap.put("items",items);
        sqlParameterMap.put("tableName",account.getAccountTemplate().getTableName());
        sqlParameterMap.put("accountId",account.getId());

//        StringBuffer sql = new StringBuffer();
//        sql.append("select count(1) from " + account.getAccountTemplate().getTableName() + " where reportId=" + account.getId() + "  ");
        //字段类型
        Map<String,List<String>> itemInstanceMap = new HashMap<>();
        List<String> integerFieldAndDoubleFieldList = new ArrayList<>();
        List<String> codeFieldList = new ArrayList<>();
        Collection<AccountField> accountFields = account.getAccountTemplate().getAccountFields();
//        sqlParameterMap.put("accountFields",accountFields);
        Collection<AccountField> isPkableListAndItemCodeEqual = new ArrayList();
        //没有查询条件 查询所有
        for (AccountField item : items) {
            String code = item.getItemCode();
            if (item.getValue() != null) {
                for (AccountField accountField : accountFields) {
                    if (accountField.isPkable() && accountField.getItemCode().equals(item.getItemCode())) {
                        point = true;
                        isPkableListAndItemCodeEqual.add(accountField);
                        //sql.append("and " + code);
                        if (item instanceof IntegerField || item instanceof DoubleField) {
                            integerFieldAndDoubleFieldList.add(code);
                            //sql.append(" = " + item.getValue() + " ");
                        } else if (item instanceof CodeField || item instanceof DateField) {
                            codeFieldList.add(code);
                            //sql.append(" = '" + item.getValue() + "' ");
                        } else {
                            //sql.append(" = '" + item.getValue() + "' ");
                        }
                        break;
                    }
                }

            }
        }
        itemInstanceMap.put("integerFieldAndDoubleFieldList",integerFieldAndDoubleFieldList);
        itemInstanceMap.put("codeFieldList",codeFieldList);
        sqlParameterMap.put("itemInstanceMap",itemInstanceMap);
        sqlParameterMap.put("isPkableListAndItemCodeEqual",isPkableListAndItemCodeEqual);
        Map<String, Object> map = new HashMap<>();
        long totalNum = super.selectOne("accountDataMapper.queryDataisExist",sqlParameterMap);

        if ( totalNum > 0 && point) {
            result = true;
        }
        return result;
    }

    @Override
    public void deleteData(AccountLine accountLine, Account account) {
        Map sqlParameterMap = new HashMap();
        sqlParameterMap.put("tableName",account.getAccountTemplate().getTableName());
        sqlParameterMap.put("id",accountLine.getId());
        super.delete("accountDataMapper.deleteData",sqlParameterMap);

//        String sql = "delete from " + account.getAccountTemplate().getTableName() + " where id=" + accountLine.getId();
//        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
    }
}
