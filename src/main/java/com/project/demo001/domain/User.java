package com.project.demo001.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Builder
public class User extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;	// 자동증가하는 id, DB에 자동으로 추가되는 id
	
	private String account;	// 유저가 결정하는 id
	private String password;	// 유저가 결정하는 pw
	private String email;		// 유저가 실사용하는 email
	private String userName;	// 유저 이름
	private String fullAddress;	// 유저 주소
	private String phoneNum;	// 유저 휴대폰 번호
	
	@OneToMany
	@JoinColumn(name="user_idx")
	private List<Board> board = new ArrayList<>();
}
