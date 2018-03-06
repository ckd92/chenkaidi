package com.fitech.account.dao;

import com.fitech.domain.system.User;
import com.fitech.vo.account.AccountProcessVo;

import java.util.List;

import org.springframework.data.domain.Page;

public interface AccountProcessDao {
	
	/**
	 * 获取待办任务
	 * @param vo
	 * @param user
	 * @return
	 */
	public Page<AccountProcessVo> findTodoTaskBySql(AccountProcessVo vo, User user);
	
	/**
	 * 分页获取已审核过得任务(查询)
	 * @param vo
	 * @param user
	 * @return
	 */
	public Page<AccountProcessVo> findDoneQuerySql(AccountProcessVo vo);
	
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
	public Page<AccountProcessVo> findDoneTaskBySql(AccountProcessVo vo, User user);

}
