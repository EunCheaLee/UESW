package com.project.demo001.service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.project.demo001.domain.Board;
import com.project.demo001.domain.ChatMessage;
import com.project.demo001.domain.Reply;
import com.project.demo001.domain.User;
import com.project.demo001.repository.BoardRepository;
import com.project.demo001.repository.ChatLogRepository;
import com.project.demo001.repository.UserRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {

	@Autowired
	private final BoardRepository boardRepository;
	@Autowired
	private final UserRepository userRepository;
	@Autowired
	private final ChatLogRepository chatLogRepository;

	@Autowired
	private final EntityManager entityManager;

	/* 📌 게시글 목록 조회 */
	public Page<Board> getBoardList(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
		return boardRepository.findAll(pageable);
	}

	public Page<Board> getNormalBoardList(int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
		Page<Board> pageResult = boardRepository.findByNoticeFalseOrderByIdDesc(pageable);

		// 댓글 수 계산해서 주입
		pageResult.forEach(board -> {
			List<Reply> replies = boardRepository.findRepliesByBoardId(board.getId());
			board.setCommentCount(replies != null ? replies.size() : 0);
		});

		return pageResult;
	}

	public List<Board> getNoticeList() {
		return boardRepository.findByNoticeTrueOrderByIdDesc();
	}

	public List<Board> noticePost() {
		return boardRepository.findAllOrderByNoticeAndCreateAtAndId();
	}

	public Page<Board> getBoardsWithLimit(Pageable pageable) {
		System.out.println("게시글 조회 시작");
		Page<Board> page = boardRepository.findAll(pageable);
		System.out.println("조회된 게시글 수: " + page.getTotalElements());
		return page != null ? page : Page.empty();
	}

	/* 📌 게시글 CRUD */
	public Board insert(String username, String title, String content) {
		User findUser = userRepository.findByUserName(username);
		Board post = Board.builder().title(title).content(content).user(findUser).build();
		return boardRepository.save(post);
	}

	public void save(Board board) {
		if (board.getUser() != null)
			userRepository.save(board.getUser());

		if (board.getFilename() != null && !board.getFilename().isEmpty()) {
			System.out.println("첨부된 파일 이름: " + board.getFilename());
		}

		boardRepository.save(board);
	}

	public Board select(Long id) {
		return boardRepository.findById(id).orElse(null);
	}

	public Optional<Board> findById(Long id) {
		return boardRepository.findById(id);
	}

	@Transactional
	public Board updateBoard(Long id, Board updatedBoard) {
		Board board = boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
		board.setTitle(updatedBoard.getTitle());
		board.setContent(updatedBoard.getContent());
		return boardRepository.save(board);
	}

	public void update(Board updatedBoard) {
		Board existingBoard = boardRepository.findById(updatedBoard.getId())
				.orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
		existingBoard.setTitle(updatedBoard.getTitle());
		existingBoard.setContent(updatedBoard.getContent());
		boardRepository.save(existingBoard);
	}

	public void deleteById(Long id) {
		if (!boardRepository.existsById(id)) {
			throw new IllegalArgumentException("해당 게시글이 존재하지 않습니다. id = " + id);
		}
		boardRepository.deleteById(id);
	}

	@Transactional
	public Board getBoardAndIncreaseView(Long id) {
		Board board = boardRepository.findById(id).orElse(null);
		if (board == null) {
			throw new IllegalArgumentException("게시글이 존재하지 않습니다.");
		}
		board.setViewNum(board.getViewNum() + 1);
		boardRepository.save(board);
		return board;
	}

	public Board getBoard(Long id) {
		return boardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
	}

	public Page<Board> searchBoardList(String searchType, String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));

		switch (searchType) {
		case "title":
			return boardRepository.findByTitleContaining(keyword, pageable);
		case "content":
			return boardRepository.findByContentContaining(keyword, pageable);
		case "writer":
			return boardRepository.findByUser_UserNameContaining(keyword, pageable);
		default:
			return boardRepository.findAll(pageable);
		}
	}

	/* 🗨️ 댓글 기능 */
	public List<Reply> getReplies(Long boardId) {
		return boardRepository.findRepliesByBoardId(boardId);
	}

	/* 채팅기능 */
	// 최근 메시지 30개 불러오기
	public List<ChatMessage> loadRecentMessages() {
		return chatLogRepository.findTop30ByOrderByTimestampAsc();
	}

	// 메시지 저장
	public void saveMessage(ChatMessage message) {
		message.setTimestamp(LocalDateTime.now()); // 저장 시 시간 지정
		chatLogRepository.save(message);
	}

}
