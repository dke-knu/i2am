
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
		    
	    $('#tbl-test-data').on('click', '.clickable-row', function(event) { //Choose
		  if($(this).hasClass('choosed')){
		    $(this).removeClass('choosed'); 
		    $(this).css("border", "");
		  } else {
		    $(this).addClass('choosed').siblings().removeClass('choosed');
		    $(this).css("border", "solid DodgerBlue").siblings().css("border", "");
		  }
		});
	    
	    $('#btn-choose').on('click', function(event) { // Select
	      if($('.choosed').html() != null) {
	   	    $('#choosed-test-data').html( $('.choosed').children('.td-name').html() );
	   	    $('#choosed-test-data').val( $('.choosed').children('.td-name').html() );
	      } else {
	    	$('#choosed-test-data').html( "Not choosed yet." );
	    	$('#choosed-test-data').val('');
	      }
		});
	    
	    $('#btn-upload-test-file').on('click', function(event) { //
          var formData = new FormData();
          formData.append('uploadFile', $('uploadFile')[0].files[0]);
           
          $.ajax({
            type : 'post',
            url : '/test.jsp',
            data : formData,
            dataType : 'json',
            processData : false,
            contentType : false,
            success : function(json) {
              alert(json);
            },
            error: function(xhr, status, error){
              alert(error);
            }
          })
	    });
	    
		$('#btn-upload').click(function(e) { //ADD
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
	    
		$('#btn-for-modal').click(function(e) {
		  reloadTblTestData();
	    })	
	    
		$('#btn-tab3').click(function(e) { 
		  if($(this).parent().hasClass('active')) {
		    $(this).parent().removeClass('active');
		    $('#tab3').removeClass('active');
		  } else {
			$(this).parent().addClass('active');
			$('#tab3').addClass('active');
		  }
	    })	    
	    
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
		
		$('#btn-src-submit').click(function(e) {
			  var action = $("#form-src").attr("action");
			  
			  var srcName = $('#src-name').val();
			  if (srcName == null || srcName == '') {
				alert("Please input source name first.");
				return false;
			  }
			  if ( $("#customizing").hasClass('active') ) {
				  srcType = "CUSTOM";
			  } else if ( $("#kafka").hasClass('active') ) {
				  srcType = "KAFKA";
			  } else if ( $("#database").hasClass('active') ) {
				  srcType = "DATABASE";  
			  } else {
				  alert("Please select source type first.");
				  return false;
			  }
			  var usesIntelligentEngine = $('#intelligent-engine').hasClass('active');
			  var testDataName = '';
			  if ( usesIntelligentEngine ) {
				  if ($('#choosed-test-data').val() == '') {
					  alert("Please select test data first.");
					  return false;
				  }
				  testDataName = $('#choosed-test-data').val();
			  }
			  
			  var form_data = {
					  src_name: $('#src-name').val(),
					  src_type: srcType,
					  uses_intelligent_engine: usesIntelligentEngine,
					  test_data_name: testDataName,

					  zookeeper_ip: $("#zookeeper-ip").val(),
					  zookeeper_port: $("#zookeeper-port").val(),
					  kafka_topic: $("#kafka-topic").val(),
					  
					  database_ip: $("#database-ip").val(),
					  database_port: $("#database-port").val(),
					  database_id: $("#database-id").val(),
					  database_pw: $("#database-pw").val(),
					  database_name: $("#database-name").val(),
					  database_query: $("#database-query").val()
			  };
			  
			  $.ajax({
				  type: "POST",
				  url: action,
				  data: form_data,
				  async: false,
				  cache: false,
				  success: function(response) {
					  //alert(response.trim());
					  console.log(response.trim());
					  window.open("./list.jsp", "_self");
				  }, 
				  error: function() {
				      alert("ERROR");	
					  //window.location.reload();
				  }
			  })
			  e.stopImmediatePropagation();
			  return false;
		  })
	  })
	</script>
    <link href="./css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
      body {
        padding-top: 40px;
        padding-bottom: 40px;
        background-color: #f5f5f5;
      }

      .form-src {
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
      .form-src .form-src-heading,
      .form-src .checkbox {
        margin-bottom: 10px;
      }
      .form-src input[type="text"],
      .form-src input[type="password"] {
        /*font-size: 16px;
        height: auto;
        margin-bottom: 15px;
        padding: 7px 9px;*/
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
    	<div class="form-src">
          <h1 class="form-src-heading">New source</h1>
          <hr />
          <form id="form-src" action="ajax/create-src.jsp" method="post">
			<div class="input-append">
			  <input placeholder="Source name" type="text" id="src-name">
			  <button class="btn" type="button" id="btn-check-redundancy">Check</button>
			</div>
		    <div>
              <ul id="tab-src" class="nav nav-pills">
                <li class="active" id="well-defined"><a href="#tab1-1" data-toggle="tab">Well-defined source</a></li>
                <li id="customizing"><a href="#tab1-2" data-toggle="tab">Customizing source</a></li>
              </ul>
              <div id="tab-src-content" class="tab-content">
                <div class="tab-pane fade in active" id="tab1-1">
				  <ul id="tab-well-defined-src" class="nav nav-pills">
                    <li id="kafka"><a href="#tab2-1" data-toggle="tab">Kafka</a></li>
                    <li id="database"><a href="#tab2-2" data-toggle="tab">Database</a></li>
                  </ul>
                  <div id="tab-well-defined-src-content" class="tab-content">
                    <div class="tab-pane fade in" id="tab2-1">
                   	  <div class="form-horizontal">
				        <div class="control-group">
				          <label class="control-label" for="zookeeper-ip">Zookeeper IP</label>
				          <div class="controls">
  				            <input type="text" id="zookeeper-ip">
				          </div>
				        </div>
				        <div class="control-group">
  				          <label class="control-label" for="zookeeper-port">Zookeeper Port</label>
				          <div class="controls">
				            <input type="text" id="zookeeper-port">
				          </div>
				        </div>
				        <div class="control-group">
  				          <label class="control-label" for="kafka-topic">Kafka Topic</label>
				          <div class="controls">
				            <input type="text" id="kafka-topic">
				          </div>
				        </div>
				      </div>
                    </div>
                    <div class="tab-pane fade" id="tab2-2">
                   	  <div class="form-horizontal">
				        <div class="control-group">
				          <label class="control-label" for="database-ip">Database IP</label>
				          <div class="controls">
  				            <input type="text" id="database-ip">
				          </div>
				        </div>
				        <div class="control-group">
  				          <label class="control-label" for="database-port">Database Port</label>
				          <div class="controls">
				            <input type="text" id="database-port">
				          </div>
				        </div>
				        <div class="control-group">
  				          <label class="control-label" for="database-id">Database ID</label>
				          <div class="controls">
				            <input type="text" id="database-id">
				          </div>
				        </div>
				        <div class="control-group">
  				          <label class="control-label" for="database-pw">Database Password</label>
				          <div class="controls">
				            <input type="text" id="database-pw">
				          </div>
				        </div>
				        <div class="control-group">
  				          <label class="control-label" for="database-name">Database Name</label>
				          <div class="controls">
				            <input type="text" id="database-name">
				          </div>
				        </div>
				        <div class="control-group">
  				          <label class="control-label" for="database-query">Query</label>
				          <div class="controls">
				            <input type="text" id="database-query" class="input-xxlarge">
				          </div>
				        </div>
				      </div>
                    </div>
                  </div>
                </div>
                <div class="tab-pane fade" id="tab1-2">
                  <ul class="nav nav-pills">                
		            <li><a href="resources/I2AM.jar" class="nav nav-pills">Download java interface</a></li> 
		          </ul>
                </div>
              </div>
            </div>
			<ul class="nav nav-pills">
			  <li id="intelligent-engine"><a href="#tab3" id="btn-tab3">Intelligent engine</a></li>
			</ul>
			<div id="tab-well-defined-src-content" class="tab-content">
			  <div class="tab-pane fade in" id="tab3" style="margin-bottom: 20px">
			    <a id="btn-for-modal" href="#test-data" role="button" class="btn btn-small" data-toggle="modal">Choose...</a> 
			    <b id="choosed-test-data">Not choosed yet.</b>
			  </div>
			</div>
      
            <hr />
		    <button class="btn btn-large btn-primary" type="submit" id="btn-src-submit">Submit</button>
		  </form>
        </div>
    </div> <!-- /container -->
    
    <div id="test-data" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
      <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
        <h3 id="myModalLabel">Choose your test data.</h3>
      </div>
      <div class="modal-body">
        <table class="table table-hover" id="tbl-test-data">
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
        <hr />
        <form class="navbar-form pull-left" name="testForm" id="testForm">
          <input type="text" placeholder="Test data name" id="uploadName">
	      <input type="file" class="input-medium" name="uploadFile" id="uploadFile" accept=".csv,.json,.xml"/>
	      <button class="btn" id="btn-upload">Add data</button>
		</form>
      </div> 
      <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true" id="btn-choose">Save changes</button>
      </div>
    </div>
  </body>
</html>
