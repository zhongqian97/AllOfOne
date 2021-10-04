layui.config({
	base : "js/"
}).use(['form','layer','jquery','layedit','laydate'],function(){
	var form = layui.form(),
		layer = parent.layer === undefined ? layui.layer : parent.layer,
		laypage = layui.laypage,
		layedit = layui.layedit,
		laydate = layui.laydate,
		$ = layui.jquery;
		datas = getTask();
 	
 	function getTask() {
		datas = window.sessionStorage.getItem(getQueryString("id"));
		if (datas != null) {
			datas = JSON.parse(datas);
			$("#name").val(datas.name);
			$("#intro").val(datas.intro);
			$("#help").val(datas.help);
			$("#path").val(datas.path);
			$("#jspath").val(datas.jspath);
			$("#show").val(datas.show);
		}
		return datas;
    }
 	
 	function getQueryString(name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return unescape(r[2]);
        return null;
    }
})
