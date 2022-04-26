package com.group.exam.admin.service;

import java.util.List;
import java.util.Map;

import com.group.exam.admin.command.AdminAuthInfoCommand;
import com.group.exam.admin.command.AdminBoardCommand;
import com.group.exam.admin.command.AdminQuestionMember;
import com.group.exam.member.vo.MemberVo;
import com.group.exam.question.vo.QuestionRegistCommand;
import com.group.exam.question.vo.QuestionVo;
import com.group.exam.utils.Criteria;

public interface AdminService {
	public List<AdminQuestionMember> questionList(Criteria cri);
	public List<AdminQuestionMember> questionAllList(Criteria cri);
	public List<MemberVo> selectMember(Criteria cri);
	public List<AdminBoardCommand> boardList(Criteria cri);
		
	public AdminAuthInfoCommand authenticate(String adminId, String adminPassword);
	public void memberDelete(int memberSeq);

	public void questionApprove(int questionSeq);
	public void addQuestion(QuestionRegistCommand qusestionRegistCommand);
	public void questionDelete(int questionSeq);

	public AdminBoardCommand selectBybseq(int boardSeq);
	public void boardDelete(int boardSeq);

	public int boardListTotal();
	public int countQuestionList();
	public int memberListTotal();
	public int questionListTotal();
	
	//하루마다 질문 호출
	public QuestionVo questionselect(int num);
	public int getSequence();
	public int currentSequence();
}