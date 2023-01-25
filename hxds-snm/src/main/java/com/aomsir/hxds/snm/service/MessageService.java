package com.aomsir.hxds.snm.service;

import com.aomsir.hxds.snm.db.pojo.MessageEntity;
import com.aomsir.hxds.snm.db.pojo.MessageRefEntity;

import java.util.HashMap;
import java.util.List;

public interface MessageService {
    public String insertMessage(MessageEntity entity);

    public List<HashMap> searchMessageByPage(long userId, String identity, long start, int length);

    public HashMap searchMessageById(String id);

    public String insertRef(MessageRefEntity entity);

    public long searchUnreadCount(long userId, String identity);

    public long searchLastCount(long userId, String identity);

    public long updateUnreadMessage(String id);

    public long deleteMessageRefById(String id);

    public long deleteUserMessageRef(long userId, String identity);
}
