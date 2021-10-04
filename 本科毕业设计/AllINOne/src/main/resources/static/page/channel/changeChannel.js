layui.config({
	base : "js/"
}).use(['form','layer','jquery','layedit','laydate','laypage','upload'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		layedit = layui.layedit,
		laydate = layui.laydate,
		$ = layui.jquery;
		datas = getChannel();
		// 加载头像等等
		layui.upload({
			url : "/404",
			before : function(){
				const file = document.querySelector('input[type=file]').files[0];
				const reader = new FileReader();
				reader.addEventListener("load", function() {
					$("#picture").val(reader.result);// 将转换后的编码保存到input供后台使用
					$("#userFace").attr("src", reader.result);
				}, false);
				if (file) {
					reader.readAsDataURL(file);
				}
				layer.msg("图片加载成功，点击提交即可保存！");
			}
		});
	
		function getChannel() {
			datas = window.sessionStorage.getItem(getQueryString("id"));
			datas = JSON.parse(datas);
			$("#name").val(datas.name);
			$("#task").val(datas.task);
			$("#picture").val(datas.picture);
			$("#userFace").attr("src", datas.picture);
			var newArray = [];
			data = getObjectToArray(datas.roles);
			newsData = data;
			rolesList();
			return datas;
	    }
		
		function getObjectToArray(obj) {
			datas = []
			for (var i in obj) {
				data = '{"key":"'+ i + '","value":' + JSON.stringify(obj[i]) + '}';
				datas.push(JSON.parse(data));
			}
			return datas;
		}
		
	function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
	
 	form.on("submit(changeChannel)",function(data){
 		var index = top.layer.msg('数据提交中，请稍候',{icon: 16,time:false,shade:0.8});
 		$.ajax({
			url : "/channel/updateChannel",
			type : "post",
			// data表示发送的数据
			data : JSON.stringify({
				"${_csrf.parameterName}":"${_csrf.token}",
				id   : getQueryString("id"),
				name : $("#name").val(),
				task : $("#task").val(),
				picture : $("#picture").val()
			}),
			// 定义发送请求的数据格式为JSON字符串
			contentType : "application/json;charset=UTF-8",
			// 定义回调响应的数据格式为JSON字符串,该属性可以省略
			dataType : "json",
			// 成功响应的结果
			success : function(data) {
				top.layer.msg(data.information);
				if (data.status == "200") {
					setTimeout(function(){
						top.layer.close(index);
			 			layer.closeAll("iframe");
			 			parent.location.reload();
		            }, 2000);
				}
				return false;
			},
			error : function(data) {
				top.layer.msg(data.information);
			}
		});
 		return false;
 	})
 	
 	$("body").on("click",".channel_roles",function(){  //reader
		var _this = $(this);
		$.ajax({
			url : "/channel/changeChannelRole",
			type : "post",
			data : {
				id   : getQueryString("id"),
				user : _this.attr("data-id"),
				role : _this.attr("data-role")
			},
			success : function(data) {
				top.layer.msg(data.information);
				if (data.status == "200") {
					datas = window.sessionStorage.getItem(getQueryString("id"));
					datas = JSON.parse(datas);
					if (_this.attr("data-role") == "pop"){
						delete datas.roles[_this.attr("data-id")];
					} else {
						datas.roles[_this.attr("data-id")].send = _this.attr("data-role");
					}
					window.sessionStorage.setItem(getQueryString("id"), JSON.stringify(datas));
					window.location.reload();
				}
			},
			error : function(data) {
				top.layer.msg(data.information);
			}
		});
	})
 	
 	function rolesList(that){
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
			    	+'<td align="left">'+currData[i].key+'</td>'
			    	+'<td>'+currData[i].value.send+'</td>'
			    	+'<td>'
					+  '<a class="layui-btn layui-btn-mini channel_roles" data-id="'+currData[i].key+'"data-role="writer"><i class="iconfont icon-edit"></i> 可读写成员组</a>'
					+  '<a class="layui-btn layui-btn-mini channel_roles" data-id="'+currData[i].key+'"data-role="reader"><i class="iconfont icon-edit"></i> 只读成员组</a>'
					+  '<a class="layui-btn layui-btn-danger layui-btn-mini channel_roles" data-id="'+currData[i].key+'"data-role="pop"><i class="layui-icon">&#xe640;</i> 踢出用户</a>'
					+'</td>'
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
				$(".channel_content").html(renderDate(newsData,obj.curr));
				$('.channel_list thead input[type="checkbox"]').prop("checked",false);
		    	form.render();
			}
		})
	}
})


