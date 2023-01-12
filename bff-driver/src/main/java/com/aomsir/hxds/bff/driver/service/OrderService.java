package com.aomsir.hxds.bff.driver.service;

import com.aomsir.hxds.bff.driver.controller.form.AcceptNewOrderForm;
import com.aomsir.hxds.bff.driver.controller.form.SearchDriverExecuteOrderForm;

import java.util.HashMap;

public interface OrderService {
    public String acceptNewOrder(AcceptNewOrderForm form);
    public HashMap searchDriverExecuteOrder(SearchDriverExecuteOrderForm form);
}
