package xmlparser;
/**
 * Stores data from a highlight
 * @author Guillermo
 *
 */
public class Highlight {
	private String type;
	private String time;
	private String url;
	private String meetpoint;
	private String xpathAnchor;
	private String xpathFocus;
	private String start;
	private String end;
	private String answer;
	private String selection;
	private String hClass;
	private String page;
	
	public Highlight() {
		
	}
	public Highlight(String type, String time, String url, String meetpoint, String xpathAnchor, String xpathFocus, String start, String end, String answer, String selection, String hClass, String page) {
		this.type = type;
		this.time= time ;
		this.url= url ;
		this.meetpoint = meetpoint;
		this.xpathAnchor = xpathAnchor;
		this.xpathFocus = xpathFocus;
		this.start= start ;
		this.end= end ;
		this.answer = answer;
		this.selection = selection ;
		this.hClass = hClass;
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

	public String getMeetpoint() {
		return meetpoint;
	}

	public void setMeetpoint(String meetpoint) {
		this.meetpoint = meetpoint;
	}

	public String getXpathAnchor() {
		return xpathAnchor;
	}

	public void setXpathAnchor(String xpathAnchor) {
		this.xpathAnchor = xpathAnchor;
	}

	public String getXpathFocus() {
		return xpathFocus;
	}

	public void setXpathFocus(String xpathFocus) {
		this.xpathFocus = xpathFocus;
	}

	public String getStart() {
		return start;
	}

	public void setStart(String start) {
		this.start = start;
	}

	public String getEnd() {
		return end;
	}

	public void setEnd(String end) {
		this.end = end;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getSelection() {
		return selection;
	}

	public void setSelection(String selection) {
		this.selection = selection;
	}

	public String gethClass() {
		return hClass;
	}

	public void sethClass(String hClass) {
		this.hClass = hClass;
	}

	public String getPage() {
		return page;
	}

	public void setPage(String page) {
		this.page = page;
	}
	@Override
	public String toString() {
		return "Highlight [type=" + type + ", time=" + time + ", url=" + url
				+ ", meetpoint=" + meetpoint + ", xpathAnchor=" + xpathAnchor
				+ ", xpathFocus=" + xpathFocus + ", start=" + start + ", end="
				+ end + ", answer=" + answer + ", selection=" + selection
				+ ", hClass=" + hClass + ", page=" + page + "]";
	}
	
	
}
