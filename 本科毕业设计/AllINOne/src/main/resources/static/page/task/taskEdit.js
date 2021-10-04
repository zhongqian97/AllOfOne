layui.config({
	base : "js/"
}).use(['form','layer','jquery','layedit','laydate','upload'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		layedit = layui.layedit,
		laydate = layui.laydate,
		$ = layui.jquery;
		datas = getTask();
		layui.upload({
			elem:'.path_file',
			url: '/404', 
			before: function(res) {
		 		var index = top.layer.msg('数据正在后台提交，请不要关闭页面',{icon: 16,time:false,shade:0.8});
				var datas = new FormData();
		 	    datas.append("file", document.getElementById("path_file").files[0]);
		 	    uploadFile(datas, function(data) {
		 	    	$("#path").val(data.object);
					top.layer.msg(data.information);
			    });
			}
		});   
		layui.upload({
			elem:'.jspath_file',
			url: '/404', 
			before: function(res) {
		 		var index = top.layer.msg('数据正在后台提交，请不要关闭页面',{icon: 16,time:false,shade:0.8});
				var datas = new FormData();
		 	    datas.append("file", document.getElementById("jspath_file").files[0]);
		 	    uploadFile(datas, function(data) {
		 	    	$("#jspath").val(data.object);
					top.layer.msg(data.information);
			    });
			}
		}); 

	//创建一个编辑器
 	var editIndex = layedit.build('task_content');
 	
 	function getTask() {
		datas = window.sessionStorage.getItem(getQueryString("id"));
		if (datas != null) {
			datas = JSON.parse(datas);
			$("#name").val(datas.name);
			$("#intro").val(datas.intro);
			$("#help").val(datas.help);
			$("#path").val(datas.path);
			$("#jspath").val(datas.jspath);
			if (datas.show == "true") {
				$('input:radio').eq(0).attr('checked', 'true');	
			} else {
				$('input:radio').eq(1).attr('checked', 'true');
			}
			form.render('radio');
		}
		return datas;
    }
 	
 	function uploadFile(datas, func) {
 		$.ajax({
 	        type: "POST",
 	        enctype: 'multipart/form-data',
 	        url: "/task/uploadFiles",
 	        data: datas,
 	        processData: false, 
 	        contentType: false,
 	        cache: false,
 	        success: func,
 	        error: function (data) {
				top.layer.msg(data.information);
 	        }
 	    });

 	}
 	
 	form.on("submit(task)",function(data){

//	 	//显示、审核状态
// 		var isShow = data.field.show=="on" ? "checked" : "",
// 			newsStatus = data.field.shenhe=="on" ? "审核通过" : "待审核";

 		var index = top.layer.msg('数据提交中，请稍候',{icon: 16,time:false,shade:0.8});
 		$.ajax({
			url : "/task/taskEdit",
			type : "post",
			// data表示发送的数据
			data : JSON.stringify({
				"${_csrf.parameterName}":"${_csrf.token}",
				id : getQueryString("id"),
				name : $("#name").val(),
				intro : $("#intro").val(),
				help : $("#help").val(),
				path : $("#path").val(),
				jspath : $("#jspath").val(),
				show : $("input[name='show']:checked").val()
			}),
			// 定义发送请求的数据格式为JSON字符串
			contentType : "application/json;charset=UTF-8",
			// 定义回调响应的数据格式为JSON字符串,该属性可以省略
			dataType : "json",
			// 成功响应的结果
			success : function(data) {
				top.layer.msg(data.information);
				if (data.status == "200") {
					top.layer.close(index);
		 			layer.closeAll("iframe");
		 			parent.location.reload();
				}
				return false;
			},
			error : function(data) {
				top.layer.msg(data.information);
			}
		});
 		return false;
 	})
	
 	function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
})
