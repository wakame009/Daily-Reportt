package com.techacademy.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techacademy.entity.Employee;
import com.techacademy.entity.Report;

public interface ReportRepository extends JpaRepository<Report, String> {

    Optional<Report> findById(Long id);

    List<Report> findByEmployee(Employee currentUser);
}