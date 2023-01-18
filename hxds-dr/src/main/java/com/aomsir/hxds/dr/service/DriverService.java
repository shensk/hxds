package com.aomsir.hxds.dr.service;

import com.aomsir.hxds.common.util.PageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface DriverService {
    public String registerNewDriver(Map param);

    public int updateDriverAuth(Map param);

    public String createDriverFaceModel(long driverId, String photo);

    public HashMap login(String code);

    public HashMap searchDriverBaseInfo(long driverId);

    public PageUtils searchDriverByPage(Map param);

    public HashMap searchDriverAuth(long driverId);

    public HashMap searchDriverRealSummary(long driverId);

    public int updateDriverRealAuth(Map param);

    public HashMap searchDriverBriefInfo(long driverId);
}
