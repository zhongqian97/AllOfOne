layui.config({
	base : "js/"
}).use(['form','layer','jquery','laypage'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		$ = layui.jquery;
	while (window.sessionStorage.getItem("_message-ChannelList_") == null); 
	//加载页面数据
	var newsData = JSON.parse(window.sessionStorage.getItem("_message-ChannelList_"));
	var userName = window.sessionStorage.getItem("userName");
	newsList(newsData);
	
	//权限控制
	form.on('select(userRole)', function(data){
		var index = top.layer.msg('修改中，请稍候',{icon: 16,time:false,shade:0.8});
		var _DOM = data.elem;
		ajaxSubmit("/channel/setUserReceiveRole", _DOM.dataset.id, data.value, function(msg) {
			top.layer.msg(msg.information);
			if (msg.status == "200") {
				var datas = JSON.parse(window.sessionStorage.getItem("_message-ChannelList_"));
				for (var i=0; i<datas.length; i++) {
					if (_DOM.dataset.id == datas[i].id) {
						datas[i].roles[userName].receive = data.value;
						break;
					}
				}
				window.sessionStorage.setItem("_message-ChannelList_", JSON.stringify(datas));
				form.render('select');
			}
		});
	})
 
	function ajaxSubmit(urls, ids, roles, func) {
		$.ajax({
			url : urls,
			type : "post",
			data : {
				id   : ids,
				role   : roles
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
					var reject = '', receive = '', important = '', urgent = '';
					var k = data[i].roles[userName].receive;
					if (k == 'reject') {
						reject = 'selected';
					} else if (k == 'receive') {
						receive = 'selected';
					} else if (k == 'important') {
						important = 'selected';
					} else {
						urgent = 'selected';
					} 
					dataHtml += '<tr>'
			    	+ '<td><input type="checkbox" name="checked" lay-skin="primary" lay-filter="choose"></td>'
			    	+ '<td align="left">'+currData[i].id+'</td>'
			    	+ '<td>'+currData[i].name+'</td>'
			    	+ '<td>'+currData[i].builder+'</td>'
			    	+ '<td>'
			    	+ '<select name="role" lay-verify="" lay-filter="userRole" data-id="'+currData[i].id+'">'
			    	+ '	<option value="reject" '+ reject + ' data-id="'+currData[i].id+'">拒绝接收信息</option>'
			    	+ '	<option value="receive" ' + receive + ' data-id="'+currData[i].id+'">可以接收信息</option>' 
			    	+ '	<option value="important" ' + important + ' data-id="'+currData[i].id+'">离线接收信息</option>'
			    	+ '	<option value="urgent" ' + urgent + ' data-id="'+currData[i].id+'">紧急接收信息</option>'
			    	+ '</select>'    
			    	+ '</td>'
			    	+ '</tr>';
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
				$(".role_content").html(renderDate(newsData,obj.curr));
				$('.role_list thead input[type="checkbox"]').prop("checked",false);
		    	form.render();
			}
		})
	}
})
