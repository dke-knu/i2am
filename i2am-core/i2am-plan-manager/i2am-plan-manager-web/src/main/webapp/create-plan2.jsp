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
    				Query Builder Here!
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

</body>

</html>