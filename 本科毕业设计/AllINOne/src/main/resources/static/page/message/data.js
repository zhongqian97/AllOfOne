/**
 * 加载数据信息，个性化渲染信息
 * @param data   需要渲染的数据
 * @return {String}   返回加载的字符串。
 */
function dataLoadInformation(data) {
	return "<p style=\"color:#00F\">" + data + "</p>";
}

/**
 * 加载控制器的html代码。
 * @return {String}   返回加载的字符串。
 */
function dataController() {
	var t = '';
	t += '	<input type="text" class="layui-input" id="data_send_text" placeholder="">';
	t += '	<a class="layui-btn" onclick="data_send_information()">发送</a>';
	return t;
}


function data_send_information() {
	sendData($("#data_send_text").val());
}

