package com.ticketsystem.controller;

import com.ticketsystem.model.KnowledgeArticle;
import com.ticketsystem.model.enums.Category;
import com.ticketsystem.service.KnowledgeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/knowledge")
public class KnowledgeController {

    private final KnowledgeService service;

    public KnowledgeController(KnowledgeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<KnowledgeArticle>> all() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/suggest")
    public ResponseEntity<List<KnowledgeArticle>> suggest(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Category category) {
        return ResponseEntity.ok(service.suggest(query, category));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<KnowledgeArticle> create(@RequestBody KnowledgeArticle article) {
        return ResponseEntity.ok(service.create(article));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
