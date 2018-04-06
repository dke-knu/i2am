<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">

<link rel="stylesheet" type="text/css" href="./css/newPlan.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">

<script src="./js/jquery-3.3.1.min.js"></script>
<script src="./js/jquery-ui.js"></script>
<script src="./js/jsplumb.js"></script>  

<!-- for QueryBuilder -->
<link href="./css/bootstrap.min.css" rel="stylesheet">
<script src="./js/bootstrap.min.js"></script> 
<script src="./js/bootbox.min.js"></script> 
<script src="./js/moment.min.js"></script>
<script src="./js/query-builder.standalone.min.js"></script>
<link href="./css/query-builder.default.min.css" rel="stylesheet">   
<!-- ---------------- -->

<title>Create Plan</title>

<script>
$(document).ready(function() { 
		
	// jsPlumb init.
	var plumb = jsPlumb.getInstance();
		
	var source = {
		isSource: true,
		isTarget: false,			
		anchor: "Right",
		paintStyle:{ fill: "#ea4335", stroke:"#333", strokeWidth:1 },
		connectorStyle:{ outlineStroke:"#ea4335", strokeWidth:2 },
		dragAllowedWhenFull:false  
	};	
	
	var destination = {
		isSource: false,
		isTarget: true,
		anchor: "Left",		
		paintStyle:{ fill: "#ea4335", stroke:"#333", strokeWidth:1 },
		connectorStyle:{ outlineStroke:"#ea4335", strokeWidth:2 },
		dragAllowedWhenFull:false  
	};	
	
	$(".drag").draggable({		
		revert: "invalid",
		helper: "clone"		
	});
	
	$(".drop").droppable({		
		classes: {
			"drag": "dragAfter"
		},
		drop: function(e, ui) {
			
			var item = $(ui.helper).clone();			
			var params = item.children(".params");
			
			if( item.hasClass('drag') ) {
				
				item.appendTo($(this));
				item.removeClass('drag');
				item.addClass('dragAfter');
				params.slideDown("slow");
								
				var min = $("<div class='control min'></div>").text("_");
				var del = $("<div class='control del'></div>").text("X");
				item.prepend(min);
				item.prepend(del);
			}
			
			if( item.hasClass('source') ) {
			
				plumb.addEndpoint(item, source);
			}
			else if ( item.hasClass('destination') ) {
			
				plumb.addEndpoint(item, destination);
			}
			else {
				
				plumb.addEndpoint(item, source);
				plumb.addEndpoint(item, destination);
			}
			
			plumb.draggable(item);
		}
	});	
	
	$(document).on("click", ".min", function(){		
		var item = $(this).parent();
		var params = item.children(".params");
		params.slideToggle();
	}); 

	$(document).on("click", ".del", function(){			
		var item = $(this).parent();			
		plumb.remove(item);		
	});

	$(document).on("click", "#btn-get-rules", function() {
	  var result = $('#builder-basic').queryBuilder('getRules');
	  
	  if ($.isEmptyObject(result)) {
		alert("Query is empty!");
		$('#queryBuilderModal').modal('show');
	  }
	  else {
	    console.log(JSON.stringify(result, null, 2));
	    alert(JSON.stringify(result, null, 2));
	  }
	});

	$(document).on("click", "#btn-reset", function() {
	  $('#builder-basic').queryBuilder('reset');
	});
});
</script>

</head>
<body>
 
	<div class="row">
	
		<div class="header">
			<h1>New Plan</h1>
			<hr><br>	
			Plan name<br>	
			<input type="text" placeholder=" input plan name"></input><button type="button">Check</button>	
		</div>		
		<!-- Source Plan Destination 리스트 표시하는 곳  -->
		<div class="column left">
			
			<div class="list">
	
			<div class="title">
				<h2> Algorithms </h2>
			</div>

			
			<div class="item source drag">			
				<i class='fa fa-filter type'></i>
				<div class="name">Source</div>						
				<div class="control tooltip"> ? <span class="tooltiptext">설띵</span></div>												
				<div class="params">
				 Parameters<br><br>
				 Data Schemes from...?
				 </div>											
			</div>								
						
			<div class="item topology drag">			
				<i class='fa fa-cog type'></i>
				<div class="name">Sampling</div>								
				<div class="control tooltip">?<span class="tooltiptext">설띵</span></div>												
				<div class="params">
				 Parameters<br><br>
				 Sample size <input type="text"></input>
				 </div>	
										
			</div>	
			
			<div class="item topology drag">			
				<i class='fa fa-cog type'></i>
				<div class="name">Query Filtering</div>				
				<div class="control tooltip">?<span class="tooltiptext">설띵</span></div>												
				 
				<div class="params">
				  <button class="btn btn-large btn-primary" href="#queryBuilderModal" data-toggle="modal">Set rules</button>
				  <button class="btn btn-large btn-success" id="btn-get-rules" data-target="basic">Get rules</button>
				  <!-- <button class="btn btn-success" id="btn-get-i2am-query" data-target="basic">I2AM query</button> -->
  				</div>	
								
			</div>	

			<div class="item destination drag">			
				<i class='fa fa-database type'></i>
				<div class="name">Destination</div>				
				<div class="control tooltip">?<span class="tooltiptext">설띵</span></div>												
				<div class="params">
				 Destinations don't need Parameters. 
				 </div>	
										
			</div>	

			</div>

		</div>
			
		<!-- Plan 구성하는 곳 -->	
		<div class="column right drop">
		
			<div class="panel">
				<div class="title">
					<h2> Panel: First, select source !</h2>				
				</div>			
				
				<div class="comment">
					<i class="fa fa-hand-paper-o"></i><br>
					Drag & Drop Here! 
				</div>
				
			</div>					
		
		</div>	
	</div>
		
	<!-- Modal -->
	<div id="queryBuilderModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="queryBuilderModalLabel" aria-hidden="true" style="width: 1000px; margin-left: -500px;">
	  <div class="modal-header">
	    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
	    <h3 id="queryBuilderModalLabel">Modal header</h3>
	  </div>
	  <div class="modal-body">
	      <div id="builder-basic" class="query-builder"></div>
	  </div>
	  <div class="modal-footer">
		<button class="btn btn-warning" id="btn-reset" data-target="basic">Reset</button>
	    <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">Save changes</button>
	  </div>
	</div>
</body>
  <script src="./js/query-builder.i2am.js"></script>

</html>