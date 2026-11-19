package com.redvecinalia.controller;

import com.redvecinalia.model.Tramite;
import com.redvecinalia.repository.TramiteRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tramites")
public class TramiteController {

    private final TramiteRepository tramiteRepository;

    public TramiteController(TramiteRepository tramiteRepository) {
        this.tramiteRepository = tramiteRepository;
    }

    @GetMapping
    public List<Tramite> listar(@RequestParam(required = false) String categoria) {
        if (categoria != null && !categoria.isBlank()) {
            return tramiteRepository.findByCategoriaOrderByTituloAsc(categoria);
        }
        return tramiteRepository.findAll();
    }

    @GetMapping("/{id}")
    public Tramite obtener(@PathVariable Long id) {
        return tramiteRepository.findById(id).orElse(null);
    }
}