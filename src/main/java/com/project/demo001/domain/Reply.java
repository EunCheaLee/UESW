package com.project.demo001.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.Data;

@Data
@Entity
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String content;
    
    private LocalDateTime createdAt;
    private int likes = 0;
    private int depth; // 들여쓰기 정도
    
    @Column(name = "report_count", nullable = false)
    private int reportCount = 0;  // 기본값 0
    
    @PrePersist
    protected void onCreate() {
    	this.createdAt = LocalDateTime.now();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    private User user; // 댓글 단 사람
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board; // 게시글

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Reply parent; // 부모 댓글

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Reply> children = new ArrayList<>();
}
