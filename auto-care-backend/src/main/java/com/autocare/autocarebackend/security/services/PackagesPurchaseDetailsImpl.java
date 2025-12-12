package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.PackagePurchase;
import com.autocare.autocarebackend.repository.PackagesPurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PackagesPurchaseDetailsImpl {
    @Autowired
    PackagesPurchaseRepository packagesPurchaseRepository;
    public PackagePurchase savePackagesPurchase(PackagePurchase packagePurchase){
        return packagesPurchaseRepository.save(packagePurchase);
    }
}
