package com.group.exam.board.controller;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.group.exam.board.command.BoardLikeCommand;
import com.group.exam.board.command.BoardlistCommand;
import com.group.exam.board.command.BoardreplyInsertCommand;
import com.group.exam.board.command.BoardupdateCommand;
import com.group.exam.board.command.QuestionAdayCommand;
import com.group.exam.board.service.BoardService;
import com.group.exam.board.vo.BoardHeartVo;
import com.group.exam.board.vo.BoardVo;
import com.group.exam.board.vo.NoticeAdminVo;
import com.group.exam.board.vo.ReplyVo;
import com.group.exam.calendar.service.CalendarService;
import com.group.exam.member.command.LoginCommand;
import com.group.exam.member.service.MemberService;
import com.group.exam.utils.Criteria;
import com.group.exam.utils.PagingVo;

@Controller
@RequestMapping("/board")
public class BoardController {
	private static final Logger logger = LoggerFactory.getLogger(BoardController.class);
	public static int num;
	private BoardService boardService;
	private CalendarService calendarService;
	private MemberService memberService;

	@Autowired
	public BoardController(BoardService boardService, MemberService memberService, CalendarService calendarService) {
		this.calendarService = calendarService;
		this.boardService = boardService;
		this.memberService = memberService;
	}


	@GetMapping(value = "/write")
	public String insertBoard(@ModelAttribute("boardData") BoardVo boardVo, HttpSession session) {
		if(session.getAttribute("memberLogin") == null) {
			return "/member/login";
		}
		return "board/writeForm";
	}

	@PostMapping(value = "/write")
	public String insertBoard(@Valid @ModelAttribute("boardData") BoardVo boardVo, BindingResult bindingResult,
			Criteria cri, HttpSession session, Model model) {
		// not null 체크
		if (bindingResult.hasErrors()) {

			return "board/writeForm";
		}

		LoginCommand loginMember = (LoginCommand) session.getAttribute("memberLogin");

		boolean memberAuth = boardService.memberAuth(loginMember.getMemberSeq()).equals("F");
		if (memberAuth == true) {
			return "errors/memberAuthError"; // 이메일 인증 x -> 예외 페이지

		}

		// 세션에서 멤버의 mSeq 를 boardVo에 셋팅
		boardVo.setMemberSeq(loginMember.getMemberSeq());

		// insert
		boardService.insertBoard(boardVo);

		// calendar insert
		// boardSeq를 넣는다.
		Long currentSeq = boardService.currentBoardSeq();
		calendarService.insertCalendar(currentSeq);

		int mytotal = boardService.mylistCount(loginMember.getMemberSeq());

		if (mytotal > 10) {
			int memberLevel = boardService.memberLevelup(loginMember.getMemberSeq(), mytotal,
					loginMember.getMemberLevel());

			if (memberLevel == 1) {
				LoginCommand member = memberService.login(loginMember.getMemberId());
				LoginCommand login = member;
				session.setAttribute("memberLogin", login);
				model.addAttribute("level", login.getMemberLevel());
				model.addAttribute("nickname", login.getMemberNickname());
				return "/member/member_alert/levelUp";

			}
		}

		return "redirect:/board/list";
	}

	@PostMapping(value = "/ckUpload")
	public void ckUpload(HttpServletRequest request, HttpServletResponse response, @RequestParam MultipartFile upload) {

		OutputStream out = null;
		PrintWriter printWriter = null;

		// 인코딩
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		// 서버의 업로드할 물리적 위치
		String resources = "C:/dev1/workspacesQcali/resources/upload";
		String folder = resources + "/" + "board" + "/" + new SimpleDateFormat("yyyy/MM/dd").format(new Date());

		// 파일 이름
		UUID uuid = UUID.randomUUID();
		String ckUploadPath = uuid + "_" + upload.getOriginalFilename();

		// 폴더 생성
		File f = new File(folder);

		if (!f.exists()) {
			f.mkdirs();
		}

		try {

			byte[] bytes = upload.getBytes();

			out = new FileOutputStream(new File(folder, ckUploadPath));
			out.write(bytes);
			out.flush(); // out에 저장된 데이터를 전송하고 초기화

			// String callback = request.getParameter("CKEditorFuncNum");
			printWriter = response.getWriter();
			// String fileUrl = "localhost:8080/exam/board/ckUploadSubmit?uuid=" + uuid +
			// "&fileName=" + upload.getOriginalFilename(); // 작성화면
			String fileUrl = "/boardImg/" + new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/" + ckUploadPath;

			// 업로드시 메시지 출력
			printWriter.println(
					"{\"filename\" : \"" + ckUploadPath + "\", \"uploaded\" : 1, \"url\":\"" + fileUrl + "\"}");
			printWriter.flush();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null) {
					out.close();
				}
				if (printWriter != null) {
					printWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	// 리스트 전체
	@GetMapping(value = "/list")
	public String boardListAll(Criteria cri, Model model, HttpSession session) {

		/*
		 * @RequestParam null 허용 방법 - (required = false) == true 가 기본 설정임 - @Nullable
		 * 어노테이션 추가
		 * 
		 * - int 형의 경우 (defaultValue="0")
		 * 
		 */

		int total = boardService.listCount();

		if (total == 0) {
			total = 1;
		}
		/*
		 * 1 1,10 2 11, 20
		 */

		List<BoardlistCommand> list = boardService.boardList(cri);

		model.addAttribute("boardList", list);

		PagingVo pageCommand = new PagingVo();
		pageCommand.setCri(cri);
		pageCommand.setTotalCount(total);
		model.addAttribute("pageMaker", pageCommand);
		model.addAttribute("boardTotal", total);

		// 질문 출력 관련
		if (num == 0) {
			num = boardService.currentSequence();
			if (num == 0) {
				num = 1;
			}
		}
		logger.info("" + num);
		QuestionAdayCommand question = boardService.questionselect(num);

		model.addAttribute("boardQuestion", question);
		System.out.println(question);
		
		//공지사항
		List<NoticeAdminVo> notice = boardService.noticelist();
		System.out.println(notice);
		model.addAttribute("notice", notice);
		
		
		return "board/list";
	}

	@Scheduled(cron = "0 0 12 1/1 * ?") // 하루마다 출력으로 표현식
	public void getSequence() {
		logger.info(new Date() + "스케쥴러 실행");
		num = boardService.getSequence();
	}

	// 해당list 내 글 모아보기
	@GetMapping(value = "/mylist")
	public String boardListMy(@RequestParam("memberSeq") Long memberSeq, Model model, Criteria cri,
			HttpSession session) {

		int total = boardService.mylistCount(memberSeq);

		List<BoardlistCommand> list = boardService.boardMyList(cri, memberSeq);
		model.addAttribute("boardList", list);

		PagingVo pageCommand = new PagingVo();
		pageCommand.setCri(cri);
		pageCommand.setTotalCount(total);
		model.addAttribute("boardTotal", total);
		model.addAttribute("pageMaker", pageCommand);

		return "board/mylist";
	}

	// 게시글 디테일
	@GetMapping(value = "/detail")
	public String boardListDetail(@RequestParam Long boardSeq, Model model, HttpSession session) {

		boardService.boardCountup(boardSeq);

		BoardlistCommand list = boardService.boardListDetail(boardSeq);
		boolean myArticle = false;
		// 세션 값 loginMember에 저장

		LoginCommand loginMember = (LoginCommand) session.getAttribute("memberLogin");

		if (loginMember != null) {
			// 세션에서 멤버의 mSeq 를 boardVo에 셋팅
			Long memberSeq = loginMember.getMemberSeq();
			// 세션에 저장된 mSeq와 게시글의 mSeq를 비교하여 내 글이면 수정 삭제 버튼이 뜨게
			if (memberSeq == list.getMemberSeq()) {
				myArticle = true;
			}

			model.addAttribute("myArticle", myArticle);
		}
		

		model.addAttribute("boardList", list);
		model.addAttribute("boardSeq", boardSeq);
		

		BoardHeartVo likeVo = new BoardHeartVo();

		likeVo.setBoardSeq(boardSeq);
		likeVo.setMemberSeq(loginMember.getMemberSeq());

		int boardlike = boardService.getBoardLike(likeVo);

		model.addAttribute("boardHeart", boardlike);

		return "board/listDetail";
	}

	@PostMapping(value = "/heart", produces = "application/json")
	@ResponseBody
	public int boardLike(@RequestBody BoardLikeCommand command, HttpSession session) {

		LoginCommand loginMember = (LoginCommand) session.getAttribute("memberLogin");

		BoardHeartVo likeVo = new BoardHeartVo();

		likeVo.setBoardSeq(command.getBoardSeq());
		likeVo.setMemberSeq(loginMember.getMemberSeq());

		if (command.getHeart() >= 1) {
			boardService.deleteBoardLike(likeVo);
			command.setHeart(0);
		} else {

			boardService.insertBoardLike(likeVo);
			command.setHeart(1);
		}

		// String result = Integer.toString(heart);

		return command.getHeart();

	}

	
	//댓글 list
	@PostMapping(value = "/reply/{boardSeq}")	
	@ResponseBody
	public List<ReplyVo> boardReply(@PathVariable Long boardSeq, HttpSession session, Model model) {
		List<ReplyVo> replyList = boardService.replyList(boardSeq);
		//logger.info(replyList.toString());
		return replyList;
	}
	
	
	//댓글 insert
		@PostMapping(value = "/replyInsert", produces = "application/json")	
		@ResponseBody
		public void replyInsert(@RequestBody BoardreplyInsertCommand command, HttpSession session) {
			LoginCommand loginMember = (LoginCommand) session.getAttribute("memberLogin");
			
			ReplyVo insertReply = new ReplyVo();
			insertReply.setBoardSeq(command.getBoardSeq());
			insertReply.setMemberSeq(loginMember.getMemberSeq());
			insertReply.setReplyContent(command.getReplyContent());
			
			boardService.replyInsert(insertReply);
		}
	
		
	//댓글 update
	@PostMapping(value = "/replyUpdate/{replySeq}", produces = "application/json")
	@ResponseBody
	public Map<String, Object> replyUpdate(@RequestBody BoardreplyInsertCommand command, @PathVariable Long replySeq) {
		Map<String, Object> map = new HashMap<String, Object>();
		
		ReplyVo updateReply = new ReplyVo();
		updateReply.setReplySeq(replySeq);
		updateReply.setReplyContent(command.getReplyContent());

		boardService.replyUpdate(updateReply);
		map.put("result", "success");
		
		return map;	
	}

	
	//댓글 delete
	@PostMapping(value = "/replydelete/{replySeq}", produces = "application/json")
	@ResponseBody
	public Map<String, Object> replyDelete(@PathVariable Long replySeq) {
		Map<String, Object> map = new HashMap<String, Object>();

		ReplyVo deleteReply = new ReplyVo();
		deleteReply.setReplySeq(replySeq);
	
		boardService.replyDelete(deleteReply);
		map.put("result", "success");
		
		return map;
	}

	// 게시글 수정
	@GetMapping(value = "/edit")
	public String boardEdit(@ModelAttribute("boardEditData") BoardVo boardVo, HttpSession session, Model model) {

		model.addAttribute("articleInfo", boardService.boardListDetail(boardVo.getBoardSeq()));
		return "board/editForm";
	}

	// 게시글 수정
	@PostMapping(value = "/edit")
	public String boardEdit(@Valid @ModelAttribute("boardEditData") BoardupdateCommand updateCommand,
			BindingResult bindingResult, Model model, HttpSession session) {

		if (bindingResult.hasErrors()) {

			return "board/editForm";
		}

		// 세션 값 loginMember에 저장
		LoginCommand loginMember = (LoginCommand) session.getAttribute("memberLogin");

		if (loginMember != null) {
			// 세션에서 멤버의 mSeq 를 boardVo에 셋팅

			Long boardSeq = updateCommand.getBoardSeq();

			BoardlistCommand list = boardService.boardListDetail(boardSeq);

			model.addAttribute("boardList", list);
			boardService.updateBoard(updateCommand.getBoardTitle(), updateCommand.getBoardContent(), boardSeq);
			System.out.println(" 수정 성공");
		} else {
			System.out.println("수정 실패");
			return "errors/mypageChangeError";
		}

		return "redirect:/board/list";
	}

	// 게시글 삭제
	@GetMapping(value = "/delete")
	public String boardDelect(@RequestParam Long boardSeq, Model model, HttpSession session, Criteria cri) {

		// 세션 값 loginMember에 저장
		LoginCommand loginMember = (LoginCommand) session.getAttribute("memberLogin");

		if (loginMember != null) {
			// 세션에서 멤버의 mSeq 를 boardVo에 셋팅
			Long memberSeq = loginMember.getMemberSeq();
			boardService.deleteBoardOne(boardSeq, memberSeq);
			System.out.println("삭제 성공");
		} else {
			System.out.println("삭제 실패");
			return "errors/mypageChangeError";
		}

		return "redirect:/board/list";
	}

	// 닉네임 , 제목으로 검색
	@GetMapping(value = "/search")
	public String boardSearchList(@RequestParam("searchOption") String searchOption,
			@RequestParam("searchWord") String searchWord, Model model, Criteria cri) {
		
		HashMap<String, Object> map = new HashMap<String, Object>();

		map.put("searchOption", searchOption);
		map.put("searchWord", searchWord);

		int total = boardService.boardSearchCount(map);
		
		map.put("rowStart", cri.getRowStart());
		map.put("rowEnd", cri.getRowEnd());
		List<BoardlistCommand> list = boardService.boardSearch(map);

		
		
		PagingVo pageCommand = new PagingVo();
		pageCommand.setCri(cri);
		pageCommand.setTotalCount(total);
		model.addAttribute("boardTotal", total);
		model.addAttribute("pageMaker", pageCommand);
		model.addAttribute("boardList", list);
		
		//질문 출력
		
		QuestionAdayCommand question = boardService.questionselect(num);
		model.addAttribute("boardQuestion", question);
		
		
		return "/board/list";
	}

}
