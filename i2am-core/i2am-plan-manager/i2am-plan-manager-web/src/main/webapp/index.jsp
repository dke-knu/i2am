<%@ page language="java" contentType="text/html; charset=EUC-KR"
	pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>

<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<meta charset="utf-8">

<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="description" content="">
<meta name="author" content="">

<link rel="stylesheet" type="text/css" href="css/sign.css">
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
<script src="./js/jquery-3.3.1.min.js"></script>

<script>
	$(document).ready(function() {	
		
		$("#btn-login").click(function(e) {
			var action = $("#form-login").attr("action");
			var form_data = {
				user_id: $("#user-login-id").val(),
				user_pw: $("#user-login-pw").val()										
			};				
			
			if (form_data.user_id == '' || form_data.user_pw == '') {
				alert("Please input first.");
				return false;
			}
			
			$.ajax({
				type: "POST",
				url: action,
				data: form_data,
				async: false,
				cache: false,
				success: function(response) {					
					if (response.trim() == "true") {
						alert("Login is success.");
						window.open("./main.jsp", "_self");
					} else {
						alert("Login is failure.");
						window.location.reload();
					}
				},
				error: function() {
					alert("ERROR");
					window.location.reload();
				}
			});
			
			e.stopImmediatePropagation();
			return false;			
		});		
		
		$("#btn-join").click(function(e) {
		
			var action = $("#form-join").attr("action");
			var form_data = {
				user_id: $("#user-join-id").val(),
				user_name: $("#user-join-name").val(),
				user_pw: $("#user-join-pw").val(),
				user_pw_rp: $("#user-join-pw-rp").val()
			};			
			
			if (form_data.user_id == '' || form_data.user_name == '' || form_data.user_pw == '') {
				  alert("Please input first.");
				  e.stopImmediatePropagation();
				  return false;
			 }			
			
			if (form_data.user_pw != form_data.user_pw_rp) {
				alert("Check Password");
				e.stopImmediatePropagation();
				return false;
			}			
			
			 $.ajax({
				  type: "POST",
				  url: action,
				  data: form_data,
				  async: false,
				  cache: false,
				  success: function(response) {
					  if (response.trim() == "true") {
						  alert("Join is success.");
						  window.open("./main.jsp", "_self");
					  } else {
						  alert("Join is failure. Your e-mail is already registered.");
					  }
				  },
				  error: function() {
				      alert("ERROR");					  	
				  }
			  })
			  e.stopImmediatePropagation();
			  return false;		
			
		});
	})
</script>


<title>Plan Manager - Sign in</title>

</head>
<body>

	<div class="outer">
		<form id="form-login" name="form-login" action="ajax/login-ok.jsp" method="post">
			<!--  Image Here -->
			<div>
				<h1>Sign In</h1>
				<hr>
				<label for="username"><b>User name</b></label> <input type="text" placeholder="Enter Username" name="user-login-id"
					id="user-login-id" required> <label for="password"><b>Password</b></label>
				<input type="password" placeholder="Enter Password" name="user-login-pw" id="user-login-pw" required> <label>
				<input type="checkbox" checked="checked" name="remember"> Remember me	</label> <br>
				<br>
				<button type="submit" id="btn-login">Login</button>
			</div>
		</form>
	</div>

	<br>
	<div class="outer2" onclick="document.getElementById('id01').style.display='block'">
		<center>
			<h4 style="width: auto;">Create an account</h4>
		</center>
	</div>

	<div id="id01" class="modal">
		<span onclick="document.getElementById('id01').style.display='none'" class="close" title="Close Modal">&times;</span>
		<form class="modal-content" id="form-join" action="ajax/join-ok.jsp" method="post">
			<div class="container">
				<h1>Sign Up</h1>
				<p>Please fill in this form to create an account.</p>
				<hr>
				<label for="email"><b>Email</b></label> <input type="text" placeholder="Enter Email" name="email" id="user-join-id" required>
				<label for="name"><b>name</b></label> <input type="text" placeholder="Enter Full Name" name="name" id="user-join-name" required>
				<label for="psw"><b>Password</b></label> <input type="password" placeholder="Enter Password" name="psw" id="user-join-pw" required>
				<label for="psw-repeat"><b>Repeat Password</b></label> <input type="password" placeholder="Repeat Password"	name="psw-repeat" id="user-join-pw-rp" required>

				<div class="clearfix">
					<button type="button"
						onclick="document.getElementById('id01').style.display='none'"
						class="cancelbtn">Cancel</button>
					<button type="submit" class="signupbtn" id="btn-join">Sign Up</button>
				</div>
			</div>
		</form>
	</div>

	<script>
		var modal = document.getElementById('id01');
	
		// When the user clicks anywhere outside of the modal, close it
		window.onclick = function(event) {
		    if (event.target == modal) {
		        modal.style.display = "none";
		    }
		}
	</script>

</body>
</html>