package com.aomsir.hxds.mps.service;

import java.util.Map;

public interface DriverLocationService {
    
    public void updateLocationCache(Map param);
    
    public void removeLocationCache(long driverId);
}

