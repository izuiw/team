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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.group.exam.board.command.BoardlistCommand;
import com.group.exam.board.command.BoardupdateCommand;
import com.group.exam.board.command.QuestionAdayCommand;
import com.group.exam.board.service.BoardService;
import com.group.exam.board.vo.BoardVo;
import com.group.exam.board.vo.NoticeAdminVo;
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
	private MemberService memberService;
	private CalendarService calendarService;
	
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
		// not null ??????
		if (bindingResult.hasErrors()) {

			return "board/writeForm";
		}

		LoginCommand loginMember = (LoginCommand) session.getAttribute("memberLogin");

		boolean memberAuth = boardService.memberAuth(loginMember.getMemberSeq()).equals("F");
		if (memberAuth == true) {
			return "errors/memberAuthError"; // ????????? ?????? x -> ?????? ?????????

		}

		// ???????????? ????????? mSeq ??? boardVo??? ??????
		boardVo.setMemberSeq(loginMember.getMemberSeq());

		// insert
		boardService.insertBoard(boardVo);

		// calendar insert
		// boardSeq??? ?????????.
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

		// ?????????
		response.setCharacterEncoding("utf-8");
		response.setContentType("text/html;charset=utf-8");

		// ????????? ???????????? ????????? ??????
		String resources = "C:/dev1/workspacesQcali/resources/upload";
		String folder = resources + "/" + "board" + "/" + new SimpleDateFormat("yyyy/MM/dd").format(new Date());

		// ?????? ??????
		UUID uuid = UUID.randomUUID();
		String ckUploadPath = uuid + "_" + upload.getOriginalFilename();

		// ?????? ??????
		File f = new File(folder);

		if (!f.exists()) {
			f.mkdirs();
		}

		try {

			byte[] bytes = upload.getBytes();

			out = new FileOutputStream(new File(folder, ckUploadPath));
			out.write(bytes);
			out.flush(); // out??? ????????? ???????????? ???????????? ?????????

			// String callback = request.getParameter("CKEditorFuncNum");
			printWriter = response.getWriter();
			// String fileUrl = "localhost:8080/exam/board/ckUploadSubmit?uuid=" + uuid +
			// "&fileName=" + upload.getOriginalFilename(); // ????????????
			String fileUrl = "/boardImg/" + new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/" + ckUploadPath;

			// ???????????? ????????? ??????
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

	// ????????? ??????
	@GetMapping(value = "/list")
	public String boardListAll(Criteria cri, Model model, HttpSession session) {

		/*
		 * @RequestParam null ?????? ?????? - (required = false) == true ??? ?????? ????????? - @Nullable
		 * ??????????????? ??????
		 * 
		 * - int ?????? ?????? (defaultValue="0")
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

		// ?????? ?????? ??????
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

		// ????????????
		List<NoticeAdminVo> notice = boardService.noticelist();
		System.out.println(notice);
		model.addAttribute("notice", notice);

		return "board/list";
	}

	@Scheduled(cron = "0 0 12 1/1 * ?") // ???????????? ???????????? ?????????
	public void getSequence() {
		logger.info(new Date() + "???????????? ??????");
		num = boardService.getSequence();
	}

	// ????????? ??????
	@GetMapping(value = "/edit")
	public String boardEdit(@ModelAttribute("boardEditData") BoardVo boardVo, HttpSession session, Model model) {

		model.addAttribute("articleInfo", boardService.boardListDetail(boardVo.getBoardSeq()));
		return "board/editForm";
	}

	// ????????? ??????
	@PostMapping(value = "/edit")
	public String boardEdit(@Valid @ModelAttribute("boardEditData") BoardupdateCommand updateCommand,
			BindingResult bindingResult, Model model, HttpSession session) {

		if (bindingResult.hasErrors()) {

			return "board/editForm";
		}

		// ?????? ??? loginMember??? ??????
		LoginCommand loginMember = (LoginCommand) session.getAttribute("memberLogin");

		if (loginMember != null) {
			// ???????????? ????????? mSeq ??? boardVo??? ??????

			Long boardSeq = updateCommand.getBoardSeq();

			BoardlistCommand list = boardService.boardListDetail(boardSeq);

			model.addAttribute("boardList", list);
			boardService.updateBoard(updateCommand.getBoardTitle(), updateCommand.getBoardContent(), boardSeq);
			System.out.println(" ?????? ??????");
		} else {
			System.out.println("?????? ??????");
			return "errors/mypageChangeError";
		}

		return "redirect:/board/list";
	}

	// ????????? ??????
	@GetMapping(value = "/delete")
	public String boardDelect(@RequestParam Long boardSeq, Model model, HttpSession session, Criteria cri) {

		// ?????? ??? loginMember??? ??????
		LoginCommand loginMember = (LoginCommand) session.getAttribute("memberLogin");

		if (loginMember != null) {
			// ???????????? ????????? mSeq ??? boardVo??? ??????
			Long memberSeq = loginMember.getMemberSeq();
			boardService.deleteBoardOne(boardSeq, memberSeq);
			System.out.println("?????? ??????");
		} else {
			System.out.println("?????? ??????");
			return "errors/mypageChangeError";
		}

		return "redirect:/board/list";
	}
	
	// ??????list ??? ??? ????????????
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

	// ????????? , ???????????? ??????
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
		
		//?????? ??????
		
		QuestionAdayCommand question = boardService.questionselect(num);
		model.addAttribute("boardQuestion", question);
		
		
		return "/board/list";
	}

}
