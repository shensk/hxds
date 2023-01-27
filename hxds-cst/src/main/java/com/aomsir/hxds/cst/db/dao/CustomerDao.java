package com.aomsir.hxds.cst.db.dao;

import java.util.HashMap;
import java.util.Map;

public interface CustomerDao {

    public int registerNewCustomer(Map param);
    public long hasCustomer(Map param);
    public String searchCustomerId(String openId);

    public String login(String openId);

    public HashMap searchCustomerInfoInOrder(long customerId);

    public HashMap searchCustomerBriefInfo(long customerId);

    public String searchCustomerOpenId(long customerId);
}
