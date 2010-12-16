package xmlparser;
/**
 * 
 * @author Guillermo
 * There are 3 input types(highlight, forms, links)
 */
public class Input {
	private String type;
	private Object data; 
	
	public Input(String type, Object data) {
		this.type = type;
		this.data = data;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
