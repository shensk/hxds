package com.aomsir.hxds.bff.driver.service;

import com.aomsir.hxds.bff.driver.controller.form.AcceptNewOrderForm;
import com.aomsir.hxds.bff.driver.controller.form.SearchDriverCurrentOrderForm;
import com.aomsir.hxds.bff.driver.controller.form.SearchDriverExecuteOrderForm;
import com.aomsir.hxds.bff.driver.controller.form.SearchOrderForMoveByIdForm;

import java.util.HashMap;

public interface OrderService {
    public String acceptNewOrder(AcceptNewOrderForm form);
    public HashMap searchDriverExecuteOrder(SearchDriverExecuteOrderForm form);

    public HashMap searchOrderForMoveById(SearchOrderForMoveByIdForm form);

    public HashMap searchDriverCurrentOrder(SearchDriverCurrentOrderForm form);
}
