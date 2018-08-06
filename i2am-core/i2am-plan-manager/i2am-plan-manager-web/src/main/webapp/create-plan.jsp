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

<script src="./js/query-builder.i2am.js"></script>

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
		url : './ajax/get-list-src-with-intelligent.jsp',
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
		var source = '<div class="itemWrap drag source"' + ' name=' + obj.NAME  + '>' +
						'<div class="item">' +			
							'<i class="fa fa-filter type sourceType"></i>' +
								'<div class="name">' + obj.NAME + '</div>' +						
								//'<div class="control myTooltip"> ? <span class="myTooltiptext">����</span></div>' +
								'<div class="control"> </div>' +
							'</div>' +
						'<div class="params">' +		 								
		 					'<table class="schema"><tr><th>#</th><th>name</th><th>type</th></tr></table>'
	 					'</div>';
	 					
	 	if(obj.IS_RECOMMENDATION == "Y") {
	 		
	 		source = source + '<div class="recommendation"><i class="fa fa-thumbs-o-up"></i> ' + obj.RECOMMENDED_SAMPLING.replace("_", " ") + '</div>';
	 		
	 	}						
	 					
	 	source = source + '</div>';				
						
		list.prepend(source);
	}	
}
</script>

<script>
function setSourceSchema() {

	var schema_list = null;
		
	$.ajax({
		type : 'post',
		url : './ajax/get-source-scheme.jsp',
		data : ({}),
		async: false,
		cache: false,
		success : function(schema_data) {
			schema_data = schema_data.replace(/(^\s*)|(\s*$)/gi, "");
			schema_list = schema_data;
		} 
	});	
	
	if (schema_list == null) {
		alert("Schema cannot load.");
		return ;
	}	
	
	schema = JSON.parse(schema_list);	
	
	for(var i=0; i<schema.length; i++) {
		
		var col = schema[i];
		var source = $("div[name='" + col.NAME + "']").find(".schema");
		var index = parseInt(col.COLUMN_INDEX) + 1;
		source.append("<tr><td>" + index + "</td><td>" + col.COLUMN_NAME + "</td><td>" + col.COLUMN_TYPE + "</td></tr>");
	}	
}
</script>

<script>
function setTargets() {
	
	var targets_list = null;
	
	$.ajax({
		type : 'post',
		url : './ajax/get-target.jsp',
		data : ({}),
		async: false,
		cache: false,
		success : function(target_data) {
			target_data = target_data.replace(/(^\s*)|(\s*$)/gi, "");
			targets_list = target_data;
		} 
	});	
	
	if (targets_list == null) {
		alert("Targets cannot load.");
		return ;
	}	
	
	targets = JSON.parse(targets_list);	
	console.log(targets_list);
	
	for(var i=0; i<targets.length; i++ ) {
		
		var target = targets[i];		
		var recommendation = $("div[name='" + target.NAME + "']").find(".schema");
		var td = recommendation.find("td:contains('" + target.COLUMN_NAME + "')");
		var tr = td.parent();
		
		console.log(tr);
		tr.addClass("target");
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
							//'<div class="control myTooltip"> ? <span class="myTooltiptext">����</span></div>' +
							'<div class="control"></div>' +
						/* '</div>' +
						 '<div class="params">' +
	 						'Parameters<br><br>' +
 							'Destination do not need params.' +
 						'</div>' + */
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
	setSourceSchema();
	setTargets();
	getDestinationList();
	
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
		
		// ���� DIV�� �����Ϸ��� $()
		var source = $(info.source);
		var target = $(info.target);		
		
		//console.log(target);
		
		var hasSource = false;
		var srcName = "";		
		
		// �ҽ� ������ ������ �ִ��� üũ > ������ �ִٸ� > �� ������ �ڿ��� ���������ؾ��Ѵٱ�����!!
		if( source.hasClass("source") ) { // �ҽ��� ���� �Ǿ��� ��,			
			hasSource = true;
			srcName = source.attr("name");
			// $(target).removeAttr("source", srcName);
			$(target).attr("source", srcName);
		}
		else if( typeof source.attr("source") != "undefined" && source.attr("source") != "") { // �ҽ��� ���� ���������� ����Ǿ��� ��,			
			hasSource = true;
			srcName = $(source).attr("source");
			// $(target).removeAttr("source", srcName);
			$(target).attr("source", srcName);
		}	
				
		// �ҽ��� �ִ�. �����ؾ��� ���� ��带 ã�ƾ� �Ѵ�...!
		if ( hasSource ) {
			
			do {
				// ���� Ÿ���� �������͸����� ���� �˻��ؾ���!
				if( $(target).attr("name") == 'qf' ) {					
					
					var columns = [];
			
					for( var i=0; i<schema.length; i++ ) {				
						if( schema[i].NAME == srcName ) {
							columns.push(schema[i]);							
						}
					}
					
					var queryFilter = [];
				
					for( var i=0; i<columns.length; i++ ) {
				
						var temp = { };
				
						temp["id"] = columns[i].COLUMN_NAME;
						temp["label"] = columns[i].COLUMN_NAME;
				
						switch (columns[i].COLUMN_TYPE) {
				
						case "TEXT":					
							temp["type"] = 'string';
							temp["operators"] = ['equal', 'not_equal', 'in', 'not_in'];					
							break;
					
						case "NUMERIC":
							temp["type"] = 'double';
							temp["operators"] = ['equal', 'not_equal', 'greater', 'less', 'greater_or_equal', 'less_or_equal'];
							break;
							
						case "TIMESTAMP":
							temp["type"] = 'datetime';
							temp["placeholder"] = 'YYYY-MM-DD HH:mm:ss',
							temp["operators"] = ['greater_or_equal', 'less_or_equal'];
							temp["validation"] = { format: 'YYYY-MM-DD HH:mm:ss' };
							break;
					
						default:
							alert("error!");
							return 0;
							break;				
					}
						queryFilter[i] = temp;
					}					
					$('#builder-basic').queryBuilder({
						filters: queryFilter
					});					
					$('#builder-basic').queryBuilder('setFilters', true, queryFilter);					
					
					
					$(".setRules").attr("disabled", false);
					target.find(".cannot").hide();
					//target.find(".schemenTarget").show();
				}
				
				// Ÿ�� �ʿ� --> ����, ����
				if( $(target).attr("name") == 'bf' || $(target).attr("name") == 'hs' || $(target).attr("name") == 'ps' ) {
					
					var columns = [];
					
					for( var i=0; i<schema.length; i++ ) {				
						
						if( schema[i].NAME == srcName ) {
							
							// if( schema[i].COLUMN_TYPE == "NUMERIC" || schema[i].COLUMN_TYPE == "TEXT" ) {
							
								columns.push(schema[i]);
							// }
						}
					}
					
					var targetScheme = $(target).find(".schemenTarget");
					
					var table = "<table><tr><th>#</th><th>name</th><th>type</th><th>target</th></tr>";
					var randomName = "id" + Math.random();
					
					for( var i=0; i<columns.length; i++ ) {
						
						var col = columns[i];
						var index = parseInt(col.COLUMN_INDEX) + 1;
						table = table + "<tr><td>" + index + "</td><td>" + col.COLUMN_NAME + "</td><td>" + col.COLUMN_TYPE + "</td>"
										+ "<td><input type='radio' class='radioTarget' name='" + "name" + randomName + "' value='" + i + "'></td></tr>";						
					}
					
					table = table + "</table>";
					
					targetScheme.html(table);
					
					target.find(".cannot").hide();
					target.find(".schemenTarget").show();					
				}
				
				// Ÿ�� �ʿ� --> ����
				if( $(target).attr("name") == 'kf' || $(target).attr("name") == 'nrkf' || $(target).attr("name") == 'ikf' ) {
					
					var columns = [];
					
					for( var i=0; i<schema.length; i++ ) {				
						
						if( schema[i].NAME == srcName ) {
							
							if( schema[i].COLUMN_TYPE == "NUMERIC" ) {
							
								columns.push(schema[i]);
							}
						}
					}
					
					var targetScheme = $(target).find(".schemenTarget");
					
					var table = "<table><tr><th>#</th><th>name</th><th>type</th><th>target</th></tr>";
					var randomName = "id" + Math.random();
					
					for( var i=0; i<columns.length; i++ ) {						
						var col = columns[i];
						var index = parseInt(col.COLUMN_INDEX) + 1;
						table = table + "<tr><td class='targetIndex'>" + index + "</td><td>" + col.COLUMN_NAME + "</td><td>" + col.COLUMN_TYPE + "</td>"
										+ "<td><input type='radio' class='radioTarget' name='" + "name" + randomName + "' value='" + i + "'></td></tr>";						
					}
					
					table = table + "</table>";
					
					targetScheme.html(table);
					
					console.log(targetScheme);
					console.log(table);
					
					target.find(".cannot").hide();
					target.find(".schemenTarget").show();		
				}
								
				$(target).attr("source", srcName);				
				if( $(target).hasClass("destination") ) return;
				
				// ���� Ÿ���� ������ ��!
				var targetPoint = plumb.getEndpoints(target).filter( function(item) {			
					if( item.isSource ) return item;			
				});
				targetPoint = targetPoint[0];	// �ϳ��ۿ� �����鼭 �迭�� ���ϵ�...	
				
				// �������� ������ �ִ��� Ȯ���մϴ�.
				var hasConnection  = true;				
				if( typeof targetPoint.connections[0] == "undefined" ) hasConnection = false;
							
				// �ִٸ� Ÿ���� �������� �̵�!
				if ( hasConnection ) {					
					var nextConnection = targetPoint.connections[0];
					target = nextConnection.target;
				} 
			} 
			while( hasConnection );
		}
	});	
	
	plumb.bind("connectionDetached", function(info) {
		
		var target = $(info.target);
		
		// ����Ƽ���̼��̸� �ƹ��͵� �� �ʿ䰡 ����.
		if( $(target).hasClass("destination") ) return;
				
		// �ҽ��� ������ �ִ��� Ȯ���ؾ߰ڼ�.		
		var hasSource = false;		
		if( typeof target.attr("source") != "undefined" ) { // �ҽ� ������ �ִٸ� ���������!			
			hasSource = true;			
		}		
		if( !hasSource ) return; // �ҽ��� ������ �׳� ������ �ȴ�!
		
		// �ҽ��� �ִٸ� �ڷ� ��ȸ�ϸ鼭 �ҽ� ������ �����ؾ���
		do {
			
			$(target).attr("source", "");
			
			// ���� ���͸��̸� ��Ȱ��ȭ ������Ѵٱ�!
			if( $(target).attr("name") == "qf" ) { // �������͸��̶�� ��Ȱ��ȭ ������Ѵٱ�!
					$(".setRules").attr("disabled", true);
					target.find(".cannot").show();
					target.find(".schemenTarget").hide();
			}			
			
			// Ÿ���� �ִ� ���ø� ��Ȱ��ȭ 
			if( $(target).attr("name") == "bf" || $(target).attr("name") == "hs" || $(target).attr("name") == "kf"
					|| $(target).attr("name") == "nrkf" || $(target).attr("name") == "ps" || $(target).attr("name") == "ikf" ) {				
							
				target.find(".cannot").show();
				target.find(".schemenTarget").hide();				
			}
						
			// ���� Ÿ���� ������ ��!
			var targetPoint = plumb.getEndpoints(target);						
			targetPoint = plumb.getEndpoints(target).filter( function(item) {			
				if( item.isSource ) return item;			
			});
			targetPoint = targetPoint[0];	// �ϳ��ۿ� �����鼭 �迭�� ���ϵ�...			
			
			// ���� ���� ����
			var hasConnection  = false;			
			if( typeof targetPoint != "undefined" ) { // ���� �ִ�.				
				// �������� ������ �ִ��� Ȯ���մϴ�.
				if( typeof targetPoint.connections[0] != "undefined" ) hasConnection = true;
			}
			else return;
			
			// ������ ������ ������ �̵��϶�
			if( hasConnection ) {				
				var nextConnection = targetPoint.connections[0];
				target = nextConnection.target;
			}
			
		} while( hasConnection );		 
	});
	
	plumb.bind("connectionMoved", function(info) {
						
		var target = $(info.originalTargetEndpoint.element);
		
		//console.log(target);
		
		// ����Ƽ���̼��̸� �ƹ��͵� �� �ʿ䰡 ����.
		if( $(target).hasClass("destination") ) return;
				
		// �ҽ��� ������ �ִ��� Ȯ���ؾ߰ڼ�.		
		var hasSource = false;		
		if( typeof target.attr("source") != "undefined" ) { // �ҽ� ������ �ִٸ� ���������!			
			hasSource = true;			
		}		
		if( !hasSource ) return; // �ҽ��� ������ �׳� ������ �ȴ�!
		
		// �ҽ��� �ִٸ� �ڷ� ��ȸ�ϸ鼭 �ҽ� ������ �����ؾ���
		do {
			
			$(target).attr("source", "");
			
			// ���� ���͸��̸� ��Ȱ��ȭ ������Ѵٱ�!
			if( $(target).attr("name") == "qf" ) { // �������͸��̶�� ��Ȱ��ȭ ������Ѵٱ�!
					target.find(".setRules").attr("disabled", true);
					target.find(".cannot").show();
					target.find(".schemenTarget").hide();
			}
			
			// Ÿ�� ��Ȱ��ȭ
			if( $(target).attr("name") == "bf" || $(target).attr("name") == "hs" || $(target).attr("name") == "kf"
				|| $(target).attr("name") == "nrkf" || $(target).attr("name") == "ps" || $(target).attr("name") == "ikf" ) {				
						
					target.find(".cannot").show();
					target.find(".schemenTarget").hide();				
			}			
						
			// ���� Ÿ���� ������ ��!
			var targetPoint = plumb.getEndpoints(target);						
			targetPoint = plumb.getEndpoints(target).filter( function(item) {			
				if( item.isSource ) return item;			
			});
			targetPoint = targetPoint[0];	// �ϳ��ۿ� �����鼭 �迭�� ���ϵ�...			
			
			// ���� ���� ����
			var hasConnection  = false;			
			if( typeof targetPoint != "undefined" ) { // ���� �ִ�.				
				// �������� ������ �ִ��� Ȯ���մϴ�.
				if( typeof targetPoint.connections[0] != "undefined" ) hasConnection = true;
			}
			else return;
			
			// ������ ������ ������ �̵��϶�
			if( hasConnection ) {				
				var nextConnection = targetPoint.connections[0];
				target = nextConnection.target;
			}
			
		} while( hasConnection );			
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
				
				itemWrap.removeClass("recommended");
								
				var min = $("<div class='control min'></div>").text("_");
				var del = $("<div class='control del'></div>").text("X");
								
				item.after(min);
				item.after(del);
				
				if( itemWrap.hasClass('source') ) {
					
					plumb.addEndpoint(itemWrap, source);
					recommendedAlgorithm(itemWrap.find(".recommendation"));
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
						
			var origin = $(".drag[name='" + ui.draggable.attr('name') + "']");
			origin.hide();
			origin.addClass("isHidden");		
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
		
		if(itemWrap.hasClass('source')) {			
			$(".itemWrap").removeClass("recommended");			
		}
		
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
	
	
	// �̸� üũ
	$(".mybutton").on('click', function(event) {
		
		var name = $('.myinput').val();
		planNameFlag = false;
		
		if (name == null || name == '') {
			alert("Please input first.");
			return;
		}
		$.ajax({
			type : 'post',
			url : './ajax/check-redundancy.jsp',
			data : ({
				name: name,
				type: 'plan'
			}),
			async: false,
			cache: false,
			success : function(data) {
			  data = data.replace(/(^\s*)|(\s*$)/gi, "");
			  if (data == 'true') {
			  	alert(name + " is available.");
			  	planNameFlag = true;
			  }
			  else
				alert(name + " is already used.");
			}
		});
	});
	
	// �����!
	$(document).on("click", ".mySubmit", function() {
		
		var valid = true;
		var connections = plumb.select();		
		var source = $(".panel").find(".source");		
		var destination = $(".panel").find(".destination");
		
		var message = "������ ���� ������ �÷� ������ �����Ͽ����ϴ�.<ul>";
		var messagePanel = $(".mySubmitFail");		
						
		// �̸� üũ
		if( typeof planNameFlag == "undefined" || planNameFlag == false ) {
			message = message + "<li>�÷� �̸� ����</li>";
			valid = false;
		}
		
		// ���� üũ
		if( connections.length == 0 ) {
			message = message + "<li>������ ����</li>";
			valid = false;
		}				
		// �ҽ� üũ
		if( source.length == 0 ) {			
			message = message + "<li>�ҽ��� ����</li>";
			valid = false;
		} 
		else if ( source.length != 1 ) {
			message = message + "<li>�ҽ��� ����</li>";
			valid = false;
		}		
		// ����Ƽ���̼� üũ
		if( destination.length == 0 ) {			
			message = message + "<li>����Ƽ���̼��� ����</li>";
			valid = false;
		} 
		else if ( destination.length != 1 ) {
			message = message + "<li>����Ƽ���̼��� ����</li>";
			valid = false;
		}		
				
		// �������� ������ �޽����� �Բ� ����Ѵ�!		
		if( !valid ) {				
			message = message + "</ul>";
			messagePanel.show(500);
			messagePanel.html(message);
			return ;
		} else {	
			message = "������ ���� ������ �÷� ������ �����Ͽ����ϴ�.<ul>";
			messagePanel.hide();
			messagePanel.html(message);
		}		
		
		var start = source; // ��� DIV �迭, �ϳ��ۿ� ����.		
		var hasNext = true;
		var next;
		
		// �÷� ������ �ʿ��� ���� //		
		var planName = $(".myinput").val();
		var srcName = source.attr("name");
		var dstName = destination.attr("name");
		
		// �������� �迭�� �ѱ���...��
		var topologies = new Array(); // Topology �迭		
		var index = 0;	
		
		while( hasNext ) {
			
			var nextPoint = plumb.getEndpoints(start).filter( function(item) {			
				if( item.isSource ) return item;			
			});		
			
			if( typeof nextPoint[0] != "undefined" ) {
								
				next = nextPoint[0].connections[0].target; // ���� ��带 �����´�.
				// console.log($(next).attr("class")); // next�� DIV �̹Ƿ� ó���ϸ�ȴ�!
								
				if($(next).hasClass("topology")) { // ������ ���������� ��ǲ�� �����´�.
										
					var parameters = new Object();
					
					if( $(next).attr("name") == "qf" ) {
						
						  var query = $('#builder-basic').queryBuilder('getRules');
						  
						  if ($.isEmptyObject(query)) {
							//alert("Query is empty!");
							//$('#queryBuilderModal').modal('show');
							message = message + "<li>���� ���͸� �Է� ����</li>";
							valid = false;
							break;
						  }
						  else {							  
						    //console.log(JSON.stringify(query, null, 2));
						    //alert(JSON.stringify(query, null, 2));	
						    queryConvert = convertJsonToI2AMQuery(query);
						  	parameters["query"] = queryConvert;
						  }						
					}
					else if( $(next).attr("name") == "bf" || $(next).attr("name") == "hs" || $(next).attr("name") == "kf"
						|| $(next).attr("name") == "nrkf" || $(next).attr("name") == "ps" || $(next).attr("name") == "ikf") {
												
						// ���� �ڽ�
						var radioName = $(next).find(".radioTarget").attr("name");
						var target = $(next).find("input[name='"+ radioName + "']:checked");												
						var targetIndex = target.attr("value"); // String ���·� ��...
									
						// �Է� �Ķ����
						var inputs = $(next).find(".paramInput");						
						
						if( target.length == 0 ) {
							
							valid = false;
							message = message + "<li>Ÿ���� �������� ����</li>"
							break;
						}						
						
						for( var i=0; i<inputs.length; i++ ) {			
							
							if( $(inputs[i]).val() == "" || $(inputs[i]).val() == null ) {																
								valid = false;
								message = message + "<li>�Ķ���� �Է� ����</li>"
								break;
								
							} else {
								parameters[$(inputs[i]).attr("parameter")] = parseInt($(inputs[i]).val());	
							}							
						}						
						parameters["target"] = parseInt(targetIndex);	
						
						if($(next).attr("name") == "nrkf") {														
							var selected = $(".mySelect option:selected").val();
							parameters["measure"] = selected;
						}
						
						
						
					}
					else {
						
						var inputs = $(next).find("input");
												
						for( var i=0; i<inputs.length; i++ ) {			
							
							if( $(inputs[i]).val() == "" || $(inputs[i]).val() == null ) {																
								valid = false;
								message = message + "<li>�Ķ���� �Է� ����</li>"
								break;
								
							} else {
								parameters[$(inputs[i]).attr("parameter")] = parseInt($(inputs[i]).val());	
							}							
						}						
					}				
					
					var topology = {
							topology_index: index,
							topology_type: $(next).attr("name"),
							topology_params: parameters
					}
					
					topologies[index] = topology;
				}
								
				if($(next).hasClass("destination")) hasNext = false; // ����Ƽ���̼��� ������ ����!		
				
				if( typeof next != "undefined" ) { // �������� �̵�! �ε����� ����!			
					start = next;
					hasNext = true;
					index = index + 1;
				}				
			} else {
				hasNext = false;
			}			
		}		
		
		// �������� ������ �޽����� �Բ� ����Ѵ�!		
		if( !valid ) {				
			message = message + "</ul>";
			messagePanel.show(500);
			messagePanel.html(message);
			return ;
		} else {
			message = "";
			messagePanel.hide(500);
			messagePanel.html(message);
		}
		
		// �÷� ����
		var newPlan = {
				planName: planName,
				srcName: srcName,
				dstName: dstName,
				topoloiges: topologies
		}	
				
		console.log(newPlan);
		alert(newPlan);
		
		// �÷� ���� > Ajax
		$.ajax({
			  type: "POST", 
			  url: "ajax/create-plan.jsp",			  
			  data: {newPlan: JSON.stringify(newPlan)},
			  async: false,
			  cache: false,
			  success: function(response) {
				  //alert(response.trim());
				  console.log(response.trim());
				  window.open("./main.jsp", "_self");
			  },
			  error: function() {
			    alert("ERROR");	
			    //window.location.reload();
			  }
		});
		
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

<script>
function recommendedAlgorithm(algorithmName) {
		
	//console.log($(algorithmName).text());
	if( algorithmName == null ) {
		
		alert("����");
		return;
	}	
	
	var algorithm = $(algorithmName).text().replace(/ /g, "");	
	var find;		
	
	switch(algorithm) {	
	case "BINARYBERNOULLISAMPLING": find = "bbs"; break;
	case "HASHSAMPLING": find = "hs"; break;
	case "KSAMPLING": find = "ks"; break;
	case "UCKSAMPLING": find = "ucks"; break;
	case "PRIORITYSAMPLING": find = "ps"; break;
	case "RESERVOIRSAMPLING": find = "rs"; break;
	case "SYSTEMATICSAMPLING": find = "ss"; break;
	case "BLOOMFILTERING": find = "bf"; break;
	case "QUERYFILTERING": find = "qf"; break;
	case "KALMANFILTERING": find = "kf"; break;
	case "NOISERECOMMENDEDKALMANFILTERING": find = "nrkf"; break;
	case "INTELLIGENTKALMANFILTERING": find = "ikf"; break;
	default: find = null;
	}
 	
	$(".itemWrap").removeClass("recommended");
	$("div[name='" + find + "']").addClass("recommended");	
}
</script>


</head>
<body> 
	<div class="myRow">	
		<div class="header">
			<h1>New Plan</h1>			
			<button class="mySubmit" type="button">Submit</button>
			<div class="mySubmitFail"> ���� ����  </div>
			Plan name<br>	
			<input placeholder=" input plan name" class="myinput"><button class="mybutton" type="button">Check</button>				
		</div>										
		<div class="columnWrap">		
			<!-- Source Plan Destination ����Ʈ ǥ���ϴ� ��  -->
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
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			<div class="paramInputLabel">Sample size</div><input class="paramInput" parameter="sample_size"></input><br>
					 			<div class="paramInputLabel">Window size</div><input class="paramInput" parameter="window_size"></input>
				 			</div>
				 		</div>
					</div>
				
					<div class="itemWrap drag topology" name="hs">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Hash Sampling</div>								
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">
								
								<div class="cannot"> Connect with source. </div>
					 			<div class="schemenTarget"></div>
										 							 		
					 			<div class="paramInputLabel">Sample ratio</div><input class="paramInput" parameter="sample_ratio"></input><br>
					 			<div class="paramInputLabel">Window size</div><input class="paramInput" parameter="window_size"></input>
				 			</div>
				 		</div>
					</div>		
					
					<div class="itemWrap drag topology" name="ks">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">K Sampling</div>								
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			<div class="paramInputLabel">Sample rate</div><input class="paramInput" parameter="sample_rate"></input><br>					 			
				 			</div>
				 		</div>
					</div>	
					
					<div class="itemWrap drag topology" name="ucks">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">UC K Sampling</div>								
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			<div class="paramInputLabel">Sample rate</div><input class="paramInput" parameter="sample_rate"></input><br>
					 			<div class="paramInputLabel">UC under bound</div><input class="paramInput" parameter="uc_under_bound"></input>
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="ps">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Priority Sampling</div>								
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			
							
								<div class="cannot"> Connect with source. </div>
					 			<div class="schemenTarget"></div>
							 							 		
					 			<div class="paramInputLabel">Sample size</div><input class="paramInput" parameter="sample_size"></input><br>
					 			<div class="paramInputLabel">Window size</div><input class="paramInput" parameter="window_size"></input>
				 			</div>
				 		</div>
					</div>	

					<div class="itemWrap drag topology" name="rs">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Reservoir Sampling</div>								
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			<div class="paramInputLabel">Sample size</div><input class="paramInput" parameter="sample_size"></input><br>
					 			<div class="paramInputLabel">Window size</div><input class="paramInput" parameter="window_size"></input>
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="ss">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Systematic Sampling</div>								
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">			 							 		
					 			<div class="paramInputLabel">Interval</div><input class="paramInput" parameter="interval"></input><br>					 			
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="bf">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Bloom Filtering</div>								
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">		
							
								<div class="cannot"> Connect with source. </div>
								<div class="schemenTarget"></div>
								
					 			<div class="paramInputLabel">Bucket size</div><input class="paramInput" parameter="bucket_size"></input><br>
					 			<div class="paramInputLabel">Keywords</div><input class="paramInput" parameter="keywords"></input>
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="qf">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Query Filtering</div>								
							<div class="control"></div>
						</div>	
						<div class="params query">		
							<div class="paramsInput">			 							 		
					 			<!-- <button class="btn btn-large btn-primary" href="#queryBuilderModal" data-toggle="modal">Set rules</button>  -->
					 			<div class="cannot"> Connect with source. </div>
					 			<div class="schemenTarget"></div>
					 			<button class="setRules" href="#queryBuilderModal" data-toggle="modal" disabled>Set rules</button>
				  				<!--  <button class="btn btn-large btn-success" id="btn-get-rules" data-target="basic">Get rules</button>  -->
				  				<!-- <button class="btn btn-success" id="btn-get-i2am-query" data-target="basic">I2AM query</button> -->
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="kf">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Kalman Filtering</div>								
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">	
								
								<div class="cannot"> Connect with source. </div>
					 			<div class="schemenTarget"></div>							
									 							 		
					 			<div class="paramInputLabel">A value</div><input class="paramInput" parameter="a_value" value=1.0></input><br>
					 			<div class="paramInputLabel">Q value</div><input class="paramInput" parameter="q_value" value=0.01></input>
					 			<div class="paramInputLabel">H value</div><input class="paramInput" parameter="h_value" value=1.0></input>
					 			<div class="paramInputLabel">Initial X value</div><input class="paramInput" parameter="x_value" value=0.0></input>
					 			<div class="paramInputLabel">Initial P value</div><input class="paramInput" parameter="p_value" value=1000.0></input>
					 			<div class="paramInputLabel">R value</div><input class="paramInput" parameter="r_value" value=0.5></input>
				 			</div>
				 		</div>
					</div>
						
					<div class="itemWrap drag topology" name="nrkf">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Noise Recommend Kalman Filtering</div>								
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">
							
								<div class="cannot"> Connect with source. </div>
					 			<div class="schemenTarget"></div>
										 							 		
					 			<div class="paramInputLabel">A value</div><input class="paramInput" parameter="a_value" value=1.0></input><br>
					 			<div class="paramInputLabel">Q value</div><input class="paramInput" parameter="q_value" value=0.01></input>
					 			<div class="paramInputLabel">H value</div><input class="paramInput" parameter="h_value" value=1.0></input>
					 			<div class="paramInputLabel">Initial X value</div><input class="paramInput" parameter="x_value" value=0.0></input>
					 			<div class="paramInputLabel">Initial P value</div><input class="paramInput" parameter="p_value" value=1000.0></input>
					 			<div class="selectLabel">Recommended Measure</div>
					 				<center>
					 				<select class="mySelect">
					 					<option value="wt" selected>Wavelet Transform</option>
					 					<option value="ma">Moving Average</option>
					 				</select>
					 				</center>					 			
					 							 		
				 			</div>
				 		</div>
					</div>
					
					<div class="itemWrap drag topology" name="ikf">
						<div class="item">			
							<i class='fa fa-cog type topologyType'></i>
							<div class="name">Intelligent Kalman Filtering</div>								
							<div class="control"></div>
						</div>	
						<div class="params">		
							<div class="paramsInput">
							
								<div class="cannot"> Connect with source. </div>
					 			<div class="schemenTarget"></div>
										 							 		
					 			<div class="paramInputLabel">A value</div><input class="paramInput" parameter="a_value" value=1.0></input><br>
					 			<div class="paramInputLabel">Q value</div><input class="paramInput" parameter="q_value" value=0.01></input>
					 			<div class="paramInputLabel">H value</div><input class="paramInput" parameter="h_value" value=1.0></input>
					 			<div class="paramInputLabel">Initial X value</div><input class="paramInput" parameter="x_value" value=0.0></input>
					 			<div class="paramInputLabel">Initial P value</div><input class="paramInput" parameter="p_value" value=1000.0></input>
					 						 		
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
	    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">��</button>
	    <h3 id="queryBuilderModalLabel">Build Query</h3>
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
function itemFilter() {
	
	var selected = $(this);
	
	var sources = $(".source.drag");
	var topologies = $(".topology.drag");
	var destinations = $(".destination.drag");
	
	sources.hide();
	topologies.hide();
	destinations.hide();	
	
	if(selected.hasClass("filterActive")) {	// ���� ����
		selected.removeClass("filterActive");
		sources.show();
		topologies.show();
		destinations.show();		
	}
	else {		
		$(".filterActive").removeClass("filterActive"); // �̹� üũ�� �� ����.
		selected = $(this).addClass("filterActive");		
		var filter = selected.attr('id');			
		//console.log(filter);
		$("." + filter).show();		
	}	
	$(".isHidden").hide();	
}
</script>

<script>
$(".filter").on("click", itemFilter)
</script>

</html>