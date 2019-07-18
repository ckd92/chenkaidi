package com.fitech.account.dao;

import com.fitech.domain.account.Dictionary;
import com.fitech.dto.DictionaryItemDto;

import java.util.List;
import java.util.Map;



public interface DictionaryDao {
	public List<Map<String,Object>> getDictionaryItemByDictionaryId(Long id);

	public List<DictionaryItemDto> getDictionaryItemByDictId(Long id);
	public List<Map<String,Object>> getDictionaryItemByDicItemName(Long id,String dicItemName);

	public Dictionary getNextDicId(Long Id);
}
