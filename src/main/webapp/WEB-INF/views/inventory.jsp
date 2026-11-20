<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.lifelink.model.User" %>
<%
String[] groups=(String[])request.getAttribute("bloodGroups");
int[] units=(int[])request.getAttribute("inventoryUnits");
%>
<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><title>Blood inventory · LifeLink</title><link rel="stylesheet" href="<%=request.getContextPath()%>/assets/css/style.css"></head><body>
<nav class="nav"><a class="brand" href="<%=request.getContextPath()%>/index.jsp">Life<span>Link</span></a><a class="secondary small" href="<%=request.getContextPath()%>/dashboard">Dashboard</a></nav>
<main class="auth"><section class="auth-card" style="width:min(100%,620px)"><p class="eyebrow">BLOOD BANK WORKSPACE</p><h1>Blood inventory</h1><p class="muted">Update the available units recorded by your blood bank for each blood group.</p>
<%if("1".equals(request.getParameter("saved"))){%><div class="notice">Inventory saved successfully.</div><%}%>
<%if("invalid".equals(request.getParameter("error"))){%><div class="notice">Enter a whole number from 0 to 99,999 for every group.</div><%}%>
<%if("save".equals(request.getParameter("error"))){%><div class="notice">We couldn't save the inventory. Please try again.</div><%}%>
<form method="post" action="<%=request.getContextPath()%>/inventory"><input type="hidden" name="csrfToken" value="<%=session.getAttribute("csrfToken")%>"/>
<div style="display:grid;grid-template-columns:repeat(2,minmax(0,1fr));gap:14px">
<%for(int i=0;i<groups.length;i++){%><div><label for="units_<%=i%>"><%=groups[i]%> blood group</label><input id="units_<%=i%>" name="units_<%=i%>" type="number" min="0" max="99999" step="1" value="<%=units[i]%>" required></div><%}%>
</div>
<button class="primary full" type="submit">Save inventory</button>
</form><p class="muted" style="font-size:12px">This is a project demonstration record. Stock changes from donations, requests and transfers will be handled by their respective workflows.</p>
</section></main></body></html>