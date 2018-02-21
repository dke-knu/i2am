var dataChanged;
function drawWordcloud() {

	var WIDTH = 800, HEIGHT = 600;
	
	var fill = d3.scale.category20b();
 	
	//var data = [ {word: "TagCloud", weight: 10} ];
	var data = [];
	
	data = data.slice();
	
	var scale = d3.scale.linear()
		.domain(d3.extent(data, function(d) { return d.weight; }))
		.range([15,60]);
	
	var layout = d3.layout.cloud()
			.timeInterval(Infinity)
			.size([WIDTH, HEIGHT])
			.padding(0)
			.font("Impact")
			.fontSize(function(d) { return d.size; })
			.text(function(d) { return d.text; })
			.on("end", draw);
	
	var svg = d3.select("#word-cloud").append("svg").attr("width", WIDTH).attr("height", HEIGHT);

	var wordcloud = svg.append("g").attr("transform", "translate(" + [WIDTH>>1, HEIGHT>>1] + ")");
	
	update();
	
	dataChanged = function(input) {
		
		//웹소켓에서 받아온 데이터 계산->data update->다시그려주기
		//input가정
//		var input = '{"Hello":3, "AboutTime":2, "Sing Street":5, "Mission Impossible":9, "Notebook":1}';
		
//		console.log(input);
		var tmp = JSON.parse(input);

		var keyset = [];
		var key, hashtag, weight;

		for (key in tmp) {
			_word = key;
			_weight = tmp[key];

			if (data.length > 0) {

				var idx = -1;// 
				for (var i = 0; i < data.length; i++) {
					if (data[i].word == _word) {idx = i; break;}
				}

				if (idx === -1) {// 새로운 단어일 때 데이터에 추가
					data.push({word : _word, weight : _weight});
				} else {// 이미 존재하는 단어일 때 weight변경
					data[idx].weight = data[idx].weight + _weight;
				}
			} else {// 데이터 배열이 비어있을 때
				data.push({word : _word, weight : _weight});
			}
		}
		scale = d3.scale.linear()
		.domain(d3.extent(data, function(d) { return d.weight; }))
		.range([15,60]);
		
		update();
	};
	
	function draw(newdata, bounds) {

		wordcloud.attr("width", WIDTH).attr("height", HEIGHT);
		
		var text = wordcloud.selectAll("text")
				.data(newdata, function(d) { return d.text; });
		
		text.transition().duration(2000) //already exist data
		.attr("transform", function(d) { return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")"; })
		.style("font-size", function(d) { return d.size + "px"; });

		text.enter().append("text") //append new data
			.text(function(d) { return d.text; })
			.style("fill", function(d, i) { return fill(i); })
			.attr("text-anchor", "middle")
			.style("font-family", "Impact")
			.style("font-size", function(d) { return d.size + "px"; })  //size need scaling
			.attr("transform", function(d) { return "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")"; });
		
		text.exit().remove(); // remove text that doesn't exist in data
	}

	function update() {
	    layout.spiral('archimedean');
	    layout
	    	.stop()
	    	.words(data.map(function(d) { return { text:d.word, size:scale(d.weight) }; }))
	    	.start();
	}
};