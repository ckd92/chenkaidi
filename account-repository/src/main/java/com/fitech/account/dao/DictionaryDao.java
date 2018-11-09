package com.fitech.account.dao;

import java.util.List;
import java.util.Map;



public interface DictionaryDao {
	public List<Map<String,Object>> getDictionaryItemByDictionaryId(Long id);
	
	public List<Map<String,String>> getDictionaryItemByDicItemName(Long id,String dicItemName);
}
