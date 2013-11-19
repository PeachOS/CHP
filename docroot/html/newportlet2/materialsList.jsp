<%@ include file="/html/newportlet2/init.jsp" %>

<portlet:actionURL name="getMaterialTitles" var="getMaterialTitles"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL var="goToMaterial">
	<portlet:param name="actionName" value="goToMaterial"/>
</portlet:actionURL>
<portlet:actionURL var="materialURL">
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
	var request = jQuery.getJSON('<%=getMaterialTitles%>', params);
	request.done(function(data) {
		var materialsDiv = document.getElementById("materialsDiv");
		for (var i in data) {
			console.log(data[i]);
			/* TODO: try a form here */
			var ref = $("<a>");
			ref
				.attr("href", "<%=goToMaterial%>")
				.attr("id", "material_" + i)
				.val(data[i].address)
				.appendTo(materialsDiv);
			document.getElementById("material_" + i).innerHTML=data[i].title;
			var materialId = $("<input>");
			materialId
				.attr("type","hidden")
				.attr("name", "material_id")
				.attr("value", data[i].id)
				.appendTo(ref);
			$("<p>").appendTo(materialsDiv);
		}
		$("#materialsDiv").buttonset();
	});
});
 
</script>
</head>
<body>
<div>
<div id="materialsBody">
<span id="materialsTitle">Materials</span>
<div id="materialsDiv">
</div>
</div>
</div>
</body>
</html>