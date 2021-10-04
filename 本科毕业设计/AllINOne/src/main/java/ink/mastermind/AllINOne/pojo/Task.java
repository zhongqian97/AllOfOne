/**
 * 
 */
package ink.mastermind.AllINOne.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.alibaba.fastjson.JSON;

/**
 * @author joshua
 *
 */
@Document(collection = "task")
public class Task {
	@Id
	private String id;
	private String name;
	private String intro;
	private String help;
	private String path;
	private String jspath;
	private String user;
	private String show;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIntro() {
		return intro;
	}
	public void setIntro(String intro) {
		this.intro = intro;
	}
	public String getHelp() {
		return help;
	}
	public void setHelp(String help) {
		this.help = help;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getJspath() {
		return jspath;
	}
	public void setJspath(String jspath) {
		this.jspath = jspath;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getShow() {
		return show;
	}
	public void setShow(String show) {
		this.show = show;
	}
	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
	public Task(String id, String name, String intro, String help, String path, String jspath, String user,
			String show) {
		super();
		this.id = id;
		this.name = name;
		this.intro = intro;
		this.help = help;
		this.path = path;
		this.jspath = jspath;
		this.user = user;
		this.show = show;
	}
	public Task() {
		super();
		// TODO Auto-generated constructor stub
	}
}
