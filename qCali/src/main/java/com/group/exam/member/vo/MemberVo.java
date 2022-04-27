package com.group.exam.member.vo;

import java.util.Date;

public class MemberVo {
	private Long memberSeq;
	private String memberId;
	private String memberBpw;
	private String memberPassword;
	private String memberNickname;
	private Date memberBirthDay;
	private Date memberRegDay;
	private String memberAuth;
	private String memberAuthkey;
	private int memberLevel;

	public MemberVo() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Long getMemberSeq() {
		return memberSeq;
	}

	public void setMemberSeq(Long memberSeq) {
		this.memberSeq = memberSeq;
	}

	public void setMemberBpw(String memberBpw) {
		this.memberBpw = memberBpw;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getMemberBpw() {
		return memberBpw;
	}

	public void setMemberBwd(String memberBpw) {
		this.memberBpw = memberBpw;
	}

	public String getMemberPassword() {
		return memberPassword;
	}

	public void setMemberPassword(String memberPassword) {
		this.memberPassword = memberPassword;
	}

	public String getMemberNickname() {
		return memberNickname;
	}

	public void setMemberNickname(String memberNickname) {
		this.memberNickname = memberNickname;
	}

	public Date getMemberBirthDay() {
		return memberBirthDay;
	}

	public void setMemberBirthDay(Date memberBirthDay) {
		this.memberBirthDay = memberBirthDay;
	}

	public Date getMemberRegDay() {
		return memberRegDay;
	}

	public void setMemberRegDay(Date memberRegDay) {
		this.memberRegDay = memberRegDay;
	}

	public String getMemberAuth() {
		return memberAuth;
	}

	public void setMemberAuth(String memberAuth) {
		this.memberAuth = memberAuth;
	}

	public String getMemberAuthkey() {
		return memberAuthkey;
	}

	public void setMemberAuthkey(String memberAuthkey) {
		this.memberAuthkey = memberAuthkey;
	}

	public int getMemberLevel() {
		return memberLevel;
	}

	public void setMemberLevel(int memberLevel) {
		this.memberLevel = memberLevel;
	}

}
