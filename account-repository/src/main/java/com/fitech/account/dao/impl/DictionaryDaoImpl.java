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
	public List<Map<String, String>> getDictionaryItemByDictionaryId(Long id) {
		return super.selectList("dictionary.getDictionaryItemByDictionaryId", id);
	}

	@Override
	public List<Map<String, String>> getDictionaryItemByDicItemName(Long id,
			String dicItemName) {
		Map<String,String> map = new HashMap<String,String>();
		map.put("dicItemName", dicItemName);
		map.put("id", String.valueOf(id));
		return super.selectList("dictionary.getDictionaryItemByDicItemName", map);
	}
	
}
