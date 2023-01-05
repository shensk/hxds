package com.aomsir.hxds.bff.driver.service;

import com.aomsir.hxds.bff.driver.controller.form.RegisterNewDriverForm;

public interface DriverService {

    public long registerNewDriver(RegisterNewDriverForm form);
}
