package com.project.demo001.controller;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.project.demo001.domain.Board;
import com.project.demo001.repository.BoardRepository;
import com.project.demo001.repository.UserRepository;
import com.project.demo001.service.BoardService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardRepository boardRepository;
    private final BoardService boardService;
	
	@GetMapping({"/board/list", "/board"})
	public String list	(@RequestParam(defaultValue = "0") int page,          // 현재 페이지 번호
		    			@RequestParam(defaultValue = "10") int count,        // 한 페이지당 게시글 수
		    			Model model) {
		System.out.println("BoardController의 list() 메서드 실행");
		
	    Pageable pageable = PageRequest.of(page, count, Sort.by(
	            Sort.Order.desc("notice"),
	            Sort.Order.desc("createAt"),
	            Sort.Order.desc("id")
	        ));
	    // Pageable을 사용하여 해당 페이지의 게시글을 조회
	    Page<Board> boardPage = boardService.getBoardsWithLimit(pageable); // Page 객체로 받기
	    model.addAttribute("boardPage", boardPage);
	    model.addAttribute("currentPage", page);
	    model.addAttribute("count", count);  // 페이지당 게시글 수
		return "board/list";
	}
	
	@GetMapping("/board/insert")
	public String insert(Model model){
		System.out.println("BoardController의 insert() 메서드 실행");
		model.addAttribute("board", new Board());
		
		return "board/insert";
	}
	
	@PostMapping("/board/save")
	public String save(@ModelAttribute Board board, Principal principal) {
		System.out.println("BoardController의 save() 메서드 실행");
		
//		현재 로그인 사용자 정보 바인딩
		if (principal != null) {
	        board.setUserName(principal.getName());
	    } else {
	        board.setUserName("비회원"); // 또는 기본값
	    }
		
		boardService.save(board);
		return "redirect:/board/list";
	}
	
	@PostMapping("/board/notice")
	public String notice(@ModelAttribute Board board, 
						@RequestParam(value = "notice", defaultValue = "false") boolean notice) {
		board.setNotice(notice);
		boardService.save(board);
		return "redirect:/board/list";
	}
	
	@GetMapping("/board/select/{id}")
	public String select(@PathVariable("id") Long id, Model model) {
		System.out.println("BoardController의 select() 메서드 실행");
		Board board = boardService.getBoardAndIncreaseView(id);
		model.addAttribute("board",board);
		return "board/select";
	}
	
	@GetMapping("/board/update/{id}")
	public String update(@PathVariable("id") Long id, Model model) {
		Board board = boardService.getBoard(id);
		model.addAttribute("board", board);
	    return "board/update";
	}
	
	@PostMapping("/board/update/{id}")
	public String updateBoard(@PathVariable("id") Long id, @ModelAttribute Board board) {
	    boardService.updateBoard(id, board);
	    return "redirect:/board/select/" + id;
	}
	
	@PostMapping("/board/delete")
	public String delete(@RequestParam("id") Long id) {
		boardService.deleteById(id);
		
		return "redirect:/board/list";
	}
	


	
}
