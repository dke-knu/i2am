
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
	<script>
	  $(document).ready(function() {
		  
		  $('#btn-login').click(function(e) {
			  var action = $("#form-login").attr("action");
			  var form_data = {
					  user_id: $('#user-login-id').val(),
					  user_pw: $('#user-login-pw').val()
			  };
			  if (form_data.user_id == '' || form_data.user_pw == '') {
				  alert("Please input first.");
				  return false;
			  }
			  
			  // validity check
			  //var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    		  //if (!re.test(form_data.user_id))	{
    		  //	  alert(form_data.user_id + " is not e-mail.");
    		  //	  return false;
    		  //}
			  
			  $.ajax({
				  type: "POST",
				  url: action,
				  data: form_data,
				  async: false,
				  cache: false,
				  success: function(response) {
					  if (response.trim() == "true") {
						  alert("Login is success.");
						  window.open("./list.jsp", "_self");
					  } else {
						  alert("Login is failure.");
						  window.location.reload();
					  }
				  },
				  error: function() {
				      alert("ERROR");	
					  window.location.reload();
				  }
			  })
			  e.stopImmediatePropagation();
			  return false;
		  })
		  
		  $('#btn-join').click(function(e) {
			  var action = $("#form-join").attr("action");
			  var form_data = {
					  user_id: $('#user-join-id').val(),
					  user_name: $('#user-join-name').val(),
					  user_pw: $('#user-join-pw').val()
			  };
			  if (form_data.user_id == '' || form_data.user_name == '' || form_data.user_pw == '') {
				  alert("Please input first.");
				  e.stopImmediatePropagation();
				  return false;
			  }
			  
			  // validity check
			  //var re = /^(([^<>()\[\]\\.,;:\s@"]+(\.[^<>()\[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
    		  //if (!re.test(form_data.user_id))	{
    		  //	  alert(form_data.user_id + " is not e-mail.");
    		  //	  return false;
    		  //}
			  
			  $.ajax({
				  type: "POST",
				  url: action,
				  data: form_data,
				  async: false,
				  cache: false,
				  success: function(response) {
					  if (response.trim() == "true") {
						  alert("Join is success.");
						  window.open("./list.jsp", "_self");
					  } else {
						  alert("Join is failure. Your e-mail is already registered.");
					  }
				  },
				  error: function() {
				      alert("ERROR");		
					  window.location.reload();	
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

      .form-sign {
        max-width: 300px;
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
      .form-sign .form-sign-heading,
      .form-sign .checkbox {
        margin-bottom: 10px;
      }
      .form-sign input[type="text"],
      .form-sign input[type="password"] {
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

      <form class="form-sign" id="form-login" action="ajax/login-ok.jsp" method="post">
        <h2 class="form-sign-heading">Sign in</h2>
        <input type="text" class="input-block-level" placeholder="Email" id="user-login-id">
        <input type="password" class="input-block-level" placeholder="Password" id="user-login-pw">
        <label class="checkbox">
          <input type="checkbox" value="remember-me"> Remember me
        </label>
        <button class="btn btn-large btn-primary" type="submit" id="btn-login">Sign in</button>
      </form>

    </div> <!-- /container -->

    <div class="container">

      <form class="form-sign" id="form-join" action="ajax/join-ok.jsp" method="post">
        <h2 class="form-sign-heading">Sign up</h2>
        <input type="text" class="input-block-level" placeholder="Email" id="user-join-id">
        <input type="text" class="input-block-level" placeholder="Full name" id="user-join-name">
        <input type="password" class="input-block-level" placeholder="Password" id="user-join-pw">
        <button class="btn btn-large btn-primary" type="submit" id="btn-join">Sign up</button>
      </form>

    </div> <!-- /container -->
  </body>
</html>
