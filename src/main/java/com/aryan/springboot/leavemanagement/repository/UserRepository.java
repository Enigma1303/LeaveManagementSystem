package com.aryan.springboot.leavemanagement.repository;

import com.aryan.springboot.leavemanagement.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT u FROM Employee u LEFT JOIN FETCH u.authorities WHERE u.email = :email")
    Optional<Employee> findByEmailWithAuthorities(@Param("email") String email);

    @Query("SELECT u FROM Employee u JOIN u.authorities a WHERE a.name = :role")
    Optional<Employee> findByRole(@Param("role") String role);
}