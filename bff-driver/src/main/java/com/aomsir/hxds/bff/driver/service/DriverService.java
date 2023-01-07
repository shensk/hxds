package com.aomsir.hxds.bff.driver.service;

import com.aomsir.hxds.bff.driver.controller.form.CreateDriverFaceModelForm;
import com.aomsir.hxds.bff.driver.controller.form.LoginForm;
import com.aomsir.hxds.bff.driver.controller.form.RegisterNewDriverForm;
import com.aomsir.hxds.bff.driver.controller.form.UpdateDriverAuthForm;

import java.util.HashMap;

public interface DriverService {

    public long registerNewDriver(RegisterNewDriverForm form);

    public int updateDriverAuth(UpdateDriverAuthForm form);

    public String createDriverFaceModel(CreateDriverFaceModelForm form);

    public HashMap login(LoginForm form);
}
