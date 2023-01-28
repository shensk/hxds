package com.aomsir.hxds.odr.service;

import com.aomsir.hxds.odr.db.pojo.OrderCommentEntity;

import java.util.HashMap;
import java.util.Map;

public interface OrderCommentService {
    public int insert(OrderCommentEntity entity);

    public HashMap searchCommentByOrderId(Map param);
}
