package com.example.micrometercloudwatchexample.service;

import java.util.ArrayList;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.micrometercloudwatchexample.repository.TestDataRepository;

@Service
@Transactional
public class TestDataCrudService {

    @Autowired
    TestDataRepository repository;
    
    public List<String> getTestData() {
        List<String> ret = new ArrayList<>();
        repository.findAll().forEach(data -> ret.add(data.getData()));
        return ret;
    }
}
