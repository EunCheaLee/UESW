package com.project.demo001.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.project.demo001.domain.Board;
import com.project.demo001.domain.ChatMessage;
import com.project.demo001.domain.Reply;
import com.project.demo001.domain.User;
import com.project.demo001.repository.BoardRepository;
import com.project.demo001.service.BoardService;
import com.project.demo001.service.ReplyService;
import com.project.demo001.service.UserService;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class BoardController {
    private final BoardRepository boardRepository;
    private final BoardService boardService;
    private final UserService userService;
    private final ReplyService replyService;
    private final EntityManager entityManager;
	
 // 파일 업로드 디렉토리 경로 설정
    private static final String UPLOAD_DIR = "board/upload/";
    
    @GetMapping({"/board/list","/board"})
    public String boardList(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "10") int count,
                            @RequestParam(required = false) String keyword,
                            @RequestParam(required = false, defaultValue = "title") String searchType,
                            Model model) {
    	List<Board> noticeList = boardService.getNoticeList(); // 공지글만
    	Page<Board> boardPage;
    	
    	if (keyword != null && !keyword.isEmpty()) {
            boardPage = boardService.searchBoardList(searchType, keyword, page, count);
        } else {
            boardPage = boardService.getNormalBoardList(page, count);
        }
    	
    	model.addAttribute("boardPage", boardPage);
        model.addAttribute("noticeList", noticeList);
        model.addAttribute("currentPage", page);
        model.addAttribute("count", count);
        model.addAttribute("keyword", keyword);
        model.addAttribute("searchType", searchType);
        return "board/list";
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

	    User user;
	    // HttpServletRequest 객체를 통해 세션을 가져옴
	    HttpSession session = request.getSession();  // 세션을 가져옴
	    // 로그인한 사용자 정보 바인딩
	    User loggedInUser = (User) session.getAttribute("loggedInUser");
	    
	    if (loggedInUser == null) {
	        request.setAttribute("errorMessage", "비회원은 글을 작성할 수 없습니다.");
	        return "error/error"; // ❗ 에러 페이지 또는 알림 페이지
	    }

	    user = loggedInUser;

	    // ✅ 공지글인데 권한이 없다면 리턴
	    if (board.isNotice() && (user.getRole() == null || !user.getRole().equalsIgnoreCase("ADMIN"))) {
	        System.out.println("공지글은 관리자만 작성할 수 있습니다.");
	        request.setAttribute("errorMessage", "공지글은 관리자만 작성할 수 있습니다.");
	        return "error/error"; // 에러 페이지 또는 알림 페이지로 연결
	    }
	    
	    board.setUser(user);	// Board 엔티티의 외래키로 연결
	    

	    // 파일 업로드 처리
	    if (!file.isEmpty()) {
	        String filename = UUID.randomUUID() + "_" +
	            file.getOriginalFilename().replaceAll("[^a-zA-Z0-9\\.\\-]", "_");

	        Path uploadPath = Paths.get(UPLOAD_DIR);
	        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

	        Path filePath = uploadPath.resolve(filename);
	        Files.write(filePath, file.getBytes());

	        board.setFilename(filename);
	    }

	    System.out.println("파일 이름: " + file.getOriginalFilename());
	    System.out.println("파일 사이즈: " + file.getSize());
	    
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
	public String select(@PathVariable Long id, Model model, HttpServletRequest request, @SessionAttribute(name = "userId", required = false) Long userId) {
		System.out.println("BoardController의 select() 메서드 실행");
		
		Board board = boardService.getBoardAndIncreaseView(id);
		
	    List<Reply> replies = replyService.getRepliesByBoardIdFlattened(id); // << 평탄화된 댓글로 변경
	    
	 // 로그인 유저 세션 처리
	    HttpSession session = request.getSession(false); // 세션이 없으면 null 반환
	    if (session != null) {
	        User loggedInUser = (User) session.getAttribute("loggedInUser");
	        if (loggedInUser != null) {
	            // ✅ 이거 빠져 있으면 무조건 null 떠요!
	            session.setAttribute("userId", loggedInUser.getId());  
	            model.addAttribute("userId", loggedInUser.getId());
	        }
	    }
		
		model.addAttribute("board",board);	// ✅ 이게 빠지면 ${board.id}는 무조건 null
	    model.addAttribute("comments", replies); // 댓글 리스트 전달
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
	public String delete(@RequestParam("id") Long id, HttpSession session) throws AccessDeniedException {
	    User loggedInUser = (User) session.getAttribute("loggedInUser");

	    if (loggedInUser == null) {
	        throw new AccessDeniedException("로그인이 필요합니다.");
	    }

	    Board board = boardService.getBoard(id);
	    User writer = board.getUser(); // null일 수도 있음
	    Long writerId = writer != null ? writer.getId() : null;
	    boolean isAdmin = "ADMIN".equalsIgnoreCase(loggedInUser.getRole());

	    // 🧩 비회원이 작성한 글은 관리자만 삭제 가능
	    if (writer == null || "GUEST".equalsIgnoreCase(writer.getRole())) {
	        if (!isAdmin) throw new AccessDeniedException("비회원의 글은 관리자만 삭제할 수 있습니다.");
	    } else {
	        boolean isAuthor = loggedInUser.getId().equals(writerId);
	     // 비회원인데 삭제 시도할 때
	        if (!isAdmin && isAuthor) {
	            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "비회원의 글은 관리자만 삭제할 수 있습니다.");
	        }
	    }

	    boardService.deleteById(id);
	    return "redirect:/board/list";
	}
	
	// 댓글 작성 폼은 그대로 사용 가능

	@PostMapping("/board/{id}/comment")
	public String addComment(@PathVariable Long id,
	                         @RequestParam String content,
	                         HttpServletRequest request) {

		HttpSession session = request.getSession(false);
	    if (session == null) {
	        return "redirect:/login"; // 로그인 안 된 사용자 처리
	    }

	    User loggedInUser = (User) session.getAttribute("loggedInUser");
	    if (loggedInUser == null) {
	        return "redirect:/login"; // 로그인 안 된 사용자 처리
	    }

	    replyService.addReply(id, loggedInUser.getId(), content);
	    return "redirect:/board/select/" + id;
	}
	
	@PostMapping("/reply/{id}/like")
	@Transactional // ✅ 트랜잭션 시작
	@ResponseBody
	public Map<String, Object> likeReply(@PathVariable Long id, HttpServletRequest request) {
	    Map<String, Object> result = new HashMap<>();
	    
	    HttpSession session = request.getSession(false);
	    if (session == null || session.getAttribute("loggedInUser") == null) {
	        result.put("success", false);
	        result.put("message", "로그인이 필요합니다.");
	        return result;
	    }

	    User user = (User) session.getAttribute("loggedInUser");
	    String key = "liked_reply_" + id + "_user_" + user.getId();

	    // 세션에서 좋아요 기록 확인
	    if (session.getAttribute(key) != null) {
	        result.put("success", false);
	        result.put("message", "이미 좋아요를 누르셨습니다.");
	        return result;
	    }

	    // 좋아요 처리
	    Reply reply = entityManager.find(Reply.class, id);
	    reply.setLikes(reply.getLikes() + 1);
	    entityManager.persist(reply); // 이제 트랜잭션 안에서 작동함

	    // 세션에 기록
	    session.setAttribute(key, true);

	    result.put("success", true);
	    result.put("likes", reply.getLikes());
	    return result;
	}
	
	@GetMapping("/board/view")
	public String viewByQueryParam(@RequestParam("id") Long id) {
	    return "redirect:/board/select/" + id;
	}
	
	// 댓글 신고
	@PostMapping("/reply/{id}/report")
	public String reportReply(@PathVariable Long id) {
	    replyService.reportReply(id);
	    return "redirect:/board/view?id=" + replyService.getBoardIdByReply(id);
	}

	// 댓글 삭제
	@GetMapping("/reply/{id}/delete")
	public String deleteReply(@PathVariable Long id, HttpSession session) throws AccessDeniedException {
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		Long userId = loggedInUser != null ? loggedInUser.getId() : null;
	    
	    if (loggedInUser != null) {
	        session.setAttribute("userId", loggedInUser.getId());  // ❗필수
	    }
	    
	    replyService.deleteReply(id, userId); // 🔍 userId가 null이거나 다른 경우 예외 발생
	    return "redirect:/board/view?id=" + replyService.getBoardIdByReply(id);
	}

	@GetMapping("/reply/{id}/edit")
	public String editReplyForm(@PathVariable Long id, Model model, HttpSession session) throws AccessDeniedException {
	    Reply reply = replyService.findById(id);
	    
	    User loggedInUser = (User) session.getAttribute("loggedInUser");
	    if (loggedInUser != null) {
	        session.setAttribute("userId", loggedInUser.getId());  // ❗필수
	    }
	    
	    if (!reply.getUser().getId().equals(session.getAttribute("userId"))) {
	        throw new AccessDeniedException("권한이 없습니다.");
	    }
	    model.addAttribute("reply", reply);
	    return "reply/edit";
	}

	@PostMapping("/reply/{id}/edit")
	@ResponseBody
	public String updateReply(@PathVariable Long id, @RequestParam String content, HttpSession session) throws AccessDeniedException {
	    Long userId = (Long) session.getAttribute("userId");  // ✅ 이 값이 null일 수도 있음
	    replyService.updateReply(id, content, userId);
	    return "ok";
	}
	
	@PostMapping("/reply/{parentId}/reply")
	public String addReplyToReply(@PathVariable Long parentId,
	                              @RequestParam String content,
	                              HttpServletRequest request) {
	    HttpSession session = request.getSession(false);
	    if (session == null) return "redirect:/login";

	    User loggedInUser = (User) session.getAttribute("loggedInUser");
	    if (loggedInUser == null) return "redirect:/login";

	    replyService.addReplyToReply(parentId, loggedInUser.getId(), content);

	    Long boardId = replyService.getBoardIdByReply(parentId);
	    return "redirect:/board/select/" + boardId;
	}
	
	@GetMapping("/chatting")
	public String chattingPage(Model model, HttpSession session) {
	    
		User loggedInUser = (User) session.getAttribute("loggedInUser");
	    if (loggedInUser == null) {
	        return "redirect:/login";
	    }

	    model.addAttribute("username", loggedInUser.getUserName()); // ✅ 이름 또는 닉네임
	    model.addAttribute("chatHistory", boardService.loadRecentMessages());
	    return "board/chatting";
	}

	@MessageMapping("/chat.sendMessage")
	@SendTo("/topic/public")
	public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
	    boardService.saveMessage(chatMessage);
	    // time 필드는 getter에서 자동 계산되므로 따로 할당할 필요 없음
	    return chatMessage;
	}

	@MessageMapping("/chat.addUser")
	@SendTo("/topic/public")
	public ChatMessage addUser(@Payload ChatMessage chatMessage) {
	    chatMessage.setType(ChatMessage.MessageType.JOIN);
	    chatMessage.setMessageContent(chatMessage.getSender() + "님이 입장했습니다.");
	    boardService.saveMessage(chatMessage);
	    return chatMessage;
	}
	
}