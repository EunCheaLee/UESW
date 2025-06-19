package com.project.demo001.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class User extends BaseEntity implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;	// 자동증가하는 id, DB에 자동으로 추가되는 id
	
	private String account;	// 유저가 결정하는 id
	private String password;	// 유저가 결정하는 pw
	private String email;		// 유저가 실사용하는 email
	private String userName;	// 유저 이름
	public String getUserName() {
	    return this.userName;
	}
	private String fullAddress;	// 유저 주소
	private String streetName;	// 중간주소
	private String phoneNum;	// 유저 휴대폰 번호
	private String birth;
	
	private String role;	// 유저 등급(권한)
	
	private String provider;     // 예: "google", "kakao", "naver"
	private String providerId;   // 소셜 로그인에서 제공하는 고유 사용자 ID
	private String profileImageUrl; 
	
	@OneToMany(mappedBy = "user")
	private List<Board> board = new ArrayList<>();
	
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }

    @Override public String getUsername() { return account; }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }

}