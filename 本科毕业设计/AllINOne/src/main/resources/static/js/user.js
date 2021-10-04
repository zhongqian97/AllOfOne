var $form;
var form;
var $;
layui.config({
	base : "../../js/"
}).use(['form','layer','upload','laydate'],function(){
	form = layui.form();
	var layer = parent.layer === undefined ? layui.layer : parent.layer;
		$ = layui.jquery;
		$form = $('form');
		laydate = layui.laydate;

        //添加验证规则
        form.verify({
        	userName : function(value, item){
            	if(value.length < 6){
                    return "用户名长度不能小于6位";
                }
            },
            oldPwd : function(value, item){
            	if(value.length < 6){
                    return "密码长度不能小于6位";
                }
            },
            newPwd : function(value, item){
                if(value.length < 6){
                    return "密码长度不能小于6位";
                }
            },
            confirmPwd : function(value, item){
                if($("#newPwd").val() != $("#confirmPwd").val()){
                    return "两次输入密码不一致，请重新输入！";
                }
            }
        })
        
        //找回密码
        form.on("submit(findPassword)",function(data){
        	var index = layer.msg('提交中，请稍候',{icon: 16,time:false,shade:0.8});
        	$.ajax({
    			url : "findPassword",
    			type : "post",
    			// data表示发送的数据
    			data : {
    				"${_csrf.parameterName}":"${_csrf.token}",
    				userName : $("#userName").val(),
    				password : $("#newPwd").val(),
    				code : $("#code").val()
    			},
    			success : function(data) {
    				layer.msg(data.information);
    				if (data.status == "200") {
    					window.location.href = 'index.html';
    					return true;
    				}
    				return false;
    			},
    			error : function(data) {
    				layer.msg(data.information);
    			}
    		});
        	return false; //阻止表单跳转。如果需要表单跳转，去掉这段即可。
        })
        
        //注册
        form.on("submit(register)",function(data){
        	var index = layer.msg('提交中，请稍候',{icon: 16,time:false,shade:0.8});
        	$.ajax({
    			url : "register",
    			type : "post",
    			// data表示发送的数据
    			data : JSON.stringify({
    				"${_csrf.parameterName}":"${_csrf.token}",
    				userName : $("#userName").val(),
    				password : $("#newPwd").val()
    			}),
    			// 定义发送请求的数据格式为JSON字符串
    			contentType : "application/json;charset=UTF-8",
    			// 定义回调响应的数据格式为JSON字符串,该属性可以省略
    			dataType : "json",
    			// 成功响应的结果
    			success : function(data) {
    				layer.msg(data.information);
    				if (data.status == "200") {
    					window.location.href = 'index.html';
    					return true;
    				}
    				return false;
    			},
    			error : function(data) {
    				layer.msg(data.information);
    			}
    		});
        	return false; //阻止表单跳转。如果需要表单跳转，去掉这段即可。
        })
        
        //登录
        form.on("submit(login)",function(data){
        	var index = layer.msg('提交中，请稍候',{icon: 16,time:false,shade:0.8});
        	$.ajax({
    			url : "login",
    			type : "post",
    			// data表示发送的数据
    			data : JSON.stringify({
    				"${_csrf.parameterName}":"${_csrf.token}",
    				userName : $("#userName").val(),
    				password : $("#newPwd").val()
    			}),
    			// 定义发送请求的数据格式为JSON字符串
    			contentType : "application/json;charset=UTF-8",
    			// 定义回调响应的数据格式为JSON字符串,该属性可以省略
    			dataType : "json",
    			// 成功响应的结果
    			success : function(data) {
    				layer.msg(data.information);
    				if (data.status == "200") {
    					window.sessionStorage.setItem("userName", $("#userName").val());
    					window.sessionStorage.setItem("_user-role_", data.object);
    					window.location.href = 'main.html';
    					return true;
    				}
    				return false;
    			},
    			error : function(data) {
    				layer.msg(data.information);
    			}
    		});
        	return false; //阻止表单跳转。如果需要表单跳转，去掉这段即可。
        })
        
})

function checkEmail(){
        	$.ajax({
    			url : "checkEmail",
    			type : "get",
    			// data表示发送的数据
    			data : {
    				"${_csrf.parameterName}":"${_csrf.token}",
    				userName : $("#userName").val(),
    				email : $("#email").val()
    			},
    			// 定义发送请求的数据格式为JSON字符串
    			contentType : "application/json;charset=UTF-8",
    			// 定义回调响应的数据格式为JSON字符串,该属性可以省略
    			dataType : "json",
    			// 成功响应的结果
    			success : function(data) {
    				layer.msg(data.information);
    			},
    			error : function(data) {
    				layer.msg(data.information);
    			}
    		});
        }
