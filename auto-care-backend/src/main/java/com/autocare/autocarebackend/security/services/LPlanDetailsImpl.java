package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.LPlan;
import com.autocare.autocarebackend.repository.LPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LPlanDetailsImpl {
    @Autowired
    LPlanRepository lPlanRepository;

    public LPlan saveLPlanDetails(LPlan lPlan){
        return lPlanRepository.save(lPlan);
    }
}
