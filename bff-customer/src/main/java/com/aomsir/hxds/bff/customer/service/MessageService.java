package com.aomsir.hxds.bff.customer.service;

import com.aomsir.hxds.bff.customer.controller.form.ReceiveBillMessageForm;

public interface MessageService {
    public String receiveBillMessage(ReceiveBillMessageForm form);
}
