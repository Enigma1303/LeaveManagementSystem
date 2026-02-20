package com.aryan.springboot.leavemanagement.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.aryan.springboot.leavemanagement.entity.Users;

public interface UserRepository extends JpaRepository<Users, Long> {

    @Query("SELECT u FROM Users u LEFT JOIN FETCH u.authorities WHERE u.email = :email")
    Optional<Users> findByEmailWithAuthorities(@Param("email") String email);
}