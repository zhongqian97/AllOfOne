var $form;
var form;
var $;
layui.config({
	base : "../../js/"
}).use([ 'form', 'layer', 'upload', 'laydate' ], function() {
	form = layui.form();
	var layer = parent.layer === undefined ? layui.layer : parent.layer;
	$ = layui.jquery;
	$form = $('form');
	laydate = layui.laydate;
	downLoadInfo();
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

	// 添加验证规则
	form.verify({
		userName : function(value, item) {
			if (value.length < 6) {
				return "用户名长度不能小于6位";
			}
		},
		oldPwd : function(value, item) {
			if (value.length < 6) {
				return "密码长度不能小于6位";
			}
		},
		newPwd : function(value, item) {
			if (value.length < 6) {
				return "密码长度不能小于6位";
			}
		},
		phone : function(value, item) {
			if (value.length < 11) {
				return "手机号码长度不能小于11位";
			}
		},
		confirmPwd : function(value, item){
            if($("#newPwd").val() != $("#confirmPwd").val()){
                return "两次输入密码不一致，请重新输入！";
            }
        }
	})

	// 提交个人资料
	form.on("submit(changeUser)", function(data) {
		var index = layer.msg('提交中，请稍候', {
			icon : 16,
			time : false,
			shade : 0.8
		});
		upLoadInfo();
		layer.close(index);
		return false; // 阻止表单跳转。如果需要表单跳转，去掉这段即可。
	})

	// 修改密码
	form.on("submit(changePwd)", function(data) {
		var index = layer.msg('提交中，请稍候', {
			icon : 16,
			time : false,
			shade : 0.8
		});
		$.ajax({
			url : "/user/changePassword",
			type : "post",
			// data表示发送的数据
			data : {
				oldPwd : $("#oldPwd").val(),
				newPwd : $("#newPwd").val()
			},
			success : function(data) {
				layer.msg(data.information);
			},
			error : function(data) {
				layer.msg(data.information);
			}
		});
		layer.close(index);
		$(".pwd").val('');
		return false; // 阻止表单跳转。如果需要表单跳转，去掉这段即可。
	})
	
	function upLoadInfo() {
		$.ajax({
			url : "/user/upLoadInfo",
			type : "post",
			// data表示发送的数据
			data : JSON.stringify({
				"${_csrf.parameterName}" : "${_csrf.token}",
				name : $("#name").val(),
				email : $("#email").val(),
				phone : $("#phone").val(),
				evaluate : $("#evaluate").val(),
				site : $("#site").val(),
				picture : $("#picture").val(),
				sex : $("input[name='sex']:checked").val()

			}),
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
	
	function downLoadInfo() {
		$.ajax({
			url : "/user/downLoadInfo",
			type : "post",
			// data表示发送的数据
			data : JSON.stringify({
				"${_csrf.parameterName}" : "${_csrf.token}"
			}),
			// 定义发送请求的数据格式为JSON字符串
			contentType : "application/json;charset=UTF-8",
			// 定义回调响应的数据格式为JSON字符串,该属性可以省略
			dataType : "json",
			// 成功响应的结果
			success : function(data) {
				$("#userName").val(data.userName);
				$("#email").val(data.email);
				$("#phone").val(data.phone);
				$("#evaluate").val(data.evaluate);
				$("#site").val(data.site);
				$("#name").val(data.name);
				if (data.picture != "")
					$("#userFace").attr("src", data.picture);
				else
					$("#userFace").attr("src","../../images/face.jpg");
				if (data.sex == "男") {
					$('input:radio').eq(0).attr('checked', 'true');	
				} else if (data.sex == "女") {
					$('input:radio').eq(1).attr('checked', 'true');
				} else {
					$('input:radio').eq(2).attr('checked', 'true');
				}
				form.render('radio');
			},
			error : function(data) {
			}
		});
	}
})

