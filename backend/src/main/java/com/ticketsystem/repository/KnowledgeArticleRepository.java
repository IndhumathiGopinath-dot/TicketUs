package com.ticketsystem.repository;

import com.ticketsystem.model.KnowledgeArticle;
import com.ticketsystem.model.enums.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, Long> {

    List<KnowledgeArticle> findByCategory(Category category);

    @Query("SELECT k FROM KnowledgeArticle k WHERE LOWER(k.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(k.keywords) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<KnowledgeArticle> searchByKeyword(@Param("keyword") String keyword);
}
