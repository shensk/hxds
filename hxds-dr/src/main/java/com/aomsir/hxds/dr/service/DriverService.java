package com.aomsir.hxds.dr.service;

import java.util.HashMap;
import java.util.Map;

public interface DriverService {
    public String registerNewDriver(Map param);

    public int updateDriverAuth(Map param);

    public String createDriverFaceModel(long driverId, String photo);

    public HashMap login(String code);

    public HashMap searchDriverBaseInfo(long driverId);

}
