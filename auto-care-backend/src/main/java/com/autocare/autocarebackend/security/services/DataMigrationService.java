package com.autocare.autocarebackend.security.services;

import com.autocare.autocarebackend.models.LPlan;
import com.autocare.autocarebackend.models.LeasingPlan;
import com.autocare.autocarebackend.repository.LPlanRepository;
import com.autocare.autocarebackend.repository.LeasingPlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataMigrationService {

    @Autowired
    private LPlanRepository lPlanRepository;

    @Autowired
    private LeasingPlanRepository leasingPlanRepository;

    public void migrateLPlansToLeasingPlans() {
        // Only migrate if the leasing_plans table is empty
        if (leasingPlanRepository.count() == 0) {
            List<LPlan> lPlans = lPlanRepository.findAll();
            for (LPlan lPlan : lPlans) {
                LeasingPlan leasingPlan = new LeasingPlan();
                leasingPlan.setPlanName(lPlan.getPlanAmount()); // Mapping planAmount to planName
                leasingPlan.setLeaseTerm(lPlan.getNoOfInstallments());
                leasingPlan.setInterestRate(lPlan.getInterest());
                leasingPlan.setMonthlyPayment(lPlan.getInstAmount());
                leasingPlan.setDescription(lPlan.getDescription());
                leasingPlan.setUser(lPlan.getUser());
                // vehicleType is not in LPlan, so it will be null
                leasingPlanRepository.save(leasingPlan);
            }
        }
    }
}
