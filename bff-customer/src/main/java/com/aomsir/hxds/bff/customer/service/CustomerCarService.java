package com.aomsir.hxds.bff.customer.service;

import com.aomsir.hxds.bff.customer.controller.form.DeleteCustomerCarByIdForm;
import com.aomsir.hxds.bff.customer.controller.form.InsertCustomerCarForm;
import com.aomsir.hxds.bff.customer.controller.form.SearchCustomerCarListForm;

import java.util.ArrayList;
import java.util.HashMap;

public interface CustomerCarService {
    public void insertCustomerCar(InsertCustomerCarForm form);

    public ArrayList<HashMap> searchCustomerCarList(SearchCustomerCarListForm form);

    public int deleteCustomerCarById(DeleteCustomerCarByIdForm form);
}
