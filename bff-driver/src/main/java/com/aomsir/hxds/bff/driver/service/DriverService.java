package com.aomsir.hxds.bff.driver.service;

import com.aomsir.hxds.bff.driver.controller.form.*;

import java.util.HashMap;

public interface DriverService {

    public long registerNewDriver(RegisterNewDriverForm form);

    public int updateDriverAuth(UpdateDriverAuthForm form);

    public String createDriverFaceModel(CreateDriverFaceModelForm form);

    public HashMap login(LoginForm form);

    public HashMap searchDriverBaseInfo(SearchDriverBaseInfoForm form);
}
