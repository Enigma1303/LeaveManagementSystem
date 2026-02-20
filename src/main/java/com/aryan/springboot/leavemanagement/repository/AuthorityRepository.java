package com.aryan.springboot.leavemanagement.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aryan.springboot.leavemanagement.entity.Authority;

public interface AuthorityRepository  extends JpaRepository<Authority,Long>{
    Optional<Authority> findByName(String name);
}
