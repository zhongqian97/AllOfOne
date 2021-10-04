package ink.mastermind.AllINOne.pojo;

public class Json {
	private Integer status;
	private String information;
	private Object object;

	private volatile static Json json = null;

	private Json() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getInformation() {
		return information;
	}

	public void setInformation(String information) {
		this.information = information;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	@Override
	public String toString() {
		return "Json [status=" + status + ", information=" + information + ", object=" + object + "]";
	}

	public Json setAndPush(Integer status, String information, Object object) {
		this.status = status;
		this.information = information;
		this.object = object;
		return json;
	}

	public static Json getJson() {
		if (json == null) {
			synchronized (Json.class) {
				if (json == null) {
					json = new Json();
				}
			}
		}
		return json;
	}

}
