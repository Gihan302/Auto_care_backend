package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.Packages;
import com.autocare.autocarebackend.repository.PackagesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PackagesDetailsImpl {
    @Autowired
    PackagesRepository packagesRepository;
    public Packages savePackages(Packages packages){
        return packagesRepository.save(packages);
    }
}
