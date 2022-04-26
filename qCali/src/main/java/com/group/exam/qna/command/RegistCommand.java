package com.group.exam.qna.command;

import org.springframework.web.multipart.MultipartFile;

public class RegistCommand {
	private String qnaTitle;
	private String qnaContent;
	private MultipartFile uploadfile;
	private int qnaSeq;
	private int memberSeq;

	
	public int getMemberSeq() {
		return memberSeq;
	}
	public void setMemberSeq(int memberSeq) {
		this.memberSeq = memberSeq;
	}
	public int getQnaSeq() {
		return qnaSeq;
	}
	public void setQnaSeq(int qnaSeq) {
		this.qnaSeq = qnaSeq;
	}
	@Override
	public String toString() {
		return "RegistCommand [qnaTitle=" + qnaTitle + ", qnaContent=" + qnaContent + ", uploadfile=" + uploadfile + "]";
	}
	
	public String getQnaTitle() {
		return qnaTitle;
	}
	public void setQnaTitle(String qnaTitle) {
		this.qnaTitle = qnaTitle;
	}
	public String getQnaContent() {
		return qnaContent;
	}
	public void setQnaContent(String qnaContent) {
		this.qnaContent = qnaContent;
	}
	public MultipartFile getUploadfile() {
		return uploadfile;
	}
	public void setUploadfile(MultipartFile uploadfile) {
		this.uploadfile = uploadfile;
	}
	
	
	
}
