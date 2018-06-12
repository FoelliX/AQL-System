
package de.foellix.aql.converter.droidsafe;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "calls",
    "syscalls",
    "score"
})
public class Display {

    @JsonProperty("calls")
    private Boolean calls;
    @JsonProperty("syscalls")
    private Boolean syscalls;
    @JsonProperty("score")
    private Boolean score;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("calls")
    public Boolean getCalls() {
        return calls;
    }

    @JsonProperty("calls")
    public void setCalls(Boolean calls) {
        this.calls = calls;
    }

    @JsonProperty("syscalls")
    public Boolean getSyscalls() {
        return syscalls;
    }

    @JsonProperty("syscalls")
    public void setSyscalls(Boolean syscalls) {
        this.syscalls = syscalls;
    }

    @JsonProperty("score")
    public Boolean getScore() {
        return score;
    }

    @JsonProperty("score")
    public void setScore(Boolean score) {
        this.score = score;
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
