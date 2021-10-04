/**
 * 
 */
package ink.mastermind.AllINOne.service;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ink.mastermind.AllINOne.dao.TaskDao;
import ink.mastermind.AllINOne.pojo.Task;
import ink.mastermind.AllINOne.pojo.UserGroup;
import ink.mastermind.AllINOne.utils.MinioClientUtils;

/**
 * @author joshua
 *
 */
@Service("taskService")
public class TaskService {
	@Autowired
	private TaskDao taskDao;
	
	@Scheduled(cron = "0 05 20 ? * *")
	private void deleteFile() {
		List<Task> list = this.taskDao.findAll();
		HashSet<String> set = new HashSet<String>();
		for (Task t : list) {
			set.add(t.getPath());
			set.add(t.getJspath());
		}
		LinkedList<String> files = MinioClientUtils.getInstance().findAll("task");
		for (String s : files) {
			if (set.contains(s)) continue;
			MinioClientUtils.deleteFile("task", s);
		}
	}

	/**
	 * @param user
	 * @return
	 */
	public List<Task> getTask(String user) {
		// TODO Auto-generated method stub
		return this.taskDao.findByUser(user);
	}

	/**
	 * @param id
	 * @param user
	 * @return
	 */
	public Task findByIdAndUser(String id, String user) {
		// TODO Auto-generated method stub
		return this.taskDao.findByIdAndUser(id, user);
	}

	/**
	 * @param task
	 */
	public void addTask(Task task) {
		// TODO Auto-generated method stub
		this.taskDao.save(task);
	}

	/**
	 * @param task
	 * @param test
	 */
	public void changeTask(Task task, Task before) {
		// TODO Auto-generated method stub
		if ("".equals(task.getName())) {
			task.setName(before.getName());
		}
		if ("".equals(task.getIntro())) {
			task.setIntro(before.getIntro());
		}
		if ("".equals(task.getHelp())) {
			task.setHelp(before.getHelp());
		}
		if ("".equals(task.getPath())) {
			fileChange(before.getPath(), this.taskDao.findByPath(before.getPath()));
			task.setPath(before.getPath());
		}
		if ("".equals(task.getJspath())) {
			task.setJspath(before.getJspath());
			fileChange(before.getJspath(), this.taskDao.findByJspath(before.getJspath()));
		}
		if ("".equals(task.getShow())) {
			task.setShow(before.getShow());
		}
		this.taskDao.save(task);
	}

	/**
	 * @param path
	 */
	private void fileChange(String path, List<Task> list) {
		if (list.size() > 1) return;
		MinioClientUtils.getInstance().deleteFile("task", path);
	}

	/**
	 * @param id
	 * @param user
	 * @return
	 * 删除任务
	 */
	public synchronized boolean deleteTask(String id, String user) {
		// TODO Auto-generated method stub
		Task task = this.taskDao.findByIdAndUser(id, user);
		if (task != null) {
			this.taskDao.delete(task);
			return true;
		}
		return false;
	}

	/**
	 * @return
	 */
	public List<Task> getTaskShare() {
		// TODO Auto-generated method stub
		return this.taskDao.findByShow("true");
	}

	/**
	 * @param taskId
	 * @return
	 */
	public Task findById(String taskId) {
		// TODO Auto-generated method stub
		try {
			return this.taskDao.findById(taskId).get();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return null;
	}

	/**
	 * @param id
	 * @return
	 */
	public Task getTaskById(String id) {
		// TODO Auto-generated method stub
		return this.taskDao.findById(id).get();
	}

	/**
	 * @param id
	 * @param name
	 * @return
	 */
	public boolean showTask(String id, String user) {
		Task task = this.taskDao.findByIdAndUser(id, user);
		if (task != null) {
			if ("true".equals(task.getShow())) {
				task.setShow("false");
			} else {
				task.setShow("true");
			}
			this.taskDao.save(task);
			return true;
		}
		return false;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getTaskName(String id) {
		try {
			Task task = this.taskDao.findById(id).get();
			if (task != null) {
				return task.getName();
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * @param task
	 * @return
	 */
	public boolean checkTaskFile(Task task) {
		return task != null 
				&& MinioClientUtils.getInstance().exist("task", task.getPath())
				&& MinioClientUtils.getInstance().exist("task", task.getJspath());
	}
}
