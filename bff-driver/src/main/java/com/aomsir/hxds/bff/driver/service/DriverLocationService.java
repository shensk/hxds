package com.aomsir.hxds.bff.driver.service;

import com.aomsir.hxds.bff.driver.controller.form.RemoveLocationCacheForm;
import com.aomsir.hxds.bff.driver.controller.form.UpdateLocationCacheForm;

public interface DriverLocationService {
    
    public void updateLocationCache(UpdateLocationCacheForm form);

    public void removeLocationCache(RemoveLocationCacheForm form);

}
