/**
 * 
 */
package ink.mastermind.AllINOne.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.Principal;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity.BodyBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import ink.mastermind.AllINOne.pojo.Json;
import ink.mastermind.AllINOne.pojo.Task;
import ink.mastermind.AllINOne.pojo.User;
import ink.mastermind.AllINOne.pojo.UserGroup;
import ink.mastermind.AllINOne.service.TaskService;
import ink.mastermind.AllINOne.utils.MinioClientUtils;
import io.minio.MinioClient;
import io.minio.PutObjectOptions;

/**
 * @author joshua 任务控制器
 */
@Controller
public class TaskController {
	@Autowired
	private TaskService taskService;

	/**
	 * @param principal
	 * @return 获取任务
	 */
	@RequestMapping("/task/getTask")
	@ResponseBody
	public List<Task> getTask(Principal principal) {
		try {
			return this.taskService.getTask(principal.getName());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	@RequestMapping("/task/seeTask")
	@ResponseBody
	public Json seeTask(String id, Principal principal) {
		if (id == null || "".equals(id)) return Json.getJson().setAndPush(500, "id为空！请检查id", null);
		return Json.getJson().setAndPush(200, "获取任务信息成功！", this.taskService.getTaskById(id));
	}

	/**
	 * @param principal
	 * @return 获取共享任务
	 */
	@RequestMapping("/task/getTaskShare")
	@ResponseBody
	public List<Task> getTaskShare(Principal principal) {
		try {
			return this.taskService.getTaskShare();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * @param task
	 * @param principal
	 * @return 添加修改任务总模块
	 */
	@RequestMapping("/task/taskEdit")
	@ResponseBody
	public Json taskEdit(@RequestBody Task task, Principal principal) {
		if (task == null)
			return Json.getJson().setAndPush(500, "上传的值是空的！", null);
		if (this.taskService.checkTaskFile(task) == false)
			return Json.getJson().setAndPush(500, "上传任务文件其中一个不存在！", null);
		try {
			if (task.getId() == null || "".equals(task.getId())) {
				task.setUser(principal.getName());
				this.taskService.addTask(task);
				return Json.getJson().setAndPush(200, "添加成功", null);
			} else {
				Task test = this.taskService.findByIdAndUser(task.getId(), principal.getName());
				if (test != null && test.getUser().equals(principal.getName())) {
					task.setUser(principal.getName());
					this.taskService.changeTask(task, test);
					return Json.getJson().setAndPush(200, "更新成功", null);
				}
				return Json.getJson().setAndPush(500, "更新失败", null);
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}

	/**
	 * @param id
	 * @param principal
	 * @return
	 * 删除任务
	 */
	@RequestMapping("/task/deleteTask")
	@ResponseBody
	public Json deleteTask(String id, Principal principal) {
		try {
			if (this.taskService.deleteTask(id, principal.getName()))
				return Json.getJson().setAndPush(200, "删除成功", null);
			return Json.getJson().setAndPush(500, "删除失败", null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}
	
	/**
	 * @param id
	 * @param principal
	 * @return
	 * 任务显示
	 */
	@RequestMapping("/task/showTask")
	@ResponseBody
	public Json showTask(String id, Principal principal) {
		try {
			if (this.taskService.showTask(id, principal.getName()))
				return Json.getJson().setAndPush(200, "修改成功", null);
			return Json.getJson().setAndPush(500, "修改失败", null);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return Json.getJson().setAndPush(500, e.getCause().toString(), null);
		}
	}

	/**
	 * @param file
	 * @return
	 * 上传文件
	 */
	@RequestMapping("/task/uploadFiles")
	@ResponseBody
	public Json uploadFile(@RequestParam("file") MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return Json.getJson().setAndPush(500, "请选择文件！", null);
		}
		try {
			ObjectId id = new ObjectId();
			InputStream stream = file.getInputStream();
			PutObjectOptions putObjectOptions = new PutObjectOptions(file.getSize(), -1);
			MinioClientUtils.getInstance().uploadFile("task", id.toString(), stream, putObjectOptions);
			return Json.getJson().setAndPush(200, "上传文件成功！", id.toString());
		} catch (Exception e) {
			return Json.getJson().setAndPush(500, "文件过大！不得超过100Mb", null);
		}
	}
	
	 @RequestMapping(value="/task/downloadFiles")
	 public ResponseEntity<byte[]> downloadFiles(String filename,
			 @RequestHeader("User-Agent") String userAgent)throws Exception{
		// ok表示Http协议中的状态 200
       BodyBuilder builder = ResponseEntity.ok();
       // 内容长度
       
       byte[] bytes = MinioClientUtils.getInstance().downloadFile("task", filename);
       builder.contentLength(bytes.length);
       // application/octet-stream ： 二进制流数据（最常见的文件下载）。
       builder.contentType(MediaType.APPLICATION_OCTET_STREAM);
       // 使用URLDecoder.decode对文件名进行解码
       filename = URLEncoder.encode(filename, "UTF-8");
       // 设置实际的响应文件名，告诉浏览器文件要用于【下载】、【保存】attachment 以附件形式
       // 不同的浏览器，处理方式不同，要根据浏览器版本进行区别判断
       if (userAgent.indexOf("MSIE") > 0) {
               // 如果是IE，只需要用UTF-8字符集进行URL编码即可
               builder.header("Content-Disposition", "attachment; filename=" + filename);
       } else {
               // 而FireFox、Chrome等浏览器，则需要说明编码的字符集
               // 注意filename后面有个*号，在UTF-8后面有两个单引号！
               builder.header("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
       }
       return builder.body(bytes);
	 }
	 
	 @RequestMapping(value="/task/downloadTask")
	 public ResponseEntity<byte[]> downloadTask(String taskId,
			 @RequestHeader("User-Agent") String userAgent)throws Exception{
		// ok表示Http协议中的状态 200
       BodyBuilder builder = ResponseEntity.ok();
       // 内容长度
       Task task = this.taskService.findById(taskId);
       if (task == null) return null;
       String filename = task.getJspath();
       byte[] bytes = MinioClientUtils.getInstance().downloadFile("task", filename);
       builder.contentLength(bytes.length);
       // application/octet-stream ： 二进制流数据（最常见的文件下载）。
       builder.contentType(MediaType.APPLICATION_OCTET_STREAM);
       // 使用URLDecoder.decode对文件名进行解码
       filename = URLEncoder.encode(filename, "UTF-8");
       // 设置实际的响应文件名，告诉浏览器文件要用于【下载】、【保存】attachment 以附件形式
       // 不同的浏览器，处理方式不同，要根据浏览器版本进行区别判断
       if (userAgent.indexOf("MSIE") > 0) {
               // 如果是IE，只需要用UTF-8字符集进行URL编码即可
               builder.header("Content-Disposition", "attachment; filename=" + filename);
       } else {
               // 而FireFox、Chrome等浏览器，则需要说明编码的字符集
               // 注意filename后面有个*号，在UTF-8后面有两个单引号！
               builder.header("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
       }
       return builder.body(bytes);
	 }
}
