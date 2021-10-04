layui.config({
	base : "js/"
}).use(['form','layer','jquery','laydate','upload'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		laydate = layui.laydate,
		$ = layui.jquery;
		datas = getDevice();
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
	
	function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
	
	function getDevice() {
		$.ajax({
			url : "/device/getDeviceInfo",
			type : "post",
			data : {
				userName   : getQueryString("id")
			},
			success : function(data) {
				$("#name").val(data.name);
				$("#picture").val(data.picture);
				$("#userFace").attr("src", data.picture);
				return datas;
			}
		});
	}
	
 	form.on("submit(changeDevice)",function(data){
 		var index = top.layer.msg('数据提交中，请稍候',{icon: 16,time:false,shade:0.8});
 		$.ajax({
			url : "/device/changeDevice",
			type : "post",
			// data表示发送的数据
			data : {
				userName   : getQueryString("id"),
				name : $("#name").val(),
				password : $("#password").val(),
				picture : $("#picture").val()
			},
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
		

