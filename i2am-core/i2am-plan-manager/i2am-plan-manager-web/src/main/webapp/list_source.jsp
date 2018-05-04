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
function loadTblSrc() {
	
	var list = null;
	var tbl = $(".table_src").children("tbody");
	tbl.html("");
	
	$.ajax({
		type : 'post',
		url : './ajax/get-list-src.jsp',
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
		var recommendation = (obj.IS_RECOMMENDATION=="N"?"Unused":
			(obj.RECOMMENDED_SAMPLING==null?'Analyzing...':obj.RECOMMENDED_SAMPLING.replace('_',' ')));
		tbl.html(tbl.html() +
			"<tr>" +
				"<td>" + obj.NAME + "</td>" +
				"<td>" + obj.CREATED_TIME + "</td>" +
				"<td>" + recommendation + "</td>" +
				//"<td>" + (obj.USES_LOAD_SHEDDING=="Y"?"Used":"Unused") + "</td>" +
				"<td>" +					 
					"<div class='status'> DEACTIVE <i class='fa fa-caret-down'></i>" + 
						"<div class='status-content'><a>ACTIVE</a></div>" +	
					"</div>" +
				"</td>" +
				"</div>" +
				"</td>" +
				"<td><div class='controlButton edit'><i class='fa fa-edit'></i> Edit</div>" +
				"<div class='controlButton delete'><i class='fa fa-trash-o'></i> Delete</div></td>" +
			"</tr>"
		);				
	}
  }
loadTblSrc();

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