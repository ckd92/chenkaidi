package com.fitech.account.dao;

/**
 * Created by SunBojun on 2017/4/5.
 */
public interface AccountFieldDAO {

    /**
     * 判断字段是否能被删除
     * @param id
     * @return
     */
    public Boolean isDeleteAble(Long id);
    
    /**
     * 判断字典是否能被报表修改
     * @param id
     * @return
     */
    public Boolean dicIsChangeable(Long id);
    
    
    /**
     * 判断字典是否被模板使用
     * @param id
     * @return
     */
    public Boolean dicIsTemplateUsed(Long id);


}
