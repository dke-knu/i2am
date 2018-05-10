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
function getSourceList() {
	// Insert Source & Destination
	
	var list = $(".list");// html list! 
	var source_list = null;	
	
	$.ajax({
		type : 'post',
		url : './ajax/get-list-src.jsp',
		data : ({}),
		async: false,
		cache: false,
		success : function(data) {
			data = data.replace(/(^\s*)|(\s*$)/gi, "");
			source_list = data;
		} 
	});	
	
	if (source_list == null) {
		alert("Src cannot load.");
		return ;
	}
	
	var arr = JSON.parse(source_list); 
	
	while (arr.length > 0) {
		var obj = arr.pop();
		//console.log(obj.NAME);	
		
		var source = '<div class="itemWrap drag source"' + 'name=' + obj.NAME  + '>' +
						'<div class="item">' +			
						'<i class="fa fa-filter type sourceType"></i>' +
							'<div class="name">' + obj.NAME + '</div>' +						
							'<div class="control myTooltip"> ? <span class="myTooltiptext">설띵</span></div>' +
						'</div>' +
						'<div class="params">' +
	 						'Parameters<br><br>' +
 							'Data Schemes from...?' +
 						'</div>' +
						'</div>';
						
		list.prepend(source);
	}
}
</script>

<script> 
function getDestinationList() {
	// Insert Source & Destination
	
	var list = $(".list");// html list! 
	var destination_list = null;	
	
	$.ajax({
		type : 'post',
		url : './ajax/get-list-dst.jsp',
		data : ({}),
		async: false,
		cache: false,
		success : function(data) {
			data = data.replace(/(^\s*)|(\s*$)/gi, "");
			destination_list = data;
		} 
	});	
	
	if (destination_list == null) {
		alert("Dst cannot load.");
		return ;
	}
	
	var arr = JSON.parse(destination_list); 
	
	while (arr.length > 0) {
		var obj = arr.pop();
		//console.log(obj.NAME);	
		
		var destination = '<div class="itemWrap drag destination"' + 'name=' + obj.NAME + '>' +
						'<div class="item">' +			
						'<i class="fa fa-database type destinationType"></i>' +
							'<div class="name">' + obj.NAME + '</div>' +						
							'<div class="control myTooltip"> ? <span class="myTooltiptext">설띵</span></div>' +
						'</div>' +
						'<div class="params">' +
	 						'Parameters<br><br>' +
 							'Destination do not need params.' +
 						'</div>' +
						'</div>';
						
		list.append(destination);
	}
}
</script>

<script>
$(document).ready(function() { 
		
	var height = $(window).height() / 10 * 6.2;
	var title = $('.lefttitle').height();
	var filter = $('.filterWrap').height();
	$('.list').height(height);
	$('.panel').height(height+title+filter);
	
	getSourceList();
	getDestinationList()
	
	// jsPlumb init.
	var plumb = jsPlumb.getInstance();
		
	var source = {
		isSource: true,
		isTarget: false,			
		anchor: "Right",
		paintStyle:{ fill: "#ea4335", stroke:"#333", strokeWidth:1 },
		connector: [ "Flowchart", {stub: [30, 30], cornerRadius: 2} ],
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
	
	plumb.bind("connection", function(info) {
		console.log("연결됨");
		//console.log(info);
	});
	
	plumb.bind("connectionDetached", function(info) {
		console.log("연결 끊김");
		//console.log(info);
	});
	
	plumb.bind("connectionMoved", function(info) {
		console.log("연결 바뀜");
		console.log(info);
	});
	
	$(".drag").draggable({		
		revert: "invalid",		
		helper: "clone"		
	});	
		
	$(".drop").droppable({		
		classes: {
			"drag": "dragAfter"
		},
		drop: function(e, ui) {
						
			var itemWrap = $(ui.helper).clone(true);
			var item = itemWrap.children(".item").children(".name");
			var params = itemWrap.children(".params");
						
			var pos = ui.position;
			var dpos = $(this).offset();
			
			if( itemWrap.hasClass('drag') ) {
				
				itemWrap.appendTo($(this));
				
				itemWrap.css({ 
					top: pos.top - dpos.top,
					left: pos.left - dpos.left
				});						
				
				itemWrap.removeClass('drag');				
				itemWrap.draggable({
				      disabled: true
			    });
				itemWrap.addClass('dragAfter');
				params.slideDown("slow");				
								
				var min = $("<div class='control min'></div>").text("_");
				var del = $("<div class='control del'></div>").text("X");
								
				item.after(min);
				item.after(del);
				
				if( itemWrap.hasClass('source') ) {
					
					plumb.addEndpoint(itemWrap, source);
				}
				else if ( itemWrap.hasClass('destination') ) {
				
					plumb.addEndpoint(itemWrap, destination);
				}
				else {				
					plumb.addEndpoint(itemWrap, source);
					plumb.addEndpoint(itemWrap, destination);
				}			
				
				plumb.draggable(itemWrap, {
					containment: "parent"
				});		
			}
			
			console.log(ui.draggable.attr('name'));
			var origin = $(".drag[name='" + ui.draggable.attr('name') + "']");
			origin.hide();
			origin.addClass("isHidden");
			console.log(origin.length);
		}
	});	
	
	$(document).on("click", ".min", function(){		
		var itemWrap = $(this).parent().parent();		
		var params = itemWrap.children(".params");		
		params.slideToggle();
	}); 

	$(document).on("click", ".del", function(){		
		var itemWrap = $(this).parent().parent();	
		//console.log(itemWrap.attr('name'));
		var origin = $(".drag[name='" + itemWrap.attr('name') + "']");
		origin.show();
		origin.removeClass("isHidden");
		plumb.remove(itemWrap);		
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
	
	$(document).on("click", ".mySubmit", function() {
				
		var connections = plumb.select();		
		
		if( connections.length == 0 ) {
			alert("연결이 없어요!");
		}
		else {
		
			connections.each(function(connection) {
				
				console.log(connection.source);
				console.log(connection.target);
			});			
		}				
	});
	
});
</script>

<script>
$(window).resize(function() {
		
	var height = $(window).height() / 10 * 6.2;
	var title = $('.lefttitle').height();
	var filter = $('.filterWrap').height();
	$('.list').height(height);
	$('.panel').height(height+title+filter);
});

$(window).trigger('resize');
</script>

</head>
<body> 
	<div class="myRow">	
		<div class="header">
			<h1>New Plan</h1>	
			<button class="mySubmit" type="button">Submit</button>
			Plan name<br>	
			<input placeholder=" input plan name" class="myinput"><button class="mybutton" type="button">Check</button>				
		</div>										
		<div class="columnWrap">		
			<!-- Source Plan Destination 리스트 표시하는 곳  -->
			<div class="column left">						 
			 	<div class="lefttitle"> 
			 		<h2> Algorithms </h2>
				</div>			
				<div class="filterWrap">
					<div class="filter" id="source"><i class="fa fa-filter"></i> Source</div>
					<div class="filter" id="topology"><i class='fa fa-cog'></i> Topology</div>
					<div class="filter" id="destination"><i class='fa fa-database'></i> Destination</div>				
				</div>				
				<div class="list">						
						
					<div class="itemWrap drag topology" name="bbs">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Binary Bernoulli Sampling</div>								
							<div class="control myTooltip">?
								<span class="myTooltiptext">설띵</span>
							</div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			Sample size<br>
					 			<input class="paramInput"></input><br>
					 			Window size<br>
					 			<input class="paramInput"></input>
				 			</div>
				 		</div>
					</div>
				
					<div class="itemWrap drag topology" name="hs">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Hash Sampling</div>								
							<div class="control myTooltip">?<span class="myTooltiptext">설띵</span></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			Sample ratio<br>
					 			<input class="paramInput"></input><br>
					 			Window size<br>
					 			<input class="paramInput"></input>
				 			</div>
				 		</div>
					</div>		
					
					<div class="itemWrap drag topology" name="ks">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">K Sampling</div>								
							<div class="control myTooltip">?<span class="myTooltiptext">설띵</span></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			Sample rate<br>
					 			<input class="paramInput"></input><br>					 			
				 			</div>
				 		</div>
					</div>	
					
					<div class="itemWrap drag topology" name="ucks">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">UC K Sampling</div>								
							<div class="control myTooltip">?<span class="myTooltiptext">설띵</span></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			Sample rate<br>
					 			<input class="paramInput"></input><br>
					 			UC under bound<br>
					 			<input class="paramInput"></input>
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="ps">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Priority Sampling</div>								
							<div class="control myTooltip">?<span class="myTooltiptext">설띵</span></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			Sample size<br>
					 			<input class="paramInput"></input><br>
					 			Window size<br>
					 			<input class="paramInput"></input>
				 			</div>
				 		</div>
					</div>	

					<div class="itemWrap drag topology" name="rs">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Reservoir Sampling</div>								
							<div class="control myTooltip">?<span class="myTooltiptext">설띵</span></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			Sample size<br>
					 			<input class="paramInput"></input><br>
					 			Window size<br>
					 			<input class="paramInput"></input>
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="ss">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Systematic Sampling</div>								
							<div class="control myTooltip">?<span class="myTooltiptext">설띵</span></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			Interval<br>
					 			<input class="paramInput"></input><br>					 			
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="bf">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Bloom Filtering</div>								
							<div class="control myTooltip">?<span class="myTooltiptext">설띵</span></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			Bucket size<br>
					 			<input class="paramInput"></input><br>
					 			Keywords<br>
					 			<input class="paramInput"></input>
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="qf">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Query Filtering</div>								
							<div class="control myTooltip">?<span class="myTooltiptext">설띵</span></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			<button class="btn btn-large btn-primary" href="#queryBuilderModal" data-toggle="modal">Set rules</button>
				  				<button class="btn btn-large btn-success" id="btn-get-rules" data-target="basic">Get rules</button>
				  				<!-- <button class="btn btn-success" id="btn-get-i2am-query" data-target="basic">I2AM query</button> -->
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="kf">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Kalman Filtering</div>								
							<div class="control myTooltip">?<span class="myTooltiptext">설띵</span></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			Q value<br>
					 			<input class="paramInput"></input><br>
					 			R value<br>
					 			<input class="paramInput"></input>
				 			</div>
				 		</div>
					</div>
						
					<div class="itemWrap drag topology" name="nrkf">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Noise Recommend Kalman Filtering</div>								
							<div class="control myTooltip">?<span class="myTooltiptext">설띵</span></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			Q value<br>
					 			<input class="paramInput"></input><br>					 		
				 			</div>
				 		</div>
					</div>						
			</div>
			 
			</div>
			
			<div class="column right">
					<div class="panel drop">					
						<div class="txt"><i class="fa fa-hand-paper-o"></i><br><br>Drag & Drop Here!</div>
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

<script>
$(".filter").on("click", function() {
		
	var selected = $(this);
	
	var sources = $(".source.drag");
	var topologies = $(".topology.drag");
	var destinations = $(".destination.drag");
	
	sources.hide();
	topologies.hide();
	destinations.hide();	
	
	if(selected.hasClass("filterActive")) {	// 필터 해제
		selected.removeClass("filterActive");
		sources.show();
		topologies.show();
		destinations.show();		
	}
	else {		
		$(".filterActive").removeClass("filterActive"); // 이미 체크된 애 해제.
		selected = $(this).addClass("filterActive");		
		var filter = selected.attr('id');			
		console.log(filter);
		$("." + filter).show();		
	}	
	$(".isHidden").hide();
})
</script>

</html>