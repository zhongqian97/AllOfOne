layui.config({
	base : "js/"
}).use(['form','layer','jquery','laypage'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		$ = layui.jquery;

	//加载页面数据
	var newsData = '';
	$.get("/message/getNotice", function(data){
		data.sort(messageSortRuleByTime);
		var newArray = [];
		newsData = data;
		newsList();
	})
	
	//重刷页面
	$(".reload_btn").click(function(){
		window.location.reload();
	})

	function newsList(that){
		//渲染数据
		function renderDate(data,curr){
			var dataHtml = '';
			if(!that){
				currData = newsData.concat().splice(curr*nums-nums, nums);
			}else{
				currData = that.concat().splice(curr*nums-nums, nums);
			}
			if(currData.length != 0){
				for(var i=0;i<currData.length;i++){
					dataHtml += '<tr>'
					+'<td>'+currData[i].addressee+'</td>'
					+'<td>'+formatTime(currData[i].time)+'</td>'
			    	+'<td align="left">'+currData[i].data+'</td>'
			    	+'</tr>';
				}
			}else{
				dataHtml = '<tr><td colspan="8">暂无数据</td></tr>';
			}
		    return dataHtml;
		}

		//分页
		var nums = 10; //每页出现的数据量
		if(that){
			newsData = that;
		}
		laypage({
			cont : "page",
			pages : Math.ceil(newsData.length/nums),
			jump : function(obj){
				$(".notice_content").html(renderDate(newsData,obj.curr));
				$('.notice_list thead input[type="checkbox"]').prop("checked",false);
		    	form.render();
			}
		})
	}
})

//消息排序规则
function messageSortRuleByTime(a, b) {
	  return parseInt(b.time) - parseInt(a.time); 
}

//格式化时间
function formatTime(_time) {
	_time = new Date(parseInt(_time));
    var year = _time.getFullYear();
    var month = _time.getMonth()+1<10 ? "0"+(_time.getMonth()+1) : _time.getMonth()+1;
    var day = _time.getDate()<10 ? "0"+_time.getDate() : _time.getDate();
    var hour = _time.getHours()<10 ? "0"+_time.getHours() : _time.getHours();
    var minute = _time.getMinutes()<10 ? "0"+_time.getMinutes() : _time.getMinutes();
    return year+"-"+month+"-"+day+" "+hour+":"+minute;
}
