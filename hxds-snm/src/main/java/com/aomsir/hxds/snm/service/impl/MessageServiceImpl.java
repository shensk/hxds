package com.aomsir.hxds.snm.service.impl;

import com.aomsir.hxds.snm.db.dao.MessageDao;
import com.aomsir.hxds.snm.db.dao.MessageRefDao;
import com.aomsir.hxds.snm.db.pojo.MessageEntity;
import com.aomsir.hxds.snm.db.pojo.MessageRefEntity;
import com.aomsir.hxds.snm.service.MessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    @Resource
    private MessageDao messageDao;

    @Resource
    private MessageRefDao messageRefDao;

    @Override
    public String insertMessage(MessageEntity entity) {
        String id = this.messageDao.insert(entity);
        return id;
    }

    @Override
    public List<HashMap> searchMessageByPage(long userId, String identity, long start, int length) {
        List<HashMap> list = this.messageDao.searchMessageByPage(userId, identity, start, length);
        return list;
    }

    @Override
    public HashMap searchMessageById(String id) {
        HashMap map = this.messageDao.searchMessageById(id);
        return map;
    }

    @Override
    public String insertRef(MessageRefEntity entity) {
        String id = this.messageRefDao.insert(entity);
        return id;
    }

    @Override
    public long searchUnreadCount(long userId, String identity) {
        long count = this.messageRefDao.searchUnreadCount(userId, identity);
        return count;
    }

    @Override
    public long searchLastCount(long userId, String identity) {
        long count = this.messageRefDao.searchLastCount(userId, identity);
        return count;
    }

    @Override
    public long updateUnreadMessage(String id) {
        long rows = this.messageRefDao.updateUnreadMessage(id);
        return rows;
    }

    @Override
    public long deleteMessageRefById(String id) {
        long rows = this.messageRefDao.deleteMessageRefById(id);
        return rows;
    }

    @Override
    public long deleteUserMessageRef(long userId, String identity) {
        long rows = this.messageRefDao.deleteUserMessageRef(userId, identity);
        return rows;
    }
}
