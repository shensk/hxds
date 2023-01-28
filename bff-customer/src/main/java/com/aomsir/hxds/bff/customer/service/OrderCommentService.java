package com.aomsir.hxds.bff.customer.service;

import com.aomsir.hxds.bff.customer.controller.form.InsertCommentForm;

public interface OrderCommentService {
    public int insertComment(InsertCommentForm form);
}
