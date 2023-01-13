package com.aomsir.hxds.bff.customer.service;

import com.aomsir.hxds.bff.customer.controller.form.CreateNewOrderForm;
import com.aomsir.hxds.bff.customer.controller.form.DeleteUnAcceptOrderForm;
import com.aomsir.hxds.bff.customer.controller.form.HasCustomerCurrentOrderForm;
import com.aomsir.hxds.bff.customer.controller.form.SearchOrderStatusForm;

import java.util.HashMap;

public interface OrderService {
    public HashMap createNewOrder(CreateNewOrderForm form);

    public Integer searchOrderStatus(SearchOrderStatusForm form);

    public String deleteUnAcceptOrder(DeleteUnAcceptOrderForm form);

    public HashMap hasCustomerCurrentOrder(HasCustomerCurrentOrderForm form);
}

