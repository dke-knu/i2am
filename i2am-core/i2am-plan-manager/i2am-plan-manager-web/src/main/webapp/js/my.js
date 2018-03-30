function checkLogin() {
	$.ajax({
		type : 'post',
		url : './ajax/get-user-id.jsp',
		data : ({}),
		async: false,
		cache: false,
		success : function(data) {
			data = data.replace(/(^\s*)|(\s*$)/gi, "");
			if (data == "null") {
				alert("Please login first.");
				window.open("./sign-in.jsp", "_self");
			}
		}
	});
}