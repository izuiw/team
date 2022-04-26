package com.group.exam.qna.dao;

import java.util.List;

import com.group.exam.qna.vo.QnaVo;
import com.group.exam.utils.Criteria;

public interface QnaDao {
	public void qnaInsert(QnaVo vo);
	
	public List<QnaVo> list(Criteria cri);
	public QnaVo detail(int qnaSeq);
	public void qnaUpdate(QnaVo vo);
	public void qnaDelete(int qnaSeq);
	public void qnaRead(int qnaSeq); //조회수 증가
	public void qnaReplyInsert(QnaVo vo);//답글
	public int qnaTotalList(); //페이징 처리를 위한 total수

}
