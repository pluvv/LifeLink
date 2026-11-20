<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.*" %>
<%
List<Map<String,Object>> banks=(List<Map<String,Object>>)request.getAttribute("banks");
List<Map<String,Object>> appointments=(List<Map<String,Object>>)request.getAttribute("appointments");
%>
<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Donation appointments · LifeLink</title><link rel="stylesheet" href="assets/css/style.css"></head><body>
<nav class="nav"><a class="brand" href="index.jsp">Life<span>Link</span></a><a class="secondary small" href="dashboard">Dashboard</a></nav>
<main class="auth"><section class="auth-card" style="width:min(100%,680px)"><p class="eyebrow">DONOR WORKSPACE</p><h1>Donation appointments</h1><p class="muted">Choose an approved blood bank and request an appointment. The bank will review your request.</p>
<%if("1".equals(request.getParameter("booked"))){%><div class="notice">Appointment request submitted. It is pending bank approval.</div><%}%>
<%if("invalid".equals(request.getParameter("error"))){%><div class="notice">Check the bank, date and notes. Choose a date at least 30 minutes from now.</div><%}%>
<%if("bank".equals(request.getParameter("error"))){%><div class="notice">That blood bank is not available for booking.</div><%}%>
<%if("save".equals(request.getParameter("error"))){%><div class="notice">We couldn't save the appointment. Please try again.</div><%}%>
<form method="post" action="appointments">
<label for="bankId">Approved blood bank</label><select id="bankId" name="bankId" required><option value="">Choose a blood bank</option><%for(Map<String,Object>b:banks){%><option value="<%=b.get("id")%>"><%=b.get("name")%> — <%=b.get("city")%></option><%}%></select>
<label for="scheduledAt">Preferred date and time</label><input id="scheduledAt" name="scheduledAt" type="datetime-local" required>
<label for="notes">Notes (optional)</label><input id="notes" name="notes" maxlength="500" placeholder="Anything the blood bank should know">
<button class="primary full" type="submit">Request appointment</button>
</form>
<h2 style="font:700 20px Manrope,sans-serif;margin-top:30px">Your appointment history</h2>
<%if(appointments.isEmpty()){%><p class="muted">You haven't requested an appointment yet.</p><%}else{%>
<div style="display:grid;gap:12px"><%for(Map<String,Object>a:appointments){%><article style="border:1px solid var(--line);border-radius:12px;padding:14px"><strong><%=a.get("bank")%></strong><p class="muted"><%=a.get("city")%> · <%=a.get("when")%></p><p>Status: <strong><%=a.get("status")%></strong></p><%if(a.get("notes")!=null&&!a.get("notes").toString().isBlank()){%><p class="muted"><%=a.get("notes")%></p><%}%></article><%}%></div><%}%>
</section></main></body></html>