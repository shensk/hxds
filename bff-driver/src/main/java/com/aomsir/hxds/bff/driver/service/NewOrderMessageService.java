package com.aomsir.hxds.bff.driver.service;

import com.aomsir.hxds.bff.driver.controller.form.ClearNewOrderQueueForm;
import com.aomsir.hxds.bff.driver.controller.form.ReceiveNewOrderMessageForm;

import java.util.ArrayList;

public interface NewOrderMessageService {
    public void clearNewOrderQueue(ClearNewOrderQueueForm form);

    public ArrayList receiveNewOrderMessage(ReceiveNewOrderMessageForm form);
}

