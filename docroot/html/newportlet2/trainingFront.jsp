<%@ include file="/html/newportlet2/init.jsp" %>
<!-- 
<portlet:defineObjects/>
<%
/*PortletURL searchURL = renderResponse.createActionURL();
searchURL.setParameter(
ActionRequest.ACTION_NAME, "search");*/
%>
 -->

<portlet:actionURL name="getTopCategories" var="getTopCategories"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="getTopQuestions" var="getTopQuestions"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL var="subCategoriesURL">
	<portlet:param name="jspPage" value="/html/newportlet2/subCategories.jsp"/>
	<portlet:param name="actionName" value="subCategories"/>
</portlet:actionURL>

<html>
<head>
<script type="text/javascript">
$(document).ready(function() {
	
	// fill categories pane
	
	var request = jQuery.getJSON('<%=getTopCategories%>');
	request.done(function(data) {
		var categoryDiv = document.getElementById("categories");
		for (var i in data) {
			console.log(data[i].id);
			var categorySpan = $("<span>");
			categorySpan
				.addClass("categorySpan")
				.appendTo(categoryDiv);
			var categoryForm = $("<form>");
			categoryForm
				.text(data[i].name)
				.addClass("categoryForm")
				.attr("method","POST")
				.attr("action","<%=subCategoriesURL.toString()%>")
				.button()
				.appendTo(categorySpan);
			var categoryButton = $("<input>");
			categoryButton
				.addClass("categoryButton")
				.attr("type","image")
				.attr("id","category_" + i)
				.attr("src","<%= request.getContextPath()%>/css/images/glasses.jpg")
				.attr("style","height:150px;width:150px;")
				.attr("category_id", data[i].id)
				.text(data[i].name)
				.val(data[i].id)
				.appendTo(categoryForm);
			var catId = $("<input>");
			catId
				.attr("type","hidden")
				.attr("name", "category_id")
				.attr("value", data[i].id)
				.appendTo(categoryForm);
		};
		$("#categories").buttonset();
		
		var request2 = jQuery.getJSON('<%=getTopQuestions%>');
		request2.done(function(data) {
			var questionsDiv = document.getElementById("questions");
			for (var i in data) {
				console.log(data[i].id);
				var questionsSpan = $("<span>");
				questionsSpan
					.addClass("questionsSpan")
					.appendTo(questionsDiv);
				var questionForm = $("<form>");
				questionForm
					.addClass("questionForm")
					.attr("method","POST")
					.attr("action","<%=subCategoriesURL.toString()%>")
					.appendTo(questionsSpan);
				var questionLink = $("<a>");
				questionLink
					.addClass("questionLink")
					.attr("href","javascript:document.forms['test'].submit()")
					.attr("id","question_" + i)
					.html(data[i].question)
					.appendTo(questionForm);
				var catId = $("<input>");
				catId
					.attr("type","hidden")
					.attr("name", "question_id")
					.attr("value", data[i].id)
					.appendTo(questionForm);
			};
		});
	});
});
</script>
</head>
<body>
<div class="trainingBody">
<div class="CHPTitle" align="center">Community Health Portal</div>
<div class="subTitle" align="center">Learning Materials</div>
<!--
<div class="search" align="center">
<form name="<portlet:namespace/>fm" method="POST" action="">
<input type="text" name="<portlet:namespace/>searchParameters" />
<input type="submit" value="Search" id="searchMaterials"/>
  
<script type="text/javascript">
$(function() {
	$("input").button();
	});
</script>
<label for="searchMaterials"></label>
</form>
</div>
-->
<div id="categories">
<span id="categoriesTitle">Categories for training materials:</span>
<p/>
</div>
<div id="questions"></div>
<span id="questionsTitle">What symptoms does the patient have?</span>
</div>

</body>
</html>