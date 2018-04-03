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
$( function() {
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
		
	/*
	<li>
		<i class="fa fa-sort fa-lg updown"></i>
		<input type="text" placeholder=" Input column name">
		<select>
			<option selected disabled hidden>Type</option>
			<option value="string">TEXT</option>
			<option value="numeric">NUMERIC</option>
			<option value="date">DATE</option>
		</select>
		<i class="fa fa-remove fa-lg del"></i>					
	</li>			
	*/	
	
	list.append("<li class='data'>" +
				"<i class='fa fa-sort fa-lg updown'></i>" +
				"<input type='text' placeholder=' Input column name' class='data_name'> " +
				"<select class='data_type'>" +
				"<option selected disabled hidden>Type</option>" +
				"<option value='TEXT'>TEXT</option>" +
				"<option value='NUMERIC'>NUMERIC</option>" +
				"<option value='DATE'>DATE</option>" +
				"</select>" +
				"<i class='fa fa-remove fa-lg del'></i>" +
				"</li>");
});
</script>

<!-- 
<script> 
$(document).ready(function() {
	checkLogin(); 
});
</script>
 -->

<title>Create Source</title>

</head>

<body>

	<form id="sourceForm" action="#">
	
		<h1> New Source </h1> <hr>
	
		<div class="tab">
			
			<h2>Source Info</h2><br>		
			
			Source name<br>
			<input type="text"><button type="button" id="btn-check-redundancy">Check</button><br><br>
			
			<div class="type">			
				<button type="button" class="typelinks" onclick="openSourceType(event,'welldefined')">Well-defined source</button>
				<button type="button" class="typelinks" onclick="openSourceType(event,'customizing')">Customizing source</button>			
			</div>
			
			<div id="welldefined" class="typecontent">
			
				<h3>Well-defined source</h3>
				
				<div class="type">												
					<button type="button" class="wdlinks" onclick="openWDType(event,'kafka')">Kafka</button>
					<button type="button" class="wdlinks" onclick="openWDType(event,'database')">Database</button>
				</div>
				
				<div id="kafka" class="wdcontent">				
				
					<div class="srcLabel">Zookeeper IP</div>
					<div class="srcInput"><input type="text"></div>
										
					<div class="srcLabel">Zookeeper Port</div>
					<div class="srcInput"><input type="text"></div>
					
					<div class="srcLabel">Kafka Topic</div>
					<div class="srcInput"><input type="text"></div>
				
				</div>
				
				<div id="database" class="wdcontent">
				
					<div class="srcLabel">Database IP</div>
					<div class="srcInput"><input type="text"></div>
										
					<div class="srcLabel">Database Port</div>
					<div class="srcInput"><input type="text"></div>
					
					<div class="srcLabel">Database ID</div>
					<div class="srcInput"><input type="text"></div>				
					
					<div class="srcLabel">Database Password</div>
					<div class="srcInput"><input type="text"></div>
					
					<div class="srcLabel">Database Name</div>
					<div class="srcInput"><input type="text"></div>
					
					<div class="srcLabel" >Query</div>
					<div class="srcInput"><input type="text" style="width: 100%"></div>
				
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
						<option value="DATE">DATE</option>
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
			<h2>Smart Engine</h2><br>
				
				
			<div class="checkEngine">
			<label class="container">
				Use Concept Drift Engine
				<input type="checkbox" name="chk_cd" value="cd">
				<span class="checkmark"></span>			
			</label>
			
			<label class="container">
				Use LoadShedding
				<input type="checkbox" name="chk_ls" value="ls">
				<span class="checkmark"></span>			
			</label>
			
			<label class="container">
				Use Intelligent Recommendation Engine
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
				
				<button type="button" onclick="document.getElementById('fileChooser').style.display='block'">Choose File</button>
							
				<div id="fileChooser" class="modal">				
								
					<div class="modal-content">
									
						<div class="modal-header">					
							<span onclick="document.getElementById('fileChooser').style.display='none'"	class="close" title="Close Modal">&times;</span>
							<h2> Choose your test data </h2>										
						</div>						
						<hr>
						<div class="modal-body-table">
	        				<table>
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
	        				<input type="file" accept=".csv,.json,.xml"/>
	        				<input type="text" placeholder="Input file name"><button type="button">Add data</button><br>
	      				</div>  
	      				
	      					      				
	      				<div class="modal-footer">	      						
								<button type="button" class="cancelbtn" onclick="document.getElementById('fileChooser').style.display='none'">Close</button>
	        					<button type="button" class="signupbtn">Save changes</button>
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
  		//if (n == 1 && !validateForm()) return false;
  		// Hide the current tab:
  			
  		x[currentTab].style.display = "none";
  		// Increase or decrease the current tab by 1:
  		currentTab = currentTab + n;
  		// if you have reached the end of the form...
  		if (currentTab >= x.length) {
    	// ... the form gets submitted:
    		document.getElementById("regForm").submit();
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
					
					target.append("<label class='radiocontainer'>" + 
							"<div class='radiolabelleft'>" + name + "</div>" +		
							// "<div class='radiolabelright'>" + type + "</div>" +																			 
							"<input type='radio' name='target'>" +
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