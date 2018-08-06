
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
	    
	    $('#tbl-test-data').on('click', '.clickable-row', function(event) {
		  if($(this).hasClass('choosed')){
		    $(this).removeClass('choosed'); 
		    $(this).css("border", "");
		  } else {
		    $(this).addClass('choosed').siblings().removeClass('choosed');
		    $(this).css("border", "solid DodgerBlue").siblings().css("border", "");
		  }
		});
	    
	    $('#btn-choose').on('click', function(event) {
	      if($('.choosed').html() != null) {
	   	    $('#choosed-test-data').html( $('.choosed').children('.td-name').html() );
	      } else {
	    	$('#choosed-test-data').html( "Not choosed yet." );
	      }
		});
	    
	    $('#btn-upload-test-file').on('click', function(event) {
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
	    })
		
		$('#btn-dst-submit').click(function(e) {
			  var action = $("#form-dst").attr("action");
			  
			  var dstName = $('#dst-name').val();
			  if (dstName == null || dstName == '') {
				alert("Please input destination name first.");
				return false;
			  }
			  
			  if ( $("#customizing").hasClass('active') ) {
				  dstType = "CUSTOM";
			  } else if ( $("#kafka").hasClass('active') ) {
				  dstType = "KAFKA";
			  } else if ( $("#database").hasClass('active') ) {
				  dstType = "DATABASE";  
			  } else {
				  alert("Please select destination type first.");
				  return false;
			  }
			  
			  var form_data = {
					  dst_name: $('#dst-name').val(),
					  dst_type: dstType,

					  zookeeper_ip: $("#zookeeper-ip").val(),
					  zookeeper_port: $("#zookeeper-port").val(),
					  kafka_topic: $("#kafka-topic").val(),
					  
					  database_ip: $("#database-ip").val(),
					  database_port: $("#database-port").val(),
					  database_id: $("#database-id").val(),
					  database_pw: $("#database-pw").val(),
					  database_db: $("#database-db").val(),
					  database_table: $("#database-table").val()
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

      .form-dst {
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
      .form-dst .form-dst-heading,
      .form-dst .checkbox {
        margin-bottom: 10px;
      }
      .form-dst input[type="text"],
      .form-dst input[type="password"] {
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
    	<div class="form-dst">
          <h1 class="form-dst-heading">New destination</h1>
          <hr />
          <form id="form-dst" action="ajax/create-dst.jsp" method="post">
			<div class="input-append">
			  <input placeholder="Destination name" type="text" id="dst-name">
			  <button class="btn" type="button" id="btn-check-redundancy">Check</button>
			</div>
		    <div>
              <ul id="tab-dst" class="nav nav-pills">
                <li class="active"><a href="#tab1-1" data-toggle="tab">Well-defined destination</a></li>
                <li id="customizing"><a href="#tab1-2" data-toggle="tab">Customizing destination</a></li>
              </ul>
              <div id="tab-dst-content" class="tab-content">
                <div class="tab-pane fade in active" id="tab1-1">
				  <ul id="tab-well-defined-dst" class="nav nav-pills">
                    <li id="kafka"><a href="#tab2-1" data-toggle="tab">Kafka</a></li>
                    <li id="database"><a href="#tab2-2" data-toggle="tab">Database</a></li>
                  </ul>
                  <div id="tab-well-defined-dst-content" class="tab-content">
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
  				          <label class="control-label" for="database-db">Database Name</label>
				          <div class="controls">
				            <input type="text" id="database-db">
				          </div>
				        </div>
				        <div class="control-group">
  				          <label class="control-label" for="database-table">Table Name</label>
				          <div class="controls">
				            <input type="text" id="database-table">
				          </div>
				        </div>
				      </div>
                    </div>
                  </div>
                </div>
                <div class="tab-pane fade" id="tab1-2">
		          <a href="resources/CustomProducer.java">Download java interface</a> 
                </div>
              </div>
            </div>
            
            <hr />
		    <button class="btn btn-large btn-primary" type="submit" id="btn-dst-submit">Submit</button>
		  </form>
        </div>
    </div> <!-- /container -->
  </body>
</html>
