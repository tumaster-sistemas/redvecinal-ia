package com.redvecinalia.repository;

import com.redvecinalia.model.Conversacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConversacionRepository extends JpaRepository<Conversacion, String> {
    Optional<Conversacion> findBySessionId(String sessionId);
}