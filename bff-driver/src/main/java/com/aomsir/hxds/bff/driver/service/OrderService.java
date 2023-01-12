package com.aomsir.hxds.bff.driver.service;

import com.aomsir.hxds.bff.driver.controller.form.AcceptNewOrderForm;

public interface OrderService {
    public String acceptNewOrder(AcceptNewOrderForm form);
}
