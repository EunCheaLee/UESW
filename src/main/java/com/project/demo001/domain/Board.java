package com.project.demo001.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Board extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;	// 글 id(글 각자의 고유값)
	private Long parentId;	// 부모 게시글의 ID(답글이 아닌 본글은 null)
	
	@Column(name = "view_num", columnDefinition = "int default 0")
	private Integer viewNum=0;	// 조회수
	private int replyNum;	// 답글수(댓글 X)
	
	@NonNull
	private String title;	// 제목
	@NonNull
	private String content;	// 본문
	private String reply;	// 답글(댓글 X)
	
	@Column(nullable=false)
	private boolean notice = false;
	
    public String getTitle() {
        if (notice) {
            return "[공지] " + title;
        }
        return title;
    }
	
	@Column(name="user_id")
	private Long userId;
	
	private String userAccount;	// 유저 ID
	private String userName;	// 유저 이름
	private String password;	// 유저 비밀번호

}
