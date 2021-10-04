layui.config({
	base : "js/"
}).use(['form','layer','jquery','layedit','laydate'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		layedit = layui.layedit,
		laydate = layui.laydate,
		$ = layui.jquery;
	getChannel();
		
	function getChannel() {
		datas = window.sessionStorage.getItem(getQueryString("id"));
		datas = JSON.parse(datas);
		$("#id").val(getQueryString("id"));
		$("#name").val(datas.name);
		$("#task").val(datas.task);
    }
	
	function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
	
 	form.on("submit(inviteChannel)",function(data){
 		var index = top.layer.msg('数据提交中，请稍候',{icon: 16,time:false,shade:0.8});
 		$.ajax({
			url : "/message/sendInviteChannel",
			type : "post",
			data : {
				id : $("#id").val(),
				name : $("#name").val(),
				user : $("#user").val()
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
