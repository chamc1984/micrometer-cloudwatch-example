package com.example.micrometercloudwatchexample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.micrometercloudwatchexample.service.TestDataCrudService;

@RestController
@RequestMapping(value = "/api")
public class ApiController {

    @Autowired
    TestDataCrudService service;

    @RequestMapping(value = "/info")
    public String getInfo() {
        return "return information.";
    }

    @RequestMapping(value = "/gettestdata")
    public String getTestData() {
        return service.getTestData().toString();
    }

}
