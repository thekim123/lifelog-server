package com.younghwan.lifelog.recipe;

import com.younghwan.lifelog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recipe extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String instructions;

    // CSV 형태 MVP: "우유,계란,양파"
    @Column(nullable = false)
    private String requiredItems;
}
