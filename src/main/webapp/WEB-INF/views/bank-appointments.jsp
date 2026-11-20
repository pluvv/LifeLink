<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
List<Map<String,Object>> appointments=(List<Map<String,Object>>)request.getAttribute("appointments");
%>
<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Donation requests · LifeLink</title><link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/style.css"></head><body>
<nav class="nav"><a class="brand" href="<%=request.getContextPath()%>/index.jsp">Life<span>Link</span></a><a class="secondary small" href="<%=request.getContextPath()%>/dashboard">Dashboard</a></nav>
<main class="auth"><section class="auth-card" style="width:min(100%,820px)"><p class="eyebrow">BLOOD BANK WORKSPACE</p><h1>Donation requests</h1><p class="muted">Review donor appointment requests. Completing an approved donation adds one unit to the donor's blood group inventory.</p>
<c:if test="${param.updated eq '1'}"><div class="notice">Appointment updated successfully.</div></c:if>
<c:if test="${not empty param.error}"><div class="notice">We couldn't apply that action. Refresh and check the appointment status.</div></c:if>
<c:choose><c:when test="${empty appointments}"><p class="muted">There are no appointment requests yet.</p></c:when><c:otherwise>
<div style="display:grid;gap:14px"><c:forEach items="${appointments}" var="a"><article style="border:1px solid var(--line);border-radius:12px;padding:16px">
<div style="display:flex;justify-content:space-between;gap:12px;flex-wrap:wrap"><strong><c:out value="${a.donor}"/></strong><strong><c:out value="${a.status}"/></strong></div>
<p class="muted"><c:out value="${a.email}"/> · Blood group <strong><c:out value="${a.bloodGroup}"/></strong></p>
<p><strong>Requested:</strong> <c:out value="${a.when}"/></p>
<c:if test="${not empty a.notes}"><p class="muted"><c:out value="${a.notes}"/></p></c:if>
<c:if test="${a.status eq 'PENDING' or a.status eq 'APPROVED'}"><form method="post" action="<%=request.getContextPath()%><input type="hidden" name="csrfToken" value="<%=session.getAttribute("csrfToken")%>"/>/bank/appointments" style="display:flex;gap:8px;flex-wrap:wrap">
<input type="hidden" name="appointmentId" value="<c:out value='${a.id}'/>"/>
<c:if test="${a.status eq 'PENDING'}"><button class="primary" name="action" value="approve" type="submit">Approve</button><button class="secondary" name="action" value="reject" type="submit">Reject</button></c:if>
<c:if test="${a.status eq 'APPROVED'}"><button class="primary" name="action" value="complete" type="submit">Mark donation completed</button></c:if>
</form></c:if>
</article></c:forEach></div></c:otherwise></c:choose>
</section></main></body></html>