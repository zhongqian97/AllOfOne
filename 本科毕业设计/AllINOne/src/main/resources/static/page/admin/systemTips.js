layui.config({
	base : "js/"
}).use(['form','layer','jquery','layedit'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		layedit = layui.layedit,
		$ = layui.jquery;

	//创建一个编辑器
 	var editIndex = layedit.build('tips',{
        tool: []
   });
 	form.on("submit(systemTips)",function(data){
 		var index = layer.msg('数据提交中，请稍候',{icon: 16,time:false,shade:0.8});
 		$.ajax({
			url : "/admin/systemTips",
			type : "post",
			data : {
				title : $("#title").val(),
				tips : layedit.getContent(editIndex)
			},
			success : function(data) {
				layer.msg(data.information);
				return false;
			},
			error : function(data) {
				layer.msg(data.information);
				return false;
			}
		});
 		return false;
 	})
	
})
