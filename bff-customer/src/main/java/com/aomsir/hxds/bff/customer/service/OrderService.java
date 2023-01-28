package com.aomsir.hxds.bff.customer.service;

import com.aomsir.hxds.bff.customer.controller.form.*;
import com.aomsir.hxds.common.util.PageUtils;

import java.util.HashMap;

public interface OrderService {
    public HashMap createNewOrder(CreateNewOrderForm form);

    public Integer searchOrderStatus(SearchOrderStatusForm form);

    public String deleteUnAcceptOrder(DeleteUnAcceptOrderForm form);

    public HashMap hasCustomerCurrentOrder(HasCustomerCurrentOrderForm form);

    public boolean confirmArriveStartPlace(ConfirmArriveStartPlaceForm form);

    public HashMap searchOrderForMoveById(SearchOrderForMoveByIdForm form);

    public HashMap searchOrderById(SearchOrderByIdForm form);

    public HashMap createWxPayment(long orderId, long customerId, Long voucherId);

    public String updateOrderAboutPayment(UpdateOrderAboutPaymentForm form);

    public PageUtils searchCustomerOrderByPage(SearchCustomerOrderByPageForm form);

}

