package com.aryan.springboot.leavemanagement.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.aryan.springboot.leavemanagement.entity.Employee;

public interface UserRepository extends JpaRepository<Employee, Long> {

    @Query("SELECT u FROM Employee u LEFT JOIN FETCH u.authorities WHERE u.email = :email")
    Optional<Employee> findByEmailWithAuthorities(@Param("email") String email);
}