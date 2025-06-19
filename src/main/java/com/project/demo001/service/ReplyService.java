package com.project.demo001.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.project.demo001.domain.Board;
import com.project.demo001.domain.Reply;
import com.project.demo001.domain.User;
import com.project.demo001.repository.BoardRepository;
import com.project.demo001.repository.ReplyRepository;
import com.project.demo001.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReplyService {

	private final ReplyRepository replyRepository;
	private final BoardRepository boardRepository;
	private final UserRepository userRepository;
	
    @Transactional
    public void addReply(Long boardId, Long userId, String content) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        Reply reply = new Reply();
        reply.setBoard(board);
        reply.setUser(user);
        reply.setContent(content);

        replyRepository.save(reply);
    }
    
    public List<Reply> getReplies(Long boardId) {
        return replyRepository.findByBoardIdWithUser(boardId);
    }

    public Reply findById(Long id) {
        return replyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));
    }

    public Long getBoardIdByReply(Long replyId) {
        return findById(replyId).getBoard().getId();
    }

    @Transactional
    public void updateReply(Long replyId, String content, Long userId) throws AccessDeniedException {
        Reply reply = boardRepository.findReplyById(replyId);

        System.out.println("댓글 작성자 ID: " + reply.getUser().getId());
        System.out.println("현재 로그인 유저 ID: " + userId);

        if (reply == null || !reply.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        reply.setContent(content);
    }

    @Transactional
    public void deleteReply(Long replyId, Long userId) throws AccessDeniedException {
        Reply reply = findById(replyId);
        if (!reply.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
        replyRepository.delete(reply);
    }

    @Transactional
    public void reportReply(Long replyId) {
        Reply reply = findById(replyId);
        reply.setReportCount(reply.getReportCount() + 1);
        replyRepository.save(reply);
    }

    @Transactional
    public void addReplyToReply(Long parentId, Long userId, String content) {
        Reply parent = replyRepository.findById(parentId).orElseThrow();
        User user = userRepository.findById(userId).orElseThrow();

        Reply reply = new Reply();
        reply.setUser(user);
        reply.setBoard(parent.getBoard());	// 대댓글도 부모와 같은 게시글
        reply.setContent(content);
        reply.setParent(parent);	// 🔥 핵심: 부모 댓글 설정

        replyRepository.save(reply);
    }
    
    public List<Reply> getRepliesByBoardIdFlattened(Long boardId) {
        List<Reply> allReplies = replyRepository.findByBoardId(boardId);
        
        // 1. 트리 형태 구성
        Map<Long, Reply> replyMap = new HashMap<>();
        List<Reply> roots = new ArrayList<>();

     // 1. replyMap에 실제 객체 등록
        for (Reply reply : allReplies) {
            reply.setChildren(new ArrayList<>()); // 자식 목록 초기화
            replyMap.put(reply.getId(), reply);
        }

     // 2. 트리 구성
        for (Reply reply : allReplies) {
            if (reply.getParent() != null) {
                Reply parent = replyMap.get(reply.getParent().getId());
                if (parent != null) {
                    parent.getChildren().add(reply);
                }
            } else {
                roots.add(reply);
            }
        }

     // 3. 평탄화
        List<Reply> flattened = new ArrayList<>();
        for (Reply root : roots) {
            flattenRecursive(root, 0, flattened);
        }
        return flattened;
    }

    private void flattenRecursive(Reply node, int depth, List<Reply> result) {
        node.setDepth(depth);
        result.add(node);
        for (Reply child : node.getChildren()) {
            flattenRecursive(child, depth + 1, result);
        }
    }
	
}
