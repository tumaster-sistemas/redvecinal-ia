package com.redvecinalia.repository;

import com.redvecinalia.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {
    List<Mensaje> findByConversacionIdOrderByTimestampAsc(String conversacionId);
}