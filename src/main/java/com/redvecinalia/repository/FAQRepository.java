package com.redvecinalia.repository;

import com.redvecinalia.model.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FAQRepository extends JpaRepository<FAQ, Long> {
    List<FAQ> findByCategoria(String categoria);
}