layui.config({
	base : "js/"
}).use(['form','layer','jquery','laypage'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		$ = layui.jquery;

	//加载页面数据
	var newsData = '';
	$.get("/task/getTaskShare", function(data){
		var newArray = [];
		newsData = data;
    	newsList(newsData);
	})

	//查询
	$(".search_btn").click(function(){
		var newArray = [];
		if($(".search_input").val() != ''){
			var index = layer.msg('查询中，请稍候',{icon: 16,time:false,shade:0.8});
            setTimeout(function(){
            	$.ajax({
					url : "/task/getTaskShare",
					type : "get",
					dataType : "json",
					success : function(newsData){
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
		            		//任务名称
		            		if(newsStr.name.indexOf(selectStr) > -1){
			            		newsStr["name"] = changeStr(newsStr.name);
		            		}
		            		//任务介绍
		            		if(newsStr.intro.indexOf(selectStr) > -1){
			            		newsStr["intro"] = changeStr(newsStr.intro);
		            		}
		            		//功能介绍
		            		if(newsStr.help.indexOf(selectStr) > -1){
			            		newsStr["help"] = changeStr(newsStr.help);
		            		}
		            		//路径
		            		if(newsStr.path.indexOf(selectStr) > -1){
			            		newsStr["path"] = changeStr(newsStr.path);
		            		}
		            		//脚本路径
		            		if(newsStr.jspath.indexOf(selectStr) > -1){
			            		newsStr["jspath"] = changeStr(newsStr.jspath);
		            		}
		            		//所属用户
		            		if(newsStr.user.indexOf(selectStr) > -1){
			            		newsStr["user"] = changeStr(newsStr.user);
		            		}
		            		//是否公开
		            		if(newsStr.show.indexOf(selectStr) > -1){
			            		newsStr["show"] = changeStr(newsStr.show);
		            		}
		            		if(
		            				newsStr.name.indexOf(selectStr)>-1 
		            				|| newsStr.intro.indexOf(selectStr)>-1 
		            				|| newsStr.help.indexOf(selectStr)>-1 
		            				|| newsStr.path.indexOf(selectStr)>-1 
		            				|| newsStr.jspath.indexOf(selectStr)>-1 
		            				|| newsStr.user.indexOf(selectStr)>-1 
		            				|| newsStr.show.indexOf(selectStr)>-1){
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

	//全选
	form.on('checkbox(allChoose)', function(data){
		var child = $(data.elem).parents('table').find('tbody input[type="checkbox"]:not([name="show"])');
		child.each(function(index, item){
			item.checked = data.elem.checked;
		});
		form.render('checkbox');
	});

	//通过判断任务是否全部选中来确定全选按钮是否选中
	form.on("checkbox(choose)",function(data){
		var child = $(data.elem).parents('table').find('tbody input[type="checkbox"]:not([name="show"])');
		var childChecked = $(data.elem).parents('table').find('tbody input[type="checkbox"]:not([name="show"]):checked')
		if(childChecked.length == child.length){
			$(data.elem).parents('table').find('thead input#allChoose').get(0).checked = true;
		}else{
			$(data.elem).parents('table').find('thead input#allChoose').get(0).checked = false;
		}
		form.render('checkbox');
	})
 
	//操作
	$("body").on("click",".task_edit",function(){  //查看
		var _this = $(this);
		var index = layui.layer.open({
			title : "查看任务",
			type : 2,
			content : "seeTask.html?id=" + _this.attr("data-id"),
			success : function(layero, index){
				layui.layer.tips('点击此处返回任务列表', '.layui-layer-setwin .layui-layer-close', {
					tips: 3
				});
			}
		})
		//改变窗口大小时，重置弹窗的高度，防止超出可视区域（如F12调出debug的操作）
		$(window).resize(function(){
			layui.layer.full(index);
		})
		layui.layer.full(index);
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
			    	+'<td><input type="checkbox" name="checked" lay-skin="primary" lay-filter="choose"></td>'
			    	+'<td align="left">'+currData[i].id+'</td>'
			    	+'<td>'+currData[i].name+'</td>'
			    	+'<td>'+currData[i].intro+'</td>'
			    	+'<td>'+currData[i].user+'</td>'
			    	+'<td>'
					+  '<a class="layui-btn layui-btn-mini task_edit" data-id="'+currData[i].id+'"><i class="iconfont icon-edit"></i> 查看</a>'
					+  '<a class="layui-btn layui-btn-normal layui-btn-mini task_collect"><i class="layui-icon">&#xe600;</i> 收藏</a>'
			        +'</td>'
			    	+'</tr>';
			    	window.sessionStorage.setItem(currData[i].id, JSON.stringify(currData[i]));
				}
			}else{
				dataHtml = '<tr><td colspan="8">暂无数据</td></tr>';
			}
		    return dataHtml;
		}

		//分页
		var nums = 5; //每页出现的数据量
		if(that){
			newsData = that;
		}
		laypage({
			cont : "page",
			pages : Math.ceil(newsData.length/nums),
			jump : function(obj){
				$(".task_content").html(renderDate(newsData,obj.curr));
				$('.task_list thead input[type="checkbox"]').prop("checked",false);
		    	form.render();
			}
		})
	}
})
