<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">

<link rel="stylesheet" type="text/css" href="css/list.css">

<script>
$(document).on("click", ".newbutton", function(){

	window.open("./create-source.jsp", "_self");	
	
});
</script>

<script>
function conceptDriftStatus() {

	var cds = $(".cd");

	for( var i=0; i<cds.length; i++ ) {	
		
		if ( $(cds[i]).text() == "Waiting..." ) {
			
			var temp = $(cds[i]).parent().find(".status");
					
			temp.addClass("status-disable");
			temp.removeClass("status");
			//temp.prop('onclick', null).off('click');
		}
	}	
}
</script>

<script>
function loadTblSrc() {
	
	var list = null;	
	var tbl = $(".table_src").children("tbody");
	tbl.html("");
	
	$.ajax({
		type : 'post',
		url : './ajax/get-list-src-with-intelligent.jsp',
		data : ({}),
		async: false,
		cache: false,
		success : function(data) {
			data = data.replace(/(^\s*)|(\s*$)/gi, "");
			list = data;
		} 
	});
	
	if (list == null)	{
		alert("Src cannot load.");
		return ;
	}
		
	var arr = JSON.parse(list);
	
	while (arr.length > 0) {
		
		var obj = arr.pop();
		var after = obj.STATUS=="ACTIVE"?"DEACTIVE":"ACTIVE";
		
		var recommendation = ""
		
		if(obj.IS_RECOMMENDATION == "N" ) {
			recommendation = "Not Used"
		}
		else if(obj.IS_RECOMMENDATION == "Y" && obj.RECOMMENDED_SAMPLING == null ) {
			recommendation = "Analyzing..."
		}
		else if(obj.IS_RECOMMENDATION == "Y" && obj.RECOMMENDED_SAMPLING != null ) {
			recommendation = "[" + obj.RECOMMENDED_TIME + "] " + obj.RECOMMENDED_SAMPLING.replace('_',' ')
		}
				
		var conceptdrift = ""
		
		if(obj.CONCEPT_DRIFT_STATUS == "NOT_USED") {
			conceptdrift = "Not Used"
		}
		else if(obj.CONCEPT_DRIFT_STATUS == "WAITING") {
			conceptdrift = "Waiting..."
		}
		else if(obj.CONCEPT_DRIFT_STATUS == "PREPARED") {
			conceptdrift = "Prepared"
		}
				
		tbl.html(tbl.html() +
			"<tr>" +
				"<td>" + obj.NAME + "</td>" +
				"<td>" + obj.CREATED_TIME + "</td>" +
				"<td>" + recommendation + "</td>" +
				"<td class='cd'>" + conceptdrift + "</td>" +
				//"<td>" + (obj.USES_LOAD_SHEDDING=="Y"?"Used":"Unused") + "</td>" +
				"<td>" +					 
					"<div class='status' onclick=changeState('src'" + ",'" + obj.NAME + "','" + after + "','" + obj.CONCEPT_DRIFT_STATUS +"')>" + obj.STATUS + " <i class='fa fa-caret-down'></i>" + 
						"<div class='status-content'><a>" + after + "</a></div>" +	
					"</div>" +
				"</td>" +
				"</div>" +
				"</td>" +
				"<td><div class='controlButton edit'><i class='fa fa-edit'></i> Edit</div>" +
				"<div class='controlButton delete' onclick=remove('SRC','" + obj.NAME  + "')><i class='fa fa-trash-o'></i> Delete</div></td>" +
			"</tr>"
		);				
	}	
  }
loadTblSrc();
conceptDriftStatus();
</script>

<script>
function changeState(type, name, after, conceptdrift) {

	alert("Change Status: " + type + ", " + name + ", " + after + ", " + conceptdrift);
		
	if(conceptdrift == "WAITING") {
		
		alert("컨셉드리프트 엔진이 학습중 이므로 실행할 수 없음");		
		return false;
	}
		
	$.ajax({
		type : 'post',
		url : './ajax/change-status.jsp',
		data : ({
		  type: type,
		  name: name,
		  after: after
		}),
		success : function(response) {			  
			  /* if (response.trim() == "true") {
				  window.open("./list_plan.jsp", "_self");
			  } else {				  
				  window.location.reload();			   
			  } */
			  loadTblSrc();
			  conceptDriftStatus();
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
			/* console.log(response.trim());
			if(response.trim() == "true") {
				window.open("./home.jsp", "_self");
			} else {
				window.location.reload();
			} */
			loadTblSrc();
			conceptDriftStatus();
		}
	});
}
</script>

<title>Plan Manager - Source</title>
</head>

<body>
	<div class="titleline">
		<h1 class="title"> Source </h1>
		<button class="newbutton"> New Source</button>
		<div class="clear"></div>			
	</div>		
	<hr><br>	
	
	<div class="tableDiv">
		
	<table id="myTable" class="table_src">
		<thead>
			<tr>
				<th>Name</th>
				<th>Created time</th>
				<th>Intelligent engine</th>
				<th>Concept drift engine
				<th>Status</th>				
				<!-- <th>Load shedding engine</th>  -->				
				<th>Edit / Delete</th>
			</tr>
		</thead>
		<tbody>
				
		</tbody>
		
	</table>	

	</div>
	
</body>
</html>