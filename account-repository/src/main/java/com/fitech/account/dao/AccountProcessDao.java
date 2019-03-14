package com.fitech.account.dao;

import com.fitech.domain.system.User;
import com.fitech.framework.lang.page.Page;
import com.fitech.vo.account.AccountProcessVo;

import java.util.List;


public interface AccountProcessDao {
	
	/**
	 * 获取待办任务
	 * @param vo
	 * @param user
	 * @return
	 */
	public List<AccountProcessVo> findTodoTaskBySql(AccountProcessVo vo, User user, Page page);
	
	/**
	 * 分页获取已审核过得任务(查询)
	 * @param vo
	 * @return
	 */
	public List<AccountProcessVo> findDoneQuerySql(AccountProcessVo vo,Page page);
	
	/**
	 * 获取全部已审核过得任务(下载)
	 * @param vo
	 * @param user
	 * @return
	 */
	public List<AccountProcessVo> findDoneQuerySqltwo(AccountProcessVo vo);

	/**
	 * 获取已办任务
	 * @param vo
	 * @param user
	 * @return
	 */
	public List<AccountProcessVo> findDoneTaskBySql(AccountProcessVo vo, User user,Page page);

	/**
	 * 创建待上报记录
	 * @param term
	 */
	public void createAccountTask(String term);
}
