layui.config({
	base : "js/"
}).use(['form','layer','jquery','laypage'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		$ = layui.jquery;

	//加载页面数据
	var newsData = '';
	$.get("/admin/getUser", function(data){
		var newArray = [];
		newsData = data;
		newsList();
	})

	//查询
	$(".search_btn").click(function(){
		var newArray = [];
		if($(".search_input").val() != ''){
			var index = layer.msg('查询中，请稍候',{icon: 16,time:false,shade:0.8});
            setTimeout(function(){
            	$.ajax({
					url : "../../json/newsList.json",
					type : "get",
					dataType : "json",
					success : function(data){
						if(window.sessionStorage.getItem("addNews")){
							var addNews = window.sessionStorage.getItem("addNews");
							newsData = JSON.parse(addNews).concat(data);
						}else{
							newsData = data;
						}
						for(var i=0;i<newsData.length;i++){
							var newsStr = newsData[i];
							var selectStr = $(".search_input").val();
		            		function changeStr(data){
		            			var dataStr = '';
		            			var showNum = data.split(eval("/"+selectStr+"/ig")).length - 1;
		            			if(showNum > 1){
									for (var j=0;j<showNum;j++) {
		            					dataStr += data.split(eval("/"+selectStr+"/ig"))[j] + "<i style='color:#03c339;font-weight:bold;'>" + selectStr + "</i>";
		            				}
		            				dataStr += data.split(eval("/"+selectStr+"/ig"))[showNum];
		            				return dataStr;
		            			}else{
		            				dataStr = data.split(eval("/"+selectStr+"/ig"))[0] + "<i style='color:#03c339;font-weight:bold;'>" + selectStr + "</i>" + data.split(eval("/"+selectStr+"/ig"))[1];
		            				return dataStr;
		            			}
		            		}
		            		//用户id
		            		if(newsStr.id.indexOf(selectStr) > -1){
			            		newsStr["id"] = changeStr(newsStr.id);
		            		}
		            		//用户名
		            		if(newsStr.userName.indexOf(selectStr) > -1){
			            		newsStr["userName"] = changeStr(newsStr.userName);
		            		}
		            		//用户名
		            		if(newsStr.role.indexOf(selectStr) > -1){
			            		newsStr["role"] = changeStr(newsStr.role);
		            		}
		            		//所属用户
		            		if(newsStr.ownedUser.indexOf(selectStr) > -1){
			            		newsStr["ownedUser"] = changeStr(newsStr.ownedUser);
		            		}
		            		//时间
		            		if(newsStr.time.indexOf(selectStr) > -1){
			            		newsStr["time"] = changeStr(newsStr.time);
		            		}
		            		if(newsStr.id.indexOf(selectStr)>-1 
		            				|| newsStr.userName.indexOf(selectStr)>-1 
		            				|| newsStr.role.indexOf(selectStr)>-1 
		            				|| newsStr.ownedUser.indexOf(selectStr)>-1 
		            				|| newsStr.time.indexOf(selectStr)>-1){
		            			newArray.push(newsStr);
		            		}
		            	}
		            	newsData = newArray;
		            	newsList(newsData);
					}
				})
            	
                layer.close(index);
            },2000);
		}else{
			layer.msg("请输入需要查询的内容");
		}
	})

	$("body").on("click",".user_ban",function(){  //解封 or 封禁
		var _this = $(this);
		$.ajax({
			url : "/admin/banUser",
			type : "post",
			data : {
				userName : _this.attr("data-id"),
				command : _this.attr("data-command")
			},
			success : function(data) {
				layer.msg(data.information);
				if (data.status == '200') {
					window.location.reload();
				}
				return false;
			},
			error : function(data) {
				layer.msg(data.information);
				return false;
			}
		});
		
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
			    	+'<td align="left">'+currData[i].id+'</td>'
			    	+'<td>'+currData[i].userName+'</td>'
			    	+'<td>'+currData[i].role+'</td>'
			    	+'<td>'+currData[i].ownedUser+'</td>'
			    	+'<td>'+formatTime(currData[i].time)+'</td>'
			    	+'<td>'
					+  '<a class="layui-btn layui-btn-warm layui-btn-mini user_ban" data-id="'+currData[i].userName+'" data-command="noBan"><i class="layui-icon">&#x1005;</i> 解禁</a>'
					+  '<a class="layui-btn layui-btn-danger layui-btn-mini user_ban" data-id="'+currData[i].userName+'" data-command="ban"><i class="layui-icon">&#x1007;</i> 禁用</a>'
					+'</td>'
			    	+'</tr>';
				}
			}else{
				dataHtml = '<tr><td colspan="8">暂无数据</td></tr>';
			}
		    return dataHtml;
		}

		//分页
		var nums = 13; //每页出现的数据量
		if(that){
			newsData = that;
		}
		laypage({
			cont : "page",
			pages : Math.ceil(newsData.length/nums),
			jump : function(obj){
				$(".ban_content").html(renderDate(newsData,obj.curr));
				$('.ban_list thead input[type="checkbox"]').prop("checked",false);
		    	form.render();
			}
		})
	}
})

//格式化时间
function formatTime(_time) {
	if (_time == null || _time == "") return "";
	_time = new Date(parseInt(_time));
    var year = _time.getFullYear();
    var month = _time.getMonth()+1<10 ? "0"+(_time.getMonth()+1) : _time.getMonth()+1;
    var day = _time.getDate()<10 ? "0"+_time.getDate() : _time.getDate();
    var hour = _time.getHours()<10 ? "0"+_time.getHours() : _time.getHours();
    var minute = _time.getMinutes()<10 ? "0"+_time.getMinutes() : _time.getMinutes();
    return year+"-"+month+"-"+day+" "+hour+":"+minute;
}