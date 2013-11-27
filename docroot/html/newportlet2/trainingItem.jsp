<%@ include file="/html/newportlet2/init.jsp" %>

<portlet:actionURL name="getMaterialContent" var="getMaterialContent"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>

<%
String matId = (String) request.getAttribute("mat_id");
%>

<html>
<head>
<script type="text/javascript">
$(document).ready(function() {
	
	var request = $.getJSON('<%=getMaterialContent%>', {"id": '<%=matId%>'});
	request.done(function(data) {
		$("#materialTitle").html(data.title);
		$("#materialBody").html(data.text);
	});
});
</script>
</head>
<body>
<div class="trainingBody">
<div class="contentBody">
<div id="materialTitle" class="title">
</div>
<div id="materialBody">
</div>
</div>
</div>
</body>
</html>