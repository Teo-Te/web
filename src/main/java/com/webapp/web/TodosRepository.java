package com.webapp.web;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TodosRepository extends JpaRepository<Todos, Integer>{
    // JPA Will also provide the implementation for this interface
    ArrayList<Todos> findByUsername(String username);
    
}
