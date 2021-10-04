var navs = [{
	"title" : "聊天窗口",
	"icon" : "&#xe63a;",
	"href" : "page/message/message.html",
	"spread" : false
},{
	"title" : "通知信息",
	"icon" : "&#xe629;",
	"href" : "page/message/notice.html",
	"spread" : false
},{
	"title" : "频道管理",
	"icon" : "&#xe613;",
	"href" : "page/channel/list.html",
	"spread" : false
},{
	"title" : "任务管理",
	"icon" : "&#xe60a;",
	"href" : "page/task/list.html",
	"spread" : false
},{
	"title" : "任务广场",
	"icon" : "&#xe615;",
	"href" : "page/task/taskShare.html",
	"spread" : false
},{
	"title" : "设备管理",
	"icon" : "&#xe631;",
	"href" : "page/device/list.html",
	"spread" : false
}]

var navsadmin = [{
	"title" : "封禁用户",
	"icon" : "&#xe640;",
	"href" : "page/admin/banUser.html",
	"spread" : false
},{
	"title" : "系统公告",
	"icon" : "&#xe609;",
	"href" : "page/admin/systemTips.html",
	"spread" : false
}]
if (window.sessionStorage.getItem("_user-role_") == "ROLE_ADMIN") {
	navs = navs.concat(navsadmin);
}
