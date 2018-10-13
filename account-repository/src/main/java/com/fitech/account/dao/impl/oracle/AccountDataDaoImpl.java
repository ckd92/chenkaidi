package com.fitech.account.dao.impl.oracle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

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

public class AccountDataDaoImpl extends NamedParameterJdbcDaoSupport implements AccountDataDao {
	@Autowired
    public AccountDataDaoImpl(DataSource dataSource) {
        setDataSource(dataSource);
        try {
            dataSource.getConnection().setAutoCommit(true);
        } catch (Exception e) {
        }
    }

    @Override
    public Page<AccountLine> findDataByCondition(AccountProcessVo accountProcessVo) {
        Account account = accountProcessVo.getAccount();

        AccountTemplate accountTemplate = account.getAccountTemplate();

        Collection<AccountField> collection = accountTemplate.getAccountFields();
        List<String> list = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT * FROM " +
                "     (SELECT A.*, rownum r " +
                "       FROM " +
                "          (");
        sql.append("select id,reportId,");
        list.add("id");
        list.add("reportId");
        for (AccountField item : collection) {
            sql.append(item.getItemCode() + ",");
            list.add(item.getItemCode());
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" from " + accountTemplate.getTableName() + " where reportId=" + account.getId() + "  ");
        //没有查询条件 查询所有

        accountTemplate.setAccountFields(null);
        Collection<AccountField> serachFileds = account.getAccountSearchs();
        if(null != serachFileds && !serachFileds.isEmpty()){
        	//将itemtype赋值
        	for(AccountField afl:serachFileds){
            	for(AccountField afd:collection){
            		if(afl.getItemCode().equals(afd.getItemCode())&&"DATE".equals(afd.getItemType())&&(!"".equals(afl.getValue()))){
            			afl.setItemType(afd.getItemType());
            		}
            	}
            }
        	//进行循环
            for (AccountField item : serachFileds) {
                String code = item.getItemCode();
                if (item.getValue() != null) {
                    sql.append("and " + code);
                    if (item instanceof IntegerField || item instanceof DoubleField) {
                        sql.append(" = " + item.getValue() + " ");
                    } else if (item instanceof CodeField) {
                        sql.append(" = '" + item.getValue() + "' ");
                    } else if ("DATE".equals(item.getItemType())) {
                        sql.append(" = to_date('" + item.getValue() + "','yyyy-mm-dd') ");
                    }else {
                        sql.append(" like '%" + item.getValue() + "%' ");
                    }
                }
            }
        }
        accountTemplate.setAccountFields(collection);
        StringBuffer totalsum=new StringBuffer();
        totalsum.append("with a as ( \n");
        totalsum.append(sql);
        totalsum.append(" ) A) \n");
        totalsum.append(")select count(*) from a ");
        
        sql.append(") A" +
                "       WHERE rownum <= " + (accountProcessVo.getPageNum()*accountProcessVo.getPageSize())+
                "     ) B " +
                "         WHERE r >= "+((accountProcessVo.getPageNum()-1)*accountProcessVo.getPageSize()+1));
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map1 = new HashMap<>();
        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql.toString(),map);
        System.out.println(sql.toString());
//        List<Map<String, Object>> totalList = this.getNamedParameterJdbcTemplate().queryForList(totalsum.toString(),map1);
        int totalList = this.getNamedParameterJdbcTemplate().queryForObject(
        		totalsum.toString(), map1, Integer.class);
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
                String value=String.valueOf(ledgerLineMap.get(s));
                if(value.indexOf("\"")!=-1){
                	value=value.replaceAll("\"", "&#34;");
                	item.setValue(value);
                }else if(value.indexOf("\t")!=-1){
                	value=value.replaceAll("\t", "");
                	item.setValue(value);
                }else{
                	item.setValue(ledgerLineMap.get(s));
                }       
                set.add(item);
            }
            ledgerLine.setAccountFields(set);
            lineList.add(ledgerLine);
        }
        //将sqltype从template复制到accountline
        for(AccountLine a:lineList){
        	for(AccountField af:a.getAccountFields()){
        		for(AccountField ac:collection){
        			if(af.getItemCode().equals(ac.getItemCode())){
        				af.setSqlType(ac.getSqlType());
                        af.setItemType(ac.getItemType());
        			}
        		}
        	}
        }
//        long totalNum = findMaxNumDataByCondition(accountProcessVo);
        Pageable pageable = new PageRequest(accountProcessVo.getPageNum() - 1, accountProcessVo.getPageSize());
        Page<AccountLine> ledgerLinePage = new PageImpl<>(lineList, pageable, totalList);
        account.setAccountLine(ledgerLinePage);

        return  ledgerLinePage;
    }
    //用于下载
    @Override
    public List<AccountLine> downLoadDataByCondition(AccountProcessVo accountProcessVo) {
        Account account = accountProcessVo.getAccount();

        AccountTemplate accountTemplate = account.getAccountTemplate();

        Collection<AccountField> collection = accountTemplate.getAccountFields();
        List<String> list = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append("select id,reportId,");
        list.add("id");
        list.add("reportId");
        for (AccountField item : collection) {
            sql.append(item.getItemCode() + ",");
            list.add(item.getItemCode());
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" from " + accountTemplate.getTableName() + " where reportId=" + account.getId() + "  ");
        //没有查询条件 查询所有

        accountTemplate.setAccountFields(null);
        Collection<AccountField> serachFileds = account.getAccountSearchs();
        if(null != serachFileds && !serachFileds.isEmpty()){
        	//将itemtype赋值
        	for(AccountField afl:serachFileds){
            	for(AccountField afd:collection){
            		if(afl.getItemCode().equals(afd.getItemCode())&&"DATE".equals(afd.getItemType())&&(!"".equals(afl.getValue()))){
            			afl.setItemType(afd.getItemType());
            		}
            	}
            }
        	//进行循环
            for (AccountField item : serachFileds) {
                String code = item.getItemCode();
                if (item.getValue() != null) {
                    sql.append("and " + code);
                    if (item instanceof IntegerField || item instanceof DoubleField) {
                        sql.append(" = " + item.getValue() + " ");
                    } else if (item instanceof CodeField) {
                        sql.append(" = '" + item.getValue() + "' ");
                    } else if ("DATE".equals(item.getItemType())) {
                        sql.append(" = to_date('" + item.getValue() + "','yyyy-mm-dd') ");
                    }else {
                        sql.append(" like '%" + item.getValue() + "%' ");
                    }
                }
            }
        }
        accountTemplate.setAccountFields(collection);
        
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> map1 = new HashMap<>();
        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql.toString(),map);
//        List<Map<String, Object>> totalList = this.getNamedParameterJdbcTemplate().queryForList(totalsum.toString(),map1);
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
                String value=String.valueOf(ledgerLineMap.get(s));
                if(value.indexOf("\"")!=-1){
                	value=value.replaceAll("\"", "&#34;");
                	item.setValue(value);
                }else if(value.indexOf("\t")!=-1){
                	value=value.replaceAll("\t", "");
                	item.setValue(value);
                }else{
                	item.setValue(ledgerLineMap.get(s));
                }       
                set.add(item);
            }
            ledgerLine.setAccountFields(set);
            lineList.add(ledgerLine);
        }
        //将sqltype从template复制到accountline
        for(AccountLine a:lineList){
        	for(AccountField af:a.getAccountFields()){
        		for(AccountField ac:collection){
        			if(af.getItemCode().equals(ac.getItemCode())){
        				af.setSqlType(ac.getSqlType());
                        af.setItemType(ac.getItemType());
        			}
        		}
        	}
        }
//        long totalNum = findMaxNumDataByCondition(accountProcessVo);
        
        return  lineList;
    }

    @Override
    public Long findMaxNumDataByCondition(AccountProcessVo accountProcessVo) {
        Account account = accountProcessVo.getAccount();

        AccountTemplate accountTemplate = account.getAccountTemplate();

        Collection<AccountField> collection = accountTemplate.getAccountFields();
        
        Collection<AccountField> serachFileds = account.getAccountSearchs();
        //将搜索值赋给模板
        if(!serachFileds.isEmpty()){
        	for(AccountField aff:collection){
        		for(AccountField afs:serachFileds){
        			if(aff.getItemCode().equals(afs.getItemCode())&&(!"".equals(afs.getValue()))&&null!=afs.getValue()){
        				aff.setValue(afs.getValue());
        			}
        		}
        	}
        }
        
        StringBuffer sql = new StringBuffer();
        sql.append("select count(1) from " + accountTemplate.getTableName() + " where reportId=" + account.getId() + "  ");
        //没有查询条件 查询所有
        for (AccountField item : collection) {
            String code = item.getItemCode();
            if (item.getValue() != null) {
                sql.append("and " + code);
                if (item instanceof IntegerField || item instanceof DoubleField) {
                    sql.append(" = " + item.getValue() + " ");
                } else if (item instanceof CodeField) {
                    sql.append(" = '" + item.getValue() + "' ");
                }else if (item instanceof DateField) {
                    sql.append("to_date('" + item.getValue() + "','yyyy-mm-dd')");
                } else {
                    sql.append(" like '%" + item.getValue() + "%' ");
                }
            }
        }
        Map<String, Object> map = new HashMap<>();
        Long totalNum = this.getNamedParameterJdbcTemplate().queryForObject(sql.toString(),map,Long.class);

        return  totalNum;
    }

//    @Override
//    public List<String> loadDataByTemplate(Long accountId, AccountTemplate accountTemplate, Sheet sheet,Account account) {
//        List<String> resultList = new ArrayList<>();
//
//    	String tableName = accountTemplate.getTableName();
//        //获取EXCEL表头
//        List<String> columnHeaderlist = ExcelUtil.getColumnHeader(sheet);
//        //获取EXCEL数据列
//        List<List<String>> datas = ExcelUtil.getDatas(sheet,2);
//        Integer size = datas.size();
//        //构建主键MAP
//        HashMap<String, Object> pkMap = this.generatePKMap(columnHeaderlist, accountTemplate);
//
//        StringBuilder insertField = new StringBuilder(500);
//        //组装批量导入SQL
//        Collection<AccountField> itemsf = accountTemplate.getAccountFields();
//        Collection<AccountField> items=new ArrayList<AccountField>();
//        insertField.append("insert into " + tableName + " ( ");
//        for (int i = 0; i < columnHeaderlist.size(); i++) {
//            insertField.append(columnHeaderlist.get(i).trim());
//            insertField.append(",");
//            for(AccountField a:itemsf){
//            	if(a.getItemCode().equals(columnHeaderlist.get(i).trim())){
//            		items.add(a);
//            	}
//            }
//        }
//        //检验主键是否存在
////        Collection<AccountField> affs=new ArrayList<AccountField>();
//        
//        insertField.append("ID, ");
//        insertField.append("REPORTID");
//        insertField.append(") values (");
//        // 拼接批量导入SQL
//        for (int i = 0; i < columnHeaderlist.size(); i++) {
//            insertField.append(":"+columnHeaderlist.get(i).trim());
//            insertField.append(",");
//        }
//        insertField.append("SEQ_FITECH.NEXTVAL" + ","+accountId+")");
//        System.out.println("insert:sql="+insertField);
//        
//
//        //集合存放复合主键的值，用于对比是否有重复数据
//        List<String> pkValueStr = new ArrayList<>();
//        //通过主键判断数据是否有重复（联合主键）
//        for( int i = 0; i < datas.size(); i++ ){
//            //存放主键的在columnHeaderlist中的index
//            List<Integer> listFlag = new ArrayList<>();
//            Iterator pkIterator = pkMap.keySet().iterator();
//            if( pkIterator.hasNext() ){
//                String pk = (String)pkIterator.next();
//                //如果columnHeaderlist中有主键，记录其位置
//                Integer index = columnHeaderlist.indexOf( pk );
//                if(columnHeaderlist.indexOf( pk ) != -1){
//                    listFlag.add( index );
//                }
//            }
//            //取出一行值
//            List<String> values = datas.get(i);
//            //拼接每条数据的复合主键
//            String str = "";
//            for(int j=0;j<listFlag.size();j++){
//                str += values.get(listFlag.get(j));
//            }
//            //判断该复合主键是否存在，不存在加入pkValueStr，存在就返回提示
//            if(StringUtil.isNotEmpty(str) && pkValueStr.contains(str)){
//                //失败返回重复数据的行号
////                resultList.add("false");
////                resultList.add(String.valueOf(i+1+2));
////                return resultList;
//            }else{
//                pkValueStr.add(str);
//            }
//        }
//        // 所有该台账所有补录数据
//        String deleteSql = "delete from "+tableName;
//        this.getNamedParameterJdbcTemplate().update(deleteSql,new HashMap<String, String>());
//        
//        
//        List<Map<String,Object>> dataMap = new ArrayList<Map<String,Object>>();
//        
//        
//        //递归数据行
//        for (int i = 0; i < datas.size(); i++) {
////            StringBuilder insertValueStr = new StringBuilder(500);
//            Map<String,Object> params = new HashMap<>();
//            //待录入值
//            List<String> values = datas.get(i);
//            for (int k = 0; k < values.size(); k++) {
//                AccountField field = null;
//                try{
//                	field = (AccountField) items.toArray()[k];
//            	}catch(IndexOutOfBoundsException e){
//            		throw new AppException(ExceptionCode.SYSTEM_ERROR, "请确认导入模板是否正确！字典项字段功能暂未完善，请确认是否导入字典项内容");
//            	}
//                
//                if (pkMap.containsKey(field.getItemCode())) {
//                    pkMap.put(field.getItemCode(), values.get(k));
//                }
//                if (values.get(k) != null) {
//                	String tempValue = values.get(k);
//                	if(StringUtil.isNotEmpty(field.getDicId()) && tempValue.indexOf("-") >-1 ){
//                		if(tempValue.indexOf("-")+1 < tempValue.length()){
//                			tempValue = tempValue.substring(tempValue.indexOf("-")+1, tempValue.length());
//                		}else{
//                			tempValue = "";
//                		}
//                	}
//                	
//                    if (field.getSqlType().equals(SqlTypeEnum.VARCHAR)) {
////                        insertValueStr.append("'" + tempValue + "'");
//                        params.put(field.getItemCode(), tempValue);
//                        field.setValue(tempValue);
//                    } else if(field.getSqlType().equals(SqlTypeEnum.DATE)) {
//                    	if("".equals(tempValue)){
////                    		insertValueStr.append("NULL");
//                    		params.put(field.getItemCode(), null);
//                    		field.setValue(tempValue);
//                    	}else{
////                    		insertValueStr.append("to_date('" + tempValue + "','yyyy-mm-dd')");
//                    		params.put(field.getItemCode(), "to_date('" + tempValue + "','yyyy-mm-dd')");
//                        	field.setValue(tempValue);
//                    	}	
//                    }else if(field.getSqlType().equals(SqlTypeEnum.INTEGER)||
//                    		field.getSqlType().equals(SqlTypeEnum.DECIMAL)||
//                    		field.getSqlType().equals(SqlTypeEnum.DOUBLE)||
//                    		field.getSqlType().equals(SqlTypeEnum.INT)||
//                    		field.getSqlType().equals(SqlTypeEnum.BIGINT)) {
//                    	if("".equals(tempValue)){
////                    		insertValueStr.append("NULL");
//                    		params.put(field.getItemCode(), null);
//                    		field.setValue(tempValue);
//                    	}else{
////                    		insertValueStr.append(tempValue);
//                    		params.put(field.getItemCode(), tempValue);
//                        	field.setValue(tempValue);
//                    	}	
//                    }else{
////                    	insertValueStr.append(tempValue);
//                    	params.put(field.getItemCode(), tempValue);
//                    	field.setValue(tempValue);
//                    }
//                } else {
////                    insertValueStr.append("NULL");
//                    params.put(field.getItemCode(), null);
//                }
////                insertValueStr.append(",");
////                affs.add(field);
//
//            }
//            dataMap.add(params);
////            insertValueStr.append("SEQ_FITECH.NEXTVAL" + ","+accountId+")");   
////            AccountLine accountline=new AccountLine(); 
////            accountline.setAccountFields(affs);
////            if(this.queryDataisExist(accountline,account)){
////                return -1;
////            }
//
////            String insertsql =  insertField.toString() + insertValueStr.toString();
////            System.out.println("insert:sql="+insertsql);
//
////            this.getNamedParameterJdbcTemplate().update(insertsql, new HashMap<String, String>());
//        }
//        
//        
//        //成功返回加载条数
//        resultList.add("true");
//        resultList.add(String.valueOf(size));
//        return resultList;
//    }


    

    @Override
    public void insertData(AccountLine accountLine, Account account) {
        String sql = "insert into " + account.getAccountTemplate().getTableName() + "(";
        Collection<AccountField> items = accountLine.getAccountFields();
        Collection<AccountField> itemegs=account.getAccountTemplate().getAccountFields();
        for(AccountField af:items){
            for(AccountField ag:itemegs){
                if(af.getItemCode().equals(ag.getItemCode())){
                    af.setSqlType(ag.getSqlType());
                }
            }
        }
        sql = sql + "id,reportId, ";
        for (AccountField item : items) {
            sql = sql + item.getItemCode() + ",";
        }
        sql = sql.substring(0, sql.length() - 1) + ")values(seq_fitech.nextval,";
        sql = sql + account.getId() + ",";
        for (AccountField item : items) {
            if (item.getValue() == null) {
                sql = sql + "null,";
            } else {
                if (SqlTypeEnum.DATE.equals(item.getSqlType())) {
                    sql = sql + "to_date('" + item.getValue() + "', 'yyyy-MM-dd') ,";
                } else if(SqlTypeEnum.INTEGER.equals(item.getSqlType())){
                    sql = sql + "'" + Integer.parseInt(item.getValue()+"") + "',";
                }else if(SqlTypeEnum.DOUBLE.equals(item.getSqlType())){
                    sql = sql + "'" + Double.parseDouble(item.getValue()+"") + "',";
                }else{
                    sql = sql + "'" + item.getValue() + "',";
                }
            }
        }
        sql = sql.substring(0, sql.length() - 1) + ")";

        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());

    }

    @Override
    public void updateData(AccountLine accountLine, Account account) {
        String sql = "update " + account.getAccountTemplate().getTableName() + " set ";
        Collection<AccountField> items = accountLine.getAccountFields();
        Collection<AccountField> itemegs=account.getAccountTemplate().getAccountFields();
        for(AccountField af:items){
            for(AccountField ag:itemegs){
                if(af.getItemCode().equals(ag.getItemCode())){
                    af.setSqlType(ag.getSqlType());
                }
            }
        }
        if (items.size() > 0) {
            for (AccountField item : items) {
                if (StringUtil.isEmpty(String.valueOf(item.getValue()))) {
                    sql = sql + item.getItemCode() + "=null,";
                } else {
                    if (SqlTypeEnum.DATE.equals(item.getSqlType()) && !StringUtil.isEmpty(String.valueOf(item.getValue()))) {
//                        String date = (new SimpleDateFormat("yyyy-MM-dd")).format(Long.parseLong(item.getValue().toString()));
                        sql = sql + item.getItemCode() + "=to_date('" + item.getValue() + "','yyyy-mm-dd'),";
                    } else if(SqlTypeEnum.INTEGER.equals(item.getSqlType())){
                        sql = sql+item.getItemCode()+"="+ "'" + Integer.parseInt(item.getValue()+"") + "',";
                    }else if(SqlTypeEnum.DOUBLE.equals(item.getSqlType())){
                        sql = sql +item.getItemCode()+"="+ "'" + Double.parseDouble(item.getValue()+"") + "',";
                    }else{
                        sql = sql+item.getItemCode()+"="+ "'" + item.getValue() + "',";
                    }
                }
            }
            sql = sql.substring(0, sql.length() - 1);
            sql = sql + " where id=" + accountLine.getId();
            this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
        }
    }

    @Override
    public void batchUpdateData(AccountLine accountLine, Account account, List<Long> lineId) {
        String sql = "update " + account.getAccountTemplate().getTableName() + " set ";
        Collection<AccountField> items = accountLine.getAccountFields();
        if (items.size() > 0) {
            for (AccountField item : items) {
                if (StringUtil.isEmpty(String.valueOf(item.getValue()))) {
                    sql = sql + item.getItemCode() + "=null,";
                } else {
                    if (SqlTypeEnum.DATE.equals(item.getSqlType()) && !StringUtil.isEmpty(String.valueOf(item.getValue()))) {
//                        String date = (new SimpleDateFormat("yyyy-MM-dd")).format(Long.parseLong(item.getValue().toString()));
                        sql = sql + item.getItemCode() + "=to_date('" + item.getValue() + "','yyyy-mm-dd'),";
                    } else{
                        sql = sql + item.getItemCode() + "='" + item.getValue() + "',";
                    }
                }
            }
            sql = sql.substring(0, sql.length() - 1);
            String idList = "";
            for (int i = 0; i < lineId.size(); i++) {
                idList += lineId.get(i)+",";
            }
            idList = idList.substring(0,idList.length()-1);
            sql = sql + " where id in("+idList+")";
            this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
        }
    }

    @Override
    public AccountLine findDataById(Account account, Long id) {
        String sql = "select * from  " + account.getAccountTemplate().getTableName() + " where id=" + id;
        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql, new HashMap<String, String>());
        Map<String, Object> map = resultList.get(0);
        AccountLine ledgerLine = new AccountLine();
        Set<AccountField> set = new LinkedHashSet<>();
        Collection<AccountField> accountFields = account.getAccountTemplate().getAccountFields();
        if(null != accountFields){
            for(AccountField accountField : accountFields){
                if(map.containsKey(accountField.getItemCode())){
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
        Boolean point=false;
        Collection<AccountField> items = accountLine.getAccountFields();
        StringBuffer sql = new StringBuffer();
        sql.append("select count(1) from " + account.getAccountTemplate().getTableName() + " where reportId=" + account.getId() + "  ");
        //没有查询条件 查询所有
        for (AccountField item : items) {
            String code = item.getItemCode();
            if (item.getValue() != null ) {

                Collection<AccountField> accountFields = account.getAccountTemplate().getAccountFields();
                for(AccountField accountField : accountFields){
                    if(accountField.isPkable() && accountField.getItemCode().equals(item.getItemCode())){
                    	point=true;
                        sql.append("and " + code);
                        if (item instanceof IntegerField || item instanceof DoubleField) {
                            sql.append(" = " + item.getValue() + " ");
                        } else if (item instanceof CodeField || item instanceof DateField) {
                            sql.append(" = '" + item.getValue() + "' ");
                        } else {
                            sql.append(" = '" + item.getValue() + "' ");
                        }
                        break;
                    }
                }

            }
        }
        Map<String, Object> map = new HashMap<>();
        Long totalNum = this.getNamedParameterJdbcTemplate().queryForObject(sql.toString(),map,Long.class);

        if(null != totalNum && totalNum > 0 && point){
            result = true;
        }
        return result;
    }

    @Override
    public void deleteData(AccountLine accountLine, Account account) {
        String sql = "delete from " + account.getAccountTemplate().getTableName() + " where id=" + accountLine.getId();
        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
    }
}
