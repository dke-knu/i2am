<%@page import="knu.cs.dke.prog.esper.EsperEngine"%>
<%@page import="knu.cs.dke.prog.util.Constant" %>
<%@ page language="java" contentType="text/html; charset=EUC-KR"
	pageEncoding="EUC-KR"%>
<!DOCTYPE html>
<html>
<head>
<script>
history.pushState(null, null, location.href); 
window.onpopstate = function(event) { 
history.go(1); 
}
</script>
<meta charset="UTF-8" >
<title>실시간 필터링</title>

</head>
<body onload="document.broadcasting.reset();">
	<form name='broadcasting'>
		<!-- 송신 메시지 작성하는 창 -->
		<!-- <input id="textMessage" type="text"> -->
		<!-- 송신 버튼 -->
		<!-- <input onclick="sendMessage();disconnect();" value="Start" type="button">-->
		<!-- 종료 버튼 -->
		<br><br>
		<p align='center'>
		<% if(Constant.InputType.equals("input_file")){ %>
		<!-- 파일 input일때-->
		<img align='center' src='image/save.png' width='50' onclick="disconnect();location.href='..\\return_file?file=1'">
		<% } else {%>
		<!-- 임시로 둠 추후에 시스템 개발 시, 실시간 형태로 들어오는 데이터 필터링 결과 저장방식 다시 생각해야 함 -->
		<img align='center' src='image/save.png' width='50' onclick="disconnect();location.href='..\\return_file?file=1'">
		<!-- <input type='button' name ='file' onclick="disconnect();location.href='..\\setting?file=1'" value='정지 후 저장'> -->
		<% } %>
		<!-- 처음 페이지로 -->
		<img align='center' src='image/home.png' width='50' onclick="disconnect();location.href='..\\return_file?file=0'">
		<!-- 피드백 -->
		<img align='center' src='image/feedback.png' height='45' >
		<br />
		</p>
	<!-- 결과 메시지 보여주는 창 -->
	<p align='center'>
	<textarea id="messageTextArea" rows="50" cols="100"></textarea>
	</p>
	</form>
	

	<script type="text/javascript">
	
        //WebSocketEx는 프로젝트 이름
        //websocket 클래스 이름
        var webSocket = new WebSocket("ws://SERVER_IP:PORT/FilteringSystem/user/broadcasting_web");
        var messageTextArea = document.getElementById("messageTextArea");
        //웹 소켓이 연결되었을 때 호출되는 이벤트
        webSocket.onopen = function(message){
            //messageTextArea.value += "Server connect...\n";
        };
        //웹 소켓이 닫혔을 때 호출되는 이벤트
        webSocket.onclose = function(message){
        	//alert("ininninini");
            //messageTextArea.value += "Server Disconnect...\n";
        };
        //웹 소켓이 에러가 났을 때 호출되는 이벤트
        webSocket.onerror = function(message){
            messageTextArea.value += "error...\n";
        };
        //웹 소켓에서 메시지가 날라왔을 때 호출되는 이벤트
        webSocket.onmessage = function(message){
            //messageTextArea.value += "Recieve From Server => "+message.data+"\n";
        	 messageTextArea.value += message.data+"\n";
        };
        //Send 버튼을 누르면 실행되는 함수
        function sendMessage(){
            //var message = document.getElementById("textMessage");
            //messageTextArea.value += "Send to Server => "+message.value+"\n";
            //웹소켓으로 textMessage객체의 값을 보낸다.
            messageTextArea.value += "Send to Server => disconnect\n";
            webSocket.send("disconnect");
            //textMessage객체의 값 초기화
            //message.value = "";
        }
        //웹소켓 종료
        function disconnect(){
            webSocket.close();
            
        }
    </script>
</body>
</html>