<%@ include file="/html/newportlet2/init.jsp" %>
<%@ page import="org.json.simple.JSONObject" %>

<portlet:actionURL name="getSubCategories" var="getSubCategories"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL var="materialsURL">
	<portlet:param name="jspPage" value="/html/newportlet2/materialsList.jsp"/>
	<portlet:param name="actionName" value="materials"/>
</portlet:actionURL>

<%
String catId = (String) request.getAttribute("category_id");
%>

<html>
<head>
<script type="text/javascript">
$(document).ready(function() {
	var params = {"category_id" : '<%=catId%>'};
	var request = jQuery.getJSON('<%=getSubCategories%>', params);
	request.done(function(data) {
			var categoryDiv = document.getElementById("subCategories");
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
					.attr("action","<%=materialsURL.toString()%>")
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
			$("#subCategories").buttonset();
	});
});

</script>
</head>
<body>
<div class="subCategoriesBody">
<div id="subCategories">
<span id="categoriesTitle">Mental health</span>
<p/>
</div>
</div>
</body>
</html>