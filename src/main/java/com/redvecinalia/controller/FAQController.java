package com.redvecinalia.controller;

import com.redvecinalia.model.FAQ;
import com.redvecinalia.repository.FAQRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faqs")
public class FAQController {

    private final FAQRepository faqRepository;

    public FAQController(FAQRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    @GetMapping
    public List<FAQ> listar(@RequestParam(required = false) String categoria) {
        if (categoria != null && !categoria.isBlank()) {
            return faqRepository.findByCategoria(categoria);
        }
        return faqRepository.findAll();
    }
}