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
$(function() {
	$(".sortable").sortable();
	$(".sortable").disableSelection();
});
</script>

<script>
$(document).on("click", ".del", function(){			
	var item = $(this).parent();			
	item.remove();
});
</script>

<script>
$(document).on("click", ".add", function(){
	
	var list = $(".sortable");
	
	list.append("<li class='data'>" +
				"<i class='fa fa-sort fa-lg updown'></i>" +
				"<input type='text' placeholder=' Input column name' class='data_name'> " +
				"<select class='data_type'>" +
				"<option selected disabled hidden>Type</option>" +
				"<option value='TEXT'>TEXT</option>" +
				"<option value='NUMERIC'>NUMERIC</option>" +
				"<option value='TIMESTAMP'>DATE</option>" +
				"</select>" +
				"<i class='fa fa-remove fa-lg del'></i>" +
				"</li>");
});
</script>

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

$(document).on("click", "#btn-upload", function(e) { // Add Button
	var owner = getUserId();
	var testName = document.getElementById("uploadName").value;
	var testFile = document.getElementById("uploadFile").files[0];
    var formdata = new FormData();
    formdata.append("owner", owner);
    formdata.append("testName", testName);
    formdata.append("testFile", testFile);
    var xhr = new XMLHttpRequest();       
    xhr.open("POST","/i2am-plan-manager-web/FileUploader", true);
    xhr.send(formdata);
    xhr.onload = function(e) {
      if (this.status == 200) {
        reloadTblTestData();
      }
    };
    e.preventDefault();	
});

function reloadTblTestData() {
	var list = null;
	var tbl = $('#tbl-test-data').children('tbody');
	tbl.html("");
	$.ajax({
		type : 'post',
		url : './ajax/get-list-test-data.jsp',
		data : ({}),
		async: false,
		cache: false,
		success : function(data) {
			data = data.replace(/(^\s*)|(\s*$)/gi, "");
			list = data;
		} 
	});
	
	if (list == null)	{
		alert("Test data cannot reload.");
		return ;
	}
	
	var arr = JSON.parse(list);
	
	if (arr.length > 0)	
	while (arr.length > 0) {
		var obj = arr.pop();
		tbl.html(tbl.html() +
			"<tr class='clickable-row'>" +
			"<td class='td-name'>" + obj.NAME + "</td>" +
			"<td>" + obj.CREATED_TIME + "</td>" +
			"<td>" + obj.FILE_NAME + "</td>" +
			"<td>" + obj.FILE_SIZE + "</td>" +
			"<td>" + obj.FILE_TYPE + "</td>" +
			"</tr>"
		);				
	}
}

$(document).on('click', '.clickable-row', function(event) {	// Choose...!
	  if($(this).hasClass('choosed')){
	    $(this).removeClass('choosed'); 
	    $(this).css("border", "");
	  } else {
	    $(this).addClass('choosed').siblings().removeClass('choosed');
	    $(this).css("border", "solid orange").siblings().css("border", "");
	  }
});

$(document).on('click', '#btn-choose', function(event) { // Save!
	
    if($('.choosed').html() != null) {
 	    $('#choosed-test-data').html("Choosed File: " + $('.choosed').children('.td-name').html() );
 	    $('#choosed-test-data').val( $('.choosed').children('.td-name').html() );
    } else {
  		$('#choosed-test-data').html("Not choosed yet.");
  		$('#choosed-test-data').val('');
    }
    
    document.getElementById('fileChooser').style.display='none';
    
});
</script>

<script> 
$(document).ready(function() {
	checkLogin(); 
});
</script>


<title>Create Source</title>

</head>

<body>

	<form id="sourceForm" action="ajax/create-source.jsp" method="post">
	
		<h1> New Source </h1> <hr>
	
		<div class="tab">
			
			<h2>Source Info</h2><br>		
			
			Source name<br>
			<input type="text" id="src-name"><button type="button" id="btn-check-redundancy">Check</button><br><br>
			
			<div class="type">			
				<button type="button" id="type-wd" class="typelinks" onclick="openSourceType(event,'welldefined')">Well-defined source</button>
				<button type="button" id="type-custom" class="typelinks" onclick="openSourceType(event,'customizing')">Customizing source</button>			
			</div>
			
			<div id="welldefined" class="typecontent">
			
				<h3>Well-defined source</h3>
				
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
				<h3>Customizing source</h3>
				<p><a href="resources/I2AM.jar" class="srcLabel">Download java interface</a></p>
			</div>
									
		
		</div>
	
		<div class="tab">
			<h2>Data Schema</h2><br>
		
			<ul class="sortable" id="sortable">
								
				<li class="data">
					<i class="fa fa-sort fa-lg updown"></i>
					<input type="text" placeholder=" Input column name" class="data_name">
					<select class="data_type">
						<option selected disabled hidden>Type</option>
						<option value="TEXT">TEXT</option>
						<option value="NUMERIC">NUMERIC</option>
						<option value="TIMESTAMP">DATE</option>
					</select>
					<i class="fa fa-remove fa-lg del"></i>					
				</li>			
			
			</ul>
		
			<br>
			
			<div class="divCenter">
				<span class="fa fa-plus-circle fa-2x add"></span>
			</div>			
			
		</div>
		
		<div class="tab">
			<h2>Use Smart Engine</h2><br>
				
				
			<div class="checkEngine">
					
			<label class="container">
				Load Shedding
				<input type="checkbox" name="chk_ls" value="ls">
				<span class="checkmark"></span>			
			</label>
			
			<label class="container">
				Concept Drift & Intelligent Recommendation Engine
				<input type="checkbox" name="chk_ir" value="ir" id="intengine">
				<span class="checkmark"></span>			
			</label>				
			</div>	
					
			<div class="divCenter useint">
				
				<h3>Select Target Column</h3><hr>	
				
				<div class="radioEngine">	
					<div class="target">	
						Select Target
					</div>
				</div>
				<br><br>				
				
				<button type="button" onclick="document.getElementById('fileChooser').style.display='block'; reloadTblTestData()">Choose File</button>
				<span id="choosed-test-data"> Not Choosed yet.</span>
							
				<div id="fileChooser" class="modal">				
								
					<div class="modal-content">
									
						<div class="modal-header">					
							<span onclick="document.getElementById('fileChooser').style.display='none'"	class="close" title="Close Modal">&times;</span>
							<h2> Choose your test data </h2>										
						</div>						
						<hr>
						<div class="modal-body">
	        				<table id="tbl-test-data">
	          				<thead>
	            				<tr> 
	              				<th>Data name</th>
				        	    <th>Created time</th>
				              	<th>File name</th>
				              	<th>File size</th>
				              	<th>File type</th>
	            				</tr>
	          				</thead>
	          				<tbody>
	          				</tbody>
	        				</table>	        				        				
	        			</div>
	        					
	        			<hr>        			
	        			<div class="modal-body-chooser">		        				
	        				<input type="file" id="uploadFile" accept=".csv,.json,.xml"/>
	        				<input type="text" placeholder="Input file name" id="uploadName">
	        					<button type="button" id="btn-upload">Add data</button><br>
	      				</div>  	      				
	      					      				
	      				<div class="modal-footer">	      						
								<button type="button" class="cancelbtn" onclick="document.getElementById('fileChooser').style.display='none'">Close</button>
	        					<button type="button" class="signupbtn" id="btn-choose">Save changes</button>
	        			</div>    				
					</div>			
				</div>	
			
			</div>	
						
		</div>			
			
		<br><br>
		<div style="overflow:auto;">
    		<div style="float:right;">
      			<button type="button" id="prevBtn" onclick="nextPrev(-1)">Previous</button>
      			<button type="button" id="nextBtn" onclick="nextPrev(1)">Next</button>
      			
    		</div>
  		</div>	
	
		<div style="text-align:center;margin-top:40px;">
    		<span class="step"></span>
    		<span class="step"></span>
    		<span class="step"></span>    		
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
			
		// Source Type
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
		
		// Data Scheme.
		var columns = $(".data");
		var scheme = new Array();		
		
		columns.each(function(index) {			  
			var column = index + "," + $(this).find(".data_name").val() + "," + $(this).find(".data_type option:selected").val();
			/* var column = {
				data_index: index,
				data_name: $(this).find(".data_name").val(),
				data_type: $(this).find(".data_type option:selected").val()					
			}; */	
			scheme.push(column);
			//scheme[index] = column;
		});		
		
		console.log(scheme);
		
		// Intelligent Engine
		var useIntelligentEngine = $("input[name='chk_ir']").is(":checked");
		
		// Intelligent Engine > Test Data
		var testData = null;	
		
		if(useIntelligentEngine) {			
			if ($('#choosed-test-data').val() == '') {
				  alert("Please select test data first.");
				  return false;
			}
			testData = $('#choosed-test-data').val();
		}		
		
		// Form
		var source_data = {			
			
				// Source Info.
			source_name: $("#src-name").val(),
			source_type: type,
				
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
			dbQuery: $("#dbQuery").val(),
		
			// Data Scheme
			dataScheme: scheme,
		
			// Smart Engine
			// useConceptDriftEngine: $("input[name='chk_cd']").is(":checked"),
			useConceptDriftEngine: useIntelligentEngine,
			useLoadShedding: $("input[name='chk_ls']").is(":checked"),
			useIntelligentEngine: useIntelligentEngine,
		
			// Test Data
			testData: testData,
			target: $("input[name='target']:checked").val()
		};		
			
		console.log(source_data);
		
		$.ajaxSettings.traditional = true;		
		$.ajax({
			type: "POST",
			url: "ajax/create-source.jsp",
			data: source_data,
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
			type: 'src'
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

<script>
$("#intengine").change(function() {
		
	var useint = $(".useint");	
	var schema = $(".data");
	var target = $(".target");
	
	target.empty();
	
	if( $(this).is(':checked') ) {	
				
		if( schema.length == 0 ) {
			
			target.append("There is no data...!");
		}
		else {
				
			var num = 0;
			
			schema.each(function() {
								
				var name = $(this).children(".data_name").val();
				var type = $(this).children(".data_type").val();				
								
				if( type == "NUMERIC" ) {
					
					// target value를 index로 할까?ㅅ?ㅎㅎ
					target.append("<label class='radiocontainer'>" + 
							"<div class='radiolabelleft'>" + name + "</div>" +		
							// "<div class='radiolabelright'>" + type + "</div>" +																			 
							"<input type='radio' name='target' value='" + name + "'>" +
							"<span class='radiocheckmark'></span>" +
							"</label><br>");
					
					num++;
				}
			});	
			
			if( num == 0 ) {
				target.append("There is no data available");
			}
		}
		
		useint.show();
	}
	else {		
		useint.hide();
	}	
});
</script>

<script>
var modal = document.getElementById('fileChooser');
	
// When the user clicks anywhere outside of the modal, close it
window.onclick = function(event) {
    if (event.target == modal) {
        modal.style.display = "none";
    }
}
</script>

</body>

</html>