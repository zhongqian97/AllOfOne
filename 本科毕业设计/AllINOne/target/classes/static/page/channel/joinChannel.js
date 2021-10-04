layui.config({
	base : "js/"
}).use(['form','layer','jquery','layedit','laydate'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		layedit = layui.layedit,
		laydate = layui.laydate,
		$ = layui.jquery;

 	form.on("submit(joinChannel)",function(data){
 		var index = top.layer.msg('数据提交中，请稍候',{icon: 16,time:false,shade:0.8});
 		$.ajax({
			url : "/channel/joinChannel",
			type : "post",
			// data表示发送的数据
			data : JSON.stringify({
				"${_csrf.parameterName}":"${_csrf.token}",
				id : $("#id").val(),
				name : $("#name").val()
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
	
})
