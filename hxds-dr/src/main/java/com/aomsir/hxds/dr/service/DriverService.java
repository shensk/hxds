package com.aomsir.hxds.dr.service;

import java.util.Map;

public interface DriverService {
    public String registerNewDriver(Map param);

    public int updateDriverAuth(Map param);

    public String createDriverFaceModel(long driverId, String photo);
}
