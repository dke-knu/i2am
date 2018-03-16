<%@ page language="java" contentType="text/html; charset=EUC-KR"
	pageEncoding="EUC-KR"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<script src="https://d3js.org/d3.v3.min.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=EUC-KR">
<title>doughnut chart</title>

</head>
<body>

<div class="widget">
    <div class="header">Browser Market Share</div>
    <div id="chart" class="chart-container"></div>
    <button value="ttt" onClick='render();'>ttt</button>
</div>
<script type="text/javascript">
        //WebSocketEx는 프로젝트 이름
        //websocket 클래스 이름
        var webSocket = new WebSocket("ws://IP:PORT/FilteringSystem/user/sampling_result");
        var messageTextArea = document.getElementById("messageTextArea");
        //웹 소켓이 연결되었을 때 호출되는 이벤트
        webSocket.onopen = function(message){
            //messageTextArea.value += "Server connect...\n";
        };
        //웹 소켓이 닫혔을 때 호출되는 이벤트
        webSocket.onclose = function(message){
            //messageTextArea.value += "Server Disconnect...\n";
        };
        //웹 소켓이 에러가 났을 때 호출되는 이벤트
        webSocket.onerror = function(message){
            messageTextArea.value += "error...\n";
        };
        //웹 소켓에서 메시지가 날라왔을 때 호출되는 이벤트
        webSocket.onmessage = function(message){
            //messageTextArea.value += "Recieve From Server => "+message.data+"\n";
        	 //messageTextArea.value += message.data+"\n";
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
	var newData=[];
	var path,path2;
	var text;
	var dataset = [
	           	{ label: 'IE', value: 39.10 },
	           	{ label: 'Chrome', value: 32.51 },
	           	{ label: 'Safari', value: 13.68 },
	           	{ label: 'Firefox', value: 8.71 },
	           	{ label: 'Others', value: 6.01 }
	];
	var pie = d3.layout.pie()
				.value(function(d){return d.value})
				.sort(null)
				.padAngle(.03);
	var w = 500, h = 300;
	
	var outerRadius=(w-200)/2;
	var innerRadius=100;
	
	var color = d3.scale.category10();
	
	var arc = d3.svg.arc()
				.outerRadius(outerRadius)
				.innerRadius(innerRadius);
	
	var svg = d3.select("#chart")
				.append("svg")
				.attr({
					width:w,
					height:h,
					class: 'shadow'
				}).append('g')
				  .attr('transform', 'translate(' + (w-200) / 2 + ',' + h / 2 + ')');
	var g = svg;
	
	path = g.datum(dataset).selectAll('path')
			.data(pie)
			.enter()
			.append("path")
			.attr({
				d:arc,
				fill:function(d,i){
					return color(d.data.label);
				}
			}).each(function(d){this._current = d;});
	path.transition()
    .ease("elastic")
    .duration(750)
    .attrTween("d", arcTween);     
	/*
	path = svg.selectAll('path')
    	    .data(pie(dataset))
       		.enter()
       		.append('path')
        	.attr({
        	    d:arc,
          		fill:function(d,i){
                	return color(d.data.name);
        	    }
     	   }).each(function(d){this._current = d;});
	
	path.transition()
    	.duration(1000)
  		.attrTween('d', function(d) {
   	    var interpolate = d3.interpolate({startAngle: 0, endAngle: 0}, d);
   	    return function(t) {
            return arc(interpolate(t));
        };
    });
	*/
	percentages(dataset);
	//percentages(dataset);
	function percentages(dataset){
		svg.selectAll('text').remove();
		console.log("in here");
		text=svg.selectAll('text')
		  .data(pie(dataset))
		  .enter()
		  .append("text")
		  .transition()
		  .duration(200)
		  .attr("transform", function (d) {
			  console.log(arc.centroid(d));
		      return "translate(" + arc.centroid(d) + ")";
		  })
		  .attr("dy", ".4em")
		  .attr("text-anchor", "middle")
		  .text(function(d){
			  console.log(d.data.value);
		      return d.data.value+"%";
		  })
		  .style({
		      fill:'#fff',
		      'font-size':'10px'
		  });
	}
	
	var legendRectSize=20;
	var legendSpacing=7;
	var legendHeight=legendRectSize+legendSpacing; 
	 
	var legend=svg.selectAll('.legend')
	  .data(color.domain())
	  .enter()
	  .append('g')
	  .attr({
	      class:'legend',
	      transform:function(d,i){
	          //Just a calculation for x and y position
	          //return 'translate(-35,60 )';
	    	  return 'translate(180,' + ((i*legendHeight)-65) + ')';
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
	      fill:color,
	      stroke:color
	  });
	 
	legend.append('text')
	  .attr({
	      x:30,
	      y:15
	  })
	  .text(function(d){
	      return d;
	  }).style({
	      fill:'#929DAF',
	      'font-size':'14px'
	  });
	function legendChanging() {
		legend.selectAll('text').remove();
		//o_legend.selectAll('text').remove();
		
		legend.append('text').attr({
			x : 30,
			y : 15
		}).text(function(d,i){
			console.log("legend! "+newData[i].label);
		      return newData[i].label;
		  }).style({
		      fill:'#929DAF',
		      'font-size':'14px'
		  });
		/*
		o_legend.append('text').attr({
			x : 30,
			y : 15
		}).text(function(d,i){
		      return label_originData[d];
		  }).style({
		      fill:'#929DAF',
		      'font-size':'14px'
		  });
	*/
	}
	function dataInput(message) {
		console.log("data Input!!");
		var parsed = JSON.parse(message);
		console.log("hey~~~ "+parsed.result[0].ho);
		console.log("hello~ "+parsed.result[0]);
		newData = parsed.result[0].ho;
		render(newData, false);
		legendChanging();
	}
	
	//render();
	function render(inputData, isOrigin){
		/*
		dataset = [
		           	{ name: 'IE', percent: 20.00 },
		           	{ name: 'Chrome', percent: 20.00 },
		           	{ name: 'Safari', percent: 20.00 },
		           	{ name: 'Firefox', percent: 20.00 },
		           	{ name: 'Others', percent: 20.00 }
		];
		*/
		dataset = inputData;
		console.log(dataset);
		for(var tmp in dataset){
			console.log("ㄴ"+dataset[tmp].label);
		}
		g.datum(dataset).selectAll("path").data(pie).transition().attrTween("d", arcTween);
						
		console.log("here");
		percentages(dataset);
		console.log("here2");
	}

	  	function arcTween(a) {
	  		console.log("sbpark");
			var i = d3.interpolate(this._current, a);
	  		this._current = i(0);
	  		return function(t) {
	  			//console.log(t);
	  			return arc(i(t));
		};}

</script>
</body>
</html>