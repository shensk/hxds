package com.aomsir.hxds.bff.customer.service;

import com.aomsir.hxds.bff.customer.controller.form.CreateNewOrderForm;

public interface OrderService {
    public int createNewOrder(CreateNewOrderForm form);
}

