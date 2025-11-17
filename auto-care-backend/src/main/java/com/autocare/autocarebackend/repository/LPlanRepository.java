package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.LPlan;
import com.autocare.autocarebackend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface LPlanRepository extends JpaRepository<LPlan,Long> {
    @Override
    Optional<LPlan> findById(Long aLong);

    @Override
    List<LPlan> findAll();


    List<LPlan> findAllByAdvertisement_Id(Long adId);

    List<LPlan> findByUser(User user);

//    Advertisement<LPlan> findByAdId(Long AdId, Pageable pageable);
//    Optional<Comment> findByIdAndPostId(Long id, Long postId);
}
