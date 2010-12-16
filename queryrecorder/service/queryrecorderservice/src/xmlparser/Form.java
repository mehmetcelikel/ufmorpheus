package xmlparser;

public class Form {

	private String type;
	private String time;
	private String url;
	private String xpath;
	private String inputList;
	private String node;
	private String page;
	
	public Form(String type, String time, String url, String xpath, String inputList,
			String node, String page) {
		super();
		this.type = type;
		this.time = time;
		this.url = url;
		this.xpath = xpath;
		this.inputList = inputList;
		this.node = node;
		this.page = page;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getXpath() {
		return xpath;
	}

	public void setXpath(String xpath) {
		this.xpath = xpath;
	}

	public String getInputList() {
		return inputList;
	}

	public void setInputList(String inputList) {
		this.inputList = inputList;
	}

	public String getNode() {
		return node;
	}

	public void setNode(String node) {
		this.node = node;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}

	@Override
	public String toString() {
		return "Form [type=" + type + ", time=" + time + ", url=" + url
				+ ", xpath=" + xpath + ", inputList=" + inputList + ", node="
				+ node + ", page=" + page + "]";
	}


	
	
	
}
