<%@ include file="/html/newportlet2/init.jsp" %>

<portlet:actionURL name="getDrugCategories" var="getDrugCategories"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="getDrugs" var="getDrugs"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="getOrderSummary" var="getOrderSummary"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="getSentOrderSummary" var="getSentOrderSummary"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="getOrderItems" var="getOrderItems"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="getSentOrderItems" var="getSentOrderItems"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="sendOrder" var="sendOrder"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="updateStock" var="updateStock"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="updateOrder" var="updateOrder"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="addNewDrug" var="addNewDrug"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>
<portlet:actionURL name="updateDrug" var="updateDrug"
	windowState="<%=LiferayWindowState.EXCLUSIVE.toString()%>">
	<portlet:param name="ajaxAction" value="getData"></portlet:param>
</portlet:actionURL>

<!--  code from DMA, index.html -->
<!DOCTYPE HTML>
<html>
<head>
        <meta charset="utf-8">
    <link rel="shortcut icon" href="./favicon.ico"/>    
        <title>DMA App</title>
        
<script type="text/javascript">
//DMA code, from script.js

$(document).ready(function(){
    sideEl = 1;
	mod = 0; // mod = 0 -> Inventory; mod = 1 -> New Order; mod = 2 -> View Order; mod = 3 -> Incoming package; mod = 4 -> Add to Inventory
	submod = 0;
	//dbUrl = "http://admin:123456@127.0.0.1:8080/dmaaglarbri";
	dbUrl = "http://admin:123456@" + location.host;
	dbName = "facility_kirehe";
	clientDoc = "client_v2";
	newOrder = new Array();
	rcvOrder = new orderObj();
	
	$("#Inventory").click(function(){										//Called when "Inventory" tab is selected
		mod = 0; sideEl = 1;
		loadDrugCategories();												//Fills the sidepane with drug categories
		$("#submodules").hide();
		$("#clearBtn").hide();	
		$("#backBtn").hide();
		$(".optBtn")
			.show()
			.button( {
				label: "Incoming Package",
				icons: {
					primary: "ui-icon ui-icon-cart"
				}
			})
			.attr("id","IncPack");		
	});
	$("#Order").click(function(){											//Called when "Order" tab is selected
		$("#submodules").show();
		switch (submod){
			case 0:
				$("#newOrder").click();	
				break;
			case 1:
				$("#viewOrder").click();
				break;
		}
	});
	$("#newOrder").click(function(){										//Called when "New Order" tab is selected
		submod = 0; mod = 1; sideEl = 1;
		$("#clearBtn").show(); $("#sidecol").show(); $("#backBtn").hide();
		$(".optBtn")
			.show()
			.button( {
				label: "Order Summary",
				icons: {
					primary: "ui-icon ui-icon-circle-arrow-e"
				}
			})
			.attr("id","OrdSummary");
		loadDrugCategories();												//Fills the sidepane with drug categories
	});
	$("#viewOrder").click(function(){										//Called when "View Order" tab is selected
		submod = 1;	mod = 2; sideEl = 1;
		$(".optBtn").hide();
		$("#clearBtn").hide();
		$("#backBtn").hide();
		loadOrders('1');													// Fills the sidepane with orders that have been sent from the hospital
	});
	$("#clearBtn").click(function(){										//Called when the "Clear" button is pressed
		createDialog("confirmation","#dialog-confirm","ui-icon ui-icon-alert","This will clear all you changes. Are you sure?");	
	});
	
	$("#backBtn").click(function(){											//Called when the "Back" button is pressed
		$(this).hide();
		switch (mod){
			case 2:															//The user is placed in "New Order"
				$("#newOrder").click();
				break;
			case 3:															//The user is placed in "Incoming Package"
				$("#Inventory").click();
				break;
			case 4:															//The user is placed in "Add to Inventory"
				$("#commitAddToInv").attr("id","IncPack");
				$("#IncPack").click();
				break;
		}
	});
	
	$("#Inventory").click();
});

$(document).on("click","#IncPack",function(){								//Called when the "Incoming Package" button is pressed
	mod = 3; sideEl = 1;
	$("#clearBtn").show(); $("#backBtn").show();
	$(this)
		.button( {
			label: "Add to Inventory",
			icons: {
				primary: "ui-icon ui-icon-circle-arrow-s"
			}
		})
		.attr("id","addToInvSummary");
	loadOrders("2");														// Fills the sidepane with orders that have been received by the hospital
});

$(document).on("click","#addToInvSummary",function(){						//Called when the "Add to Inventory" button is pressed
	
	if (rcvOrder.drugsInfo.length > 0){
		mod = 4;		
		$("#clearBtn").hide();
		$(this)
			.button( {
				label: "Commit Changes",
				icons: {
					primary: "ui-icon ui-icon-circle-check"
				}
			})
			.attr("id","commitAddToInv");	
		showNewInvSummary();												// Shows summary of changes made by user after receiving an order - changes that will be made to the database
	}else
		createDialog("notification","#notification-message","ui-icon ui-icon-info","Please select one or more drugs to Add to Inventory");	
});

$(document).on("click","#commitAddToInv",function(){						//Called when the "Commit Changes" button is pressed
	addToInventory();
});

$(document).on("click","#OrdSummary",function(){							//Called when the "Order Summary" button is pressed
	if (newOrder.length){													//If user has chosen any drugs to order
		mod = 2;
		$("#clearBtn").hide();
		$(this)
			.button( {
				label: "Send",
				icons: {
					primary: "ui-icon ui-icon-circle-check"
				}
			})
			.attr("id","SendOrder");						
		showOrderSummary();													//Show summary of drugs that will be ordered
	}else {
		console.log("nothing chosen");
		createDialog("notification","#notification-message","ui-icon ui-icon-info","Please choose at least one drug to order");
	}
});

$(document).on("click","#SendOrder",function(){								//Called when the "Send" button is pressed
	sendOrder();
});

// Not used anymore, replaced by UpdateStock button
/*
$(document).on("click",".AddReduce", function(){							//Called when either the "+" or "-" sign is pressed on "Inventory"
	var drugIndex = parseInt($(this).attr("id").substr($(this).attr("id").indexOf("_")+1));
	var sG = $(this).attr("id").search("l");
	var curStock = getDrVal("#stock",drugIndex);
	if (!(curStock == 0 && sG == -1)){
		updateVal("current",drugIndex,parseInt(curStock) + sG + '');
	}
});
*/

$(document).on("click",".sideBtn",function(){								//Called when any of the side bar elements is pressed
	sideEl = parseInt($(this).attr("id").substr($(this).attr("id").indexOf("_")+1)); // index of sidepane button, starting from 1
	console.log("mod: " + mod);
	switch(mod){
	case 0:																	//If in Inventory mode, fills main area with drugs of that category
		var cat_id = $("#side_"+ sideEl).attr("cat_id");
		showDrugs(cat_id);
		break;
	case 1:																	//If in New Order mode, fills main area with drugs of selected category and adds SendOrder button
		var cat_id = $("#side_"+ sideEl).attr("cat_id");
		showDrugs(cat_id);
		
		$("#SendOrder")
		.show()
			.button( {
				label: "Order Summary",
				icons: {
					primary: "ui-icon ui-icon-circle-arrow-e"
				}
			})
			.attr("id","OrdSummary");
		
		break;
	case 2:																	//If in View Order mode, shows items in that order
		sideEl += 1;
		showOrderItems(sideEl);
		break;
	case 3:																	// If in Incoming Package mode, shows items in that order
		sideEl += 1;
		console.log($(this).attr("orderid") + ", " + rcvOrder.orderid);
		if ($(this).attr("orderid") != rcvOrder.orderid) {					// If switching orders, then clear changes made by user to the previous order. If coming back to the same order from Back button, then changes will not be cleared.
			rcvOrder = new orderObj();
		}
		showOrderItems(sideEl);
		break;
	}
});

$(document).on("click","#sideAddBtn", function() {							// When the ADD NEW DRUG button is clicked
	createDrugForm("add");	
});

$(document).on("click",".EditDrug", function() {							// When the "Edit drug info" button is clicked
	createDrugForm("edit", $(this).attr("drugid"));
});

$(document).on("click","#addDrugBtn", function() {							// When the "Add to database" button is clicked in the new drug adding form
	sendDrugUpdate('<%=addNewDrug%>', $(this));
});

$(document).on("click","#updateDrugBtn", function() {						// When the "Update info in database" button is clicked in the drug editing form
	sendDrugUpdate('<%=updateDrug%>', $(this));
});

$(document).on("click",".newOrdBtn",function(){							//Called when a checkbox for a drug in "New Order" is marked
	
	var button = $(this);
	var drugIndex = parseInt(button.attr("for").substr(button.attr("id").indexOf("_")+1));	//Index of the checked drug in the list
	var amount = parseInt($("#amount_" + drugIndex).val());									//Amount of drugs ordered by user (Request Qty)
	
	if (isNaN(amount)) {												// No amount specified when "order" button is clicked, gives error message
		createDialog("notification","#notification-message","ui-icon ui-icon-info","Please insert the amount you would like to order");
		$("#amount_" + drugIndex).val(0);
		amount = 0;
	}

	var id = $("#check_" + drugIndex + drugIndex).attr("drugid");	// id of drug in database
	var orderDrug = new drugObj(id, amount);
	
	if (button.attr("added-to-list") == "true") {					//If button was deactivated, remove from list
		console.log("removing");
		newOrder.splice(getDrugIndex(id,newOrder),1);
		button.attr("added-to-list", false);
		}
	else {
		console.log("adding");										//If button was activated, add to list of drugs to order
		newOrder.push(orderDrug);
		button.attr("added-to-list", true);
	}
});

$(document).on("click",".IncPackChkbox",function(){							//Called when a checkbox for a drug in "Incoming Package" is marked

	var checkbox = $(this);
	var drugIndex = parseInt(checkbox.attr("id").substr(checkbox.attr("id").indexOf("_")+1)); // Index of drug in drugs list	
	var orderDrug = new drugObj(checkbox.attr("drugid"), parseInt($("#amount_" + drugIndex).val()));
	
if (checkbox.attr("added-to-list") == "true") {						//If button was deactivated, remove from list						
		console.log("removing");
		rcvOrder.drugsInfo.splice(getDrugIndex(orderDrug.drugid,rcvOrder.drugsInfo),1);
		checkbox.attr("added-to-list", false);
		}
	else {															//If button was activated, add to list of drugs to order
		console.log("adding");
		rcvOrder.drugsInfo.push(orderDrug);
		checkbox.attr("added-to-list", true);
	}
});

$(document).on("click",".UpdateStock", function() {					// Clicking "Update stock"
	
	$("#statusgif").show();
	
	var drugIndex = ($(this).attr("id").substr($(this).attr("id").indexOf("_")+1));	// Index of drug in drugs list
	var add = parseInt($("#add_" + drugIndex).val());								// Added amount
	var reduce = parseInt($("#reduce_" + drugIndex).val());							// Removed amount
	var diff = ( isNaN(add) ? 0 : add) - (isNaN(reduce) ? 0 : reduce);
	var updateInfo = {};
	updateInfo[$(this).attr("drugid")] = diff;
	updateInfo["facility_id"] = "1";
	
	var sendUpdate = $.ajax({url: '<%=updateStock%>', data: updateInfo});
	sendUpdate.done(function (msg){
		console.log("update sent, success");
		createDialog("notification","#success-message","ui-icon ui-icon-circle-check","Your update has been successfully sent!");
		$("#side_" + sideEl).click();
	});
	sendUpdate.fail(function(error2){
		console.log("order failure");
		console.log(error2);
		createDialog("notification","#error-message","ui-icon ui-icon-circle-check","An error occured while sending the update. Please try again");
	});	
	$("#statusgif").hide();
});

$(document).on("change",".reqInput",function(){					// Request Qty in New Order is changed
	
	var drugid = $(this).attr("drugid");
	var drugIndex = getDrugIndex(drugid, newOrder);				
	if (drugIndex >= 0) {										// If is already set to be ordered, update amount in order
		newOrder[drugIndex].amount = $(this).val();
	}
});


/* <--------- Function calls ---------> */
 
 function sendDrugUpdate(url, button) {							// Send update to database, if new drug is added or existing drug edited
		var catMenu = document.getElementById("catIn");
		var catIDs = catMenu.getAttribute("catIDs").split(",");
		
		var id = button.attr("drugid");
		console.log("id: " + id);
		var med_name = $("#drugNameIn").val();
		var common_name = $("#commonNameIn").val();
		var category_id = catIDs[catMenu.selectedIndex];
		var unit = $("#unIssueIn").val();
		var unit_details = $("#drugFormIn").val();
		var unit_price = $("#priceIn").val();
		var msdcode = $("#msdCodeIn").val();
																// Validation
		if (!msdcode) {
			createDialog("notification","#error-message","ui-icon ui-icon-circle-check", "Please insert MSD code");
			return;
		}
		
		if (!med_name) {
			createDialog("notification","#error-message","ui-icon ui-icon-circle-check", "Please insert drug name");
			return;
		}
		
		if (!unit_price) {
			createDialog("notification","#error-message", "ui-icon ui-icon-circle-check", "Please insert price");
			return;
		}
		
		if (!isFloat(unit_price) && unit_price != "") {
			createDialog("notification","#error-message","ui-icon ui-icon-circle-check","Price has to be a number");
			return;
		}
		
		if (!isInt(msdcode)) {
			createDialog("notification","#error-message","ui-icon ui-icon-circle-check","MSD code has to be numerical");
			return;
		}
															// Parameters for DB request
		var parameters = {};
		parameters["id"] = id;
		parameters["med_name"] = med_name;
		parameters["common_name"] = common_name;
		parameters["category_id"] = category_id;
		parameters["unit"] = unit;
		parameters["unit_details"] = unit_details;
		parameters["unit_price"] = parseInt(unit_price);
		parameters["msdcode"] = parseInt(msdcode);
		
		console.log(parameters);
		
		$("#statusgif").show();
		var sendData = $.ajax({url: url, data: parameters});
		sendData.done(function (msg){
			console.log("data sent, success");
			createDialog("notification","#success-message","ui-icon ui-icon-circle-check","Your update has been successfully sent!");
			$("#side_0").click();
		});
		sendData.fail(function(error2){
			console.log("failure");
			console.log(error2);
			createDialog("notification","#error-message","ui-icon ui-icon-circle-check","An error occured while sending the update. Please try again");
			$("#side_0").click();
		});	
		$("#statusgif").hide();
}

function createDrugForm(mode, drugid) {								// Creates a form for adding or editing drugs
	
	var tbody = $("#inner-content");
	tbody.html("");
	var cell = $("<div>");
	cell
		.addClass("col1")
		.appendTo(tbody);
	
	var title = $("<p>");
	title
		.attr("id","title")
		.addClass("Title")
		.appendTo(cell);
	
	createInputField("Drug name: ", "drugNameIn", "text", cell);
	createInputField("Common name: ", "commonNameIn", "text", cell);
	createInputField("MSD code: ", "msdCodeIn", "text", cell);
	createInputField("Category: ", "catIn", "category", cell);
	createInputField("Unit of issue: ", "unIssueIn", "text", cell);
	createInputField("Drug form: ", "drugFormIn", "text", cell);
	createInputField("Price: ", "priceIn", "text", cell);
	
	var sendButton = $("<button>");	
	sendButton.appendTo(cell);

	if (mode == "add") {
		title.text("Add new drug");
		sendButton
			.attr("id", "addDrugBtn")
			.text("Add to database");
	}
	else if (mode == "edit") {
		
		title.text("Edit drug info");
		sendButton
			.attr("id","updateDrugBtn")
			.attr("drugid", drugid)
			.text("Update info in database");
		
		var parameters = {"drug_id": drugid, "facility_id": 1};
		var request = $.getJSON('<%=getDrugs%>', parameters);			// Fetch info of the drug being edited from DB
		request.done(function(data) {
			
			$("#drugNameIn").val(data[0].med_name);
			$("#commonNameIn").val(data[0].common_name);
			
			var catMenu = document.getElementById("catIn");
			var catIDs = catMenu.getAttribute("catIDs").split(",");
			for (var i in catIDs) {
				if (catIDs[i] == data[0].category_id) {
					catMenu.selectedIndex = i;
				}
			}
			
			$("#unIssueIn").val(data[0].unit_details);
			$("#drugFormIn").val(data[0].unit);
			$("#priceIn").val(data[0].unit_price);
			$("#msdCodeIn").val(data[0].msdcode);
		});
	}
};

function createInputField(labelText, id, type, cell) {			// Creates an input field for drug adding/editing form
	var row = $("<tr>");
	var left = $("<td>");
	var right = $("<td>");
	
	var label = $("<p>");
	label
		.text(labelText)
		.addClass("label")
		.appendTo(left);
	
	if (type == "category") {
		addCategoryDropdown(right, id);
	}
	
	else {
		var drugNameInput = $("<input>");
		drugNameInput
			.attr("type",type)
			.attr("id",id)
			.appendTo(right);
	}
	
	left.appendTo(row);
	right.appendTo(row);
	row.appendTo(cell);
}
 
 function addCategoryDropdown(cell, id) {					// Creates a dropdown menu in drug adding/editing form for categories
	 
	var url = '<%=getDrugCategories%>';
		$("#statusgif").show();
		
		var request = jQuery.getJSON(url);
		request.done(function(data){
			$("#statusgif").hide();
			var dropdown = $("<select>");
			dropdown
				.attr("id",id)
				.appendTo(cell);
			
			catIDs = "";
			for (var i in data) {							// Add categories to dropdown menu
				var categoryOpt = $("<option>");
				categoryOpt
					.text(data[i].category_name)
					.appendTo(dropdown);
				catIDs += data[i].category_id + ",";
			}
			dropdown.attr("catIDs", catIDs);
		});
		request.fail(function(jqXHR, textStatus) {
	 		createDialog("notification","#error-message","ui-icon ui-icon-alert","Connection to the DMA server is unavailable. Please try again later");
			$("#statusgif").attr("src","/css/custom-theme/images/important.gif");
		});	
}
 
 function loadDrugCategories () {							//Retrieves from DMA server the category names and displays them in side bar
	
	$("#statusgif").show();	
	var url = '<%=getDrugCategories%>';
	var request = jQuery.getJSON(url);
	request.done(function(data){
		$("#statusgif").hide();
		drawSideCol("#sidecol",data,"drugCategories");
		$("#side_0").click();								// Select first retrieved category
	});
	request.fail(function(jqXHR, textStatus) {
 		createDialog("notification","#error-message","ui-icon ui-icon-alert","Connection to the DMA server is unavailable. Please try again later");
		$("#statusgif").attr("src","/css/custom-theme/images/important.gif");
	});
}

function showDrugs(category){ 								//Retrieves drug information from DB and displays it to the user
	
	$("#statusgif").show();
	var url = '<%=getDrugs%>';
	
	var catJSON = {};										// Parameters for request
	
	if (category != null) {
		catJSON["category_id"] = category;
		catJSON["facility_id"] = 1;
	}
	var request = $.getJSON(url, catJSON);	
	request.done(function(data){
		$("#statusgif").hide();
		putTable("drugs_table",data.length,3);				// Create table in main section and add retrieved data to it
		for (var i in data){
			var drugName = data[i].med_name;
			if (data[i].common_name.length != 0) {
				drugName += (" (" + data[i].common_name + ")"); 
			}
			
			$("#drugName_" + i).text(drugName);
			$("#update_" + i).attr("drugid",data[i].id);		//Add ID of drug to certain action buttons/fields
			$("#edit_" + i).attr("drugid",data[i].id);
			$("#check_" + i + i).attr("drugid",data[i].id);
			$("#amount_" + i).attr("drugid",data[i].id);
			putDrVal("#unIssue",i,data[i].unit);
			putDrVal("#drugForm",i,data[i].unit_details);
			putDrVal("#stock",i,data[i].unit_number);
			putDrVal("#price",i,data[i].unit_price);
						
			var drugIndex = getDrugIndex(data[i].id,newOrder);	// If opening page from Back button, select drugs that have been added to order list, so that user wouldn't have to perform all actions again
			if (drugIndex >= 0){
				$("#check_" + i + i).attr("aria-pressed",true); 
				$("#check_" + i + i).addClass("ui-state-active"); 
				$("#check_" + i).attr("checked",true); 
				$("#check_" + i + i).attr("added-to-list",true);
				$("#amount_" + i).val(newOrder[drugIndex].amount);
			}
			else												// If drug is not in order list, add suggested order amount as request qty
				$("#amount_" + i).attr("value",data[i].suggested);
		}
		$(".col3").buttonset();									// Make buttons look pretty with jquery-ui		
	});
}

function loadOrders(status){								//Retrieves from DMA server the orders and displays them in side bar
	
	$("#statusgif").show();
	
	var params = {"facility_id": "1", "summarize": "false"};
	if (status != null) {
		params["order_status"] = status;
	}
	
	var request = $.getJSON('<%=getOrderSummary%>', params);
	request.done(function(data){
		if (data.length){
			var orderInfo = new Array(data.length);
			for (var i in data)
				orderInfo[i] = new ordSummaryObj(data[i].order_id,data[i].order_timestamp,
						data[i].drugs.length,data[i].order_status);
				if (rcvOrder.orderid == data[i].order_id) {	// Save index if this is the order the user is currently working on
					rcvOrder.orderindex = i;
				}
			$("#statusgif").hide();
			drawSideCol("#sidecol",orderInfo,"Orders");		// Create side pane with queried order items
			if (rcvOrder.orderindex >= 0) {					// If opening this view from Back button in Incoming package mode, then go back to the order which was selected
				$("#side_" + rcvOrder.orderindex).click();	
			}
			else {
				$("#side_0").click();						// If opening this view fresh, open first order
			}
		}else{
			createDialog("notification","#notification-message","ui-icon ui-icon-info","There are no orders to show");	
			$("#Inventory").click();
		}
	});
	request.fail(function(msg){
		console.log("getOrderSummary request failed");
		console.log(msg);
	})
}

function showOrderItems(sideEl){							//Displays all of the items contained in the selected order
	
	$("#statusgif").show();
	
	var parameters = {};										
	parameters["order_id"] = $("#side_" + parseInt(sideEl-1)).attr("orderid");
	parameters["summarize"] = "false";
	parameters["facility_id"] = "1";
	
	var request = $.getJSON('<%=getOrderSummary%>', parameters);
	request.done(function(data){
		$("#statusgif").hide();
		putTable("order",data[0].drugs.length,3);			// Create table structure for order items
		console.log(data[0]);
		for (var i in data[0].drugs){						// Add data from request to table
			console.log("unit number: " + data[0].drugs[i].unit_number);
			$("#drugName_" + i).text(data[0].drugs[i].med_name);
			putDrVal("#unIssue",i,data[0].drugs[i].unit);
			putDrVal("#drugForm",i,data[0].drugs[i].unit_details);
			putDrVal("#price",i,data[0].drugs[i].unit_price);
			putDrVal("#reqLbl",i,data[0].drugs[i].unit_number);
			putDrVal("#sentQty",i,data[0].drugs[i].unit_number);
			$("#amount_" + i).val(data[0].drugs[i].unit_number);
			$("#check_" + i).attr("drugid", data[0].drugs[i].id);
			if (mod == 3) {									// Incoming package
				rcvOrder.orderid = data[0].order_id;		// Mark ID of order that the user is working with
				var rcvIndex = getDrugIndex(data[0].drugs[i].id,rcvOrder.drugsInfo);	
				if (rcvIndex >= 0) {						// If a drug was already marked to be added to inventory by user, make the mark again
					$("#amount_" + i).val(rcvOrder.drugsInfo[rcvIndex].amount);
					rcvOrder.drugsInfo.splice(rcvIndex,1);	// First remove from list
					$("#check_" +i).click();				// And then click button again, which will automatically add the drug to list again
				}
			}
				
		}
	});
	request.fail(function(msg) {
		console.log("request failed");
		console.log(msg);
	});
}

function showOrderSummary() {								//Displays the user with the order summary
	$("#statusgif").show();
	putTable("order",newOrder.length,3);					// Create table structure
	
	for (var i in newOrder) {
															// Request information about drugs in the order
		var parameters = {"drug_id": newOrder[i].drugid, "index": i, "facility_id": 1};
		var request = $.getJSON('<%=getDrugs%>', parameters);
		request.done(function(data){
			$("#statusgif").hide();
			for (var j in data) {
				var drugName = data[j].med_name;			// Fill table with fetched information
				if (data[j].common_name.length != 0) {
					drugName += ("(" + data[j].common_name + ")"); 
				}
				$("#drugName_" + data[j].index).text(drugName);
				putDrVal("#unIssue",data[j].index,data[j].unit);
				putDrVal("#drugForm",data[j].index,data[j].unit_details);
				putDrVal("#price",data[j].index,data[j].unit_price);
				putDrVal("#reqLbl",data[j].index,newOrder[parseInt(data[j].index)].amount);
				break;
			}
		});
	}
	mod = 1;
}

function putDrVal(id,index,newVal){					//displays the new value entered by the user
	var field = $(id + "_" + index);
	var curVal = getDrVal(id,index);
	field.text(field.text().replace(curVal,newVal));
}

function putTable (id, rows, columns){			//creats object tableObj and calls method to draw the table 
	table = new tableObj(id,rows,columns);
	table.drawText("#inner-content");
}

function drawTextTable(tbody){
	$(tbody).html("");
	for (var r = 0; r < this.rows; r++) {
		var trowMain = $("<div>");
		trowMain.addClass("trow")
			.addClass("mainrow")
			.appendTo(tbody);
		var drugName = $("<p>");
		drugName
			.attr("id","drugName_" + r)
			.addClass("Drug_name")
			.text("[Drug name]")
			.appendTo(trowMain);
		var trow = $("<div>");
			trow.addClass("trow")
			.appendTo(trowMain);
		var cell = $("<div>");
		cell
			.addClass("col1")
			.appendTo(trow);
		
		var unIssue = $("<p>");
		unIssue
			.attr("id","unIssue_" + r)
			.text("Unit of Issue: []")
			.appendTo(cell);
		
		var drugForm = $("<p>");
		drugForm
			.attr("id","drugForm_" + r)
			.text("Drug Form: []")
			.appendTo(cell);
			
		var price = $("<p>");
		price
			.attr("id","price_" + r)
			.text("Price: []")
			.appendTo(cell);
			
		var cell2 = $("<div>");
		cell2
			.addClass("col2")
			.appendTo(trow);
		
		var currentStock = $("<div>"); 
		var addCell = $("<div>");
		var reduceCell = $("<div>");
			
		var stockLeft = $("<td>");
		var stockRight = $("<td>");
		
		var stock = $("<p>");
		stock
			.attr("id","stock_" + r)
			.addClass("stock")
			.text("Stock: []")
			.appendTo(currentStock);
		
		var addLeft = $("<td>");
		var addMiddle = $("<td>");
		var addRight = $("<td>");
		
		var addInput = $("<input>");
		addInput
			.attr("id","add_" + r)
			.addClass("AddReduceInput")
			.attr("type","number")
			.attr("min","0")
			.appendTo(addMiddle);
		
		var addLblLeft = $("<p>");
		addLblLeft.text("Add ")
		.attr("id","addLblLeft_" + r)
		.addClass("label")
		.appendTo(addLeft);
		
		var addLblRight = $("<p>");
		addLblRight.text("items to stock")
		.attr("id","addLblLeft_" + r)
		.addClass("label")
		.appendTo(addRight);
		
		addLeft.appendTo(addCell);
		addMiddle.appendTo(addCell);
		addRight.appendTo(addCell);
		
		var reduceLeft = $("<td>");
		var reduceMiddle = $("<td>");
		var reduceRight = $("<td>");
		
		var reduceInput = $("<input>");
		reduceInput
			.attr("id","reduce_" + r)
			.addClass("AddReduceInput")
			.attr("type","number")
			.attr("min","0")
			.appendTo(reduceMiddle);
		
		var reduceLblLeft = $("<p>");
		reduceLblLeft.text("Remove ")
		.attr("id","reduceLblLeft_" + r)
		.addClass("label")
		.appendTo(reduceLeft);
		
		var reduceLblRight = $("<p>");
		reduceLblRight.text("items from stock")
		.attr("id","reduceLblLeft_" + r)
		.addClass("label")
		.appendTo(reduceRight);
		
		reduceLeft.appendTo(reduceCell);
		reduceMiddle.appendTo(reduceCell);
		reduceRight.appendTo(reduceCell);
			
		var cell3 = $("<div>");
		cell3
			.addClass("col3")
			.appendTo(trow);
		
		var updateBtn = $("<button>");
		updateBtn
			.attr("id", "update_" + r)
			.addClass("UpdateStock")
			.text("Update stock");
		
		var editBtn = $("<button>");
		editBtn
			.attr("id", "edit_" + r)
			.addClass("EditDrug")
			.text("Edit drug info");

		switch(mod){
		case 0: //Inventory
			currentStock.appendTo(cell2);
			addCell.appendTo(cell2);
			reduceCell.appendTo(cell2);
			var updateDiv = $("<div>");
			var editDiv = $("<div>");
			updateBtn.appendTo(updateDiv);
			editBtn.appendTo(editDiv);
			updateDiv.appendTo(cell3);
			editDiv.appendTo(cell3);
			break;
		case 1: //New Order
			stockLeft.appendTo(cell2);
			stockRight.appendTo(cell2);
			var subrow_left = $("<td>");
			var subrow_right = $("<td>");
			subrow_left.appendTo(cell2);
			subrow_right.appendTo(cell2);	
			$("<p>").text("Request Qty: ")
			.attr("id","reqLbl_" + r)
			.addClass("ReqQLbl")
			.appendTo(subrow_left);
			var reqQuantity = $("<input>");
			reqQuantity
			.attr("id","amount_" + r)
			.addClass("reqInput")
			.attr("type","number")
			.attr("min","0");
			reqQuantity.appendTo(subrow_right);
			var checkbox = $("<input>");
			checkbox
				.attr("type","checkbox")
				.attr("id","check_" + r)
				.addClass("newOrdChkbox")
				.appendTo(cell3);
			var label = $("<label>");
			label
				.attr("for","check_" + r)
				.attr("id","check_"+ r + r)
				.addClass("newOrdBtn")
				.text("order")
				.appendTo(cell3);
			checkbox
				.button( {
					icons: {
						primary: "ui-icon-check"
					}
				});

			break;
		case 2: // View order
			$("<p>").text("Requested Qty: []")
			.attr("id","reqLbl_" + r)
			.addClass("reqQlabel")
			.appendTo(cell2);
			break;	
		case 3: // Incoming package
			$("<p>").text("Sent Qty: []")
				.addClass("SentQLbl")
				.attr("id","sentQty_" + r)
				.appendTo(cell2);				
			var subrow_left = $("<td>");
			var subrow_right = $("<td>");
			subrow_left.appendTo(cell2);
			subrow_right.appendTo(cell2);
			$("<p>").text("Received Qty: ")
				.addClass("RcvQLbl")
				.appendTo(subrow_left);
			var RcvQty = $("<input>");
			RcvQty
				.attr("id","amount_" + r)
				.addClass("rcvInput")
				.attr("type","number")
				.attr("min","0")
			.appendTo(subrow_right);		
			var checkbox = $("<input>");
			checkbox
				.attr("type","checkbox")
				.attr("id","check_" + r)
				.addClass("IncPackChkbox")
				.appendTo(cell3);
			var label = $("<label>");
			label
				.attr("for","check_" + r)
				.attr("id","check_"+ r + r)
				.addClass("incOrdBtn")
				.appendTo(cell3);
			checkbox
				.button( {
					icons: {
						primary: "ui-icon-check"
					},
					text: false
				});

			break;
		case 4:	//Add to inventory		
			$("<p>").text("Received Qty: []")
				.addClass("RcvQLbl")
				.attr("id","rcvQty_" + r)
				.appendTo(cell2);
			var newStock = $("<p>");
			newStock
				.text("New Stock: []")
				.attr("id","newStock_" + r)
				.addClass("NewStock")
				.appendTo(cell2);
			break;	
		}
	}
}

function showNewInvSummary(){												//Displays a summary of the drugs that will be added to Inventory
	$("#statusgif").show();
	
	putTable("InvSummary",rcvOrder.drugsInfo.length,3);						// Create table structure
	for (var i in rcvOrder.drugsInfo){										// Fetch info about each drug
		var parameters = {"drug_id": rcvOrder.drugsInfo[i].drugid, "index": i, "facility_id": 1};
		var request = $.getJSON('<%=getDrugs%>', parameters);
		request.done(function(data){
			for (var j in data){											// Add fetched info to table
				var drugName = data[j].med_name;
				if (data[j].common_name.length != 0) {
					drugName += ("(" + data[j].common_name + ")"); 
				}
				$("#drugName_" + data[j].index).text(drugName);
				putDrVal("#unIssue",data[j].index,data[j].unit);
				putDrVal("#drugForm",data[j].index,data[j].unit_details);
				putDrVal("#price",data[j].index,data[j].unit_price);
				putDrVal("#rcvQty",data[j].index,rcvOrder.drugsInfo[data[j].index].amount);
				putDrVal("#newStock",data[j].index,parseInt(getDrVal("#rcvQty",data[j].index))+parseInt(data[j].unit_number));
				break;
			}
		});
	}
	$("#statusgif").hide();
}



function sendOrder(){														//Sends new order information to the DMA server
	$("#statusgif").show();
	var parameters = {"facility_id": "1", "status": "1"};
	for (var i in newOrder) {
		parameters[newOrder[i].drugid] = newOrder[i].amount;				// For each id amount of the drug ordered
	}
	var sendNewOrder = $.ajax({url: '<%=sendOrder%>', data: parameters});
	sendNewOrder.done(function (msg){
		$("#statusgif").hide();
		console.log("order sent, success");
		createDialog("notification","#success-message","ui-icon ui-icon-circle-check","Your order has been successfully sent!");
		newOrder = new Array();
		$("#newOrder").click();					
	});
	sendNewOrder.fail(function(error2){
		console.log("order failure");
		createDialog("notification","#error-message","ui-icon ui-icon-circle-check","An error occured while sending the order. Please try again");
		$("#newOrder").click();
	});
}

function addToInventory(){													//Updates all inventory values which were changed at the "Incoming package" screen
	$("#statusgif").show();
	for (var i in rcvOrder.drugsInfo){
		var parameters = {};
		parameters["facility_id"] = "1";
		parameters[rcvOrder.drugsInfo[i].drugid] = rcvOrder.drugsInfo[i].amount;
		var request = $.ajax({url: '<%=updateStock%>', data: parameters});
		request.done(function(msg) {
			$("#statusgif").hide();
			sendReport();
			changeOrderStatus("delivered");
			createDialog("notification","#success-message","ui-icon ui-icon-circle-check","Your changes have been updated and a report has been sent");	
			$("#Inventory").click();
		});
		request.fail(function(error) {
			createDialog("notification","#error-message","ui-icon ui-icon-alert","Something went wrong and your changes were not updated. Please try again");
			$("#Inventory").click();
			console.log("Adding to inventory failed");
		});
	}
}

/* TODO I think this should be replaced by just general logging somehow 
function sendReport(){														//Sends a report of the received drugs
	var dbViewUri = "_design/" + clientDoc + "/_view/orders";
	var getOrders = $.getJSON(dbUrl + "/" + dbName + "/" + dbViewUri);	
	getOrders.done(function(data){
		var orders = data.rows;
		dbViewUri = "_uuids";
		var getNewId = $.getJSON(dbUrl + "/" + dbViewUri);	
		getNewId.done(function(_uuids){
			var newId = _uuids.uuids[0];
			var report = orders[getOrderIndex(rcvOrder.orderid,orders)].value;
			var drugIndex;
			for (var i in report.items){
				drugIndex = getDrugIndex(report.items[i]._id,rcvOrder.drugsInfo);
				if (drugIndex>=0)
					report.items[i].amount = rcvOrder.drugsInfo[drugIndex].amount;
				else
					report.items[i].amount = 0;	
			}
			report.created_at = getDateFormat();
			report.status = "delivered";
			report.type = "report";
			var reportText = JSON.stringify(report);
			var putReport = $.ajax({
				url: dbUrl + "/" + dbName + "/" + newId,
				type: "PUT",
				processData: false,
				data: reportText,
				contentType: "application/json"
			});		
			putReport.fail(function (msg){
				//createDialog("notification","#error-message","ui-icon ui-icon-alert","The report was not delivered to the DMA server. Contact your administrator");
			});			
		});
	});
}*/

/* TODO this doesn't actually work, there is no backend to respond to that */
function changeOrderStatus(newStatus){										//Updates the status of an order
	
	var parameters = {"id": rcvOrder.orderid, "newStatus": newStatus};
	var changeStatus = $.ajax('<%=updateOrder%>', parameters);
	changeStatus.done(function(msg) {
		console.log("changeStatus done");
	});
	changeStatus.fail(function (msg){
		alert("Fail CoS:(\n" + msg);
	});
} 

function getDrVal(id,index){						//returns the value stored in HTML and which corresponds to the user's value
	var field = $(id + "_" + index);
	return field.text().substr(field.text().indexOf(":")+2);
}

function getDrugIndex(_id,array){						//returns the index where the drug_id is located within the Order array
	for (var i in array){
		if (array[i].drugid == _id)
			return i;
	}
	return -1;
}

function getOrderIndex (orderid, array){
	for (var i in array){
		if (array[i].id == orderid)
			return i;
	}
	return -1;
}

function orderItemObj (_id,name,msdcode,unitofissue,drugform,price,amount){		//an Order object
	this.drugid = _id;
	this.name = name;
	this.msdcode = msdcode;
	this.unitofissue = unitofissue;
	this.drugform = drugform;
	this.price = price;
	this.amount = amount;
	this.mergeFields = mergeDrugFields;
}

function mergeDrugFields() {						//merges the fields into a Json-compliant format
	return "{" + this.drugid + "," + this.name + "," + this.msdcode + "," + this.unitofissue + "," + this.drugform + "," + this.price + "," + this.amount + "}"; 
}

function getDateFormat(){
	var d = new Date();
	var date = d.getDate();
	var month = d.getMonth() + 1;
	if (date < 10)
		date = "0" + date;
	if (month < 10)
		month = "0" + month;		
	return d.getFullYear() + "-" + month + "-" + date + "T" + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds()+ "." + d.getMilliseconds()+"Z";
}

function drugObj (drugid, amount) {
	this.drugid = drugid;
	this.amount = amount;
}

function orderObj (orderid, orderindex) {
	this.orderid = orderid;
	this.orderindex = orderindex;
	this.drugsInfo = new Array();
}

function ordSummaryObj(orderid,date,items,status){
	this.orderid = orderid;
	this.date = date;
	this.items = items;
	this.status = status;
}

function tableObj(id, rows, columns){ /*table Object*/
	this.id = id;
	this.rows = rows;
	this.cols = columns;	
	this.drawText = drawTextTable;
}

function createDialog(type,dialog_id,icon,message){

	$(dialog_id).html("");
	$("<span>")
		.addClass(icon)
		.attr("style","float:left; margin:0 7px 50px 0;")
		.appendTo(dialog_id);
	$("<p>")
		.text(message)
		.attr("style","padding:0;")
		.appendTo(dialog_id);
	switch (type){
		case "notification":
			$(dialog_id).dialog({
				modal: true,
				buttons: {
					Ok: function() {
						$( this ).dialog( "close" );
					}
				}
			});			
			break;
		case "confirmation":
			$(dialog_id).dialog({
				resizable: false,
				height:160,
				width: 350,
				modal: true,
				buttons: {
					"Clear": function() {
						switch (mod){
							case 1:															//The user is placed in "New Order"
								newOrder = new Array();
								$(".newOrdBtn").attr("aria-pressed",false); 
								$(".newOrdBtn").removeClass("ui-state-active"); 
								$(".newOrdChkbox").attr("checked",false);
								break;
							case 3:															//The user is placed on "Incoming Package"
								rcvOrder = new orderObj();
								$(".incOrdBtn").attr("aria-pressed",false); 
								$(".incOrdBtn").removeClass("ui-state-active"); 
								$(".IncPackChkbox").attr("checked",false);
								break;
						}
						$( this ).dialog( "close" );
					},
					Cancel: function() {
						$( this ).dialog( "close" );
					}
				}
			});
			break;
	}
}

function drawSideCol (tbody,info,type){
	$(tbody).html("");
	

	if (type == "drugCategories") {
		var button = $("<input>");
		button
			.attr("id","sideAddBtn")
			.attr("type","radio")
			.attr("name","sideBtns");
		var label = $("<label>");
		label
		.attr("for","sideAddBtn")
		.attr("id","sideAddLbl")
		.text("ADD NEW DRUG");

		button.appendTo(tbody);
		label.appendTo(tbody);
	}
	
	for (var r = 0; r < info.length; r++){		
		var button = $("<input>");
		var label = $("<label>");
		button
			.attr("id","sideBtn_" + r)
			.attr("type","radio")
			.attr("name","sideBtns");
		switch (type){
		case "drugCategories":
			label
				.attr("for","sideBtn_" + r)
				.attr("id","side_" + r)
				.attr("cat_id",info[r].category_id)
				.addClass("sideBtn")
				.text(info[r].category_name);
			break;
		case "Orders":
			label
				.attr("for","sideBtn_" + r)
				.attr("id","side_" + r)
				.attr("orderid",info[r].orderid)
				.addClass("sideBtn")
				.text("Date: " + info[r].date +  " Items: " + info[r].items + " -Status: " + info[r].status);
			break;
		}
		button.appendTo(tbody);
		label.appendTo(tbody);
	}
	$('#sidecol').buttonset();
}

function isInt(n) {
	return !isNaN(parseInt(n)) && (parseInt(n) === Number(n));
}

function isFloat(n) {
	return !isNaN(parseFloat(n));
}

</script>
     








        <script>        // DMA code from index.html

        $(function() {
                $("button").button();
                $(".dialog-messages").hide();
                $('#modules').buttonset();
                $('#submodules').buttonset();
                $("#Order")
                        .button( {
                                icons: {
                                        primary: null,
                                        secondary: "ui-icon-triangle-1-s"
                                }
                        });
                $(".optBtn")
                        .button( {
                                label: "Incoming Package",
                                icons: {
                                        primary: "ui-icon-cart"
                                }
                        });
                $("#clearBtn")
                        .button( {
                                label: "Clear",
                                icons: {
                                        primary: "ui-icon-trash"
                                }
                        });
                $("#backBtn")
                        .button( {
                                label: "Back",
                                icons: {
                                        primary: "ui-icon-circle-arrow-w"
                                }
                        });                                
        });        
</script>
</head>

<body>



        <div id="main" class="container">
        <div id="header">
                        <div class="floatWrap centerWrap">
                                <h1><a id="logo" href="">DMA</a></h1>
                                <a id="referenceUrl" href="file:///C|"></a>
                        <div id="modules">
                                <input type="radio" id="Inventory" name="radio0" checked="checked" /><label class="mods" for="Inventory">Inventory</label>
                                <input type="radio" id="Order" name="radio0"/><label class="mods" for="Order">Order</label>
                        </div>
                        <div id="options">
                                <button id= "backBtn"></button>
                                <button id= "IncPack" class="optBtn"></button>                                
                        </div>
                        <img src="<%= request.getContextPath()%>/css/images/loading-gif-animation.gif" alt="[loading...]" name="statusgif" width="24" height="20" id="statusgif">        
                        <br /><br />
                        <div id="submodules">
                                <input type="radio" id="newOrder" name="radio1" checked="checked" /><label class="submods" for="newOrder">New Order</label>
                                <input type="radio" id="viewOrder" name="radio1" /><label class="submods" for="viewOrder">View Order</label>
                        </div>
                        <div id="clear">
                                <button id="clearBtn">Clear!</button>
                        </div>                                
                        </div> <!-- / header -->
                </div>
                <div id="blueline"></div>                 
                <div id="info">
                        <div id="sidecol">
                                <!--/*content is filled in by JS/couchDB*/-->
                        </div>
                        <div id="inner-content">
                                <!--/*content is filled in by JS*/-->
                        </div>
                </div>
                <div id="footer">
                  Copyright Aglarbri 2011
                </div>
                <div class= "dialog-messages">
                        <div id="notification-message" title="Important">
                                <span class="ui-icon ui-icon-info" style="float:left; margin:0 7px 50px 0;"></span>
                                [msg]
                        </div>
                        <div id="success-message" title="Success">
                                <span class="ui-icon ui-icon-circle-check" style="float:left; margin:0 7px 50px 0;"></span>
                                [msg]
                        </div>                        
                        <div id="error-message" title="Error">
                                <span class="ui-icon ui-icon-circle-alert" style="float:left; margin:0 7px 50px 0;"></span>
                                [msg]
                        </div>
                        <div id="dialog-confirm" title="Clear?">
                                <span class="ui-icon ui-icon-help" style="float:left; margin:0 7px 20px 0;"></span>
                                This will clear all you changes. Are you sure?
                        </div>                        
                </div>
        </div>
</body>
</html>
