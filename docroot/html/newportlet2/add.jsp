<%@ include file="/html/newportlet2/init.jsp" %>

<portlet:resourceURL id="getTopCategories" var="getTopCategories">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:resourceURL>
<portlet:resourceURL id="sendForm" var="sendFormURL">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
	<portlet:param name="jspPage" value="/html/newportlet2/trainingFront.jsp"></portlet:param>
</portlet:resourceURL>

<html>
<head>
<script type="text/javascript">
$(document).ready(function() {
	
	var url = '<%=getTopCategories%>';
	$("#statusgif").show();
	
	var request = jQuery.getJSON(url);
	request.done(function(data){
		$("#statusgif").hide();
		console.log(data);
		if (data.error != null) {
			createDialog("notification","#error-message","ui-icon ui-icon-alert","Fetching categories failed", data.details);
		}
		else {
			var dropdown = document.getElementsByName("topCategories");
			catIDs = "";
			for (var i in data) {							// Add categories to dropdown menu
				var categoryOpt = $("<option>");
				categoryOpt
					.text(data[i].name)
					.addClass("category")
					.appendTo(dropdown);
				catIDs += data[i].id + ",";
			}
			dropdown.attr("catIDs", catIDs);
		}
	});
});

$(document).on("click",".category",function() {
	
	var catMenu = document.getElementByName("topCategories");
	var catIDs = catMenu.getAttribute("catIDs").split(",");
	var category_id = catIDs[catMenu.selectedIndex];

});
</script>
</head>
<body>
<div class="trainingBody">
<div class="contentBody">
<span class="title">Add new materials</span><p/>
<form name="addForm" method="POST" action='<%=sendFormURL.toString()%>'>
<div id="formDiv">
Pick category:
<p/>
<select name="topCategories">
<option></option>
</select>
<p/>
<span id="saveBtn">Save</span>
</div>
</form>
</div>
</div>
</body>
</html>