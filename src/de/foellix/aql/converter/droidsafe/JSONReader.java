
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
@JsonPropertyOrder({
    "indicator-type",
    "visibility",
    "display",
    "contents"
})
public class JSONReader {

    @JsonProperty("indicator-type")
    private String indicatorType;
    @JsonProperty("visibility")
    private Visibility visibility;
    @JsonProperty("display")
    private Display display;
    @JsonProperty("contents")
    private List<Content> contents = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("indicator-type")
    public String getIndicatorType() {
        return indicatorType;
    }

    @JsonProperty("indicator-type")
    public void setIndicatorType(String indicatorType) {
        this.indicatorType = indicatorType;
    }

    @JsonProperty("visibility")
    public Visibility getVisibility() {
        return visibility;
    }

    @JsonProperty("visibility")
    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    @JsonProperty("display")
    public Display getDisplay() {
        return display;
    }

    @JsonProperty("display")
    public void setDisplay(Display display) {
        this.display = display;
    }

    @JsonProperty("contents")
    public List<Content> getContents() {
        return contents;
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
