package com.project.demo001.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.project.demo001.domain.Board;
import com.project.demo001.domain.User;
import com.project.demo001.repository.BoardRepository;
import com.project.demo001.service.BoardService;
import com.project.demo001.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardRepository boardRepository;
    private final BoardService boardService;
    private final UserService userService;
	
 // 파일 업로드 디렉토리 경로 설정
    private static final String UPLOAD_DIR = "board/upload/";
    
    @GetMapping({"/board/list", "/board"})
    public String listBoards(@RequestParam(required = false) String keyword,
                             @RequestParam(defaultValue = "0") int page,          // 현재 페이지 번호
                             @RequestParam(defaultValue = "10") int count,        // 한 페이지당 게시글 수
                             Model model, HttpSession session) {
        System.out.println("BoardController의 listBoards() 메서드 실행");

     // 로그인한 사용자 정보 가져오기
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        System.out.println("로그인한 사용자: " + (loggedInUser != null ? loggedInUser.getUserName() : "비회원"));

        
        Pageable pageable = PageRequest.of(page, count, Sort.by(
        		Sort.Order.asc("refer"),
        		Sort.Order.asc("step"),
        		Sort.Order.asc("depth"),
                Sort.Order.desc("notice"),
                Sort.Order.desc("createAt"),
                Sort.Order.desc("id")
        ));

        boolean searchMode = false;
        Page<Board> boardPage;
        
        // 검색어가 있는 경우
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색 결과 리스트 (검색은 전체 리스트를 반환하므로 페이징 생략)
            List<Board> searchResult = boardService.getBoardList(keyword); // 검색 결과를 List로 받기
            boardPage = boardRepository.findByTitleContaining(keyword, pageable);
            searchMode = true;
            model.addAttribute("boards", searchResult);  // 검색 결과 리스트
            model.addAttribute("searchMode", true);      // 뷰에서 검색 여부 구분 가능
        } else {
        	 // 검색어가 없는 경우, 일반 게시글 목록 (페이징)
            boardPage = boardRepository.findAll(pageable); // 페이징된 게시글 목록
            
            // boardPage가 null인 경우 빈 페이지로 처리
            if (boardPage == null) {
                boardPage = Page.empty();
            }
            
            // content가 null인 경우 빈 리스트로 처리
            if (boardPage.getContent() == null) {
                boardPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
            }
            
            model.addAttribute("boardPage", boardPage); // 페이징된 결과
            model.addAttribute("currentPage", page);
            model.addAttribute("count", count);  // 페이지당 게시글 수
            model.addAttribute("searchMode", false);  // 검색 모드 아님
        }

        // 검색어도 모델에 담아서 전달
        model.addAttribute("keyword", keyword);
        // 로그인한 사용자 정보를 전달하는 부분 (필요에 따라)
        if (loggedInUser != null) {
            model.addAttribute("loggedInUser", loggedInUser);
        }
        
        return "board/list";  // 반환할 뷰 페이지
    }
	
    @GetMapping("/board/insert")
    public String insert(Model model){
        model.addAttribute("board", new Board());
        return "board/insert";
    }
	
	@PostMapping("/board/save")
	public String save(@ModelAttribute Board board, 
	                   @RequestParam("file") MultipartFile file, 
	                   Principal principal,
	                   HttpServletRequest request) throws IOException {
	    System.out.println("BoardController의 save() 메서드 실행");

	    User user = new User();
	    
	    // HttpServletRequest 객체를 통해 세션을 가져옴
	    HttpSession session = request.getSession();  // 세션을 가져옴
	    // 로그인한 사용자 정보 바인딩
	    User loggedInUser = (User) session.getAttribute("loggedInUser");
	    if (loggedInUser != null) {
	        user.setUserName(loggedInUser.getUserName());   // 로그인한 사용자의 이름 설정
	    } else {
	        // 비회원이면 IP 주소로 표시
	        String ip = request.getHeader("X-Forwarded-For");
	        if (ip == null || ip.isEmpty()) {
	            ip = request.getRemoteAddr();  // 클라이언트 IP
	        }
	        user.setUserName("비회원(" + ip + ")");
	    }

	    
	    board.setUser(user);	// Board 엔티티의 외래키로 연결
	    

	    // 파일 업로드 처리
	    if (!file.isEmpty()) {
	        // 파일 이름 저장
	    	String filename = file.getOriginalFilename();
	    	String safeFilename = filename.replaceAll("[^a-zA-Z0-9\\.\\-]", "_"); // 안전하게
	        
	    	String uniqueFilename = UUID.randomUUID() + "_" + safeFilename;
	    	
	        // 파일을 서버의 static 디렉토리로 저장
	    	Path uploadPath = Paths.get(UPLOAD_DIR);
	    	if (!Files.exists(uploadPath)) {
	    	    Files.createDirectories(uploadPath); // 누락된 디렉토리 자동 생성
	    	}

	    	Path filePath = uploadPath.resolve(uniqueFilename);
	    	 try {
	             Files.write(filePath, file.getBytes());  // 파일을 실제로 쓰는 부분
	             System.out.println("파일이 저장되었습니다: " + filePath);
	             board.setFilename(uniqueFilename);  // DB에 저장할 파일명 설정
	         } catch (IOException e) {
	             System.out.println("파일 업로드 오류: " + e.getMessage());
	             // 파일 저장 실패 시 적절한 예외 처리
	         }
	    }

	    // Board 객체에 파일명과 사용자 정보를 설정
	    board.setUser(user);           // 로그인한 사용자 정보 저장 (User 객체 필요)
	    System.out.println("파일 이름: " + file.getOriginalFilename());
	    System.out.println("파일 사이즈: " + file.getSize());
	    
	    board.setUser(user);  // 로그인한 사용자 정보 저장 (User 객체 필요)
	    boardService.save(board);  // 게시글을 DB에 저장

	    // 게시글 등록 메시지
	    return "redirect:/board/list";  // 게시글 목록 페이지로 리디렉션
	}
	
    // 파일 다운로드
    @GetMapping("/board/upload/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename) {
        try {
            // 파일 경로 설정
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.internalServerError().build();
        }
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
		List<Board> comments = boardService.getCommentsForBoard(id); // 댓글 조회
	    if (board.getUser() == null) {
	        board.setUser(new User());  // user가 null이면 기본값으로 새로운 User 객체를 설정
	    }
		model.addAttribute("board",board);
		model.addAttribute("comments", comments);
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
	
	// 댓글 작성 폼은 그대로 사용 가능

	@PostMapping("/board/{id}/comment")
	public String addComment(@PathVariable Long id,
	                         @RequestParam String content,
	                         @RequestParam Long userId) {

	    User user = userService.findUserById(userId);
	    boardService.addComment(id, content, user);
	    return "redirect:/board/select/" + id;
	}
	

	
}