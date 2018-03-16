<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Search Keyword from twitter!</title>
<script type="text/javascript" src="./js/jquery-3.1.0.js"></script>
<link rel="stylesheet" type="text/css" href="./style/layout.css" />
<link rel="stylesheet" type="text/css" href="./style/blog.css">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<script>
$('document').ready(function() {
	$('#word').hide();
	$('#form_words').keydown(function(e) {
		var code = e.keyCode || e.which;
		if(code == 9 || code == 188) {
			e.preventDefault();
			add_word();
		} else if (code == 13) {
			e.preventDefault();
		}
	});
});

var words = [];

function add_word(){
	var newword = $('#search-word').val();
	var addflag = true;
	var span = null;
	
	if(newword == "") {
		alert('단어를 입력해주세요');
		
	} else {
		var word = $('#words').children('span').first();
		while(word.next().length > 0) {
			word = word.next();
			if(word.text() == newword) {
				alert('이미 존재하는 단어입니다.');
				addflag = false;
				break;
			} 
		}
		if(addflag) {
			span = $('#word').clone();
			span.attr('id', newword);
			span.html(newword);
			span.on('click', function() {//word remove
				if(confirm($(this).text()+'을(를) 삭제할까요?')) {
					$(this).remove();
				}
			});
		}
	}
	if(span!=null){
		$('#search-word').val("");
		$('#words').append(span.show());
		$('#search-word').blur(); 
		$('#search-word').focus();
	}
}

function search_word() {
	var word = $('#words').children('span').first();
	while(word.next().length>0){
		word = word.next();
		console.log(word.length);
		words.push(word.text());
	}
 	if(words.length < 1) {
		alert('단어를 입력한 후 검색해주세요!');
		return false;
	} else {
		$('#search_words').attr('value', words);
		return true;
	} 
}
</script>
</head>
<body>
	<div class="blog-masthead">
		<div class="container">
			<nav class="blog-nav">
				<a class="blog-nav-item active" href="#">Home</a>
			</nav>
		</div>
	</div>

	<div class="container">
		<h2>Tag Cloud</h2>
		<div id="words">
			<p>keywords</p>
			<span class='span_word' id="word">example word</span>
		</div>
		<div id="contents">
		<div class="col-sm-12 blog-main">
			<form action="wordcloud.jsp" method="post" id="form_words" onsubmit="return search_word();">
				<p class="blog-post-meta">Press 'Tab' to add words and press 'search' button to search hashtag!</p>
				<div class="row">
					<div class="col-sm-10">
						<input type="text" class="form-control" id="search-word" />
					</div>
					<div class="col-sm-2">
						<input type="submit" class="btn btn-default btn-block" value="search" />
					</div>
				</div>
				<input type="hidden" name='search' id="search_words" />
			</form>
			
			
		</div>	
		</div>
	</div>
	<footer class="blog-footer">
		<div class="container">
			 <p class="text-muted">Copyrightⓒ2016 by Data and Knowledge Engineering Lab</p>
		</div>
	</footer>
</body>
</html>