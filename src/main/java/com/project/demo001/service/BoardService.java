package com.project.demo001.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.demo001.domain.Board;
import com.project.demo001.domain.User;
import com.project.demo001.repository.BoardRepository;
import com.project.demo001.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

	@Autowired
	private final BoardRepository boardRepository;
	@Autowired
	private final UserRepository userRepository;
	
	public Board insert(String username, String title, String content) {
		
		User findUser = userRepository.findByUserName(username);
		Board post = Board.builder().title(title).content(content).build();
		
		return boardRepository.save(post);
	}

	public List<Board> list() {
		return boardRepository.findAll();
	}

	public void save(Board board) {
        // User 객체가 아직 저장되지 않았다면 먼저 저장
		if (board.getUser() != null) {
	        userRepository.save(board.getUser());
	    }

	    // 파일 첨부된 게시글의 경우, 파일 정보를 설정
	    if (board.getFilename() != null && !board.getFilename().isEmpty()) {
	        // 파일이 제대로 첨부된 경우에만 처리
	        System.out.println("첨부된 파일 이름: " + board.getFilename());
	    }

        // Board 객체 저장
        boardRepository.save(board);
	}

	public Board select(Long id) {
		return boardRepository.findById(id).orElse(null);
	}

	public Optional<Board> findById(Long id) {
		return boardRepository.findById(id);
	}

    public void update(Board updatedBoard) {
        Board existingBoard = boardRepository.findById(updatedBoard.getId())
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 수정할 필드만 덮어쓰기
        existingBoard.setTitle(updatedBoard.getTitle());
        existingBoard.setContent(updatedBoard.getContent());

        // 저장
        boardRepository.save(existingBoard);
    }
	
	public void deleteById(Long id) {
	    if (!boardRepository.existsById(id)) {
	        throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id = " + id);
	    }
	    boardRepository.deleteById(id);
	}

	
//	조회수 증가 메서드
    @Transactional
    public Board getBoardAndIncreaseView(Long id) {
        Board board = boardRepository.findBoardById(id).orElseThrow();
        board.setViewNum(board.getViewNum() + 1);
        boardRepository.save(board);
        return board;
    }
    
    @Transactional
    public Board updateBoard(Long id, Board updatedBoard) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));

            // 제목과 내용만 수정 (작성자, 날짜, 조회수 유지)
            board.setTitle(updatedBoard.getTitle());
            board.setContent(updatedBoard.getContent());

            return boardRepository.save(board); // 변경 감지로 저장
    }


    public Board getBoard(Long id) {
        return boardRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
    }
    
    public List<Board> noticePost() {
        return boardRepository.findAllOrderByNoticeAndCreateAtAndId();
    }
    
    public Page<Board> getBoardsWithLimit(Pageable pageable) {
        System.out.println("게시글 조회 시작");
        Page<Board> page = boardRepository.findAll(pageable);
        System.out.println("조회된 게시글 수: " + page.getTotalElements());
        return page != null ? page : Page.empty();  // 빈 Page 객체 반환
    }

	public List<Board> getBoardList(String keyword) {
		return boardRepository.findByTitleContaining(keyword);  // 예시로 제목에 포함된 검색어를 찾음
	}
	
	@Transactional
	public Board addReply(Long id, String title, String content, User user) {
	    // 기존 Board 객체를 조회하여 답글 생성 로직
	    Board board = boardRepository.findById(id)
	            .orElseThrow(() -> new IllegalArgumentException("본글을 찾을 수 없습니다."));

		
		Board reply = Board.builder()
				.title(title)
				.content(content)
				.user(user)
				.refer(board.getRefer())	// 같은 그룹(refer) 사용
				.step(board.getStep()+1)		// 부모글보다 step을 1 증가
				.depth(board.getDepth()+1)	// 부모글보다 depth를 1 증가
				.build();
		
//		원본 글의 답글 수 증가
		board.incrementReplyNum();
		
//		원본 글 저장(답글 수 증가 반영)
		boardRepository.save(board);
		
//		답글 저장
		return boardRepository.save(reply);
	}
	
	

	
}