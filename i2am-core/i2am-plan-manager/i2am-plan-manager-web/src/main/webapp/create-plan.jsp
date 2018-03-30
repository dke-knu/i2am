<%@ page language="java" contentType="text/html; charset=EUC-KR"
    pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">

<link rel="stylesheet" type="text/css" href="./css/newPlan.css">

<script src="./js/jquery-3.3.1.min.js"></script>
<script src="./js/jquery-ui.js"></script>
<script src="./js/jsplumb.js"></script>

<title>Create Plan</title>

<script>
$(document).ready(function() {
		
	// jsPlumb init.
	var plumb = jsPlumb.getInstance();
		
	var source = {
		isSource: true,
		isTarget: false,		
		anchor: "Right",
		endpoint: "Rectangle",
		dragAllowedWhenFull:false  
	};	
	
	var destination = {
		isSource: false,
		isTarget: true,
		anchor: "Left",	
		endpoint: "Rectangle",
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
								
				var min = $("<div class='itemControl min'></div>").text("_");
				var del = $("<div class='itemControl del'></div>").text("X");
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
});
</script>

</head>
<body>

	<h1>New Plan</h1>
	<hr><br>
		
	<p>
	Plan name:	
	<input type="text"></input>
	</p><br>
	
	<div class="row">
		
		<!-- Source Plan Destination 리스트 표시하는 곳  -->
		<div class="column left">

			<h2> Algorithms </h2>
						
			
			<div class="item source drag">			
				Source				
				
				<div class="itemControl tooltip"> ? <span class="tooltiptext">설띵</span></div>												
				<div class="params">
				 Parameters<br><br>
				 Data Schemes from...?
				 </div>											
			</div>								
						
			<div class="item topology drag">			
				Sampling				
				
				<div class="itemControl tooltip"> ? <span class="tooltiptext">설띵</span></div>												
				<div class="params">
				 Parameters<br><br>
				 Sample size <input type="text"></input>
				 </div>	
										
			</div>	
			
			<div class="item topology drag">			
				Filtering
				<div class="itemControl tooltip"> ? <span class="tooltiptext">설띵</span></div>												
				
				<div class="params">
    				Query Builder Here!
  				</div>				
								
			</div>	

			<div class="item destination drag">			
				Destination				
				
				<div class="itemControl tooltip"> ? <span class="tooltiptext">설띵</span></div>												
				<div class="params">
				 Destinations don't need Parameters. 
				 </div>	
										
			</div>	

		</div>
			
		<!-- Plan 구성하는 곳 -->	
		<div class="column right drop">
		
			<h2> Panel </h2>
		
			<form> 
			
			
			</form>			
		
		</div>
	
	</div>

</body>

</html>