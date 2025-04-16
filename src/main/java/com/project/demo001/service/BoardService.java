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
        Page<Board> boardPage = boardRepository.findAll(pageable);
        System.out.println("조회된 게시글 수: " + boardPage.getTotalElements());
        return boardPage;
    }


	
}
