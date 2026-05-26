package com.ticketsystem.model;

import com.ticketsystem.model.enums.Category;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "knowledge_articles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KnowledgeArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(length = 500)
    private String keywords;
}
