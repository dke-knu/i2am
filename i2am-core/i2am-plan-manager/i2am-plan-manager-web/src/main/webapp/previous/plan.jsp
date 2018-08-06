
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
		  
		function loadSrcList() {
	      var list = null;
	      var parent = $('#parent-of-src');
	      parent.html("");
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
			
		  if (arr.length > 0)	
		  while (arr.length > 0) {
			  var obj = arr.pop(); 
			  var recommended = obj.RECOMMENDED_SAMPLING;
			  parent.html(parent.html() +
					  "<li><a href='#' onclick=\"showRecommendModal('" + recommended + "');\" data-toggle='tab'>" + obj.NAME + "</a></li>"
			  );				
		  }
		}
		loadSrcList();
		  
		function loadDstList() {
	      var list = null;
	      var parent = $('#parent-of-dst');
	      parent.html("");
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
			  parent.html(parent.html() +
					  "<li><a href='#' data-toggle='tab'>" + obj.NAME + "</a></li>"
			  );				
		  }
		}
		loadDstList();
		
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
			var keywords;
			var ucUnderBound;
			var qVal;
			var rVal;
			if ( $("#bbs").hasClass('active') ) {
				algorithmType = "BINARY_BERNOULLI_SAMPLING";
				sampleSize = $("#sample-size-for-BBS").val();
				windowSize = $("#window-size-for-BBS").val();
			}
			else if ( $("#hs").hasClass('active') ) {
				algorithmType = "HASH_SAMPLING";
				sampleRatio = $("#sample-ratio-for-HS").val();
				windowSize = $("#window-size-for-HS").val();
				hashFunction = $("#hash-function-for-HS").val();
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
				sampleSize = $("#sample-size-for-StS").val();
				windowSize = $("#window-size-for-StS").val();
			}
			else if ( $("#sys").hasClass('active') ) {
				algorithmType = "SYSTEMATIC_SAMPLING";
				sampleRatio = $("#interval-for-SyS").val();
			}
			else if ( $("#ks").hasClass('active') ) {
				algorithmType = "K_SAMPLING";
				sampleRatio = $("#sample-ratio-for-KS").val();
			}
			else if ( $("#ucks").hasClass('active') ) {
				algorithmType = "UC_K_SAMPLING";
				sampleRatio = $("#sample-ratio-for-UCKS").val();
				ucUnderBound = $("#uc-under-bound-for-UCKS").val();
			}
			else if ( $("#qf").hasClass('active') ) {
				algorithmType = "QUERY_FILTERING";
				keywords = $("#keywords-for-QF").val();
			}
			else if ( $("#blf").hasClass('active') ) {
				algorithmType = "BLOOM_FILTERING";
				bucketSize = $("#bucket-size-for-BlF").val();
				keywords = $("#keywords-for-BlF").val();
			}
			else if ( $("#kf").hasClass('active') ) {
				algorithmType = "KALMAN_FILTERING";
				qVal = $("#q-val-for-KF").val();
				rVal = $("#r-val-for-KF").val();
			}
			else if ( $("#nrkf").hasClass('active') ) {
				algorithmType = "NOISE_RECOMMEND_KALMAN_FILTERING"; 
				qVal = $("#q-val-for-NRKF").val();
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
			  bucket_size: bucketSize,
			  keywords: keywords,
			  uc_under_bound: ucUnderBound,
			  q_val: qVal,
			  r_val: rVal
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
	  
	  function showRecommendModal(algorithm) {
		  if (algorithm == null || algorithm == 'null')	return;
		  
		  $('#recommend-on-modal').html('We recommend <u>' + algorithm.replace('_',' ').toLowerCase() + '</u> for this source.');
		  $('#recommend-sampling-modal').val(algorithm);
		  $('#recommend-sampling-modal').modal('show');
	  }
	  
	  function applyRecommendSampling() {
		  var algorithm = $('#recommend-sampling-modal').val();
		  var acronym;
		  switch (algorithm) {
		  case 'RESERVOIR_SAMPING'						: acronym='rs'; break;
		  case 'PRIORITY_SAMPLING' 						: acronym='ps'; break;
		  case 'STRATIFIED_SAMPLING'					: acronym='sts'; break;
		  case 'BINARY_BERNOULLI_SAMPLING'				: acronym='bbs'; break;
		  case 'SYSTEMATIC_SAMPLING'					: acronym='sys'; break;
		  case 'HASH_SAMPLING'							: acronym='hs'; break;
		  case 'K_SAMPLING'								: acronym='ks'; break;
		  case 'UC_K_SAMPLING'							: acronym='ucks'; break;
		  }
		  $('#'+acronym).addClass('active').siblings().removeClass('active');
		  $('#tab-for-'+acronym).removeClass('fade').addClass('active').siblings().removeClass('active');
	  }
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
                <li id="bbs"><a href="#tab-for-bbs" data-toggle="tab">Binary Bernoulli Sampling</a></li>
                <li id="hs"><a href="#tab-for-hs" data-toggle="tab">Hash Sampling</a></li>
                <li id="ps"><a href="#tab-for-ps" data-toggle="tab">Priority Sampling</a></li>
                <li id="rs"><a href="#tab-for-rs" data-toggle="tab">Reservoir Sampling</a></li>
                <li id="sts"><a href="#tab-for-sts" data-toggle="tab">Stratified Sampling</a></li>
                <li id="sys"><a href="#tab-for-sys" data-toggle="tab">Systematic Sampling</a></li>
                
                <li id="ks"><a href="#tab-for-ks" data-toggle="tab">KSample</a></li>
                <li id="ucks"><a href="#tab-for-ucks" data-toggle="tab">UC KSample</a></li>
                
                <li style="width: -webkit-fill-available;"></li>
                
                <li id="qf"><a href="#tab-for-qf" data-toggle="tab">Query Filtering</a></li>
                                
                <li id="baf"><a href="#tab-for-baf" data-toggle="tab">Bayesian Filtering</a></li>
                <li id="blf"><a href="#tab-for-blf" data-toggle="tab">Bloom Filtering</a></li>
                <li id="kf"><a href="#tab-for-kf" data-toggle="tab">Kalman Filtering</a></li>
                <li id="nrkf"><a href="#tab-for-nrkf" data-toggle="tab">Noise Recommend Kalman Filtering</a></li>
              </ul> 

              <div id="tab-src-content" class="tab-content">
                <div class="tab-pane fade" id="tab-for-bbs">
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
                
                <div class="tab-pane fade" id="tab-for-hs">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="sample-ratio-for-HS">Sample Ratio</label>
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
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab-for-ps">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="sample-size-for-PS">Sample Size</label>
				      <div class="controls">
  				       <input type="text" id="sample-size-for-PS">
				      </div>
				    </div>
				    <div class="control-group">
  				      <label class="control-label" for="window-size-for-PS">Window Size</label>
				      <div class="controls">
				        <input type="text" id="window-size-for-PS">
				      </div>
				    </div>
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab-for-rs">
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
                
                <div class="tab-pane fade" id="tab-for-sts">
                  <div class="form-horizontal">
                  TO BE IMPLEMENTED
                  <!-- 
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
				  -->
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab-for-sys">
                  <div class="form-horizontal">
				    <div class="control-group">
  				      <label class="control-label" for="interval-for-SyS">Interval</label>
				      <div class="controls">
				        <input type="text" id="interval-for-SyS">
				      </div>
				    </div>
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab-for-ks">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="sample-ratio-for-KS">Sampling Rate</label>
				      <div class="controls">
  				       <input type="text" id="sample-ratio-for-KS">
				      </div>
				    </div>
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab-for-ucks">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="sample-ratio-for-UCKS">Sampling Rate</label>
				      <div class="controls">
  				       <input type="text" id="sample-ratio-for-UCKS">
				      </div>
				    </div>
				    <div class="control-group">
				      <label class="control-label" for="uc-under-bound-for-UCKS">UC Under Bound</label>
				      <div class="controls">
  				       <input type="text" id="uc-under-bound-for-UCKS">
				      </div>
				    </div>
				  </div> 
                </div>

                <div class="tab-pane fade" id="tab-for-qf">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="keywords-for-QF">Keywords</label>
				      <div class="controls">
  				       <input type="text" id="keywords-for-QF">
				      </div>
				    </div>
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab-for-baf">
                  <div class="form-horizontal">
                  TO BE IMPLEMENTED
				  </div> 
                </div>
                
                <div class="tab-pane fade" id="tab-for-blf">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="bucket-size-for-BlF">Bucket Size</label>
				      <div class="controls">
  				       <input type="text" id="bucket-size-for-BlF">
				      </div>
				    </div>
				    <div class="control-group">
				      <label class="control-label" for="keywords-for-BlF">Keywords</label>
				      <div class="controls">
  				       <input type="text" id="keywords-for-BlF">
				      </div>
				    </div>
				  </div>
                </div>
                
                <div class="tab-pane fade" id="tab-for-kf">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="q-val-for-KF">Process Noise(Q)</label>
				      <div class="controls">
  				       <input type="text" id="q-val-for-KF">
				      </div>
				    </div>
				    <div class="control-group">
				      <label class="control-label" for="r-val-for-KF">Measurement Noise(R)</label>
				      <div class="controls">
  				       <input type="text" id="r-val-for-KF">
				      </div>
				    </div>
				  </div> 
                </div> 
                
                <div class="tab-pane fade" id="tab-for-nrkf">
                  <div class="form-horizontal">
				    <div class="control-group">
				      <label class="control-label" for="q-val-for-NRKF">Process Noise(Q)</label>
				      <div class="controls">
  				       <input type="text" id="q-val-for-NRKF">
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
  
	<div id="recommend-sampling-modal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	  <div class="modal-header">
	    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">x</button>
	    <h3 id="myModalLabel">Intelligent Engine</h3>
	  </div>
	  <div class="modal-body">
	    <p id='recommend-on-modal'></p>
	  </div>
	  <div class="modal-footer">
	    <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
	    <button class="btn btn-primary" onclick="applyRecommendSampling();" data-dismiss="modal" aria-hidden="true">Apply</button>
	  </div>
	</div>
</html>
