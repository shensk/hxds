package com.aomsir.hxds.snm.controller;

import com.aomsir.hxds.common.util.R;
import com.aomsir.hxds.snm.controller.form.*;
import com.aomsir.hxds.snm.db.pojo.MessageEntity;
import com.aomsir.hxds.snm.service.MessageService;
import com.aomsir.hxds.snm.task.MessageTask;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;

@RestController
@RequestMapping("/message")
@Tag(name = "MessageController", description = "消息模块网络接口")
public class MessageController {

    @Resource
    private MessageService messageService;

    @Resource
    private MessageTask messageTask;

    @PostMapping("/searchMessageById")
    @Operation(summary = "根据ID查询消息")
    public R searchMessageById(@Valid @RequestBody SearchMessageByIdForm form) {
        HashMap map = this.messageService.searchMessageById(form.getId());
        return R.ok()
                .put("result", map);
    }

    @PostMapping("/updateUnreadMessage")
    @Operation(summary = "未读消息更新成已读消息")
    public R updateUnreadMessage(@Valid @RequestBody UpdateUnreadMessageForm form) {
        long rows = this.messageService.updateUnreadMessage(form.getId());
        return R.ok()
                .put("rows", rows);
    }

    @PostMapping("/deleteMessageRefById")
    @Operation(summary = "删除消息")
    public R deleteMessageRefById(@Valid @RequestBody DeleteMessageRefByIdForm form) {
        long rows = this.messageService.deleteMessageRefById(form.getId());
        return R.ok()
                .put("rows", rows);
    }

    @PostMapping("/refreshMessage")
    @Operation(summary = "刷新用户消息")
    public R refreshMessage(@Valid @RequestBody RefreshMessageForm form) {
        this.messageTask.receive(form.getIdentity(), form.getUserId());
        long lastRows = this.messageService.searchLastCount(form.getUserId(), form.getIdentity());
        long unreadRows = this.messageService.searchUnreadCount(form.getUserId(), form.getIdentity());
        HashMap map = new HashMap(){{
            put("lastRows", lastRows + "");
            put("unreadRows", unreadRows + "");
        }};
        return R.ok()
                .put("result", map);
    }

    @PostMapping("/sendPrivateMessage")
    @Operation(summary = "同步发送私有消息")
    public R sendPrivateMessage(@RequestBody @Valid SendPrivateMessageForm form) {
        MessageEntity entity = new MessageEntity();
        entity.setSenderId(form.getSenderId());
        entity.setSenderIdentity(form.getSenderIdentity());
        entity.setSenderPhoto(form.getSenderPhoto());
        entity.setSenderName(form.getSenderName());
        entity.setMsg(form.getMsg());
        entity.setSendTime(new Date());
        this.messageTask.sendPrivateMessage(form.getReceiverIdentity(), form.getReceiverId(), form.getTtl(), entity);
        return R.ok();
    }

    @PostMapping("/sendPrivateMessageSync")
    @Operation(summary = "异步发送私有消息")
    public R sendPrivateMessageSync(@RequestBody @Valid SendPrivateMessageForm form) {
        MessageEntity entity = new MessageEntity();
        entity.setSenderId(form.getSenderId());
        entity.setSenderIdentity(form.getSenderIdentity());
        entity.setSenderPhoto(form.getSenderPhoto());
        entity.setSenderName(form.getSenderName());
        entity.setMsg(form.getMsg());
        entity.setSendTime(new Date());
        this.messageTask.sendPrivateMessageAsync(form.getReceiverIdentity(), form.getReceiverId(), form.getTtl(), entity);
        return R.ok();
    }

}