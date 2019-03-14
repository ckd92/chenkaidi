package com.fitech.account.dao;


import java.util.List;
import java.util.Map;

import com.fitech.domain.account.AccountTemplate;

/**
 * Created by SunBojun on 2017/2/28.
 */
public interface AccountTemplateDAO {
	/**
	 * 根据报文模板数据创建表结构
	 * @param accountTemplate
	 */
    public void createTemplate(AccountTemplate accountTemplate);
    /**
	 * 根据报文模板数据删除表结构
	 * @param accountTemplate
	 */
    public void dropTemplate(AccountTemplate accountTemplate);

//    public AccountTemplate getAccountTemplate(String tableName);
//    /**
//     * 报文数据新增
//     * @param ledgerLine
//     * @param ledgerReport
//     */
//    public void insertData(LedgerLine ledgerLine, LedgerReport ledgerReport);
//    /**
//     * 报文数据批量新增
//     * @param ledgerLines
//     * @param ledgerReport
//     */
//    public void batchInsert(Collection<LedgerLine> ledgerLines, LedgerReport ledgerReport);
//    /**
//     * 报文数据修改
//     * @param ledgerLine
//     * @param ledgerReport
//     */
//    public void updateData(LedgerLine ledgerLine, LedgerReport ledgerReport);
//    /**
//     * 报文数据批量修改
//     * @param ledgerLine
//     * @param ledgerReport
//     */
//    public void batchUpdate(Collection<LedgerLine> ledgerLines, LedgerReport ledgerReport);
//    /**
//     * 报文数据删除
//     * @param ledgerLines
//     * @param ledgerReport
//     */
//    public void deleteData(LedgerLine ledgerLine, LedgerReport ledgerReport);
//    /**
//     * 报文数据批量删除
//     * @param ledgerLines
//     * @param ledgerReport
//     */
//    public void batchDelete(Collection<LedgerLine> ledgerLines, LedgerReport ledgerReport);
//    /**
//     * 产出改报文表中所有数据
//     * @param ledgerReport
//     */
//    public void deleteAll(LedgerReport ledgerReport);
//
//    public LedgerLine findDataById(LedgerReport ledgerReport, Long id);
//
//    public List<LedgerLine> findDataByCondition(LedgerReport ledgerReport, Map<String, Object> map, Collection<LedgerItem> collection);
//
//    public List<LedgerLine> findAllDataByCondition(LedgerReport ledgerReport, Map<String, Object> map, Collection<LedgerItem> collection);
//
    public Boolean isDeleteAble(Long id);
//
//    public List<LedgerReport> findDataByRepordIdIn(LedgerReportTemplate ledgerReportTemplate, Collection<LedgerReport> ledgerReport);
//
    /**
     * 查询此报文对应的数据表是否有数据
     * @param accountTemplate
     * @return
     */
    public Boolean dataIsExist(AccountTemplate accountTemplate);
//
//    public Boolean IsRoleUse(Long RoleId);
//
//    public List<LedgerLine> findLederLine(LedgerReport ledgerReport, Map<String, Object> map, Collection<LedgerItem> collection);
//
//
    /**
     * 删除所有报文模板数据库表
     * @param accountTemplate
     */
    public void dropAllTemplate(AccountTemplate accountTemplate);
//
//    public List<LedgerLine> findDataByLedgerReportId(LedgerReport ledgerReport);
//
//    public List<LedgerLine> findSpecifyDataByLedgerReportId(LedgerReportVo vo);
//
//    public void selBatchInsert(Collection<LedgerLine> ledgerLines, String tableName);
//
//    /**
//     * 元数据同步
//     * @param ledgerReport 元数据表
//     * @param tableName 目标表
//     */
//    public void selBatchInsert(LedgerReport ledgerReport, String tableName);
//
//    /**
//     * 未通过校验的数据改变校验状态为1校验未通过
//     * @param ledgerLine
//     */
//    public void updateStatus(LedgerLine ledgerLine, LedgerReportTemplate ledgerReportTemplate);
//
//    public Map<String,String> getResult(ValidateResultParameter vo, ValidateBatch batch, String tableName);
//
//    public Map<String,String> getResultForEAST(ValidateResultParameter vo, ValidateBatch batch, String tableName);
//
//    /**
//     * 批量修改单个字段
//     * @param
//     * @param item
//     * @param tableName
//     */
//    public void batchUpdateData(ValidateResultParameter parameters, ValidateBatch batch, LedgerItem item, String tableName);
//
//    public void batchUpdateDataForEAST(ValidateResultParameter parameters, ValidateBatch batch, LedgerItem item, String tableName);
//
// /**
//  * 查询需要修改的数据ID
//  * @param tableName
//  * @param vo
//  * @return
//  */
//    public String getLedgerLineId(String tableName, ValidateResultParameter vo);
//
//    public String getLedgerLineIdForEAST(String tableName, ValidateResultParameter vo);
//
//    /**
//     * 模糊查询校验结果
//     * @param parameters
//     * @param tableName
//     * @return
//     */
//
//    public List<ValidateResultVo> findValidateResultByCondition(ValidateResultParameter parameters, String tableName);
//
//    /**
//     * 查询某sql的记录总数
//     * @param sql
//     * @return
//     */
//    public long getCountBySql(String sql);
//
//    /**
//     * s
//     * @param sql
//     * @return
//     */
//    public  List<Map<String, Object>>  getResultBySql(String sql);
//
//    public long  getCountBySqlAndParameter(String sql, HashMap<String, Object> values) ;
//
//
//    public  List<Map<String, Object>>  getResultBySqlAndParameter(String sql, HashMap<String, Object> values) ;
    public List<Map<String,Object>> getAllDate(AccountTemplate accountTemplate,Long id);
}
