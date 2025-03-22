package com.example.AlgosWeb.Repository;

import com.example.AlgosWeb.Entity.Box;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoxRepository extends JpaRepository<Box,Integer> {
}
