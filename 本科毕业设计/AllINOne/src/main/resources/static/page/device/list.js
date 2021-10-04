layui.config({
	base : "js/"
}).use(['form','layer','jquery','laypage'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		$ = layui.jquery;
		
	//加载页面数据
	var newsData = '';
	$.get("/device/getDevice", function(data){
		var newArray = [];
		newsData = data;
		newsList();
	})
	
	//重刷页面
	$(".reload_btn").click(function(){
		window.location.reload();
	})
	
	//添加设备
	$(".deviceAdd_btn").click(function(){
		$.ajax({
			url : "/device/addDevice",
			type : "post",
			success : function(data) {
				layer.msg(data.information);		
				window.location.reload();
			},
			error : function(data) {
				layer.msg(data.information);
			}
		});
	})

	//批量删除
	$(".batchDel").click(function(){
		var $checkbox = $('.device_list tbody input[type="checkbox"][name="checked"]');
		var $checked = $('.device_list tbody input[type="checkbox"][name="checked"]:checked');
		if($checkbox.is(":checked")){
			layer.confirm('确定删除选中的信息？',{icon:3, title:'提示信息'},function(index){
				var index = layer.msg('删除中，请稍候',{icon: 16,time:false,shade:0.8});
	            setTimeout(function(){
	            	//删除数据
	            	for(var j=0;j<$checked.length;j++){
	            		for(var i=0;i<newsData.length;i++){
							if(newsData[i].userName == $checked.eq(j).parents("tr").find(".device_del").attr("data-id")){
								deleteDevice(newsData[i].userName, null);
								newsData.splice(i,1);
								newsList(newsData);
							}
						}
	            	}
	            	$('.device_list thead input[type="checkbox"]').prop("checked",false);
	            	form.render();
	                layer.close(index);
					layer.msg("删除成功");
	            },2000);
	        })
		}else{
			layer.msg("请选择需要删除的设备");
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

	//通过判断设备是否全部选中来确定全选按钮是否选中
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
	$("body").on("click",".device_edit",function(){  //编辑
		var _this = $(this);
		var index = layui.layer.open({
			title : "修改设备",
			type : 2,
			content : "changeDevice.html?id=" + _this.attr("data-id"),
			success : function(layero, index){
				layui.layer.tips('点击此处返回设备列表', '.layui-layer-setwin .layui-layer-close', {
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

	$("body").on("click",".device_del",function(){  //删除
		var _this = $(this);
		layer.confirm('确定删除此信息？',{icon:3, title:'提示信息'},function(index){
			deleteDevice(_this.attr("data-id"), function(data) {
				for(var i=0;i<newsData.length;i++){
					if(newsData[i].userName == _this.attr("data-id")){
						newsData.splice(i,1);
						newsList(newsData);
					}
				}
				layer.msg(data.information);
			});
			layer.close(index);
		});
	})
	
	function deleteDevice(ids, func) {
		$.ajax({
			url : "/device/deleteDevice",
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
			    	+'<td align="left">'+currData[i].userName+'</td>'
			    	+'<td>'+currData[i].ownedUser+'</td>'
			    	+'<td>'+currData[i].role+'</td>'
			    	+'<td>'
					+  '<a class="layui-btn layui-btn-mini device_edit" data-id="'+currData[i].userName+'"><i class="iconfont icon-edit"></i> 编辑</a>'
					+  '<a class="layui-btn layui-btn-danger layui-btn-mini device_del" data-id="'+currData[i].userName+'"><i class="layui-icon">&#xe640;</i> 删除</a>'
			        +'</td>'
			    	+'</tr>';
			    	window.sessionStorage.setItem(currData[i].userName, JSON.stringify(currData[i]));

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
				$(".device_content").html(renderDate(newsData,obj.curr));
				$('.device_list thead input[type="checkbox"]').prop("checked",false);
		    	form.render();
			}
		})
	}
})
