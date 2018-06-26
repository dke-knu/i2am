
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>I2AM</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">


	<script src="http://code.jquery.com/jquery-1.7.2.min.js"></script>
	<script src="./js/bootstrap.js"></script>
	<script src="./js/my.js"></script>
	<script>
	  $(document).ready(function() {
		  checkLogin();
		  
		  $('#btn-new-src').click(function(e) {
		    window.open("./src.jsp", "_self");
		  }) 
		  
		  $('#btn-new-dst').click(function(e) {
		    window.open("./dst.jsp", "_self");
		  })
		  
		  $('#btn-new-plan').click(function(e) {
		    window.open("./plan.jsp", "_self");
		  })
		  
		  function loadTblSrc() {
	    	var list = null;
	    	var tbl = $('#tbl-src').children('tbody');
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
							"<div class='btn-group'>" +
								"<button class='btn btn-mini dropdown-toggle' data-toggle='dropdown'>" + 
									obj.STATUS + "<span class='caret'></span>" + 
								"</button>" +
								"<ul class='dropdown-menu'>" +
									"<li><a href='#' onclick=\"changeState('src', '" + obj.NAME + "', '" + after + "');\">" + after + "</a></li>" +
								"</ul>" +
							"</div>" +
						"</td>" +
					"</tr>"
				);				
			}
		  }
	      loadTblSrc();
		  
		  function loadTblDst() {
	    	var list = null;
	    	var tbl = $('#tbl-dst').children('tbody');
	    	tbl.html("");
			$.ajax({
				type : 'post',
				url : './ajax/get-list-dst.jsp',
				data : ({}),
				async: false,
				cache: false,
				success : function(data) {
					data = data.replace(/(^\s*)|(\s*$)/gi, "");
					list = data;
				} 
			});
			
			if (list == null)	{
				alert("Dst cannot load.");
				return ;
			}
			
			var arr = JSON.parse(list);
			
			if (arr.length > 0)	
			while (arr.length > 0) {
				var obj = arr.pop();
				var after = obj.STATUS=="ACTIVE"?"DEACTIVE":"ACTIVE";
				tbl.html(tbl.html() +
					"<tr>" +
						"<td>" + obj.NAME + "</td>" +
						"<td>" + obj.CREATED_TIME + "</td>" +
						"<td>" + 
							"<div class='btn-group'>" +
								"<button class='btn btn-mini dropdown-toggle' data-toggle='dropdown'>" + 
									obj.STATUS + "<span class='caret'></span>" + 
								"</button>" +
								"<ul class='dropdown-menu'>" +
									"<li><a href='#' onclick=\"changeState('dst', '" + obj.NAME + "', '" + after + "');\">" + after + "</a></li>" +
								"</ul>" +
							"</div>" +
						"</td>" +
					"</tr>"
				);				
			}
		  }
		  loadTblDst();
		  
		  function loadTblPlan() {
	    	var list = null;
	    	var tbl = $('#tbl-plan').children('tbody');
	    	tbl.html("");
			$.ajax({
				type : 'post',
				url : './ajax/get-list-plan.jsp',
				data : ({}),
				async: false,
				cache: false,
				success : function(data) {
					data = data.replace(/(^\s*)|(\s*$)/gi, "");
					list = data;
				} 
			});
			
			if (list == null)	{
				alert("Plan cannot load.");
				return ;
			}
			
			var arr = JSON.parse(list);
			
			if (arr.length > 0)	
			while (arr.length > 0) {
				var obj = arr.pop();
				var after = obj.STATUS=="ACTIVE"?"DEACTIVE":"ACTIVE";
				tbl.html(tbl.html() +
					"<tr>" +
						"<td>" + obj.NAME + "</td>" +
						"<td>" + obj.CREATED_TIME + "</td>" +
						"<td>" + 
							"<div class='btn-group'>" +
								"<button class='btn btn-mini dropdown-toggle' data-toggle='dropdown'>" + 
									obj.STATUS + "<span class='caret'></span>" + 
								"</button>" +
								"<ul class='dropdown-menu'>" +
									"<li><a href='#' onclick=\"changeState('plan', '" + obj.NAME + "', '" + after + "');\">" + after + "</a></li>" +
								"</ul>" +
							"</div>" +
						"</td>" +
						"<td><a href='http://114.70.235.43:3000/dashboard/db/kafka_monitoring?orgId=1&var-topic=" + obj.INPUT + "&var-topic2=" + obj.OUTPUT + "'>" +
							"<button class='btn btn-mini'>View</button></a></td>" +
					"</tr>"
				);				
			}
		  }
		  loadTblPlan();
	  })

	  function changeState(type, name, after) {
		$.ajax({
			type : 'post',
			url : './ajax/change-status.jsp',
			data : ({
			  type: type,
			  name: name,
			  after: after
			}),
			success : function(response) {
				  //alert(response.trim());
				  console.log(response.trim());
				  if (response.trim() == "true") {
					  window.open("./list.jsp", "_self");
				  } else {
					  window.location.reload();
				  }
			} 
		});
	  }	
	</script>
    <link href="./css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
      body {
        padding-top: 40px;
        padding-bottom: 40px;
        background-color: #f5f5f5;
      }

      .form-mgr {
        max-width: 800px;
        padding: 19px 29px 29px;
        margin: 0 auto 20px;
        background-color: #fff;
        border: 1px solid #e5e5e5;
        -webkit-border-radius: 5px;
           -moz-border-radius: 5px;
                border-radius: 5px;
        -webkit-box-shadow: 0 1px 2px rgba(0,0,0,.05);
           -moz-box-shadow: 0 1px 2px rgba(0,0,0,.05);
                box-shadow: 0 1px 2px rgba(0,0,0,.05);
      }
      .form-mgr .form-mgr-heading,
      .form-mgr .checkbox {
        margin-bottom: 10px;
      }
      .form-mgr input[type="text"],
      .form-mgr input[type="password"] {
        font-size: 16px;
        height: auto;
        margin-bottom: 15px;
        padding: 7px 9px;
      }

    </style>
    <link href="./css/bootstrap-responsive.css" rel="stylesheet">

    <!-- HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="./js/html5shiv.js"></script>
    <![endif]-->

    <!-- Fav and touch icons -->
    <link rel="apple-touch-icon-precomposed" sizes="144x144" href="./ico/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114" href="./ico/apple-touch-icon-114-precomposed.png">
      <link rel="apple-touch-icon-precomposed" sizes="72x72" href="./ico/apple-touch-icon-72-precomposed.png">
                    <link rel="apple-touch-icon-precomposed" href="./ico/apple-touch-icon-57-precomposed.png">
                                   <link rel="shortcut icon" href="./ico/favicon.png">
  </head>

  <body>
	
    <div class="container">
    	<div class="form-mgr">
        	<div class="form-mgr-heading">
        	  <div class="row show-grid">
        	    <h1 style="display: inline;" class="span6">Source</h1>
        	    <div class="span2"><button class="btn btn-large btn-danger" type="button" id="btn-new-src">New source</button></div>
        	  </div>
        	</div>
        	<table class="table table-striped" id="tbl-src">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Created time</th>
                  <th>Intelligent engine</th>
                  <!-- <th>Load shedding engine</th>  -->
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
              </tbody>
            </table>
        </div>
    </div> <!-- /container -->

    <div class="container">
    	<div class="form-mgr">
        	<div class="form-mgr-heading">
        	  <div class="row show-grid">
        	    <h1 style="display: inline;" class="span6">Destination</h1>
        	    <div class="span2"><button class="btn btn-large btn-danger" type="button" id="btn-new-dst">New destination</button></div>
        	  </div>
        	</div>
    	    <table class="table table-striped" id="tbl-dst">
              <thead>
                <tr> 
                  <th>Name</th>
                  <th>Created time</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
              </tbody>
            </table>
        </div>
    </div> <!-- /container -->

    <div class="container">
    	<div class="form-mgr">
        	<div class="form-mgr-heading">
        	  <div class="row show-grid">
        	    <h1 style="display: inline;" class="span6">Plan</h1>
        	    <div class="span2"><button class="btn btn-large btn-danger" type="button" id="btn-new-plan">New plan</button></div>
        	  </div>
        	</div>
        	<table class="table table-striped" id="tbl-plan">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Created time</th>
                  <th>Status</th>
                  <th>Monitoring</th>
                </tr>
              </thead>
              <tbody>
              </tbody>
            </table>
        </div>
    </div> <!-- /container -->
  </body>
</html>
