<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib  prefix="spring" uri="http://www.springframework.org/tags" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link href="<c:url value='/resources/static/css/dropdown.css'/> " rel="stylesheet" type="text/css">
<title>Q&A list</title>
</head>
<body>
	<h3>Q&A</h3>
	
	<!-- 목록 -->
	<table>
		<tr>
			<th>번호</th>
			<th>제목</th>
			<th>작성자</th>
			<th>작성일자</th>
			<th>첨부파일</th>
		</tr>
		<c:forEach items="${qnaList }" var="list">
		<tr>
			<td>${list.qnaNo }</td>
			<td>
				<c:forEach var="i" begin="1" end="${list.qnaIndent }">
					<c:choose>
					<c:when test="${i eq list.qnaIndent }">
						<img src="<c:url value='/resources/static/images/re.gif '/> "/>
					</c:when>
					<c:otherwise>
						&nbsp;&nbsp;
					</c:otherwise>		
					</c:choose>			
				</c:forEach>
				
				<c:choose>
					<c:when test="${list.qnaTitle eq 'none' }">
						삭제된 글입니다.
					</c:when>
					<c:otherwise>
						<a href="<c:url value='/qna/detail/${list.qnaSeq} '/> ">${list.qnaTitle }</a>
					</c:otherwise>
				</c:choose>
			</td>
			<!-- 관리자 일 경우 -->
			
			<c:if test="${!empty list.qnaWriter }">	
				<td>
				<div class="dropdown">
					<a class="dropbtn">${list.qnaWriter }</a>
					<div class="dropdown-content">
						<a href="#">게시물 보기</a>
						<a href="#">회원 정보 보기</a>
					</div>
				</div>
				</td>
			</c:if>
			
			<c:if test="${empty list.qnaWriter }">
				<c:if test="${empty list.memberSeq }">
					<!-- on delete set null로 회원이 null로 바뀔 경우 -->
					<td>탈퇴회원</td>
				</c:if>
				
				<c:if test="${!empty list.memberNickname }">
					<!-- 회원의 닉네임이 있을 경우 -->
					<td><div class="dropdown">
							<button class="dropbtn">${ list.memberNickname}</button>
							<div class="dropdown-content">
								<a href="<c:url value='/board/mylist/memberSeq=${list.memberSeq }'/> ">게시물 보기</a>
								<a href="<c:url value='/board/'/> ">회원 정보 보기</a>
							</div>
						</div></td>
				</c:if>
			</c:if>
			
			<td>${list.qnaRegDay }</td>
			<td>
				<c:if test="${!empty list.qnaFileName }">
						<img title="${list.qnaFileName }" /> <!-- src="img/attach.png" -->
				</c:if>
				</td>
			</tr>
		</c:forEach>
	</table>
	
		<div>
	  <ul>
	    <c:if test="${pageMaker.prev}">
	    	<li><a href="list${pageMaker.makeQuery(pageMaker.startPage - 1)}">이전</a></li>
	    </c:if> 
	
	    <c:forEach begin="${pageMaker.startPage}" end="${pageMaker.endPage}" var="idx">
	    	<li><a href="list${pageMaker.makeQuery(idx)}">${idx}</a></li>
	    </c:forEach>
	
	    <c:if test="${pageMaker.next && pageMaker.endPage > 0}">
	    	<li><a href="list${pageMaker.makeQuery(pageMaker.endPage + 1)}">다음</a></li>
	    </c:if> 
	  </ul>
	</div>
	
</body>
</html>