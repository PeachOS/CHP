<%@ include file="/html/newportlet2/init.jsp" %>

<portlet:renderURL var="DMAurl">
        <portlet:param name="jspPage" value="/html/newportlet2/DMA.jsp"/>
</portlet:renderURL>
<html>
<body>
<div class="buttonLink" align="center"> 
<br/><a class="link" href="<%= DMAurl %>">Drug Management Application</a>
</div>
</div>

<!-- 
<div class="buttonLink2">
<aui:form action="<%= DMAurl %>">
    <input type="submit" value="DMA">
</aui:form>
</div>
 -->
 
<script type="text/javascript">
$(function() {
    $( "input[type=submit], a[class=link], button" )
      .button();
      });
</script>
</body>
</html>