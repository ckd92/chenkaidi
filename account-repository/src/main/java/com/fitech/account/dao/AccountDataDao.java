package com.fitech.account.dao;

import java.util.List;

import com.fitech.domain.account.Account;
import com.fitech.domain.account.AccountLine;
import com.fitech.framework.lang.page.Page;
import com.fitech.vo.account.AccountProcessVo;

public interface AccountDataDao {
	
//	/**
//	 * 批量导入数据表数据
//	 * @param accountId
//	 * @param accountTemplate
//	 * @param sheet
//	 * @return 字符串集合  第一个元素表示是否添加成功，第二个元素表示提示行数
//	 */
//	public List<String> loadDataByTemplate(Long accountId, AccountTemplate accountTemplate, Sheet sheet, Account account);

	/**
	 * 查询台账表数据
	 * @param accountProcessVo
	 * @return
	 */
	public List<AccountLine> findDataByCondition(AccountProcessVo accountProcessVo, Page page);
	
	
	/**
	 * 下载台账数据
	 * @param accountProcessVo
	 * @return
	 */
	public List<AccountLine> downLoadDataByCondition(AccountProcessVo accountProcessVo);

	/**
	 * 查询总数量
	 * @param accountProcessVo
	 * @return
	 */
	public Long findMaxNumDataByCondition(AccountProcessVo accountProcessVo);

	/**
	 * 插入单条台账数据
	 * @param accountLine
	 * @param account
	 */
	public void insertData(AccountLine accountLine, Account account);

	/**
	 * 更新单条台账数据
	 * @param accountLine
	 * @param account
	 */
	public void updateData(AccountLine accountLine, Account account);

	/**
	 * 批量更新台账数据
	 * @param accountLine
	 * @param account
	 */
	public void batchUpdateData(AccountLine accountLine, Account account, List<Long> lineId);

	/**
	 * 删除单条数据
	 * @param accountLine
	 * @param account
	 */
	public void deleteData(AccountLine accountLine, Account account);


	/**
	 * 查询单条台账数据
	 * @param account
	 * @param id
	 * @return
	 */
	public AccountLine findDataById(Account account, Long id);

	/**
	 *
	 * 根据业务主键查询新增台账数据是否存在
	 * @param accountLine
	 * @param account
	 * @return
	 */
	public Boolean queryDataisExist(AccountLine accountLine, Account account);
}
