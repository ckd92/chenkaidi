package com.fitech.account.dao;

import com.fitech.domain.ledger.FAAccountRow;

import java.util.List;

/**
 * Created by chun on 16/9/4.
 */
public interface FAAccountDAO {

    public void setAccountTblName(String accountTblName);

    public FAAccountRow getRowTemplate();

    public FAAccountRow getRowTemplate(String accountTblName);

    //C
    public void insertRow(FAAccountRow row);

    //U
    public void updateRow(FAAccountRow row);

    //R
    public FAAccountRow getRow(Long id);

    //R
    public List<FAAccountRow> getAllRows();

    //D
    public void deleteRow(FAAccountRow row);

    public void createTemplate(FAAccountRow row);

    public void dorpTemplate(FAAccountRow row);

}
