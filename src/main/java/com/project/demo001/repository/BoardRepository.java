package com.project.demo001.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.demo001.domain.Board;
import com.project.demo001.domain.User;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {
	// 특정 사용자 글 조회
	List<Board> findByUserName(String userName);	
    // 제목을 기준으로 조회 (부분 일치)
    @Query("SELECT b FROM Board b WHERE b.title LIKE %?1%")
    List<Board> findByTitleContaining(String title);
    // 내용으로 게시글 조회 (부분 일치)
    @Query("SELECT b FROM Board b WHERE b.content LIKE %?1%")
    List<Board> findByContentContaining(String content);
	
	List<Board> findAll();
	
	@Modifying
	@Query("UPDATE Board b SET b.viewNum = b.viewNum + 1 WHERE b.id = :id")
	void incrementViewNum(@Param("id") Long id);
	
	@Query("SELECT b FROM Board b WHERE b.id = :id")
	Optional<Board> findBoardById(@Param("id") Long id);
	
	List<Board> findAllByOrderByIdDesc();
	
	@Query("SELECT b FROM Board b ORDER BY b.notice DESC, b.createAt DESC, b.id DESC")
	List<Board> findAllOrderByNoticeAndCreateAtAndId();
}
