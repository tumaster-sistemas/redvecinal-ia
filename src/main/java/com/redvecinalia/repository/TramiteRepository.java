package com.redvecinalia.repository;

import com.redvecinalia.model.Tramite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TramiteRepository extends JpaRepository<Tramite, Long> {
    List<Tramite> findByCategoriaOrderByTituloAsc(String categoria);
}