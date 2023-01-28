package com.aomsir.hxds.mis.api.feign;

import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.mis.api.controller.form.AcceptCommentAppealForm;
import com.aomsir.hxds.mis.api.controller.form.HandleCommentAppealForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(value = "hxds-workflow")
public interface WorkflowServiceApi {

    @PostMapping("/comment/acceptCommentAppeal")
    public R acceptCommentAppeal(AcceptCommentAppealForm form);

    @PostMapping("/comment/handleCommentAppeal")
    public R handleCommentAppeal(HandleCommentAppealForm form);
}
