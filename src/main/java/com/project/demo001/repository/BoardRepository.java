package com.project.demo001.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.project.demo001.domain.Board;
import com.project.demo001.domain.Reply;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    /* 📌 기본 조회 */
    List<Board> findAll(); // 전체 조회
    List<Board> findAllByOrderByIdDesc(); // ID 기준 내림차순
    Optional<Board> findBoardById(@Param("id") Long id); // 단건 조회

    /* 🔍 검색 기능 */
    Page<Board> findByTitleContaining(String keyword, Pageable pageable);
    Page<Board> findByContentContaining(String keyword, Pageable pageable);
    Page<Board> findByUser_UserNameContaining(String keyword, Pageable pageable);

    /* 👤 사용자 기반 조회 */
    List<Board> findByUser_UserName(String userName); // 작성자 기준 조회

    /* 📌 공지/일반 게시글 분리 */
    List<Board> findByNoticeTrueOrderByIdDesc(); // 공지글만
    Page<Board> findByNoticeFalseOrderByIdDesc(Pageable pageable); // 일반글만 (페이징)

    /* 📑 정렬된 게시글 목록 */
    @Query("SELECT b FROM Board b ORDER BY b.notice DESC, b.createAt DESC, b.id DESC")
    List<Board> findAllOrderByNoticeAndCreateAtAndId(); // 공지 먼저 정렬

    /* 💬 댓글, 답글 관련 */
    List<Board> findByReferOrderByStepAsc(int refer); // 같은 그룹 내 step 순서대로
    // 댓글은 ReplyRepository 없이 Board 기준으로 조회
    @Query("SELECT r FROM Reply r WHERE r.board.id = :boardId ORDER BY r.createdAt DESC")
    List<Reply> findRepliesByBoardId(@Param("boardId") Long boardId);
    @Query("SELECT r FROM Board b JOIN b.replies r WHERE r.id = :replyId")
    Reply findReplyById(@Param("replyId") Long replyId);
    
    /*  */
    @Query("SELECT b FROM Board b JOIN b.replies r WHERE r.id = :replyId")
    Board findBoardByReplyId(@Param("replyId") Long replyId);
    
    /* 👁️ 조회수 증가 */
    @Modifying
    @Query("UPDATE Board b SET b.viewNum = b.viewNum + 1 WHERE b.id = :id")
    void incrementViewNum(@Param("id") Long id);
}
