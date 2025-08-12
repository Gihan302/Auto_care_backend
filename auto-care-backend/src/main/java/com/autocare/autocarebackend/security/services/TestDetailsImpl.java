package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.Test;
import com.autocare.autocarebackend.repository.TestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestDetailsImpl {
    @Autowired
    private TestRepository testRepository;

    public Test saveTestDetails(Test test){
        return testRepository.save(test);
    }


    public Test editTestDetails(Test test){
        Test testUpdate = testRepository.findById(test.getId()).orElse(null);
        testUpdate.setName(test.getName());
        testUpdate.setAge(test.getAge());
        return testRepository.save(testUpdate);
    }
    public String deleteTestDetails(Long id){
        testRepository.deleteById(id);
        return "Dete remove" + id;
    }
}
