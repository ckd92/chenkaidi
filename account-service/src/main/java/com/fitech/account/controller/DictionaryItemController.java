package com.fitech.account.controller;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.fitech.account.service.DictionaryItemService;
import com.fitech.domain.account.DictionaryItem;
import com.fitech.framework.lang.result.GenericResult;

/**
 * Created by wangjianwei on 2017/7/31.
 */
@RestController
public class DictionaryItemController {
    @Autowired
    private DictionaryItemService dictionaryItemService;

    /**
     * 根据字典id条件查询字典项
     * @param id
     * @param dictionaryItem
     * @return
     */
    @PostMapping("/dictionaryItem/{id}")
    public GenericResult<List<DictionaryItem>> getDictionaryItemByDicItemName(@PathVariable("id") Long id, @RequestBody DictionaryItem dictionaryItem) {
        GenericResult<List<DictionaryItem>> result = new GenericResult<List<DictionaryItem>>();
        try {
            List<DictionaryItem> list = dictionaryItemService.getDictionaryItemByDicItemName(id, dictionaryItem.getDicItemName());
            result.setData(list);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    /**
     * 根据字典id查询字典项
     * @param id
     * @return
     */
    @GetMapping("/dictionaryItems/{id}")
    public GenericResult<List<DictionaryItem>> getDictionaryItemByDictionaryId(@PathVariable("id") Long id) {
        GenericResult<List<DictionaryItem>> result = new GenericResult<List<DictionaryItem>>();
        try {
            List<DictionaryItem> list = dictionaryItemService.getDictionaryItemByDictionaryId(id);
            result.setData(list);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    /**
     * 查询字典项
     * @param dictionaryItem
     * @return
     */
    @GetMapping("/dictionaryItem")
    public GenericResult<List<DictionaryItem>> getAllDictionaryItem(DictionaryItem dictionaryItem) {
        GenericResult<List<DictionaryItem>> result = new GenericResult<List<DictionaryItem>>();
        try {
            List<DictionaryItem> list = dictionaryItemService.findDictionaryItem(dictionaryItem);
            result.setData(list);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }
    
    /**
     * 根据字典项id查找字典项实体
     * @param id
     * @param request
     * @return
     */
    @GetMapping("/findDictionaryItemById/{id}")
    public GenericResult<DictionaryItem> findById(@PathVariable("id") Long id, HttpServletRequest request) {
        GenericResult<DictionaryItem> result = new GenericResult<>();
        try {
            result.setData(dictionaryItemService.findById(id));
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    /**
     * 新增字典项
     * @param dictionaryItem
     * @param request
     * @return
     */
    @PostMapping("/dictionaryItem")
    public GenericResult<Boolean> save(@RequestBody DictionaryItem dictionaryItem, HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = dictionaryItemService.save(dictionaryItem);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    /**
     * 修改字典项
     * @param id
     * @param dictionaryItem
     * @param request
     * @return
     */
    @PutMapping("/dictionaryItem/{id}")
    public GenericResult<Boolean> update(@PathVariable("id") Long id, @RequestBody DictionaryItem dictionaryItem, HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = dictionaryItemService.update(id, dictionaryItem);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

    /**
     * 删除字典项
     * @param id
     * @param request
     * @return
     */
    @DeleteMapping("/dictionaryItem/{id}")
    public GenericResult<Boolean> delete(@PathVariable("id") Long id, HttpServletRequest request) {
        GenericResult<Boolean> result = new GenericResult<>();
        try {
            result = dictionaryItemService.delete(id);
        } catch (Exception e) {
            result.setSuccess(false);
            e.printStackTrace();
        } finally {
        }
        return result;
    }

}
