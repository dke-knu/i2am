<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">

<link rel="stylesheet" type="text/css" href="css/newSource.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">

<script src="./js/jquery-3.3.1.min.js"></script>
<script src="./js/jquery-ui.js"></script>
<script src="./js/my.js"></script>

<script>
function getUserId() {
	var id = null;
	$.ajax({
		type : 'post',
		url : './ajax/get-user-id.jsp',
		data : ({}),
		async: false,
		cache: false,
		success : function(data) {
			data = data.replace(/(^\s*)|(\s*$)/gi, "");
			id = data;
		}
	});
	return id;
}
</script>

<script> 
$(document).ready(function() {
	checkLogin(); 
});
</script>

<title>Create Destination</title>

</head>

<body>

	<form id="sourceForm" action="ajax/create-destination.jsp" method="post">
	
		<h1> New Destination </h1> <hr>
	
		<div class="tab">
			
			<h2>Destination Info</h2><br>		
			
			Destination name<br>
			<input type="text" id="dst-name"><button type="button" id="btn-check-redundancy">Check</button><br><br>
			
			<div class="type">			
				<button type="button" id="type-wd" class="typelinks" onclick="openSourceType(event,'welldefined')">Well-defined destination</button>
				<button type="button" id="type-custom" class="typelinks" onclick="openSourceType(event,'customizing')">Customizing destination</button>			
			</div>
			
			<div id="welldefined" class="typecontent">
			
				<h3>Well-defined destination</h3>
				
				<div class="type">												
					<button type="button" id="type-kafka" class="wdlinks" onclick="openWDType(event,'kafka')">Kafka</button>
					<button type="button" id="type-db" class="wdlinks" onclick="openWDType(event,'database')">Database</button>
				</div>
				
				<div id="kafka" class="wdcontent">				
				
					<div class="srcLabel">Zookeeper IP</div>
					<div class="srcInput"><input type="text" id="zookeeperIp"></div>
										
					<div class="srcLabel">Zookeeper Port</div>
					<div class="srcInput"><input type="text" id="zookeeperPort"></div>
					
					<div class="srcLabel">Kafka Topic</div>
					<div class="srcInput"><input type="text" id="kafkaTopic"></div>
				
				</div>
				
				<div id="database" class="wdcontent">
				
					<div class="srcLabel">Database IP</div>
					<div class="srcInput"><input type="text" id="dbIp"></div>
										
					<div class="srcLabel">Database Port</div>
					<div class="srcInput"><input type="text" id="dbPort"></div>
					
					<div class="srcLabel">Database ID</div>
					<div class="srcInput"><input type="text" id="dbId"></div>				
					
					<div class="srcLabel">Database Password</div>
					<div class="srcInput"><input type="text" id="dbPw"></div>
					
					<div class="srcLabel">Database Name</div>
					<div class="srcInput"><input type="text" id="dbName"></div>
					
					<div class="srcLabel" >Query</div>
					<div class="srcInput"><input type="text" id="dbQuery" style="width: 100%"></div>
				
				</div>
				
			</div>
			
			<div id="customizing" class="typecontent">
				<h3>Customizing destination</h3>
				<p><a href="resources/I2AM.jar" class="srcLabel">Download java interface</a></p>
			</div>
									
		
		</div>		
			
		<br><br>
		<div style="overflow:auto;">
    		<div style="float:right;">
      			<button type="button" id="prevBtn" onclick="nextPrev(-1)">Previous</button>
      			<button type="button" id="nextBtn" onclick="nextPrev(1)">Next</button>
      			
    		</div>
  		</div>		
		
	</form>

	<script>

	var currentTab = 0; // Current tab is set to be the first tab (0)
	showTab(currentTab); // Display the crurrent tab
	
	function showTab(n) {
	  	// This function will display the specified tab of the form...
  		var x = document.getElementsByClassName("tab");
  		x[n].style.display = "block";
  		//... and fix the Previous/Next buttons:
  			
  		if (n == 0) {
    		document.getElementById("prevBtn").style.display = "none";
  		} else {
    		document.getElementById("prevBtn").style.display = "inline";
  		}
  		
  		if (n == (x.length - 1)) {    		
  			document.getElementById("nextBtn").innerHTML = "Submit";    		
  		} else {
    		document.getElementById("nextBtn").innerHTML = "Next";
  		}
  		//... and run a function that will display the correct step indicator:
  		fixStepIndicator(n)
	}

	function nextPrev(n) {
  		// This function will figure out which tab to display
  		var x = document.getElementsByClassName("tab");
  		// Exit the function if any field in the current tab is invalid:
  		// if (n == 1 && !validateForm()) return false;
  		// Hide the current tab:
  			
  		x[currentTab].style.display = "none";
  		// Increase or decrease the current tab by 1:
  		currentTab = currentTab + n;
  		// if you have reached the end of the form...
  		if (currentTab >= x.length) {
    	// ... the form gets submitted:
    		//document.getElementById("sourceForm").submit();
  			submitForm();
    		return false;
  		}
  		// Otherwise, display the correct tab:
  		showTab(currentTab);
	}

	function validateForm() {
  		// This function deals with validation of the form fields
  		var x, y, i, valid = true;
  		x = document.getElementsByClassName("tab");
  		y = x[currentTab].getElementsByTagName("input");
  		// A loop that checks every input field in the current tab:
  		for (i = 0; i < y.length; i++) {
    		// If a field is empty...
    		if (y[i].value == "") {
      			// add an "invalid" class to the field:
      			y[i].className += " invalid";
      			// and set the current valid status to false
			    valid = false;
    		}
  		}
  		// If the valid status is true, mark the step as finished and valid:
  		if (valid) {
    		document.getElementsByClassName("step")[currentTab].className += " finish";
  		}
  		return valid; // return the valid status
	}

	function fixStepIndicator(n) {
  		// This function removes the "active" class of all steps...
  		var i, x = document.getElementsByClassName("step");
  		for (i = 0; i < x.length; i++) {
    		x[i].className = x[i].className.replace(" active", "");
  		}
  		//... and adds the "active" class on the current step:
  		x[n].className += " active";
	}	
	
	function submitForm() {
				
		// Destination Type
		var type = "";		
		if( $("#type-custom").hasClass("active") )
			type = "CUSTOM";
		else {
			if( $("#type-kafka").hasClass("active") ) {				
				type = "KAFKA";								
			}	
			else if ( $("#type-db").hasClass("active") ) {				
				type = "DATABASE";
			} 							
		}
				
		var destination_data = {			
			
			// Destination Info.
			destination_name: $("#dst-name").val(),
			destination_type: type,
				
			// Kafka
			zookeeperIp: $("#zookeeperIp").val(),		
			zookeeperPort: $("#zookeeperPort").val(),
			kafkaTopic: $("#kafkaTopic").val(),
			
			// DataBase
			dbIp: $("#dbIp").val(),
			dbPort: $("#dbPort").val(),
			dbId: $("#dbId").val(),
			dbPw: $("#dbPw").val(),
			dbName: $("#dbName").val(),
			dbQuery: $("#dbQuery").val()			
		};		
			
		console.log(destination_data);
		
		$.ajaxSettings.traditional = true;		
		$.ajax({
			type: "POST",
			url: "ajax/create-destination.jsp",
			data: destination_data,
			async: false,
			cache: false,
			success: function(response) {
				alert(response.trim());				
				console.log(response.trim());
				window.open("./main.jsp", "_self");
			}, 
			error: function() {
				alert("ERROR");	
				//window.location.reload();
			}
		});		
		return false;
};	
</script>

<script>
function openSourceType(event, sourceType) {
	
	var i, typecontent, typelinks;
	typecontent = document.getElementsByClassName("typecontent");
	
	for( i=0; i<typecontent.length; i++ ) {
		typecontent[i].style.display = "none";
	}
	
	typelinks = document.getElementsByClassName("typelinks");
	
	for( i=0; i<typelinks.length; i++ ) {
		typelinks[i].className = typelinks[i].className.replace(" active", "");
	}
			
	document.getElementById(sourceType).style.display="block";
	event.currentTarget.className += " active";	
	
	return false;
}

function openWDType(event, sourceType) {
	
	var i, typecontent, typelinks;	
	
	typecontent = document.getElementsByClassName("wdcontent");
	
	for( i=0; i<typecontent.length; i++ ) {
		typecontent[i].style.display = "none";
	}
		
	typelinks = document.getElementsByClassName("wdlinks");
	
	for( i=0; i<typelinks.length; i++ ) {
		typelinks[i].className = typelinks[i].className.replace(" active", "");		
	}
			
	document.getElementById(sourceType).style.display="block";
	event.currentTarget.className += " active";	
	
	return false;
}
</script>

<script>
$('#btn-check-redundancy').on('click', function(event) {
	var name = $('#btn-check-redundancy').prev().val();
	if (name == null || name == '') {
		alert("Please input first.");
		return;
	}
	$.ajax({
		type : 'post',
		url : './ajax/check-redundancy.jsp',
		data : ({
			name: name,
			type: 'dst'
		}),
		async: false,
		cache: false,
		success : function(data) {
		  data = data.replace(/(^\s*)|(\s*$)/gi, "");
		  if (data == 'true')
		  	alert(name + " is available.");
		  else
			alert(name + " is already used.");
		}
	});
});
</script>

</body>

</html>