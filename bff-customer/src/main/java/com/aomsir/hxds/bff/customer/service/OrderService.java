package com.aomsir.hxds.bff.customer.service;

import com.aomsir.hxds.bff.customer.controller.form.CreateNewOrderForm;

import java.util.HashMap;

public interface OrderService {
    public HashMap createNewOrder(CreateNewOrderForm form);
}

