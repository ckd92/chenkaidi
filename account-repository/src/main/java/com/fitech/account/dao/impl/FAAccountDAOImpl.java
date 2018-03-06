package com.fitech.account.dao.impl;

import com.fitech.account.dao.FAAccountDAO;
import com.fitech.domain.ledger.FAAccountField;
import com.fitech.domain.ledger.FAAccountRow;
import com.fitech.domain.ledger.FAFieldFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by chun on 16/9/4.
 */
//@Repository
public class FAAccountDAOImpl extends NamedParameterJdbcDaoSupport implements FAAccountDAO {

    private static final Logger log = Logger.getLogger(FAAccountDAOImpl.class.getName());
    private String accountTblName;

    @Autowired
    public FAAccountDAOImpl(DataSource dataSource) {
        setDataSource(dataSource);
        try {
            dataSource.getConnection().setAutoCommit(true);

        } catch (Exception e) {

        }
    }

    @Override
    public void setAccountTblName(String accountTblName) {
        this.accountTblName = accountTblName;
    }

    @Override
    public FAAccountRow getRowTemplate() {
        return this.getRowTemplate(this.accountTblName);
    }

    @Override
    public FAAccountRow getRowTemplate(String accountTblName) {

        Connection connection = null;

        FAAccountRow row = new FAAccountRow(accountTblName);
        FAFieldFactory factory = new FAFieldFactory();

        try {
            connection = DataSourceUtils.getConnection(this.getDataSource());

            String catalog = null;
            String schemaPattern = null;
            String tableNamePattern = accountTblName;
            String columnNamePattern = null;


            DatabaseMetaData databaseMetaData = connection.getMetaData();

            ResultSet result = databaseMetaData.getColumns(
                    catalog, schemaPattern, tableNamePattern, columnNamePattern);

            while (result.next()) {
                log.log(Level.INFO,
                        "  " + result.getString("TABLE_SCHEM")
                                + ", " + result.getString("TABLE_NAME")
                                + ", " + result.getString("COLUMN_NAME")
                                + ", " + result.getString("TYPE_NAME")
                                + ", " + result.getInt("COLUMN_SIZE")
                                + ", " + result.getInt("NULLABLE"));

                FAAccountField field = factory.getFieldInstance(result.getString("COLUMN_NAME"), result.getString("TYPE_NAME"));
                field.setFieldLength(result.getInt("COLUMN_SIZE"));
                row.addField(field);

            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }


        return row;

    }

    @Override
    @Transactional
    public void insertRow(FAAccountRow row) {

        Map<String, Object> parameters = new HashMap<>();

        String sql = "INSERT INTO " + row.getTemplateName() + "(";
        String paraStr = " VALUES (";

        for (Map.Entry<String, FAAccountField> entry : row.getFields().entrySet()) {

            if (!entry.getKey().equals("ID")) {
                sql += entry.getKey() + ", ";
                paraStr += ":" + entry.getKey() + ", ";

                parameters.put(entry.getKey(), entry.getValue().getFieldValue());
            }
        }

        sql = sql.substring(0, sql.length() - 2) + ")";
        paraStr = paraStr.substring(0, paraStr.length() - 2) + ")";

        this.getNamedParameterJdbcTemplate().update(sql + paraStr, parameters);

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void updateRow(FAAccountRow row) {

        Map<String, Object> parameters = new HashMap<>();

        String sql = "UPDATE " + row.getTemplateName() + " SET ";
        String setStr = "";
        String where = " WHERE ID = " + row.getId();

        for (Map.Entry<String, FAAccountField> entry : row.getFields().entrySet()) {

            if (!entry.getKey().equals("ID")) {
                setStr += entry.getKey() + " = :" + entry.getKey() + ", ";
                parameters.put(entry.getKey(), entry.getValue().getFieldValue());
            }
        }

        sql = sql + setStr.substring(0, setStr.length() - 2) + where;

        this.getNamedParameterJdbcTemplate().update(sql, parameters);
    }

    @Override
    public FAAccountRow getRow(Long id) {

        String sql = "SELECT * FROM " + this.accountTblName + " WHERE ID =  " + id;
        Map<String, Object> resultMap = this.getNamedParameterJdbcTemplate().queryForMap(sql, new HashMap<String, String>());

        FAAccountRow row = this.getRowTemplate();

        for (Map.Entry<String, FAAccountField> fieldEntry : row.getFields().entrySet()) {
            FAAccountField field = fieldEntry.getValue();
            Object value = resultMap.get(field.getFieldName());
            field.setFieldValue(value);
        }

        row.setId(new Long((Integer) resultMap.get("ID")));

        return row;
    }

    @Override
    public List<FAAccountRow> getAllRows() {

        String sql = "SELECT * FROM " + this.accountTblName;
        List<Map<String, Object>> resultList = this.getNamedParameterJdbcTemplate().queryForList(sql, new HashMap<String, String>());

        List<FAAccountRow> rowList = getRowList(resultList);

        return rowList;
    }


    public List<FAAccountRow> getRowList(List<Map<String, Object>> resultList) {

        List<FAAccountRow> rowList = new ArrayList<>();
        FAAccountRow template = this.getRowTemplate();

        for (Map<String, Object> resultMap : resultList) {
            FAAccountRow row = null;
            try {
                row = template.clone();
                row.setId(new Long((Integer) resultMap.get("ID")));
                for (Map.Entry<String, FAAccountField> fieldEntry : row.getFields().entrySet()) {
                    FAAccountField field = fieldEntry.getValue();
                    Object value = resultMap.get(field.getFieldName());
                    field.setFieldValue(value);
                }
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            rowList.add(row);
        }

        return rowList;
    }

    @Override
    public void deleteRow(FAAccountRow row) {

        String sql = "DELETE FROM " + this.accountTblName + " WHERE ID = " + row.getId();
        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
    }

    @Override
    public void createTemplate(FAAccountRow row) {
        String sql = "CREATE TABLE " + row.getTemplateName() + "(";
        sql += "ID INTEGER NOT NULL AUTO_INCREMENT,";
        for (Map.Entry<String, FAAccountField> fieldEntry : row.getFields().entrySet()) {
            FAAccountField field = fieldEntry.getValue();
            sql += field.getFieldName() + " " + (field.getSqlType().toString().equals("VARCHAR") ? field.getSqlType().toString() + "(" + field.getFieldLength() + ")" : field.getSqlType().toString()) + ",";
        }
        sql += "PRIMARY KEY(ID)" + ")";
        log.info(sql);
        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
    }

    @Override
    public void dorpTemplate(FAAccountRow row) {
        String sql = "DROP TABLE IF EXISTS " + row.getTemplateName() + ";";
        this.getNamedParameterJdbcTemplate().update(sql, new HashMap<String, String>());
    }
}
