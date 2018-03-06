package com.fitech.account.dao.impl.oracle;

import com.fitech.account.dao.AccountTemplateDAO;
import com.fitech.domain.account.AccountField;
import com.fitech.domain.account.AccountTemplate;
import com.fitech.domain.ledger.TableNameType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.*;

/**
 * Created by SunBojun on 2017/2/28.
 */
public class AccountTemplateDAOImpl extends NamedParameterJdbcDaoSupport implements AccountTemplateDAO {
    @Autowired
    public AccountTemplateDAOImpl(DataSource dataSource) {
        setDataSource(dataSource);
        try {
            dataSource.getConnection().setAutoCommit(true);
        } catch (Exception e) {
        }
    }


    public void createTemplate(AccountTemplate accountTemplate) {
    	String tableName = accountTemplate.getTableName().toUpperCase();
//        String history = tableName + "_" + TableNameType.HISTORY.getTableName().toUpperCase();
        String standard = tableName + "_" + TableNameType.STANDARD.getTableName().toUpperCase();
//        String origin = tableName + "_" + TableNameType.ORIGIN.getTableName().toUpperCase();
//        String zipper = tableName + "_" + TableNameType.ZIPPER.getTableName().toUpperCase();
        
            createTemplate(tableName, accountTemplate);
//        createTemplate(history, accountTemplate);
        createTemplate(standard, accountTemplate);
//        createTemplate(origin, accountTemplate);
//        createTemplate_zipper(zipper, ledgerReportTemplate);
    }



    /**
     * 初始化数据表结构
     *
     * @param sql
     * @param accountTemplate
     */
    private void createSQL(StringBuffer sql, AccountTemplate accountTemplate) {
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
    }
    
    private void createTemplate(String tableName,AccountTemplate accountTemplate){
    	StringBuffer sql = new StringBuffer();
    	sql.append("CREATE TABLE " + tableName + "(");
    	this.createSQL(sql, accountTemplate);
    	sql.append(" syncAble VARCHAR(50),check_status VARCHAR(20))");
    	this.getNamedParameterJdbcTemplate().update(sql.toString(), new HashMap<String, Object>());
    }
//    /**
//     * 拉链表
//     * @param tableName
//     * @param ledgerReportTemplate
//     */
//    private void createTemplate_zipper(String tableName,LedgerReportTemplate ledgerReportTemplate){
//    	StringBuffer sql = new StringBuffer();
//    	sql.append("CREATE TABLE " + tableName + "(");
//    	this.createSQL(sql, ledgerReportTemplate);
//    	sql.append(" begin_date date,end_date date,first_date date");
//    	sql.append(" syncAble VARCHAR(50),check_status VARCHAR(20))");
//    	this.getNamedParameterJdbcTemplate().update(sql.toString(), new HashMap<String, Object>());
//    }
//
//    @Override
//    @Transactional
//    public void insertData(LedgerLine ledgerLine, LedgerReport ledgerReport) {
//        String sql = "insert into " + ledgerReport.getLedgerReportTemplate().getTableName() + "(";
//        Collection<LedgerItem> items = ledgerLine.getLedgerItems();
//        sql = sql + "id,reportId, ";
//        for (LedgerItem item : items) {
//            sql = sql + item.getItemCode() + ",";
//        }
//        sql = sql.substring(0, sql.length() - 1) + ")values(seq_fitech.nextval,";
//        sql = sql + ledgerReport.getId() + ",";
//        for (LedgerItem item : items) {
//            if (item.getValue() == null) {
//                sql = sql + "null,";
//            } else {
//                if (SqlType.DATE.equals(item.getSqlType())) {
//                    sql = sql + "to_date('" + item.getValue() + "', 'yyyy-MM-dd') ,";
//                } else {
//                    sql = sql + "'" + item.getValue() + "',";
//                }
//            }
//        }
//        sql = sql.substring(0, sql.length() - 1) + ")";
//
//        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
//
//    }
//
//    @Override
//    @Transactional
//    public void batchInsert(Collection<LedgerLine> ledgerLines, LedgerReport ledgerReport) {
//        for (LedgerLine ledgerLine : ledgerLines) {
//            this.insertData(ledgerLine, ledgerReport);
//        }
//    }
//
//    @Override
//    @Transactional
//    public void updateData(LedgerLine ledgerLine, LedgerReport ledgerReport) {
//        String sql = "update " + ledgerReport.getLedgerReportTemplate().getTableName() + " set ";
//        Collection<LedgerItem> items = ledgerLine.getLedgerItems();
//        if (items.size() > 0) {
//            for (LedgerItem item : items) {
//                if (item.getValue() == null) {
//                    sql = sql + item.getItemCode() + "=null,";
//                } else {
//                    if (item.getItemCode().toUpperCase().contains("DATE")) {//含有日期型的单独处理
//                        String date = (new SimpleDateFormat("yyyy-MM-dd")).format(Long.parseLong(item.getValue().toString()));
//                        sql = sql + item.getItemCode() + "=to_date('" + date + "','yyyy-mm-dd'),";
//                    } else {
//                        sql = sql + item.getItemCode() + "='" + item.getValue() + "',";
//                    }
//                }
//            }
//            sql = sql.substring(0, sql.length() - 1);
//            sql = sql + " where id=" + ledgerLine.getId();
//            this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
//        }
//    }
//
//    @Transactional
//    public void batchUpdateData(ValidateResultParameter vo, ValidateBatch batch, LedgerItem item, String tableName) {
//        StringBuffer sb = new StringBuffer()
//                .append("update " + tableName + " set ")
//                .append(item.getItemCode() + "='" + item.getValue() + "'")
//                .append("  where id in(");
//        if (StringUtil.isEmpty(vo.getLineIds())) {
//            sb.append(" select a.id from(SELECT DISTINCT D.id FROM")
//                    .append(" dqc_validate_result t2,")
//                    .append(tableName + "  D,")
//                    .append(" dqc_validate_result_details t1,")
//                    .append(" Institution i")
//                    .append(" WHERE t1.validateResult_id = t2.id")
//                    .append(" and validate_batch='" + batch.getBatchId() + "'");
//            if (!StringUtil.isEmpty(vo.getInstitutionName())) {
//                sb.append(" AND D.orig_org_id=i.institutionId")
//                        .append(" AND i.institutionName like '%" + vo.getInstitutionName() + "%'");
//            }
//            sb.append(" And t2.check_rule_id='" + vo.getRuleId() + "'")
//                    .append(" AND  D.ID=t1.details_key_group")
//                    .append(" AND t2.validate_column='" + vo.getItemCode() + "'");
//            if(!StringUtil.isEmpty(vo.getGatherDate())){
//                sb.append(" AND D.GATHER_DATE=to_date('" + vo.getGatherDate() + "','yyyy-mm-dd')");
//            }
//            sb.append(")a");
//        } else {
//            sb.append(vo.getLineIds());
//        }
//        sb.append(")");
//        this.getNamedParameterJdbcTemplate().update(sb.toString(), new HashMap<String, String>());
//    }
//
//
//
//    @Transactional
//    public void batchUpdateDataForEAST(ValidateResultParameter vo, ValidateBatch batch, LedgerItem item, String tableName) {
//        StringBuffer sb = new StringBuffer()
//                .append("update " + tableName + " set ")
//                .append(item.getItemCode() + "='" + item.getValue() + "'")
//                .append("  where id in(");
//        if (StringUtil.isEmpty(vo.getLineIds())) {
//            sb.append(" select a.id from(SELECT DISTINCT D.id FROM")
//                    .append(" dqc_validate_result t2,")
//                    .append(tableName + "  D,")
//                    .append(" dqc_validate_result_details t1,")
//                    .append(" Institution i")
//                    .append(" WHERE t1.validateResult_id = t2.id")
//                    .append(" and validate_batch='" + batch.getBatchId() + "'");
//            if (!StringUtil.isEmpty(vo.getInstitutionName())) {
//                sb.append(" AND D.nbjgh=i.institutionId")
//                        .append(" AND i.institutionName like '%" + vo.getInstitutionName() + "%'");
//            }
//            sb.append(" And t2.check_rule_id='" + vo.getRuleId() + "'")
//                    .append(" AND  D.ID=t1.details_key_group")
//                    .append(" AND t2.validate_column='" + vo.getItemCode() + "'");
//            if(!StringUtil.isEmpty(vo.getGatherDate())){
//                sb.append(" AND D.GATHER_DATE=to_date('" + vo.getGatherDate() + "','yyyy-mm-dd')");
//            }
//            sb.append(")a");
//        } else {
//            sb.append(vo.getLineIds());
//        }
//        sb.append(")");
//        this.getNamedParameterJdbcTemplate().update(sb.toString(), new HashMap<String, String>());
//    }
//
//    @Override
//    @Transactional
//    public void batchUpdate(Collection<LedgerLine> ledgerLines, LedgerReport ledgerReport) {
//        for (LedgerLine ledgerLine : ledgerLines) {
//            this.updateData(ledgerLine, ledgerReport);
//        }
//    }
//
//    @Override
//    @Transactional
//    public void deleteData(LedgerLine ledgerLine, LedgerReport ledgerReport) {
//        String sql = "delete from " + ledgerReport.getLedgerReportTemplate().getTableName() + " where id=" + ledgerLine.getId();
//        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
//    }
//
//    @Override
//    @Transactional
//    public void batchDelete(Collection<LedgerLine> ledgerLines, LedgerReport ledgerReport) {
//        for (LedgerLine ledgerLine : ledgerLines) {
//            this.deleteData(ledgerLine, ledgerReport);
//        }
//    }
//
//    @Override
//    @Transactional
//    public void deleteAll(LedgerReport ledgerReport) {
//        String sql = "TRUNCATE TABLE " + ledgerReport.getLedgerReportTemplate().getTableName();
//        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
//    }
//
//
    @Transactional
    public void dropTemplate(AccountTemplate accountTemplate) {
        String ss = "select count(1) from user_tables where table_name = '" + accountTemplate.getTableName() + "'";
        int count = this.getNamedParameterJdbcTemplate().getJdbcOperations().queryForObject(ss, int.class);
        if (count > 0) {
            String sql = "DROP TABLE " + accountTemplate.getTableName();
            this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
        }
    }
//
//    @Override
//    public LedgerReportTemplate getReportTemplate(String tableName) {
//        Connection connection = null;
//        LedgerReportTemplate ledgerReportTemplate = new LedgerReportTemplate();
//
//        try {
//            connection = DataSourceUtils.getConnection(this.getDataSource());
//            ItemFactory itemFactory = new ItemFactory();
//            String catalog = null;
//            String schemaPattern = null;
//            String tableNamePattern = tableName;
//            String columnNamePattern = null;
//            DatabaseMetaData databaseMetaData = connection.getMetaData();
//            ResultSet result = databaseMetaData.getColumns(
//                    catalog, schemaPattern, tableNamePattern, columnNamePattern);
//            Set<LedgerItem> set = new HashSet<>();
//            while (result.next()) {
//                String itemName = (result.getString("COLUMN_NAME"));
//                String typeName = result.getString("TYPE_NAME");
//                LedgerItem ledgerItem = itemFactory.getItemInstance(itemName, typeName);
//                //如果类型为VARCHAR类型，需要设置长度
//                if (("VARCHAR").equals(result.getString("TYPE_NAME"))) {
//                    StringLedgerItem stringLedgerItem = new StringLedgerItem();
//                    stringLedgerItem = (StringLedgerItem) ledgerItem;
//                    stringLedgerItem.setItemLength(result.getInt("COLUMN_SIZE"));
//                    set.add(stringLedgerItem);
//                } else {
//                    set.add(ledgerItem);
//                }
//            }
//            ledgerReportTemplate.setItems(set);
//        } catch (IllegalArgumentException e) {
//            e.printStackTrace();
//            return null;
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//        return ledgerReportTemplate;
//    }
//
//
//    @Override
//    public LedgerLine findDataById(LedgerReport ledgerReport, Long id) {
//        String sql = "select * from  " + ledgerReport.getLedgerReportTemplate().getTableName() + " where id=" + id;
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql, new HashMap<String, String>());
//        Map<String, Object> map = resultList.get(0);
//        LedgerLine ledgerLine = new LedgerLine();
//        Set<LedgerItem> set = new LinkedHashSet<>();
//        for (String s : map.keySet()) {
//            LedgerItem item = new LedgerItem();
//            item.setItemCode(s);
//            item.setValue(map.get(s));
//            set.add(item);
//        }
//        ledgerLine.setLedgerItems(set);
//        return ledgerLine;
//    }
//
//    @Override
//    public List<LedgerLine> findDataByCondition(LedgerReport ledgerReport, Map<String, Object> map, Collection<LedgerItem> collection) {
//        String selectFiled = new String();
//        if (collection != null && collection.size() > 0) {
//            for (LedgerItem ledgerItem : collection) {
//                selectFiled = selectFiled + ledgerItem.getItemCode() + ",";
//            }
//            selectFiled = selectFiled.substring(0, selectFiled.length() - 1);
//        }
//        //String sql = "select " + selectFiled + " from " + ledgerReport.getLedgerReportTemplate().getTableName() + " where  reportId=" + ledgerReport.getId();
//        String sql = "select " + selectFiled + " from " + ledgerReport.getLedgerReportTemplate().getTableName() + " where 1=1 ";
//        for (LedgerItem item : collection) {
//            String code = item.getItemCode();
//            if (map.get(code) != null) {
//                sql = sql + "and " + code;
//                if ("INTEGER".equals(item.getItemType()) || "DOUBLE".equals(item.getItemType())) {
//                    sql = sql + " = " + map.get(code) + " ";
//                } else if ("CODELIB".equals(item.getItemType()) || "DATE".equals(item.getItemType())) {
//                    sql = sql + " = '" + map.get(code) + "' ";
//                } else {
//                    sql = sql + " like '%" + map.get(item.getItemCode()) + "%' ";
//                }
//            }
//        }
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql, new HashMap<String, Object>());
//        List<LedgerLine> list = new ArrayList<>();
//        for (Map<String, Object> ledgerLineMap : resultList) {
//            LedgerLine ledgerLine = new LedgerLine();
//            Collection<LedgerItem> set = new ArrayList<>();
//            for (String s : ledgerLineMap.keySet()) {
//                LedgerItem item = new LedgerItem();
//                item.setItemCode(s);
//                item.setValue(ledgerLineMap.get(s));
//                set.add(item);
//            }
//            ledgerLine.setLedgerItems(set);
//            list.add(ledgerLine);
//        }
//        return list;
//    }
//
//    @Override
//    public List<LedgerLine> findAllDataByCondition(LedgerReport ledgerReport, Map<String, Object> map, Collection<LedgerItem> collection) {
//        StringBuffer sql = new StringBuffer();
//        sql.append("select d.ID from " + ledgerReport.getLedgerReportTemplate().getTableName() + " d,Institution i where 1=1");
//        if (!StringUtil.isEmpty(map.get("institutionName").toString())) {
//            sql.append(" AND i.institutionId=d.ORG_ID");
//            sql.append(" AND institutionName like '%" + map.get("institutionName") + "%'");
//        }
//        sql.append(" AND GATHER_DATE='" + map.get("gather_Date") + "'");
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql.toString(), new HashMap<String, Object>());
//        List<LedgerLine> list = new ArrayList<>();
//        for (Map<String, Object> ledgerLineMap : resultList) {
//            LedgerLine ledgerLine = new LedgerLine();
//            Set<LedgerItem> set = new LinkedHashSet<>();
//            for (String s : ledgerLineMap.keySet()) {
//                LedgerItem item = new LedgerItem();
//                item.setItemCode(s);
//                item.setValue(ledgerLineMap.get(s));
//                set.add(item);
//            }
//            ledgerLine.setLedgerItems(set);
//            list.add(ledgerLine);
//        }
//
//        return list;
//    }
//
    @Override
    public Boolean isDeleteAble(Long id) {
        Boolean flag = true;
        StringBuffer sb = new StringBuffer();
        sb.append("select count(*) from ReportPermission f,Role_ReportPermission r ");
        sb.append(" where f.id=r.reportPermission_id");
        sb.append(" and f.accounttemplate_id=" + id);
        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sb.toString(), new HashMap<String, Object>());
        if (!"0".equals(resultList.get(0).get("count(*)").toString())) {
            flag = false;
        }
        return flag;
    }
//
//    public List<LedgerReport> findDataByRepordIdIn(LedgerReportTemplate ledgerReportTemplate, Collection<LedgerReport> ledgerReports) {
//        StringBuilder sql = new StringBuilder();
//        sql.append("select distinct lrt.reportId from  " + ledgerReportTemplate.getTableName() + " lrt where lrt.reportId in(");
//        Iterator<LedgerReport> it = ledgerReports.iterator();
//        while (it.hasNext()) {
//            LedgerReport lr = it.next();
//            sql.append(lr.getId() + ",");
//        }
//        sql.deleteCharAt(sql.length() - 1);
//        sql.append(")");
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql.toString(), new HashMap<String, Object>());
//        List<LedgerReport> list = new ArrayList<>();
//        for (Map<String, Object> ledgerLineMap : resultList) {
//            LedgerReport ledgerReport = new LedgerReport();
//            for (String s : ledgerLineMap.keySet()) {
//                ledgerReport.setId(Long.parseLong(ledgerLineMap.get(s).toString()));
//            }
//            list.add(ledgerReport);
//        }
//        return list;
//    }
//
    @Override
    public Boolean dataIsExist(AccountTemplate accountTemplate) {
        Boolean flag = false;
        String s = "select count(1) from " + accountTemplate.getTableName();
        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(s, new HashMap<String, Object>());
        if (null !=resultList && !resultList.isEmpty()) {
            Object obj = resultList.get(0).get("count(1)");
            if(null != obj && !obj.toString().equals("0")){
                flag = true;
            }
        }
        return flag;
    }
//
    @Override
    public void dropAllTemplate(AccountTemplate accountTemplate) {
        String tableName = accountTemplate.getTableName().toUpperCase();
//        String history = tableName + "_" + TableNameType.HISTORY.getTableName().toUpperCase();
        String standard = tableName + "_" + TableNameType.STANDARD.getTableName().toUpperCase();
//        String origin = tableName + "_" + TableNameType.ORIGIN.getTableName().toUpperCase();
//        String zipper = tableName + "_" + TableNameType.ZIPPER.getTableName().toUpperCase();
        dropTemplate(tableName);
//        dropTemplate(history);
        dropTemplate(standard);
//        dropTemplate(origin);
//        dropTemplate(zipper);
    }
    private void dropTemplate(String tableName){
    	String sql = "select count(1) from user_tables where table_name = '" + tableName + "'";
        int count = this.getNamedParameterJdbcTemplate().getJdbcOperations().queryForObject(sql, int.class);
        if (count > 0) {
            sql = "DROP TABLE " + tableName;
            this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
        }
    }
//
//    @Override
//    public Boolean IsRoleUse(Long RoleId) {
//        Boolean flag = false;
//        StringBuffer sb = new StringBuffer();
//        sb.append("select count(*) from Role r,SysUser_Role ur ");
//        sb.append(" where r.id=ur.roles_id");
//        sb.append(" and r.id=" + RoleId);
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sb.toString(), new HashMap<String, Object>());
//        if (!"0".equals(resultList.get(0).get("count(*)").toString())) {
//            flag = true;
//        }
//        return flag;
//    }
//
//    @Override
//    public List<LedgerLine> findLederLine(LedgerReport ledgerReport, Map<String, Object> map, Collection<LedgerItem> collection) {
//        String selectFiled = new String();
//        if (collection != null && collection.size() > 0) {
//            for (LedgerItem ledgerItem : collection) {
//                selectFiled = selectFiled + ledgerItem.getItemCode() + ",";
//            }
//            selectFiled = selectFiled.substring(0, selectFiled.length() - 1);
//        }
//        String sql = "select id," + selectFiled + " from " + ledgerReport.getLedgerReportTemplate().getTableName() + " where  reportId=" + ledgerReport.getId();
//        if (null != map.get("LedgerLineId")) {
//            sql = sql + " and id=" + map.get("LedgerLineId");
//        }
//        for (LedgerItem item : collection) {
//            String code = item.getItemCode();
//            if (map.get(code) != null) {
//                sql = sql + "and " + code;
//                if ("INTEGER".equals(item.getItemType()) || "DOUBLE".equals(item.getItemType())) {
//                    sql = sql + " = " + map.get(code) + " ";
//                } else if ("CODELIB".equals(item.getItemType()) || "DATE".equals(item.getItemType())) {
//                    sql = sql + " = '" + map.get(code) + "' ";
//                } else {
//                    sql = sql + " like '%" + map.get(item.getItemCode()) + "%' ";
//                }
//            }
//        }
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql, new HashMap<String, Object>());
//        List<LedgerLine> list = new ArrayList<>();
//        for (Map<String, Object> ledgerLineMap : resultList) {
//            LedgerLine ledgerLine = new LedgerLine();
//            Collection<LedgerItem> set = new ArrayList<>();
//            for (String s : ledgerLineMap.keySet()) {
//                LedgerItem item = new LedgerItem();
//                if ("id".equals(s)) {
//                    ledgerLine.setId(Long.parseLong(String.valueOf(ledgerLineMap.get(s))));
//                    continue;
//                }
//                item.setItemCode(s);
//                item.setValue(ledgerLineMap.get(s));
//                set.add(item);
//            }
//            ledgerLine.setLedgerItems(set);
//            list.add(ledgerLine);
//        }
//        return list;
//    }
//
//    @Override
//    public List<LedgerLine> findDataByLedgerReportId(LedgerReport ledgerReport) {
//        String sql = "select * from  " + ledgerReport.getLedgerReportTemplate().getTableName() + " where reportId=" + ledgerReport.getId();
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql, new HashMap<String, String>());
//        List<LedgerLine> list = new ArrayList<>();
//        for (Map<String, Object> ledgerLineMap : resultList) {
//            LedgerLine ledgerLine = new LedgerLine();
//            Set<LedgerItem> set = new LinkedHashSet<>();
//            for (String s : ledgerLineMap.keySet()) {
//                LedgerItem item = new LedgerItem();
//                item.setItemCode(s);
//                item.setValue(ledgerLineMap.get(s));
//                set.add(item);
//            }
//            ledgerLine.setLedgerItems(set);
//            list.add(ledgerLine);
//        }
//        return list;
//    }
//
//    @Override
//    public List<LedgerLine> findSpecifyDataByLedgerReportId(LedgerReportVo vo) {
//        String sql = "select " + vo.getRecordColumn() + " from  " + vo.getLedgerReport().getLedgerReportTemplate().getTableName() + " where reportId=" + vo.getLedgerReport().getId();
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql, new HashMap<String, String>());
//        List<LedgerLine> list = new ArrayList<>();
//        for (Map<String, Object> ledgerLineMap : resultList) {
//            LedgerLine ledgerLine = new LedgerLine();
//            Set<LedgerItem> set = new LinkedHashSet<>();
//            for (String s : ledgerLineMap.keySet()) {
//                LedgerItem item = new LedgerItem();
//                item.setItemCode(s);
//                item.setValue(ledgerLineMap.get(s));
//                set.add(item);
//            }
//            ledgerLine.setLedgerItems(set);
//            list.add(ledgerLine);
//        }
//        return list;
//    }
//
//
//    @Override
//    @Transactional
//    public void selBatchInsert(Collection<LedgerLine> ledgerLines, String tableName) {
//        for (LedgerLine ledgerLine : ledgerLines) {
//            String sql = "insert into " + tableName + "(";
//            Collection<LedgerItem> items = ledgerLine.getLedgerItems();
//            for (LedgerItem item : items) {
////                if ("ID".equals(item.getItemCode())) {
////                    continue;
////                }
//                sql = sql + item.getItemCode() + ",";
//            }
//            sql = sql.substring(0, sql.length() - 1) + ")values(";
//            for (LedgerItem item : items) {
////                if ("ID".equals(item.getItemCode())) {
////                    continue;
////                }
//                if (item.getValue() == null) {
//                    sql = sql + "null,";
//                } else {
//                    sql = sql + "'" + item.getValue() + "',";
//                }
//            }
//            sql = sql.substring(0, sql.length() - 1) + ")";
//
//            this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
//        }
//    }
//
//    @Override
//    public void selBatchInsert(LedgerReport ledgerReport, String tableName) {
//        String sql = "insert into " + tableName + " select * from " + ledgerReport.getLedgerReportTemplate().getTemplateCode() + " where reportId=" + ledgerReport.getId();
//        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, Object>());
//    }
//
//
//    @Override
//    public void updateStatus(LedgerLine ledgerLine, LedgerReportTemplate ledgerReportTemplate) {
//        String sql = "update " + ledgerReportTemplate.getTableName() + " set ";
//        sql = sql + "check_status='1' where id=" + ledgerLine.getId();
//        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
//    }
//
//    public Map<String, String> getResult(ValidateResultParameter vp, ValidateBatch batch, String tableName) {
//        Map<String, String> map = new HashMap<>();
//        StringBuffer sql = new StringBuffer()
//                .append("SELECT DISTINCT t2.id,t2.check_rule_id,t2.validate_column,l.itemName ")
//                .append(" FROM dqc_validate_result t2,LedgerItem l,  ")
//                .append(" dqc_validate_result_details t1  ")
//                .append(" where t1.validateResult_id = t2.id and l.itemCode=t2.validate_column")
//                .append(" and validate_batch='" + batch.getBatchId() + "'")
//                .append(" and EXISTS (")
//                .append("select d.ID from " + tableName + " d,Institution i where 1=1 and t1.details_key_group = d.ID ");
//        if (!StringUtil.isEmpty(vp.getInstitutionName())) {
//            sql.append(" AND i.institutionId=d.ORIG_ORG_ID AND i.institutionName like '%" + vp.getInstitutionName() + "%'");
//        }
//        if (!StringUtil.isEmpty(vp.getGatherDate())) {
//            sql.append(" AND d.GATHER_DATE=to_date('" + vp.getGatherDate() + "','yyyy-mm-dd')");
//        }
//        sql.append(")");
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql.toString(), new HashMap<String, String>());
//        for (Map<String, Object> resultMap : resultList) {
//            map.put(resultMap.get("id").toString(), resultMap.get("itemName").toString());
//        }
//        return map;
//    }
//
//    public Map<String, String> getResultForEAST(ValidateResultParameter vp, ValidateBatch batch, String tableName) {
//        Map<String, String> map = new HashMap<>();
//        StringBuffer sql = new StringBuffer()
//                .append("SELECT DISTINCT t2.id,t2.check_rule_id,t2.validate_column,l.itemName ")
//                .append(" FROM dqc_validate_result t2,LedgerItem l,  ")
//                .append(" dqc_validate_result_details t1  ")
//                .append(" where t1.validateResult_id = t2.id and l.itemCode=t2.validate_column")
//                .append(" and validate_batch='" + batch.getBatchId() + "'")
//                .append(" and EXISTS (")
//                .append("select d.ID from " + tableName + " d,Institution i where 1=1 and t1.details_key_group = d.ID ");
//        if (!StringUtil.isEmpty(vp.getInstitutionName())) {
//            sql.append(" AND i.institutionId=d.nbjgh AND i.institutionName like '%" + vp.getInstitutionName() + "%'");
//        }
//        if (!StringUtil.isEmpty(vp.getGatherDate())) {
//            sql.append(" AND d.GATHER_DATE=to_date('" + vp.getGatherDate() + "','yyyy-mm-dd')");
//        }
//        sql.append(")");
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql.toString(), new HashMap<String, String>());
//        for (Map<String, Object> resultMap : resultList) {
//            map.put(resultMap.get("id").toString(), resultMap.get("itemName").toString());
//        }
//        return map;
//    }
//
//    public String getLedgerLineId(String tableName, ValidateResultParameter vo) {
//        String ledgerLineIds = "";
//        StringBuffer sb = new StringBuffer();
//        sb.append("SELECT DISTINCT D.id FROM");
//        sb.append(" dqc_validate_result t2,");
//        sb.append(tableName + "  D,");
//        sb.append(" dqc_validate_result_details t1,");
//        sb.append(" Institution i");
//        sb.append(" WHERE t1.validateResult_id = t2.id");
//        if (!StringUtil.isEmpty(vo.getInstitutionName())) {
//            sb.append(" AND D.ORG_ID=i.institutionId");
//            sb.append(" AND i.institutionName like '%" + vo.getInstitutionName() + "%'");
//        }
//        if(!StringUtil.isEmpty(vo.getGatherDate())){
//            sb.append(" AND D.GATHER_DATE=to_date('" + vo.getGatherDate() + "','yyyy-mm-dd')");
//        }
//        sb.append(" And t2.check_rule_id='" + vo.getRuleId() + "'");
//        sb.append(" AND  D.ID=t1.details_key_group");
//        sb.append(" AND t2.validate_column='" + vo.getItemCode() + "'");
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sb.toString(), new HashMap<String, String>());
//        for (Map<String, Object> resultMap : resultList) {
//            ledgerLineIds = ledgerLineIds + resultMap.get("id").toString() + ",";
//        }
//        if (ledgerLineIds.length() > 0) {
//            ledgerLineIds = ledgerLineIds.substring(0, ledgerLineIds.length() - 1);
//        }
//        return ledgerLineIds;
//    }
//
//    public String getLedgerLineIdForEAST(String tableName, ValidateResultParameter vo) {
//        String ledgerLineIds = "";
//        StringBuffer sb = new StringBuffer();
//        sb.append("SELECT DISTINCT D.id FROM");
//        sb.append(" dqc_validate_result t2,");
//        sb.append(tableName + "  D left join LEDGERREPORT r on D.REPORTID = r.id,");
//        sb.append(" dqc_validate_result_details t1,");
//        sb.append(" Institution i");
//        sb.append(" WHERE t1.validateResult_id = t2.id");
//        if (!StringUtil.isEmpty(vo.getInstitutionName())) {
//            sb.append(" AND r.institution_id=i.id");
//            sb.append(" AND i.institutionName like '%" + vo.getInstitutionName() + "%'");
//        }
//        if(!StringUtil.isEmpty(vo.getGatherDate())){
//            sb.append(" AND D.cjrq=to_date('" + vo.getGatherDate() + "','yyyy-mm-dd')");
//        }
//        sb.append(" And t2.check_rule_id='" + vo.getRuleId() + "'");
//        sb.append(" AND  D.ID=t1.details_key_group");
//        sb.append(" AND t2.validate_column='" + vo.getItemCode() + "'");
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sb.toString(), new HashMap<String, String>());
//        for (Map<String, Object> resultMap : resultList) {
//            ledgerLineIds = ledgerLineIds + resultMap.get("id").toString() + ",";
//        }
//        if (ledgerLineIds.length() > 0) {
//            ledgerLineIds = ledgerLineIds.substring(0, ledgerLineIds.length() - 1);
//        }
//        return ledgerLineIds;
//    }
//
//    public List<ValidateResultVo> findValidateResultByCondition(ValidateResultParameter parameters, String tableName) {
//        StringBuffer sb = new StringBuffer();
//        List<ValidateResultVo> list = new ArrayList<>();
//        sb.append("SELECT DISTINCT ");
//        if ("DEPOSIT_STOCK".equals(tableName)) {
//            sb.append("D.AGREEMENT_ID,");
//        } else {
//            sb.append("D.LOA_DUEBILLID,");
//        }
//        sb.append("D.ID lineId,t2.ID,l.itemName,l.itemType,l.itemCode,l.codeLib_Id  FROM ")
//                .append(tableName + " D,")
//                .append("dqc_validate_result_details t1,")
//                .append("LedgerItem l,")
//                .append(" dqc_validate_result t2,")
//                .append(" Institution i ")
//                .append(" WHERE t1.validateResult_id = t2.id ")
//                .append(" AND l.itemCode=t2.validate_column ");
//        if (!StringUtil.isEmpty(parameters.getInstitutionName())) {
//            sb.append(" AND D.ORG_ID=i.institutionId")
//                    .append(" AND i.institutionName like'%" + parameters.getInstitutionName() + "'");
//        }
//
//
//        sb.append(" AND  D.ID=t1.details_key_group")
//                .append(" AND D.GATHER_DATE='" + parameters.getGatherDate() + "'")
//                .append("  And t2.check_rule_id='" + parameters.getRuleId() + "'")
//                .append(" AND  t2.validate_column='" + parameters.getItemCode() + "'");
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sb.toString(), new HashMap<String, String>());
//        for (Map<String, Object> resultMap : resultList) {
//            ValidateResultVo validateResultVo = new ValidateResultVo();
//            validateResultVo.setValidateColumn(resultMap.get("itemCode") == null ? "" : resultMap.get("itemCode").toString());
//            validateResultVo.setId(resultMap.get("ID") == null ? "" : resultMap.get("ID").toString());
//            validateResultVo.setColumnName(resultMap.get("itemName").toString() == null ? "" : resultMap.get("itemName").toString());
//            validateResultVo.setItemType(resultMap.get("itemType") == null ? "" : resultMap.get("itemType").toString());
//            validateResultVo.setCodeLibId(resultMap.get("codeLib_Id") == null ? "" : resultMap.get("codeLib_Id").toString());
//            validateResultVo.setLedgerLineId(resultMap.get("lineId") == null ? "" : resultMap.get("lineId").toString());
//            list.add(validateResultVo);
//        }
//        return list;
//    }
//
//    @Override
//    public long getCountBySql(String sql) {
//        long cnt = 0;
//        String s = "select count(*) from (" + sql +") t";
//        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(s, new HashMap<String, Object>());
//        cnt = Long.parseLong(resultList.get(0).get("count(*)").toString());
//        return cnt;
//    }
//
//    @Override
//    public List<Map<String, Object>>  getResultBySql(String sql) {
//        return   this.getNamedParameterJdbcTemplate().queryForList(sql, new HashMap<String, Object>());
//    }
//
//    @Override
//    public long getCountBySqlAndParameter(String sql, HashMap<String, Object> params) {
//        long cnt =0;
//        List<Map<String, Object>> resultList  = getNamedParameterJdbcTemplate().queryForList(sql, params);
//        cnt = Long.parseLong(resultList.get(0).get("count(*)").toString());
//        return cnt;
//    }
//
//    @Override
//    public  List<Map<String, Object>>  getResultBySqlAndParameter(String sql, HashMap<String, Object> values) {
//        return  this.getNamedParameterJdbcTemplate().queryForList(sql,values);
//    }

    public List<Map<String,Object>> getAllDate(AccountTemplate accountTemplate,Long id){  	
    	String sql = "select null";
    	for(AccountField accountField:accountTemplate.getAccountFields()){
    		if(accountField.isVisible()){
    			sql+=","+accountField.getItemCode();
    		}
    	}
    	sql+=" from "+accountTemplate.getTemplateCode()+" where reportid="+id;
    	List<Map<String,Object>> list = this.getNamedParameterJdbcTemplate().getJdbcOperations().queryForList(sql);
    	return list;
    }
}
