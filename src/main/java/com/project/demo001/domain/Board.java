package com.project.demo001.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
	private int refer;	// 글 그룹(답글 묶인 범위)
	private int step;	// 글 순서(목록에 나열되는 순서)
	private int depth;	// 글 들여쓰기
	
	@Column(name = "view_num", columnDefinition = "int default 0")
	private Integer viewNum=0;	// 조회수
	private int replyNum;	// 답글수(댓글 X)
	
	@NonNull
	private String title;	// 제목
	@NonNull
	private String content;	// 본문
	private String reply;	// 답글(댓글 X)
	private String boardFile;
	private String Filename;
	
	@Column(nullable=false)
	private boolean notice = false;
	
    public String getTitle() {
        if (notice && !title.startsWith("[공지]")) {
            return "[공지] " + title;
        }
        return title;
    }
	
    @ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="user_id")
	private User user;

	public void incrementReplyNum() {
		this.replyNum++;
	}
	
    // User의 password에 접근할 때
    public String getPassword() {
        return user != null ? user.getPassword() : null;  // Lombok이 자동으로 getter를 생성
    }

    

}