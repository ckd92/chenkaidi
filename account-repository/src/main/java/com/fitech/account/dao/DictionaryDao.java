package com.fitech.account.dao;

import com.fitech.domain.account.Dictionary;
import com.fitech.domain.account.DictionaryItem;
import com.fitech.dto.DictionaryDto;
import com.fitech.dto.DictionaryItemDto;
import com.fitech.vo.account.AccountDicVo;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.List;
import java.util.Map;



public interface DictionaryDao {
	public List<Map<String,Object>> getDictionaryItemByDictionaryId(Long id);

	public List<DictionaryItemDto> getDictionaryItemByDictId(Long id);
	public List<Map<String,Object>> getDictionaryItemByDicItemName(Long id,String dicItemName);

	public Dictionary getNextDicId(Long Id);

	public Dictionary getDicByParentOrId(Long Id,Long parentId,String isenable);

	public List<DictionaryDto> getAllDic();

	public int addDictionary(Dictionary dictionary);

	public int addDictionaryItem(DictionaryItem dictionaryItem);

	public List<AccountDicVo> searchDictionary();
}
