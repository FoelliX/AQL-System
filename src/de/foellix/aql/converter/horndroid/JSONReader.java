package de.foellix.aql.converter.horndroid;

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
@JsonPropertyOrder({ "tag", "numberOfQueries", "reportEntries" })
public class JSONReader {

	@JsonProperty("tag")
	private String tag;
	@JsonProperty("numberOfQueries")
	private Integer numberOfQueries;
	@JsonProperty("reportEntries")
	private List<ReportEntry> reportEntries = null;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("tag")
	public String getTag() {
		return this.tag;
	}

	@JsonProperty("tag")
	public void setTag(String tag) {
		this.tag = tag;
	}

	@JsonProperty("numberOfQueries")
	public Integer getNumberOfQueries() {
		return this.numberOfQueries;
	}

	@JsonProperty("numberOfQueries")
	public void setNumberOfQueries(Integer numberOfQueries) {
		this.numberOfQueries = numberOfQueries;
	}

	@JsonProperty("reportEntries")
	public List<ReportEntry> getReportEntries() {
		return this.reportEntries;
	}

	@JsonProperty("reportEntries")
	public void setReportEntries(List<ReportEntry> reportEntries) {
		this.reportEntries = reportEntries;
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