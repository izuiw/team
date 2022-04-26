<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>board</title>
</head>
<body>
<h2>공지사항</h2>
	<c:forEach var="n" items="${notice }">
		<label>${n.rownum } 
		<a href="<c:url value='/notice/read/${n.noticeSeq}' />">  ${n.noticeTitle} </a>
		| ${n.adminNickname} </label><br>
		
		제발 하고 싶어요
		
	</c:forEach>
</body>
</html>