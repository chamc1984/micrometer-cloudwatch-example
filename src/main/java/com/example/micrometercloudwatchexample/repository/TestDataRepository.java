package com.example.micrometercloudwatchexample.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.micrometercloudwatchexample.entity.TestData;

@Repository
public interface TestDataRepository extends JpaRepository<TestData,Integer> {

}
