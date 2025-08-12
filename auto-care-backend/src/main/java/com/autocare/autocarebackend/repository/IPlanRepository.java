package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.IPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IPlanRepository extends JpaRepository<IPlan,Long> {
    @Override
    Optional<IPlan> findById(Long aLong);

    @Override
    List<IPlan> findAll();

    List<IPlan> findAllByAdvertisement_Id(Long adId);

}