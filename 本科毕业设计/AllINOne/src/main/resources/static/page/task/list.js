layui.config({
	base : "js/"
}).use(['form','layer','jquery','laypage'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		$ = layui.jquery;

	//加载页面数据
	var newsData = '';
	$.get("/task/getTask", function(data){
		var newArray = [];
		newsData = data;
		newsList(newsData);
	})
	
		
	//重刷页面
	$(".reload_btn").click(function(){
		window.location.reload();
	})

	//查询
	$(".search_btn").click(function(){
		var newArray = [];
		if($(".search_input").val() != ''){
			var index = layer.msg('查询中，请稍候',{icon: 16,time:false,shade:0.8});
            setTimeout(function(){
            	$.ajax({
					url : "/task/getTask",
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

	//添加任务
	$(".taskAdd_btn").click(function(){
		var index = layui.layer.open({
			title : "添加任务",
			type : 2,
			content : "taskEdit.html",
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

	//批量删除
	$(".batchDel").click(function(){
		var $checkbox = $('.task_list tbody input[type="checkbox"][name="checked"]');
		var $checked = $('.task_list tbody input[type="checkbox"][name="checked"]:checked');
		if($checkbox.is(":checked")){
			layer.confirm('确定删除选中的信息？',{icon:3, title:'提示信息'},function(index){
				var index = layer.msg('删除中，请稍候',{icon: 16,time:false,shade:0.8});
	            setTimeout(function(){
	            	//删除数据
	            	for(var j=0;j<$checked.length;j++){
	            		for(var i=0;i<newsData.length;i++){
							if(newsData[i].id == $checked.eq(j).parents("tr").find(".task_del").attr("data-id")){
								ajaxSubmit("/task/deleteTask", newsData[i].id, null);
								newsData.splice(i,1);
								newsList(newsData);
							}
						}
	            	}
	            	$('.task_list thead input[type="checkbox"]').prop("checked",false);
	            	form.render();
	                layer.close(index);
					layer.msg("删除成功");
	            },2000);
	        })
		}else{
			layer.msg("请选择需要删除的任务");
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

	//通过判断列表是否全部选中来确定全选按钮是否选中
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

	//是否展示
	form.on('switch(isShow)', function(data){
		var index = layer.msg('修改中，请稍候',{icon: 16,time:false,shade:0.8});
		var _this = $(this);
		ajaxSubmit("/task/showTask", _this.attr("data-id"), function(data) {
			layer.msg(data.information);
			if (data.status == "200") {
				window.location.reload();
			}
		});
	})
 
	//操作
	$("body").on("click",".task_edit",function(){  //编辑
		var _this = $(this);
		console.log(_this);
		var index = layui.layer.open({
			title : "修改任务",
			type : 2,
			content : "taskEdit.html?id=" + _this.attr("data-id"),
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

	$("body").on("click",".task_del",function(){  //删除
		var _this = $(this);
		layer.confirm('确定删除此信息？',{icon:3, title:'提示信息'},function(index){
			//_this.parents("tr").remove();
			ajaxSubmit("/task/deleteTask", _this.attr("data-id"), function(data) {
				layer.msg(data.information);
				if (data.status == "200") {
					for(var i=0;i<newsData.length;i++){
						if(newsData[i].id == _this.attr("data-id")){
							newsData.splice(i,1);
							newsList(newsData);
						}
					}
				}
			});
			layer.close(index);
		});
	})
	function ajaxSubmit(urls, ids, func) {
		$.ajax({
			url : urls,
			type : "post",
			// data表示发送的数据
			data : {
				id   : ids
			},
			success : func,
			error : function(data) {
				layer.msg(data.information);
			}
		});
	}
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
			    	if (currData[i].show == 'true') {
			    		dataHtml += '<td><input type="checkbox" data-id="'+currData[i].id+'" name="show" lay-skin="switch" lay-text="是|否" lay-filter="isShow" checked></td>'
			    	} else {
			    		dataHtml += '<td><input type="checkbox" data-id="'+currData[i].id+'" name="show" lay-skin="switch" lay-text="是|否" lay-filter="isShow"></td>'
			    	}
			    	dataHtml += '<td>'
					+  '<a class="layui-btn layui-btn-mini task_edit" data-id="'+currData[i].id+'"><i class="iconfont icon-edit"></i> 编辑</a>'
					+  '<a class="layui-btn layui-btn-danger layui-btn-mini task_del" data-id="'+currData[i].id+'"><i class="layui-icon">&#xe640;</i> 删除</a>'
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
		var nums = 8; //每页出现的数据量
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
