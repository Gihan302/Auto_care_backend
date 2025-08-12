package com.autocare.autocarebackend.repository;

import com.autocare.autocarebackend.models.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface TestRepository extends JpaRepository<Test,Long> {

}
