package com.group.exam.admin.dao;

import java.util.List;
import java.util.Map;

import com.group.exam.admin.command.AdminBoardCommand;
import com.group.exam.admin.command.AdminQuestionMember;
import com.group.exam.member.vo.MemberVo;
import com.group.exam.question.vo.QuestionVo;
import com.group.exam.utils.Criteria;

public interface AdminDao {
	
	public List<AdminQuestionMember> questionList(Criteria cri);
	public List<AdminQuestionMember> questionAllList(Criteria cri);
	public List<AdminBoardCommand> boardList(Criteria cri);
	public List<MemberVo> selectMember(Criteria cri);
	
	
	public Object selectByaId(String adminId);
	public void memberDelete(int memberSeq);

	public void questionApprove(int questionSeq);
	public void questionDelete(int questionSeq);
	public void questionAdd(QuestionVo questionVo);

	public AdminBoardCommand selectBybseq(int boardSeq);
	public void boardDelete(int boardSeq);
	
	
	public int countBoardList();
	public int countQuestionList();
	public int memberListTotal();
	public int questionListTotal();
	
	//질문 하루마다 호출
	public QuestionVo questionselect(int num);
	public int getSequence();
	public int currentSequence();
}
