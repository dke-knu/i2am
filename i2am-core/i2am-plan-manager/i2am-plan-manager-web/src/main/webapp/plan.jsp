
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
		
		$('#btn-plan-submit').click(function(e) {
			var action = $("#form-plan").attr("action");
			  
			var planName = $('#plan-name').val();
			if (planName == null || planName == '') {
			  alert("Please input plan name first.");
			  return false;
			}
			
			var srcName;
			if ( $('#parent-of-src').children(".active").length <= 0 ) {
				alert("Please select a source first.");
				return false;
			} else {
				srcName = $('#parent-of-src').children(".active").children().html();
			}
			
			var dstName;
			if ( $('#parent-of-dst').children(".active").length <= 0 ) {
				alert("Please select a destination first.");
				return false;
			} else {
				dstName = $('#parent-of-dst').children(".active").children().html();
			}
			
			var algorithmType;
			var sampleSize;
			var sampleRatio;
			var windowSize;
			var hashFunction;
			var bucketSize;
			if ( $("#bbs").hasClass('active') ) {
				algorithmType = "BINARY_BERNOULLI_SAMPLING";
				sampleSize = $("#sample-size-for-BBS").val();
				windowSize = $("#window-size-for-BBS").val();
			}
			else if ( $("#hs").hasClass('active') ) {
				algorithmType = "HASH_SAMPLING";
				sampleSize = $("#sample-size-for-HS").val();
				windowSize = $("#window-size-for-HS").val();
				hashFunction = $("#hash-function-for-HS").val();
				bucketSize = $("#bucket-size-for-HS").val();
			}
			else if ( $("#ps").hasClass('active') ) {
				algorithmType = "PRIORITY_SAMPLING";
				sampleSize = $("#sample-size-for-PS").val();
				windowSize = $("#window-size-for-PS").val();
			}
			else if ( $("#rs").hasClass('active') ) {
				algorithmType = "RESERVOIR_SAMPLING";
				sampleSize = $("#sample-size-for-RS").val();
				windowSize = $("#window-size-for-RS").val();
			}
			else if ( $("#sts").hasClass('active') ) {
				algorithmType = "STRATIFIED_SAMPLING";
				sampleSize = $("#sample-size-for-STS").val();
				windowSize = $("#window-size-for-STS").val();
			}
			else if ( $("#sys").hasClass('active') ) {
				algorithmType = "SYSTEMATIC_SAMPLING";
				sampleRatio = $("#sample-ratio-for-SYS").val();
				windowSize = $("#window-size-for-SYS").val();
			} else {
				alert("Please select a algorithm first.");
				return false;
			}
			
			var form_data = {
			  plan_name: planName,
			  src_name: srcName,
			  dst_name: dstName,
			  algorithm_type: algorithmType,
			  sample_size: sampleSize,
			  sample_ratio: sampleRatio,
			  window_size: windowSize,
			  hash_function: hashFunction,
			  bucket_size: bucketSize
			};
			
			$.ajax({
			  type: "POST", 
			  url: action,
			  data: form_data,
			  async: false,
			  cache: false,
			  success: function(response) {
				  /*
			    if (response.trim() == "true") {
				  window.open("./list.jsp", "_self");
			    } else {
				  window.location.reload();
			    } 
				  */
				  alert(response.trim());
				  console.log(response.trim());
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
          <h1 class="form-dst-heading">New plan</h1>
          <hr />
          <form id="form-plan" action="ajax/create-plan.jsp" method="post">
			<div class="input-append">
			  <input placeholder="Plan name" type="text" id="plan-name">
			  <button class="btn" type="button" id="btn-check-redundancy">Check</button>
			</div>
		    <div>
		      <h4>Your sources</h4> 
              <ul class="nav nav-pills" id="parent-of-src">
                <li><a href="#" data-toggle="tab">Src-1</a></li>
                <li><a href="#" data-toggle="tab">Src-2</a></li>
              </ul>
            
		      <h4>Your destinations</h4>
              <ul class="nav nav-pills" id="parent-of-dst">
                <li><a href="#" data-toggle="tab">Dst-1</a></li>
                <li><a href="#" data-toggle="tab">Dst-2</a></li>
              </ul>
             
		      <h4>Algorithms</h4>
              <ul class="nav nav-pills hero-unit" style="padding: 10px;">
                <li id="bbs"><a href="#tab1-1" data-toggle="tab">Binary Bernoulli Sampling</a></li>
                <li id="hs"><a href="#tab1-2" data-toggle="tab">Hash Sampling</a></li>
                <li id="ps"><a href="#tab1-3" data-toggle="tab">Priority Sampling</a></li>
                <li id="rs"><a href="#tab1-4" data-toggle="tab">Reservoir Sampling</a></li>
                <li id="sts"><a href="#tab1-5" data-toggle="tab">Stratified Sampling</a></li>
                <li id="sys"><a href="#tab1-6" data-toggle="tab">Systematic Sampling</a></li>
              </ul>

              <div id="tab-src-content" class="tab-content">
                <div class="tab-pane fade" id="tab1-1">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="sample-size-for-BBS">Sample Size</label>
				      <div class="controls">
  				       <input type="text" id="sample-size-for-BBS">
				      </div>
				    </div>
				    <div class="control-group">
  				      <label class="control-label" for="window-size-for-BBS">Window Size</label>
				      <div class="controls">
				        <input type="text" id="window-size-for-BBS">
				      </div>
				    </div>
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab1-2">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="sample-ratio-for-HS">Sample Size</label>
				      <div class="controls">
  				       <input type="text" id="sample-ratio-for-HS">
				      </div>
				    </div>
				    <div class="control-group">
  				      <label class="control-label" for="window-size-for-HS">Window Size</label>
				      <div class="controls">
				        <input type="text" id="window-size-for-HS">
				      </div>
				    </div>
				    <div class="control-group">
  				      <label class="control-label" for="hash-function-for-HS">Hash Function</label>
				      <div class="controls">
				        <input type="text" id="hash-function-for-HS">
				      </div>
				    </div>
				    <div class="control-group">
  				      <label class="control-label" for="bucket-size-for-HS">Bucket Size</label>
				      <div class="controls">
				        <input type="text" id="bucket-size-for-HS">
				      </div>
				    </div>
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab1-3">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="sample-size-for-PS">Sample Size</label>
				      <div class="controls">
  				       <input type="text" id="sample-size-for-RS">
				      </div>
				    </div>
				    <div class="control-group">
  				      <label class="control-label" for="window-size-for-PS">Window Size</label>
				      <div class="controls">
				        <input type="text" id="window-size-for-RS">
				      </div>
				    </div>
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab1-4">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="sample-size-for-RS">Sample Size</label>
				      <div class="controls">
  				       <input type="text" id="sample-size-for-RS">
				      </div>
				    </div>
				    <div class="control-group">
  				      <label class="control-label" for="window-size-for-RS">Window Size</label>
				      <div class="controls">
				        <input type="text" id="window-size-for-RS">
				      </div>
				    </div>
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab1-5">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="sample-size-for-StS">Sample Size</label>
				      <div class="controls">
  				       <input type="text" id="sample-size-for-StS">
				      </div>
				    </div>
				    <div class="control-group">
  				      <label class="control-label" for="window-size-for-StS">Window Size</label>
				      <div class="controls">
				        <input type="text" id="window-size-for-StS">
				      </div>
				    </div>
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab1-6">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="sample-ratio-for-SyS">Sample Ratio</label>
				      <div class="controls">
  				       <input type="text" id="sample-ratio-for-SyS">
				      </div>
				    </div>
				    <div class="control-group">
  				      <label class="control-label" for="window-size-for-SyS">Window Size</label>
				      <div class="controls">
				        <input type="text" id="window-size-for-SyS">
				      </div>
				    </div>
				  </div>
                </div>
              </div>                            
            </div>      
                  
            <hr />
		    <button class="btn btn-large btn-primary" type="submit" id="btn-plan-submit">Submit</button>
		  </form>
        </div>
    </div> <!-- /container -->
  </body>
</html>
