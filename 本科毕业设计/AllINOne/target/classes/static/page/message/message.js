var $, form, layedit, layer, userName, body, ws, pages, id, taskId, wsReady, messageTableFlag, dataLoadFlag;

layui.config({
	base : "../../js/"
}).use(['form','layer','layedit'],function(){
    	form = layui.form(),
        layer = parent.layer === undefined ? layui.layer : parent.layer,
        layedit = layui.layedit;
        $ = layui.jquery;
        
        //加载自己名称
        userName = window.sessionStorage.getItem("userName");
        
        //加载ws
        body = null;
        wsReady = false;
        messageTableFlag = false;
        dataLoadFlag = false;
        ws = websockets();
        
        //加载页面
        pages = messageTable();
        id = '';
        taskId = '';
})

//在线通知提醒
function showNotice(title, text) {
	console.log(window.Notification);
	console.log(Notification.permission);
	if (window.Notification) {
		if(Notification.permission != "denied") {
		    Notification.requestPermission(function(status) {
		        var n = new Notification(title, { body: text }); 
		    });
		    return;
		}
	}
	layer.tips(text, '.layui-layer-setwin .layui-layer-close', {
		tips: 1
	});
}

//频道列表加载
function channelList(data) {
	var msgHtml = '';
    for (var i=0; i<data.length; i++) {
    	var pic = data[i].picture;
    	if (pic == null || pic == '') pic = "../../images/face.jpg";
        msgHtml += '<tr>';
        msgHtml += '  <td class="msg_info">';
        msgHtml += '    <img src="'+pic+'" width="50" height="50"><input type="hidden" value="'+data[i].id+'">';
        msgHtml += '    <div class="user_info">';
        msgHtml += '        <h2>频道名称：'+data[i].name+'</h2>';
        msgHtml += '        <h3>频道主管：'+data[i].builder+'</h3>';
        msgHtml += '        <p>本频道人员：</p>';
        msgHtml += '        <p>';
        for (var r in data[i].roles) {
        	if (data[i].roles[r].send != "push") {
        		loadInformation(r);
        		msgHtml += r;
            	msgHtml += '；';
        	}
        }
        msgHtml += '</p>';
        msgHtml += '    </div>';
        msgHtml += '  </td>';
        msgHtml += '  <td class="msg_opr">';
        msgHtml += '    <a class="layui-btn layui-btn-mini reply_msg"><i class="layui-icon">&#xe611;</i> 进入频道</a>';
        msgHtml += '    <a class="layui-btn layui-btn-mini layui-btn-danger msg_exit" data-id="'+data[i].id+'"><i class="layui-icon">&#xe640;</i> 退出频道</a>';
        msgHtml += '    <a class="layui-btn layui-btn-mini layui-btn-warm task_see" data-id="'+data[i].task+'"><i class="layui-icon">&#xe60a;</i> 查看任务</a>';
        msgHtml += '    <a class="layui-btn layui-btn-mini layui-btn-normal msg_role" data-id="'+data[i].id+'"><i class="layui-icon">&#xe620;</i> 频道权限</a>';
        msgHtml += '  </td>';
        msgHtml += '</tr>';
    }
    $(".msgHtml").empty();
    $(".msgHtml").html(msgHtml);
}

//加载离线消息
function channelOfflineMessage(data) {
	var msgReplyHtml = '';
    for (var i=0; i<data.length; i++) {
    	var user = null;
    	if (window.sessionStorage.getItem("user_"+ data[i].sender) != "") {
    		user = JSON.parse(window.sessionStorage.getItem("user_"+ data[i].sender));
    	}
    	var pic;
    	if (user == null || user == "") pic = "../../images/face.jpg";
    	else pic = user.picture;
    	msgReplyHtml += '<tr>';
        msgReplyHtml += '  <td class="msg_info">';
        msgReplyHtml += '    <img src="'+pic+'" name="'+ data[i].sender + '" width="50" height="50">';
        msgReplyHtml += '    <div class="user_info">';
        msgReplyHtml += '        <h2>'+data[i].sender+'</h2>';
        msgReplyHtml += '        <h2>'+formatTime(data[i].time)+'</h2>';
        if (dataLoadFlag) {
        	msgReplyHtml += dataLoadInformation(data[i].data);
        } else {
            msgReplyHtml += '        <p>'+data[i].data+'</p>';
        }
        msgReplyHtml += '    </div>';
        msgReplyHtml += '  </td>';
        msgReplyHtml += '</tr>';
    }
    if (msgReplyHtml != '') {
        $(".msgReplyHtml").empty();
        $(".msgReplyHtml").html(msgReplyHtml);
    }
}

//websocket建立专用函数
function websockets(ws){
	if (!WebSocket) {
		layer.msg("该浏览器不支持websocket");
	} else {
		if (ws == null || ws.readyState != 1) {
			ws = new WebSocket((location.protocol.indexOf('https') == -1 ? 'ws://': 'wss://')
					+ location.hostname + ':5433/ws');

			ws.onerror = function(e) {
				layer.msg('发生错误，错误原因如下: ' + e.message);
			}

			ws.onopen = function() {
				layer.msg("客户端成功连接，等待上线！");
			}

			ws.onclose = function() {
				layer.msg("客户端已经断开连接！");
			}

			ws.onmessage = function(d) {
				data = JSON.parse(d.data);
				
				//频道注册
				if (data.type == "ChannelRegister") {
					toRegister(data.data);
					return;
				}
				
				//通知消息
				if (data.type == "Notice") {
					showNotice(data.type, data.data);
					return;
				}
				
				//切记：使用时注意是数组还是字符串，不然直接爆炸。
				if (data.type == "ChannelList") {
					window.sessionStorage.setItem("_message-ChannelList_", data.data);
					channelList(JSON.parse(data.data));
					return;
				}
				
				//切记：使用时注意是数组还是字符串，不然直接爆炸。
				if (data.type == "loadInformation") {
					if (data.data == "") {
						window.sessionStorage.setItem("user_" + data.task, "");
						return;
					}
					var datas = JSON.parse(data.data);
					window.sessionStorage.setItem("user_" + datas.userName, data.data);
					return;
				}
				
				//加载频道
				if (data.type == "ChannelAdd") {
					//通知
					showNotice(data.type, data.data);
					
					//保存
					list = window.sessionStorage.getItem("_message-ChannelList_");
					list = JSON.parse(list);
					if (list == null || list.length == 0) list = [];
					list.push(JSON.parse(data.task));// 频道信息在任务中
					window.sessionStorage.setItem("_message-ChannelList_", JSON.stringify(list));
					
					//刷新显示
					if (pages == 'messageTable') 
						channelList(list);
					return;
				}
				
				//删除频道
				if (data.type == "ChannelDel") {
					showNotice(data.type, data.data);
					//保存
					list = window.sessionStorage.getItem("_message-ChannelList_");
					list = JSON.parse(list);
					for (var i = 0 ; i < list.length ; i ++ ) {
						if (data.task == list[i].id) { //频道ID在任务中
							list.splice(i, 1);
							break;
						}
					}
					window.sessionStorage.setItem("_message-ChannelList_", JSON.stringify(list));
					
					//显示
					if (pages == 'messageTable') 
						channelList(list);
					return;
				}
				
				// 离线消息加载
				if (data.type == "ChannelMessage") {
					var datas = JSON.parse(data.data);
					datas.sort(messageSortRuleByTime);
					window.sessionStorage.setItem("message_" + data.addressee, JSON.stringify(datas));
					channelOfflineMessage(datas);
					return;
				}
				
				//信息加载
				if (data.addressee != "") {
					//消息保存
					var datas = window.sessionStorage.getItem("message_" + id);
					datas = JSON.parse(datas);
					if (datas == null) {
						var datas = '{"id":"","type":"ChannelMessage","task":"","data":"","time":"","sender":"","addressee":"'+id+'"}';
				        ws.send(datas);
						datas = [];
					}
					datas.push(data);
					window.sessionStorage.setItem("message_" + data.addressee, JSON.stringify(datas));
					
					if(data.addressee == id && pages == 'messageRepTable') {
						showMessage(data);
						return;
					}

					//消息提醒
					var list = null;
					if (window.sessionStorage.getItem("_message-ChannelList_") != "") 
						list = JSON.parse(window.sessionStorage.getItem("_message-ChannelList_"));
					for (var l in list) {
						if (data.addressee == list[l].id) {
							showNotice(data.type, list[l].name + '频道有你一条新消息');
							break;
						}
					}
					
				}
			}

		} else {
			layer.msg("客户端失去连接！请重启页面");
			ws.close();
		}
	}
	return ws;
}

function loadInformation(userName) {
	if (window.sessionStorage.getItem("user_"+ userName) != null) return;
	var datas = '{"id":"","type":"loadInformation","task":"","data":"' + userName + '","time":"","sender":"","addressee":""}';
    ws.send(datas);
}

//显示频道详细消息
function showMessage(data) {
	var replyHtml = '';
	
	var user = null;
	if (window.sessionStorage.getItem("user_"+ data.sender) != "")
		user = JSON.parse(window.sessionStorage.getItem("user_"+ data.sender));
	var pic;
	if (user == null || user == "") pic = "../../images/face.jpg";
	else pic = user.picture;
    replyHtml += '<tr>';
    replyHtml += '  <td class="msg_info">';
    replyHtml += '    <img src="'+pic+'" name="'+ data.sender + '" width="50" height="50">';
    replyHtml += '    <div class="user_info">';
    replyHtml += '        <h2>' + data.sender + '</h2>';
    replyHtml += '        <h2>' + formatTime(data.time) + '</h2>';
    if (dataLoadFlag) {
    	replyHtml += dataLoadInformation(data.data);
    } else {
    	replyHtml += '        <p>'+data.data+'</p>';
    }
    replyHtml += '    </div>';
    replyHtml += '  </td>';
    replyHtml += '</tr>';
    

    $(".msgReplyHtml").append(replyHtml);
    var scrollHeight = $('.msgReplyHtml').prop("scrollHeight");
    $('.msgReplyHtml').animate({scrollTop:scrollHeight}, 400);
}

//注册频道
function toRegister(id) {
	$.ajax({
		url : "/channel/register",
		type : "post",
		data : {
			"${_csrf.parameterName}" : "${_csrf.token}",
			channelId : id
		},
		dataType : "json",
		success : function(data) {
			var datas = '{"id":"","type":"ChannelList","task":"","data":"","time":"","sender":"","addressee":""}';
	        ws.send(datas);
	        wsReady = true;
			layer.msg(data.information + '频道列表正在加载中');
		},
		error : function(data) {
			layer.msg(data.information);
		}
	});
}

//表格切换到频道信息列表
function messageTable() {
	var t = '';
	t += '<form class="layui-form">';
	t += '	<table class="layui-table msg_box" lay-skin="line">';
	t += '		<colgroup>';
	t += '			<col>';
	t += '			<col width="15%">';
	t += '		</colgroup>';
	t += '		<tbody class="msgHtml" id="msgHtml"></tbody>';
	t += '	</table>';
	t += '</form>';
    $(".msg_body").empty();
    $(".msg_body").html(t);
    
    if (wsReady == true && messageTableFlag) {
    	var datas = window.sessionStorage.getItem("_message-ChannelList_");
    	if (datas != "")
    		datas = JSON.parse(datas);
    	channelList(datas);
    }
    
    messageTableFlag = true; // 第一次加载阀门值
    dataload = false;
    
    //频道信息加载
    $("body").on("click",".reply_msg,.msgHtml .user_info h2,.msgHtml .msg_info>img",function(){
        id = $(this).parents("tr").find("input[type=hidden]").val();
        var text = $(this).parents("tr").find(".user_info h2").text();
        pages = messageRepTable(text);
    })
    //退出频道
    $("body").on("click",".msg_exit",function(){
    	var _this = $(this);
    	layer.confirm('确定退出此频道？',{icon:3, title:'提示信息'},function(index){
			ajaxSubmit("/channel/exitChannel", _this.attr("data-id"), function(data) {
				layer.msg(data.information);
				if (data.status == "200") {
					window.location.reload();
				}
			});
		});
    })
    
    //查看任务
    $("body").on("click",".task_see",function(){  
		var _this = $(this);
		ajaxSubmit("/task/seeTask", _this.attr("data-id"), function(data) {
			layer.msg(data.information);
			if (data.status == "200") {
				window.sessionStorage.setItem(_this.attr("data-id"), JSON.stringify(data.object));
				//显示页面
				var index = layui.layer.open({
					title : "查看任务",
					type : 2,
					content : "../task/seeTask.html?id=" + _this.attr("data-id"),
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
			}
		});
	})
	
	//用户权限
    $("body").on("click",".msg_role",function(){  
		var _this = $(this);
		var index = layui.layer.open({
			title : "用户频道权限",
			type : 2,
			content : "userRole.html?id=" + _this.attr("data-id"),
			success : function(layero, index){
				layui.layer.tips('点击此处返回频道列表', '.layui-layer-setwin .layui-layer-close', {
					tips: 3
				});
			}
		})
		$(window).resize(function() {
			layui.layer.full(index);
		})
		layui.layer.full(index);
	})

    return 'messageTable';
}

//表格切换到频道信息详情
function messageRepTable(text) {
	var t = '';
	t += '<form class="layui-form">';
	t += '	<a class="layui-btn exit">'+ text +'：点击此处退出</a>';
	t += '	<a class="layui-btn getMessage">点击获取历史消息</a>';
	t += '	<table class="layui-table msg_box rep" lay-skin="line">';
	t += '		<colgroup>';
	t += '			<col>';
	t += '		</colgroup>';
	t += '		<tbody class="msgReplyHtml rep" id="msgReplyHtml"></tbody>';
	t += '	</table>';
	t += '</form>';
	t += '<div id="dataController"></div>'
	t += '<div class="replay_edit">'
	t += '	<textarea class="layui-textarea" id="msgReply"></textarea>';
	t += '	<a class="layui-btn send_msg">发送</a>';
	t += '</div>';
    $(".msg_body").empty();
    $(".msg_body").html(t);
    
    var datas = window.sessionStorage.getItem("message_" + id);
    var list = window.sessionStorage.getItem("_message-ChannelList_");
	list = JSON.parse(list);
	for (var i = 0 ; i < list.length ; i ++ ) {
		if (id == list[i].id) { 
			taskId = list[i].task;
			dataLoadFlag = loadJS("dataLoad", "/task/downloadTask?taskId=" + list[i].task);
			break;
		}
	}
    if (datas == null || datas.length <= 0) {
    	var datas = '{"id":"","type":"ChannelMessage","task":"","data":"","time":"","sender":"","addressee":"'+id+'"}';
        ws.send(datas);
    } else {
    	datas = JSON.parse(datas);
    	channelOfflineMessage(datas);
    }
    
    if (dataLoadFlag) {//数据渲染数据
    	var t = dataController();
    	$("#dataController").empty();
        $("#dataController").html(t);
    } else {
    	layer.msg("无法找到该频道的任务，该任务可能已被删除，请联系频道管理员重新设置频道任务！");
    }
    
    //退出
    $(".exit").click(function(){
    	pages = messageTable();
    });
    
    //获取之前消息
    $(".getMessage").click(function(){
    	var datas = '{"id":"","type":"ChannelMessage","task":"","data":"","time":"","sender":"","addressee":"'+id+'"}';
        ws.send(datas);
    });
    
    //消息回复 加载编辑器
    var editIndex = layedit.build('msgReply',{
         tool: [],
         height:100
    });
    
    //提交消息
    $(".send_msg").click(function(){
        if(layedit.getContent(editIndex) != ''){
        	if (wsReady == true) {
        		var data = '{"id":"","type":"text","task":"text","data":"' + layedit.getContent(editIndex)
            	+ '","time":"","sender":"' +  userName
            	+ '","addressee":"' + id + '"}';
            	 ws.send(data);
        	}
            $("#LAY_layedit_1").contents().find("body").html('');
        }else{
            layer.msg("请输入回复信息");
        }
    });
    return 'messageRepTable';
}

//消息排序规则
function messageSortRuleByTime(a, b) {
	  return parseInt(a.time) - parseInt(b.time); // 如果a>=b，返回自然数，不用交换位置
}

//ajax提交器
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

//发送信息
function sendData(data) {
	var msg = '{"id":"","type":"UseTask","task":"' + taskId
	+ '","data":"","time":"","sender":"' +  userName
	+ '","addressee":"' + id + '"}';
	msg = JSON.parse(msg);
	msg.data = data;
	ws.send(JSON.stringify(msg));
}
