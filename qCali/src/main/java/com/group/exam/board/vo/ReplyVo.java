package com.group.exam.board.vo;

public class ReplyVo {
	private int replySeq;
	private int boardSeq;
	private int memberSeq;
	private String replyContent;
	private String memberNickname;
	private String replyRegDay;
	
	public int getReplySeq() {
		return replySeq;
	}
	public void setReplySeq(int replySeq) {
		this.replySeq = replySeq;
	}
	public int getBoardSeq() {
		return boardSeq;
	}
	public void setBoardSeq(int boardSeq) {
		this.boardSeq = boardSeq;
	}
	public int getMemberSeq() {
		return memberSeq;
	}
	public void setMemberSeq(int memberSeq) {
		this.memberSeq = memberSeq;
	}
	public String getReplyContent() {
		return replyContent;
	}
	public void setReplyContent(String replyContent) {
		this.replyContent = replyContent;
	}
	public String getMemberNickname() {
		return memberNickname;
	}
	public void setMemberNickname(String memberNickname) {
		this.memberNickname = memberNickname;
	}
	public String getReplyRegDay() {
		return replyRegDay;
	}
	public void setReplyRegDay(String replyRegDay) {
		this.replyRegDay = replyRegDay;
	}
	@Override
	public String toString() {
		return "ReplyVo [replySeq=" + replySeq + ", boardSeq=" + boardSeq + ", memberSeq=" + memberSeq
				+ ", replyContent=" + replyContent + ", memberNickname=" + memberNickname + ", replyRegDay="
				+ replyRegDay + "]";
	}
	
	
}
