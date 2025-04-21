package com.project.demo001.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.project.demo001.domain.Board;
import com.project.demo001.service.BoardService;

import lombok.RequiredArgsConstructor;

@SpringBootTest
@RequiredArgsConstructor
class BoardRepositoryTest {

	@Autowired
	private BoardRepository boardRepository;
	
    @Test
    void findPostListTest() {
        List<Board> boardList = boardRepository.findAll();
        for (Board board : boardList) {
            System.out.println(board);
        }

        // 예시로 데이터 존재 여부도 테스트해볼 수 있음
        assertFalse(boardList.isEmpty(), "게시글 리스트가 비어있습니다.");
    }

}
