package com.project.demo001.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.demo001.domain.Reply;

@Repository
public interface ReplyRepository extends JpaRepository<Reply, Long> {

	@Query("SELECT r FROM Reply r JOIN FETCH r.user WHERE r.board.id = :boardId")
	List<Reply> findByBoardIdWithUser(@Param("boardId") Long boardId);
	
	List<Reply> findByBoardId(Long boardId);

}
