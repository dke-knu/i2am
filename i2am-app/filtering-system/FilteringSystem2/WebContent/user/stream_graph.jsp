<%@page import="knu.cs.dke.prog.util.Constant"%>
<%@ page language="java" contentType="text/html; charset=EUC-KR"
	pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script src="https://d3js.org/d3.v3.min.js"></script>
<script src="http://d3js.org/colorbrewer.v1.min.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>d3 test</title>
</head>
<body>
	<style>
body {
	font-family: "Helvetica Neue", Helvetica, Arial, sans-serif;
	margin: auto;
	position: relative;
	width: 960px;
}

button {
	position: absolute;
	right: 10px;
	top: 10px;
}

</style>

	<button onclick="transition()">Update</button>
	<p align='center'>
		<textarea id="messageTextArea" rows="20" cols="100"></textarea>

	</p>
	<br>
	<br>
	<p align='center'>
		<!-- 처음 페이지로 -->
		<img align='center' src='image/home.png' width='50'
			onclick="disconnect();location.href='..\\return_file?file=0'">
		<!-- 피드백 -->
		<img align='center' src='image/feedback.png' height='45'> <br />
	</p>
	<div class="widget">
    <div class="header">Browser Market Share</div>
    <div id="chart" class="chart-container"></div><br><br>
    <div class="header">Original Data</div>
    <div id="o_chart" class="chart-container"></div>
	</div>
</body>
<script type="text/javascript">
        //WebSocketEx는 프로젝트 이름
        //websocket 클래스 이름
        var webSocket = new WebSocket("ws://IP:PORT/FilteringSystem/user/sampling_result");
        var messageTextArea = document.getElementById("messageTextArea");
        //웹 소켓이 연결되었을 때 호출되는 이벤트
        webSocket.onopen = function(message){
            //messageTextArea.value += "Server connect...\n";
            alert("open??");
        };
        //웹 소켓이 닫혔을 때 호출되는 이벤트
        webSocket.onclose = function(message){
            //messageTextArea.value += "Server Disconnect...\n";
            alert("close");
        };
        //웹 소켓이 에러가 났을 때 호출되는 이벤트
        webSocket.onerror = function(message){
            messageTextArea.value += "error...\n";
        };
        //웹 소켓에서 메시지가 날라왔을 때 호출되는 이벤트
        webSocket.onmessage = function(message){
            //messageTextArea.value += "Recieve From Server => "+message.data+"\n";
        	 messageTextArea.value += message.data+"\n";
        	 dataInput(message.data);
        };
        //Send 버튼을 누르면 실행되는 함수
        function sendMessage(){
            //웹소켓으로 textMessage객체의 값을 보낸다.
            messageTextArea.value += "Send to Server => disconnect\n";
            webSocket.send("disconnect");
        }
        //웹소켓 종료
        function disconnect(){
            webSocket.close();
            
        }
        
    </script>
<script>
	//drawGraph();
	var data = [];
	var newData = [], originData = [], label_newData=[], label_originData=[];
	/*
	var margin = {
		    'top'    : 5, 
		    'right'  : 20, 
		    'bottom' : 20, 
		    'left'   : 50 
		  };
	*/
	//새 데이터 들어옴!
	var width = 1000, height = 400;
	//var width = 480, height = 250;
	var layers0, layers1, o_layers0, o_layers1;
		var colorRange = ["#E34A33", "#FDBB84", "#FEF0D9"];
		var origin_colorRange = ["#DD1C77","#C994C7","#F1EEF6"];
		var sbNum = 0,oriNum = 0;
		var messageTextArea = document.getElementById("messageTextArea");
		messageTextArea.value += "start\n";
		
		var n = 9, // number of layers
		m = 20, // number of samples per layer
		stack1 = d3.layout.stack().offset("wiggle"), 
		layers0 = stack1(d3.range(n).map(function() {return bumpLayer(m, 0);}));
		layers1 = stack1(d3.range(n).map(function() {return bumpLayer(m, 1);}));

		//original
		var stack2 = d3.layout.stack().offset("wiggle"), 
		o_layers0 = stack2(d3.range(n).map(function() {return bumpLayer(m, 0);}));
		o_layers1 = stack2(d3.range(n).map(function() {return bumpLayer(m, 1);}));
		
		var x = d3.scale.linear().domain([ 0, m - 1 ]).range([ 0, width-200 ]);
		var y = d3.scale.linear().domain(
				[ 0, d3.max(layers0.concat(layers1), function(layer) {
					return d3.max(layer, function(d) {
						return d.y0 + d.y;
					});
				}) ]).range([ height, 0 ]);

		//var color = d3.scale.linear().range([ "#aad", "#556" ]);
		var color_origin = d3.scale.ordinal().range(colorbrewer.Blues[9]);
		var colorZ = d3.scale.ordinal().range(colorbrewer.Reds[9]);
		var area = d3.svg.area().interpolate("monotone").x(function(d) {
			return x(d.x);
		}).y0(function(d) {
			return y(d.y0);
		}).y1(function(d) {
			return y(d.y0 + d.y);
		});

		var svg = d3.select("#chart").append("svg").attr("width", width).attr(
				"height", height);
		var o_svg = d3.select("#o_chart").append("svg").attr("width", width).attr(
				"height", height);

		svg.selectAll("path").data(layers0).enter().append("path").attr("d",
				area).style("fill", function(d,i) {
					return colorZ(i);
		});
		o_svg.selectAll("path").data(o_layers0).enter().append("path").attr("d",
				area).style("fill", function(d,i) {
					return color_origin(i);
		});
		//legend
	var legendRectSize=20;
	var legendSpacing=7;
	var legendHeight=legendRectSize+legendSpacing; 
	 
	var legend=svg.selectAll('.legend')
	  .data(colorZ.domain())
	  .enter()
	  .append('g')
	  .attr({
	      class:'legend',
	      transform:function(d,i){
	          //Just a calculation for x and y position
	          return 'translate('+(width-150)+',' + ((i*legendHeight)+60) + ')';
	      }
	  });
	legend.append('rect')
	  .attr({
	      width:legendRectSize,
	      height:legendRectSize,
	      rx:20,
	      ry:20
	  })
	  .style({
	      fill:colorZ,
	      stroke:colorZ
	  });
	var o_legend=o_svg.selectAll('.legend')
	  .data(color_origin.domain())
	  .enter()
	  .append('g')
	  .attr({
	      class:'legend',
	      transform:function(d,i){
	          //Just a calculation for x and y position
	          return 'translate('+(width-150)+',' + ((i*legendHeight)+60) + ')';
	      }
	  });
	o_legend.append('rect')
	  .attr({
	      width:legendRectSize,
	      height:legendRectSize,
	      rx:20,
	      ry:20
	  })
	  .style({
	      fill:color_origin,
	      stroke:color_origin
	  });
	

	function legendChanging() {
		legend.selectAll('text').remove();
		o_legend.selectAll('text').remove();
		
		legend.append('text').attr({
			x : 30,
			y : 15
		}).text(function(d,i){
		      return label_newData[d];
		  }).style({
		      fill:'#929DAF',
		      'font-size':'14px'
		  });
		o_legend.append('text').attr({
			x : 30,
			y : 15
		}).text(function(d,i){
		      return label_originData[d];
		  }).style({
		      fill:'#929DAF',
		      'font-size':'14px'
		  });

	}

	legendChanging();
	function dataInput(message) {
		var parsed = JSON.parse(message);
		//alert(parsed.result[0].sb1);
		newData = parsed.result[0];
		originData = parsed.result[1];
		layers1 = stack1(d3.range(n).map(function() {
			return additionValues(newData, m, false);
		}));
		o_layers1 = stack2(d3.range(n).map(function() {
			return additionValues(originData, m, true)
		}));
		console.log("here dataInput " + label_newData.length);
		//change legend
		legendChanging();
		//change values of data
		transition();

	}
	function transition() {
		svg.selectAll("path").data(function() {
			var d = layers1;
			layers1 = layers0;
			return layers0 = d;
		}).transition().duration(2500).attr("d", area);
		o_svg.selectAll("path").data(function() {
			var d = o_layers1;
			o_layers1 = o_layers0;
			return o_layers0 = d;
		}).transition().duration(2500).attr("d", area);
	}

	//데이터 카운터
	var total_datas = -1;

	function additionValues(inputData, m, isOrigin) {
		var datas = new Array();
		//여기 동적으로 하고싶은데.....ㅎㅎ했당
		if (!isOrigin) {
			//initialization
			label_newData = [];
			for ( var tmp in inputData) {
				console.log("data name: " + tmp);
				label_newData.push(tmp);
				datas.push(inputData[tmp].split(','));
			}
			console.log("label_newData : " + label_newData[0]);
		} else {
			//initialization
			label_originData = [];
			for ( var tmp in inputData) {
				console.log("origin name: " + tmp);
				label_originData.push(tmp);
				datas.push(inputData[tmp].split(','));
			}
		}
		//string to interger
		for (var i = 0; i < datas.length; i++) {
			for (var j = 0; j < datas[i].length; j++) {
				datas[i][j] = parseInt(datas[i][j]);
			}
		}
		console.log(datas[0], datas[1]);
		total_datas++;
		messageTextArea.value += "[" + total_datas + ", " + n + "] " + (j % n)
				+ "\n";
		return datas[total_datas % n].map(function(d, i) {
			//alert(d);
			return {
				x : i,
				y : d
			};
		});

	}

	function bumpLayer(n, lNum) {
		var b1 = [ 100, 100, 100, 100, 100, 100 ];
		var b2 = [ 100, 100, 100, 100, 100, 100 ];
		var b3 = [ 100, 100, 100, 100, 100, 100 ];
		function bump(a) {
		}

		var a = [], i;
		for (i = 0; i < n; ++i)
			a[i] = 0;
		messageTextArea.value += "[lNum:" + lNum + "] [sbNum%3:" + sbNum % 3
				+ "]\n";
		if (lNum == 0) {
			if ((sbNum % 3) == 0) {
				sbNum++;
				return b1.map(function(d, i) {
					//alert(d);
					return {
						x : i,
						y : d
					};
				});
			} else if ((sbNum % 3) == 1) {
				sbNum++;
				return b2.map(function(d, i) {
					//alert(d);
					return {
						x : i,
						y : d
					};
				});
			} else {
				sbNum++;
				return b3.map(function(d, i) {
					//alert(d);
					return {
						x : i,
						y : d
					};
				});
			}

			//}
		} else {
			if ((sbNum % 3) == 0) {
				sbNum++;
				return b3.map(function(d, i) {
					//alert(d);
					return {
						x : i,
						y : d
					};
				});
			} else if ((sbNum % 3) == 1) {
				sbNum++;
				return b2.map(function(d, i) {
					//alert(d);
					return {
						x : i,
						y : d
					};
				});
			} else {
				sbNum++;
				return b1.map(function(d, i) {
					//alert(d);
					return {
						x : i,
						y : d
					};
				});
			}
		}
		// */
	}
</script>
</html>