package com.aomsir.hxds.bff.driver.service;

import com.aomsir.hxds.bff.driver.controller.form.ClearNewOrderQueueForm;

public interface NewOrderMessageService {
    public void clearNewOrderQueue(ClearNewOrderQueueForm form);
}

