package com.aomsir.hxds.dr.db.dao;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface DriverDao {
    public long hasDriver(Map param);

    public int registerNewDriver(Map param);

    public String searchDriverId(String openId);

    public int updateDriverAuth(Map param);

    public HashMap searchDriverNameAndSex(long driverId);

    public int updateDriverArchive(long driverId);

    public HashMap login(String openId);

    public HashMap searchDriverBaseInfo(long driverId);

    public ArrayList<HashMap> searchDriverByPage(Map param);

    public long searchDriverCount(Map param);

    public HashMap searchDriverAuth(long driverId);

    public HashMap searchDriverRealSummary(long driverId);

    public int updateDriverRealAuth(Map param);
    public HashMap searchDriverBriefInfo(long driverId);

    public String searchDriverOpenId(long driverId);
}




