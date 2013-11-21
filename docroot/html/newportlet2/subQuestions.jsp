<%@ include file="/html/newportlet2/init.jsp" %>
<%@ page import="org.json.simple.JSONObject" %>

<portlet:actionURL name="getSubQuestions" var="getSubQuestions"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL var="materialsURL">
	<portlet:param name="jspPage" value="/html/newportlet2/materialsList.jsp"/>
	<portlet:param name="actionName" value="materials"/>
</portlet:actionURL>
<portlet:actionURL var="subQuestionsURL">
	<portlet:param name="jspPage" value="/html/newportlet2/subQuestions.jsp"/>
	<portlet:param name="actionName" value="subQuestions"/>
</portlet:actionURL>
<portlet:actionURL var="diagnosisURL">
	<portlet:param name="jspPage" value="/html/newportlet2/diagnosis.jsp"/>
	<portlet:param name="actionName" value="diagnosis"/>
</portlet:actionURL>
<%
String questionId = (String) request.getAttribute("question_id");
String title = (String) request.getAttribute("title");
%>

<html>
<head>
<script type="text/javascript">
$(document).ready(function() {
	console.log("question_id: "+'<%=questionId%>');
	var params = {"question_id" : '<%=questionId%>'};
	var request = jQuery.getJSON('<%=getSubQuestions%>', params);
	request.done(function(data) {
		console.log(data);
		$("#questionsTitle").html(data.title);
		var questionsDiv = document.getElementById("subQuestions");
		for (var i in data.questions) {
			
			// Question
			var qh = $("<h3>");
			qh
				.html(data.questions[i].question)
				.appendTo(questionsDiv);
			
			// Description how to measure symptom
			
			var qAnsDiv = $("<div>");
			qAnsDiv
				.appendTo(questionsDiv);
			var qAnsP = $("<p>");
			qAnsP
				.html(data.questions[i].description)
				.appendTo(qAnsDiv);
			$("<p>").appendTo(qAnsP);
			
			// Yes/No radio buttons
			var ans1 = $("<input>");
			ans1
				.attr("type","radio")
				.attr("name","ans_" + i)
				.attr("id","ans_" + i + "1")
				.attr("question_id",data.questions[i].id)
				.appendTo(qAnsP);
			var ansLbl1 = $("<label>");
			ansLbl1
				.attr("for","ans_" + i + "1")
				.text("Yes")
				.appendTo(qAnsP);
			$("<p>").appendTo(qAnsP);
			var ans2 = $("<input>");
			ans2
				.attr("type","radio")
				.attr("name","ans_" + i)
				.attr("id","ans_" + i + "2")
				.attr("question_id",data.questions[i].id)
				.appendTo(qAnsP);
			var ansLbl2 = $("<label>");
			ansLbl2
				.attr("for","ans_" + i + "2")
				.text("No")
				.appendTo(qAnsP);
		};
		$("#subQuestions").accordion();
				
		var nextForm=$("<form>");
		nextForm
			.attr("action", "<%=subQuestionsURL.toString()%>")
			.attr("method","POST")
			.appendTo(questionsDiv);
		var nextBtn = $("<input>");
		nextBtn
			.attr("type", "submit")
			.attr("id","nextBtn")
			.attr("value","Next")
			.appendTo(nextForm);
		var questionId = $("<input>");
		questionId
			.attr("type","hidden")
			.attr("name", "question_id")
			.attr("value", data.next)
			.appendTo(nextForm);
	});
});

$(document).on("click","#nextBtn",function(){
	var params = {"page" : "subQuestions.jsp", "question_id" : $(this).attr("question_id")};
	
});

</script>
</head>
<body>
<div class="subQuestionsBody">
<span id="questionsTitle"></span>
<p/>
<div id="subQuestions">
</div>
</div>
</body>
</html>