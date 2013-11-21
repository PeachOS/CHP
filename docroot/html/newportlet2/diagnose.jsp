<%@ include file="/html/newportlet2/init.jsp" %>
<%@ page import="org.json.simple.JSONObject" %>

<portlet:actionURL name="getTreatment" var="getTreatment"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>

<html>
<header>
<script type="text/javascript">
$(document).ready(function() {
	var request = $.getJSON('<%=getTreatment%>');
	request.done(function(data) {
		$("#diagnoseTitle").html(data.title);
		$("#diagnoseBody").append("<b>Treatment:</b> " + data.treatment);
	});
});
</script>
</header>
<body>
<div id="diagnoseBody">
<div id="diagnoseTitle"></div>
</div>
</body>
</html>
	