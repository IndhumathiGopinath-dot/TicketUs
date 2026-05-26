package com.ticketsystem.service;

import com.ticketsystem.model.KnowledgeArticle;
import com.ticketsystem.model.enums.Category;
import com.ticketsystem.repository.KnowledgeArticleRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class KnowledgeService {

    private final KnowledgeArticleRepository repository;

    public KnowledgeService(KnowledgeArticleRepository repository) {
        this.repository = repository;
    }

    public List<KnowledgeArticle> getAll() {
        return repository.findAll();
    }

    public List<KnowledgeArticle> getByCategory(Category category) {
        return repository.findByCategory(category);
    }

    public List<KnowledgeArticle> suggest(String query, Category category) {
        Set<KnowledgeArticle> results = new HashSet<>();
        if (category != null) {
            results.addAll(repository.findByCategory(category));
        }
        if (query != null && !query.isBlank()) {
            for (String w : query.toLowerCase().split("\\s+")) {
                if (w.length() < 3) continue;
                results.addAll(repository.searchByKeyword(w));
            }
        }
        return results.stream().limit(5).toList();
    }

    public KnowledgeArticle create(KnowledgeArticle article) {
        return repository.save(article);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
