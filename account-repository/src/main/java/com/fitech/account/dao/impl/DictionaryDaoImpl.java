package com.fitech.account.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fitech.account.dao.DictionaryDao;
import com.fitech.framework.core.dao.Dao;
import com.fitech.framework.core.dao.mybatis.DaoMyBatis;

@Dao
public class DictionaryDaoImpl extends DaoMyBatis implements DictionaryDao {

	@Override
	public List<Map<String, Object>> getDictionaryItemByDictionaryId(Long id) {
		return super.selectList("dictionary.getDictionaryItemByDictionaryId", id);
	}

	@Override
	public List<Map<String, Object>> getDictionaryItemByDicItemName(Long id,
			String dicItemName) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("dicItemName", dicItemName);
		map.put("id", id);
		return super.selectList("dictionary.getDictionaryItemByDicItemName", map);
	}
	
}
