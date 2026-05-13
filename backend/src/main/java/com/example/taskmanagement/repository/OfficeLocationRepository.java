package com.example.taskmanagement.repository;

import com.example.taskmanagement.entity.OfficeLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfficeLocationRepository extends JpaRepository<OfficeLocation, Long> {
    List<OfficeLocation> findAllByStatusOrderByNameAsc(OfficeLocation.Status status);
}
