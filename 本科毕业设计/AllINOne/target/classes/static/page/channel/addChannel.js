layui.config({
	base : "js/"
}).use(['form','layer','jquery','layedit','laydate','upload'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		layedit = layui.layedit,
		laydate = layui.laydate,
		$ = layui.jquery;
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
			top.layer.msg("图片加载成功，点击提交即可保存！");
		}
	});
	
 	form.on("submit(addChannel)",function(data){
 		var index = top.layer.msg('数据提交中，请稍候',{icon: 16,time:false,shade:0.8});
 		$.ajax({
			url : "/channel/updateChannel",
			type : "post",
			// data表示发送的数据
			data : JSON.stringify({
				"${_csrf.parameterName}":"${_csrf.token}",
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
	
})
