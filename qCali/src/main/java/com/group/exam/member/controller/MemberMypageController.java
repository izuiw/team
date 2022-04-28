package com.group.exam.member.controller;

import java.util.List;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.group.exam.board.command.BoardlistCommand;
import com.group.exam.board.service.BoardService;
import com.group.exam.member.command.LoginCommand;
import com.group.exam.member.command.MemberFindPwdCommand;
import com.group.exam.member.command.MemberchangePwd;
import com.group.exam.member.service.MailSendService;
import com.group.exam.member.service.MemberService;
import com.group.exam.utils.Criteria;
import com.group.exam.utils.PagingVo;

@Controller
@RequestMapping(value = "/member/mypage")
public class MemberMypageController {

	private MemberService memberService;
	
	private BoardService boardService;

	private BCryptPasswordEncoder passwordEncoder;

	@Autowired
	public MemberMypageController(MemberService memberService, BCryptPasswordEncoder passwordEncoder, BoardService boardService) {

		this.memberService = memberService;
		this.passwordEncoder = passwordEncoder;
		this.boardService = boardService;
	}

	@GetMapping(value = "/confirmPwd")
	public String confirmPwd(String memberPassword, HttpSession session, Model model, Criteria cri) {
		
		boolean confirmPW = false;
		
		// api 로그인 시, 비밀번호 확인 안하고 마이페이지 바로 이동.
		LoginCommand command = (LoginCommand) session.getAttribute("memberLogin");
		
		if (command.getNaver().equals("T") || command.getKakao().equals("T")) {
			
			confirmPW = true;
			model.addAttribute("confirmPW", confirmPW);
			
			//마이페이지에 본인 쓴 글 바로 출력 
			int total = boardService.mylistCount(command.getMemberSeq());

			List<BoardlistCommand> list = boardService.boardMyList(cri, command.getMemberSeq());
			model.addAttribute("boardList", list);

			PagingVo pageCommand = new PagingVo();
			pageCommand.setCri(cri);
			pageCommand.setTotalCount(total);
			model.addAttribute("boardTotal", total);
			model.addAttribute("pageMaker", pageCommand);
			
			return "member/mypage";
		}
		
		model.addAttribute("confirmPW", confirmPW);
		return "/member/mypage";
	}

	// 마이페이지 가기 전에 비밀번호 체크
	@PostMapping(value = "/confirmPwd")
	public String confirmPwd(@RequestParam String memberPassword, Model model, HttpSession session, Criteria cri) {
		
		boolean confirmPW = false;
		
		LoginCommand command = (LoginCommand) session.getAttribute("memberLogin");

		String encodePassword = memberService.findPwd(command.getMemberId()).getMemberPassword();
		boolean pwdEncode = passwordEncoder.matches(memberPassword, encodePassword);

		if (pwdEncode) {
			confirmPW = true;
			model.addAttribute("confirmPW", confirmPW);
			
			//마이페이지에 본인 쓴 글 바로 출력 
			int total = boardService.mylistCount(command.getMemberSeq());

			List<BoardlistCommand> list = boardService.boardMyList(cri, command.getMemberSeq());
			model.addAttribute("boardList", list);

			PagingVo pageCommand = new PagingVo();
			pageCommand.setCri(cri);
			pageCommand.setTotalCount(total);
			model.addAttribute("boardTotal", total);
			model.addAttribute("pageMaker", pageCommand);
			
			return "/member/mypage";
		}
		
		model.addAttribute("confirmPW", confirmPW);
		model.addAttribute("msg", "비밀번호가 다릅니다.");

		return "/member/mypage";
	}

	// 비밀번호 변경
	@GetMapping(value = "changePwd")
	public String changePwd(@ModelAttribute("changepwdData") MemberchangePwd changepwdData, HttpSession session) {
		
		return "/member/changePwdForm";
	}

	@PostMapping(value = "changePwd")
	public String changePwd(@Valid @ModelAttribute("changepwdData") MemberchangePwd changepwdData,
			BindingResult bindingResult, HttpSession session, Model model) {

		if (bindingResult.hasErrors()) {
			return "/member/changePwdForm";
		}

		// 비밀번호-비밀번호 확인 check
		boolean pwdcheck = changepwdData.getMemberPassword().equals(changepwdData.getMemberPasswordCheck());

		if (pwdcheck != true) {

			return "/member/changePwdForm";
		}

		LoginCommand command = (LoginCommand) session.getAttribute("memberLogin");

		// 임시 비밀번호와 같은지 체크
		String encodePassword = memberService.findPwd(command.getMemberId()).getMemberPassword();
		boolean pwdEncode = passwordEncoder.matches(changepwdData.getMemberPassword(), encodePassword);

		if (pwdEncode) {

			model.addAttribute("msg", "기존 비밀번호와 동일합니다.");
			return "/member/changePwdForm";
		}

		// 기존 비밀번호와 같은지 체크

		encodePassword = memberService.findPwd(command.getMemberId()).getMemberBpw();

		pwdEncode = passwordEncoder.matches(changepwdData.getMemberPassword(), encodePassword);

		if (pwdEncode) {
			model.addAttribute("msg", "기존 비밀번호와 동일합니다.");
			return "/member/changePwdForm";
		}

		String updateEncodePassword = passwordEncoder.encode(changepwdData.getMemberPassword());

		int result = memberService.updateMemberPwd(updateEncodePassword, command.getMemberSeq());

		if (result != 1) {
			System.out.println("비밀번호 변경 실패");
			return "/errors/mypageChangeError";
		}

		// 세션 로그인 정보
		LoginCommand login = memberService.login(command.getMemberId());

		session.setAttribute("memberLogin", login);

		return "/member/member_alert/changeNext";
	}

	// 닉네임 변경
	@GetMapping(value = "/changeNickname")
	public String changeNickname(HttpSession session) {
		System.out.println("dddd");

		return "/member/changeNicknameForm";
	}

	@PostMapping(value = "/changeNickname")
	@ResponseBody
	public String changeNickname(@RequestBody String memberNickname, HttpSession session,
			Model model) {

		LoginCommand command = (LoginCommand) session.getAttribute("memberLogin");
		
	
		System.out.println(memberNickname);
		
		int result = memberService.updateMemberNickname(memberNickname, command.getMemberSeq());
		
		if (result != 1) {
			System.out.println("닉네임 변경 실패");
			return "errors/mypageChangeError";// 에러 페이지
		}

		// 세션 로그인 정보
		LoginCommand login = memberService.login(command.getMemberId());

		session.setAttribute("memberLogin", login);
		return "/member/changeNicknameForm";

	}

	// 회원 탈퇴
	@GetMapping(value = "/delete")
	public String deleteMember(HttpSession session) {

		return "/member/mypage";

	}

	@PostMapping(value = "/delete")
	public String deleteMember(@RequestParam String memberPassword, Model model, HttpSession session) {

		LoginCommand command = (LoginCommand) session.getAttribute("memberLogin");

		String encodePassword = memberService.findPwd(command.getMemberId()).getMemberPassword();
		boolean pwdEncode = passwordEncoder.matches(memberPassword, encodePassword);

		if (pwdEncode) {

			int result = memberService.deleteMember(command.getMemberSeq());

			if (result != 1) {
				System.out.println("회원 탈퇴 실패");
				return "errors/mypageChangeError";
			}

			session.invalidate(); // 탈퇴 성공시, 로그인 세션 제거

			return "/member/member_alert/memberDeleteNext";

		}
		model.addAttribute("msg", "비밀번호가 다릅니다.");

		return "/member/mypage";
	}

}
