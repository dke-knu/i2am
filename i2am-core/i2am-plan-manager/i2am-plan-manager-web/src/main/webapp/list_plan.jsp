<%@ page language="java" contentType="text/html; charset=EUC-KR"
	pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">

<link rel="stylesheet" type="text/css" href="css/list.css">

<title>Plan Manager - Plan</title>

<script>
$(document).on("click", ".newbutton", function(){

	window.open("./create-plan.jsp", "_self");	
	
});
</script>

<script>
function loadTblPlan() {
	
	var list = null;
	var tbl = $('#myTable').children('tbody');
	tbl.html("");
	
	$.ajax({
		type : 'post',
		url : './ajax/get-list-plan.jsp',
		data : ({}),
		async: false,
		cache: false,
		success : function(data) {
			data = data.replace(/(^\s*)|(\s*$)/gi, "");
			list = data;
		} 
	});
	
	if (list == null)	{
		alert("Plan cannot load.");
		return ;
	}
	
	var arr = JSON.parse(list);
	
	console.log(arr);
	
	if (arr.length > 0)	 {	
		while (arr.length > 0) {
			var obj = arr.pop();
			var after = obj.STATUS=="ACTIVE"?"DEACTIVE":"ACTIVE";
			tbl.html(tbl.html() +
				"<tr>" +
					"<td>" + obj.NAME + "</td>" +
					"<td>" + obj.CREATED_TIME + "</td>" +
					"<td>" +						
						"<div class='status' onclick=changeState('plan'" + ",'" + obj.NAME + "','" + after + "')>" + obj.STATUS + " <i class='fa fa-caret-down'></i>" + 
							"<div class='status-content'><a>" + after + "</a></div>" +	
						"</div>" +
					"</td>" +
					"<td><div class='controlButton monitoring'><i class='fa fa-eye'></i> VIEW</div></td>" +
					"<td><div class='controlButton edit'><i class='fa fa-edit'></i> Edit</div>" +
					"<div class='controlButton delete' onclick=remove('PLAN','" + obj.NAME  + "')><i class='fa fa-trash-o'></i> Delete</div></td>" +
					
				"</tr>"
			)				
		}
  	}	
}
loadTblPlan();
</script>

<script>
function changeState(type, name, after) {

	alert("Change Status: " + type + ", " + name + ", " + after);
		
	$.ajax({
		type : 'post',
		url : './ajax/change-status.jsp',
		data : ({
		  type: type,
		  name: name,
		  after: after
		}),
		success : function(response) {			  
			  /* console.log(response.trim());
			  if (response.trim() == "true") {				  
				  window.open("./list_plan.jsp", "_self");
			  } else {				  
				  window.location.reload();
				   
			  } */
			loadTblPlan();
		} 
	});
}
</script>

<script>
function remove(type, name) {

	alert("Remove: " + type + ", " + name);
	
	// 예외처리1: 만약 동작중이면 삭제할 수 없도록
	
	$.ajax({
		type: 'post',
		url: './ajax/destroy.jsp',
		data: ({
			type: type,
			name: name
		}), 
		success: function(response) {
/* 			console.log(response.trim());
			if(response.trim() == "true") {
				window.open("./home.jsp", "_self");
			} else {
				window.location.reload();
			} */
			loadTblPlan();			
		}
	});
}
</script>

</head>

<body>

	<div class="titleline">
		<h1 class="title">Plan</h1>
		<button class="newbutton">New Plan</button>
		<div class="clear"></div>
	</div>


	<hr>
	<br>

	<div class="tableDiv">

		<table id="myTable" class="table_plan">
			<thead>
				<tr>
					<th>Name</th>
					<th>Created time</th>
					<th>Status</th>
					<th>Monitoring</th>
					<th>Edit / Delete</th>
				</tr>
			</thead>
			<tbody>

			</tbody>

		</table>

	</div>


</body>
</html>