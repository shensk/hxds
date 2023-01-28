package com.aomsir.hxds.bff.driver.feign;

import com.aomsir.hxds.bff.driver.controller.form.StartCommentWorkflowForm;
import com.aomsir.hxds.common.util.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-workflow")
public interface WorkflowServiceApi {

    @PostMapping("/comment/startCommentWorkflow")
    public R startCommentWorkflow(StartCommentWorkflowForm form);
}
