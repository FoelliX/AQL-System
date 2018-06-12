
package de.foellix.aql.converter.droidsafe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "type", "link", "signature", "src-loc", "syscalls", "calls", "score", "contents" })
public class Content {

	@JsonProperty("type")
	private String type;
	@JsonProperty("link")
	private String link;
	@JsonProperty("signature")
	private String signature;
	@JsonProperty("src-loc")
	private SrcLoc srcLoc;
	@JsonProperty("syscalls")
	private Integer syscalls;
	@JsonProperty("calls")
	private Integer calls;
	@JsonProperty("score")
	private Integer score;
	@JsonProperty("contents")
	private List<Content> contents = null;
	@JsonIgnore
	private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("type")
	public String getType() {
		return this.type;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
	}

	@JsonProperty("link")
	public String getLink() {
		return this.link;
	}

	@JsonProperty("link")
	public void setLink(String link) {
		this.link = link;
	}

	@JsonProperty("signature")
	public String getSignature() {
		return this.signature;
	}

	@JsonProperty("signature")
	public void setSignature(String signature) {
		this.signature = signature;
	}

	@JsonProperty("src-loc")
	public SrcLoc getSrcLoc() {
		return this.srcLoc;
	}

	@JsonProperty("src-loc")
	public void setSrcLoc(SrcLoc srcLoc) {
		this.srcLoc = srcLoc;
	}

	@JsonProperty("syscalls")
	public Integer getSyscalls() {
		return this.syscalls;
	}

	@JsonProperty("syscalls")
	public void setSyscalls(Integer syscalls) {
		this.syscalls = syscalls;
	}

	@JsonProperty("calls")
	public Integer getCalls() {
		return this.calls;
	}

	@JsonProperty("calls")
	public void setCalls(Integer calls) {
		this.calls = calls;
	}

	@JsonProperty("score")
	public Integer getScore() {
		return this.score;
	}

	@JsonProperty("score")
	public void setScore(Integer score) {
		this.score = score;
	}

	@JsonProperty("contents")
	public List<Content> getContents() {
		return this.contents;
	}

	@JsonProperty("contents")
	public void setContents(List<Content> contents) {
		this.contents = contents;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
