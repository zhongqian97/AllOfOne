var $,tab,skyconsWeather;
layui.config({
	base : "js/"
}).use(['bodyTab','form','element','layer','jquery'],function(){
	var form = layui.form(),
		layer = layui.layer,
		element = layui.element();
		$ = layui.jquery;
		tab = layui.bodyTab();
		downLoadInfo();

	//锁屏
	function lockPage(){
		layer.open({
			title : false,
			type : 1,
			content : $("#lock-box"),
			closeBtn : 0,
			shade : 0.9
		})
	}
	$(".lockcms").on("click",function(){
		window.sessionStorage.setItem("lockcms",true);
		lockPage();
	})
	// 判断是否显示锁屏
	if(window.sessionStorage.getItem("lockcms") == "true"){
		lockPage();
	}
	// 解锁
	$("#unlock").on("click",function(){
		if($(this).siblings(".admin-header-lock-input").val() == ''){
			layer.msg("请输入解锁密码！");
		}else{
			$.ajax({
				url : "/user/checkPassword",
				type : "post",
				data : {
					oldPwd : $(this).siblings(".admin-header-lock-input").val()
				},
				success : function(data) {
					if (data.status == "200") {
						window.sessionStorage.setItem("lockcms",false);
						$(this).siblings(".admin-header-lock-input").val('');
						layer.closeAll("page");
					} else {
						layer.msg("密码错误，请重新输入！");
					}
				},
				error : function(data) {
					layer.msg(data.information);
				}
			});
		}
	});
	$(document).on('keydown', function() {
		if(event.keyCode == 13) {
			$("#unlock").click();
		}
	});

	//手机设备的简单适配
	var treeMobile = $('.site-tree-mobile'),
		shadeMobile = $('.site-mobile-shade')

	treeMobile.on('click', function(){
		$('body').addClass('site-mobile');
	});

	shadeMobile.on('click', function(){
		$('body').removeClass('site-mobile');
	});

	// 添加新窗口
	$(".layui-nav .layui-nav-item a").on("click",function(){
		addTab($(this));
		$(this).parent("li").siblings().removeClass("layui-nav-itemed");
	})

	//刷新后还原打开的窗口
	if(window.sessionStorage.getItem("menu") != null){
		menu = JSON.parse(window.sessionStorage.getItem("menu"));
		curmenu = window.sessionStorage.getItem("curmenu");
		var openTitle = '';
		for(var i=0;i<menu.length;i++){
			openTitle = '';
			if(menu[i].icon.split("-")[0] == 'icon'){
				openTitle += '<i class="iconfont '+menu[i].icon+'"></i>';
			}else{
				openTitle += '<i class="layui-icon">'+menu[i].icon+'</i>';
			}
			openTitle += '<cite>'+menu[i].title+'</cite>';
			openTitle += '<i class="layui-icon layui-unselect layui-tab-close" data-id="'+menu[i].layId+'">&#x1006;</i>';
			element.tabAdd("bodyTab",{
				title : openTitle,
		        content :"<iframe src='"+menu[i].href+"' data-id='"+menu[i].layId+"'></frame>",
		        id : menu[i].layId
			})
			//定位到刷新前的窗口
			if(curmenu != "undefined"){
				if(curmenu == '' || curmenu == "null"){  //定位到后台首页
					element.tabChange("bodyTab",'');
				}else if(JSON.parse(curmenu).title == menu[i].title){  //定位到刷新前的页面
					element.tabChange("bodyTab",menu[i].layId);
				}
			}else{
				element.tabChange("bodyTab",menu[menu.length-1].layId);
			}
		}
	}

})

//打开新窗口
function addTab(_this){
	tab.tabAdd(_this);
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
			window.sessionStorage.setItem("picture", data.picture);
			window.sessionStorage.setItem("userName", data.userName);
			window.sessionStorage.setItem("name", data.name);
			var name = "";
			if (data != null && data.name != null) {
				name = data.name;
			} else {
				name = data.userName;
			}
			$("#userName").append(name);
			$("#userName1").append(name);
			$("#userName2").append(name);
			$("#picture").attr("src", data.picture);
			$("#picture1").attr("src", data.picture);
			$("#picture2").attr("src", data.picture);
		},
		error : function(data) {
		}
	});
}