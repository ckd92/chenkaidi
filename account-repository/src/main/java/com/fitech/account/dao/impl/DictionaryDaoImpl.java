package com.fitech.account.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fitech.account.dao.DictionaryDao;
import com.fitech.domain.account.Dictionary;
import com.fitech.dto.DictionaryDto;
import com.fitech.dto.DictionaryItemDto;
import com.fitech.framework.core.dao.Dao;
import com.fitech.framework.core.dao.mybatis.DaoMyBatis;

@Dao
public class DictionaryDaoImpl extends DaoMyBatis implements DictionaryDao {

	@Override
	public List<Map<String, Object>> getDictionaryItemByDictionaryId(Long id) {
		return super.selectList("dictionary.getDictionaryItemByDictionaryId", id);
	}

	@Override
	public List<DictionaryItemDto> getDictionaryItemByDictId(Long id) {
		return super.selectList("dictionary.getDictionaryItemByDictId", id);

	}

	@Override
	public List<Map<String, Object>> getDictionaryItemByDicItemName(Long id,
			String dicItemName) {
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("dicItemName", dicItemName);
		map.put("id", id);
		return super.selectList("dictionary.getDictionaryItemByDicItemName", map);
	}

	@Override
	public Dictionary getNextDicId(Long Id) {
		return super.selectOne("dictionary.getNextDicId",Id);
	}

	@Override
	public Dictionary getDicByParentOrId(Long Id,Long parentId,String isenable){
		Map<String,Object> map = new HashMap<>();
		String sql = "";
		if(null != Id) {
			sql = "id = " + Id;
		}
		if(null != parentId){
			sql = "PARENTID = " + parentId ;
		}
		if(null != isenable){
			sql += " and isenable = '" + isenable + "'";
		}
		map.put("sql" ,sql);
		return super.selectOne("dictionary.getDicId",map);
	}

	@Override
	public List<DictionaryDto> getAllDic() {
		return super.selectList("dictionary.getAllDic");
	}

}
