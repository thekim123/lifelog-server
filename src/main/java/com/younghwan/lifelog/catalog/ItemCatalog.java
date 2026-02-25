package com.younghwan.lifelog.catalog;

import com.younghwan.lifelog.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCatalog extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String canonicalName;

    // CSV aliases for MVP: "대파,파,쪽파"
    @Lob
    private String aliases;

    private String defaultUnit;

    private String category;
}
