package com.aomsir.hxds.dr.db.dao;


import com.aomsir.hxds.dr.db.pojo.WalletEntity;

import java.util.Map;

public interface WalletDao {

    public int insert(WalletEntity entity);

    public int updateWalletBalance(Map param);

}




