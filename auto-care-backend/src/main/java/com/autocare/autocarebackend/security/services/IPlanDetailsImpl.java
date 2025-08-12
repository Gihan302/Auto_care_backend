package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.IPlan;
import com.autocare.autocarebackend.repository.IPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IPlanDetailsImpl {
    @Autowired
    IPlanRepository iPlanRepository;

    public IPlan saveIPlanDetails(IPlan iPlan) { return  iPlanRepository.save(iPlan);
    }
}
