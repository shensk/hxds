package com.aomsir.hxds.bff.driver.service.impl;

import com.aomsir.hxds.bff.driver.controller.form.StartCommentWorkflowForm;
import com.aomsir.hxds.bff.driver.feign.WorkflowServiceApi;
import com.aomsir.hxds.bff.driver.service.OrderCommentService;
import com.codingapi.txlcn.tc.annotation.LcnTransaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class OrderCommentServiceImpl implements OrderCommentService {
    @Resource
    private WorkflowServiceApi workflowServiceApi;

    @Override
    @Transactional
    @LcnTransaction
    public void startCommentWorkflow(StartCommentWorkflowForm form) {
        this.workflowServiceApi.startCommentWorkflow(form);
    }
}
