//package com.fitech.acount.service;
//
//import com.fitech.framework.core.junit.JunitCase;
//import com.fitech.account.service.DictionaryItemService;
//import com.fitech.account.service.DictionaryService;
//import com.fitech.domain.account.Dictionary;
//import com.fitech.domain.account.DictionaryItem;
//import java.util.List;
//
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//
///**
// * Created by wangxw on 2017/7/25.
// */
//public class DictionartServiceTests extends JunitCase{
//
//	@Autowired
//	private DictionaryService dictionaryService;
//
//	@Autowired
//	private DictionaryItemService dictionaryItemService;
//
//	//@Test
//	public void testFindAll(){
//		System.out.println("222222222");
//		/*List<Dictionary> list = dictionaryService.findDictionary(null);
//    		for(Dictionary dictionary:list){
//    			System.out.println("getDicId---------"+dictionary.getId());
//    		}
//    		System.out.println("findAll----------------"+list);*/
//		Dictionary dictionary = new Dictionary();
//		dictionary.setId(30210035L);
//		DictionaryItem dictionaryItem = new DictionaryItem();
//		dictionaryItem.setDictionary(dictionary);
//		List<DictionaryItem> list = dictionaryItemService.findDictionaryItem(dictionaryItem);
//		for(DictionaryItem d:list){
//			System.out.println(d.getId()+"-------"+d.getDicItemId()+":"+d.getDicItemName());
//		}
//
//	}
//
//	//@Test
//	public void testAdd(){
//		Dictionary dictionary = new Dictionary();
//    	
//    	dictionary.setDicName("名称6");
//    	dictionaryService.saveDictionary(dictionary);
//		/*Dictionary dictionary = new Dictionary();
//		dictionary.setId(30210144L);
//		
//		DictionaryItem dictionaryItem = new DictionaryItem();
//		dictionaryItem.setDicItemId("222");
//		dictionaryItem.setDicItemName("111");
//		dictionaryItem.setDicItemDesc("222");
//		dictionaryItem.setDictionary(dictionary);
//		dictionaryItemService.saveDictionaryItem(dictionaryItem);*/
//		
//
//	}
//
//	@Test
//	public void testUpdate(){
//		/*Dictionary dictionary = new Dictionary();
//    	dictionary.setDicId("编码4");
//    	//dictionary.setDicName("名称3");
//    	dictionaryService.updateDictionary(30210035L, dictionary);*/
//		/*Dictionary dictionary = new Dictionary();
//		dictionary.setId(30210144L);
//		DictionaryItem dictionaryItem = new DictionaryItem();
//		dictionaryItem.setDicItemId("9999");
//		dictionaryItem.setDicItemName("11");
//		dictionaryItem.setDictionary(dictionary);
//		dictionaryItemService.updateDictionaryItem(30210145L, dictionaryItem);*/
//		
//
//	}
//
//	//@Test
//	public void testDelete(){
//		dictionaryService.deleteDictionary(30210035L);
//		//dictionaryItemService.deleteDictionaryItem(1L);
//		//dictionaryItemService.deleteDictionaryItemByDictionaryId(30210149L);
//	}
//
//	//@Test
//	public void testFindOne(){
//		Dictionary dictionary =	dictionaryService.findOne(30209511l);
//		System.out.println("11111111");
//		System.out.println("findOne--------"+dictionary);
//	}
//
//	//@Test
//	public void testFindBy(){
//		List<DictionaryItem> list = dictionaryItemService.getDictionaryItemByDictionaryId(30209511L);
//		for(DictionaryItem d:list){
//			System.out.println(d.getDicItemId()+":"+d.getDicItemName());
//		}
//		System.out.println(list+"!!!!!!!!!!!!!!!!!");
//	}
//
//
//
//}
